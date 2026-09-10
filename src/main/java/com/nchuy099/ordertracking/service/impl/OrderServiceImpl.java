package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.DiscountTypeEnum;
import com.nchuy099.ordertracking.common.OrderStatusEnum;
import com.nchuy099.ordertracking.common.PaymentMethodEnum;
import com.nchuy099.ordertracking.common.PaymentStatusEnum;
import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.OrderDetailResponse;
import com.nchuy099.ordertracking.dto.response.OrderStatusResponse;
import com.nchuy099.ordertracking.dto.response.OrderListResponse;
import com.nchuy099.ordertracking.dto.response.DailyOrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.entity.*;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.*;
import com.nchuy099.ordertracking.service.OrderService;
import com.nchuy099.ordertracking.service.spec.OrderSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30000);
    private static final DateTimeFormatter CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DiscountRepository discountRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserAddressRepository userAddressRepository;
    private final EntityManager entityManager;

    @Override
    public OrderSummaryResponse getSummary(OrderSummaryRequest request) {
        // get cart
        UserEntity user = getCurrentUserEntity();
        List<CartItemEntity> cartItems = getCartItems(user);

        // calc summary
        BigDecimal subTotal = calculateSubTotal(cartItems);
        BigDecimal discountAmount = calculateDiscountAmount(request.getDiscountCode(), subTotal);
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;

        return OrderSummaryResponse.builder()
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .grandTotal(subTotal.subtract(discountAmount).add(shippingFee))
                .build();
    }

    @Transactional
    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        // get cart
        UserEntity user = getCurrentUserEntity();
        Optional<CartEntity> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isEmpty()) {
            throw new BusinessException("CART_NOT_FOUND",
                    "Cart not found",
                    HttpStatus.NOT_FOUND);
        }

        CartEntity cart = cartOpt.get();
        List<CartItemEntity> cartItems = cartItemRepository.findCartItemsByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("CART_EMPTY",
                    "Cart is empty",
                    HttpStatus.BAD_REQUEST);
        }

        // check stock
//        checkInventory(cartItems);

        // calc summary
        BigDecimal subTotal = calculateSubTotal(cartItems);
        BigDecimal discountAmount = calculateDiscountAmount(request.getDiscountCode(), subTotal);
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;
        BigDecimal grandTotal = subTotal.subtract(discountAmount).add(shippingFee);

        // get user address
        UserAddressEntity userAddress = getUserAddress(request.getUserAddressId(), user.getId());

        // create order
        OrderEntity order = OrderEntity.builder()
                .code(generateOrderCode())
                .status(OrderStatusEnum.PENDING)
                .recipientName(userAddress.getRecipientName())
                .recipientPhone(userAddress.getRecipientPhone())
                .shippingAddress(buildShippingAddress(userAddress))
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .grandTotal(grandTotal)
                .note(request.getNote())
                .orderedAt(LocalDateTime.now())
                .user(user)
                .build();
        orderRepository.save(order);

        // create order item
        for (CartItemEntity cartItem : cartItems) {
            ProductVariantEntity productVariant = cartItem.getProductVariant();
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .productVariant(productVariant)
                    .quantity(cartItem.getQuantity())
                    .productName(productVariant.getProduct().getName())
                    .variantName(productVariant.getName())
                    .sku(productVariant.getSku())
                    .unitPrice(productVariant.getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // create payment
        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .paymentCode(generatePaymentCode())
                .method(getPaymentMethod(request.getPaymentMethod()))
                .status(PaymentStatusEnum.PENDING)
                .amount(grandTotal)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();
        paymentRepository.save(payment);

        // decrea  stock
//        decreaseInventory(cartItems);
    decreaseInventoryWithLock(cartItems);
        // del cart items
        cartItemRepository.deleteAll(cartItems);

        return PlaceOrderResponse.builder()
                .orderId(order.getId().toString())
                .orderCode(order.getCode())
                .paymentId(payment.getId().toString())
                .paymentCode(payment.getPaymentCode())
                .grandTotal(grandTotal)
                .build();
    }

    @Override
    @Transactional
    public OrderDetailResponse getDetails(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(
                        "ORDER_NOT_FOUND",
                        "Order not found",
                        HttpStatus.NOT_FOUND
                ));

        UserEntity currentUser = getCurrentUserEntity();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(
                    "ORDER_ACCESS_DENIED",
                    "You do not have permission to view this order",
                    HttpStatus.FORBIDDEN
            );
        }

        List<OrderDetailResponse.OrderItemResponse> items = toOrderItemResponses(
                orderItemRepository.findAllByOrderIdWithProduct(orderId)
        );

        OrderDetailResponse.PaymentResponse payment = paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .map(this::toPaymentResponse)
                .orElse(null);

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .note(order.getNote())
                .items(items)
                .pricing(OrderDetailResponse.PricingResponse.builder()
                        .subTotal(order.getSubTotal())
                        .discountAmount(order.getDiscountAmount())
                        .shippingFee(order.getShippingFee())
                        .grandTotal(order.getGrandTotal())
                        .build())
                .shipping(OrderDetailResponse.ShippingResponse.builder()
                        .recipientName(order.getRecipientName())
                        .recipientPhone(order.getRecipientPhone())
                        .shippingAddress(order.getShippingAddress())
                        .build())
                .payment(payment)
                .build();
    }

    @Override
    @Transactional
    public OrderListResponse getOrders(List<String> status, int page, int size, String sortBy, String sortDir) {
        return getOrderList(status, page, size, sortBy, sortDir, null, true);
    }

    @Override
    @Transactional
    public OrderListResponse getMyOrders(List<String> status, int page, int size, String sortBy, String sortDir) {
        UserEntity user = getCurrentUserEntity();
        return getOrderList(status, page, size, sortBy, sortDir, user.getId(), false);
    }

    @Override
    @Transactional
    public DailyOrderSummaryResponse getDailySummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        DailyOrderSummaryProjection summary = orderRepository.getDailySummary(
                startOfDay,
                startOfNextDay,
                OrderStatusEnum.DELIVERED,
                OrderStatusEnum.PENDING
        );

        return DailyOrderSummaryResponse.builder()
                .totalOrdersToday(summary.getTotalOrdersToday())
                .deliveredOrdersToday(summary.getDeliveredOrdersToday())
                .pendingOrdersToday(summary.getPendingOrdersToday())
                .build();
    }

    @Override
    @Transactional
    public OrderStatusResponse confirmOrder(UUID orderId) {
        OrderEntity order = getPendingOrder(orderId);
        order.setStatus(OrderStatusEnum.CONFIRMED);
        orderRepository.save(order);

        return OrderStatusResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    @Override
    @Transactional
    public OrderStatusResponse rejectOrder(UUID orderId) {
        OrderEntity order = getPendingOrder(orderId);
        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrderIdWithProduct(orderId);

        restoreInventoryWithLock(orderItems);

        order.setStatus(OrderStatusEnum.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderRepository.save(order);

        return OrderStatusResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    private OrderListResponse getOrderList(
            List<String> status,
            int page,
            int size,
            String sortBy,
            String sortDir,
            UUID userId,
            boolean includeCustomerName
    ) {
        List<OrderStatusEnum> statuses = parseStatuses(status);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<OrderEntity> specification = OrderSpecification.isNotDeleted();
        if (!statuses.isEmpty()) {
            specification = specification.and(OrderSpecification.hasStatusIn(statuses));
        }
        if (userId != null) {
            specification = specification.and(OrderSpecification.belongsToCustomer(userId));
        }

        Page<OrderEntity> orders = findOrders(specification, pageable);
        List<OrderListResponse.OrderResponse> content = new ArrayList<>();

        if (!orders.isEmpty()) {
            List<UUID> orderIds = new ArrayList<>();
            for (OrderEntity order : orders.getContent()) {
                orderIds.add(order.getId());
            }

            Map<UUID, List<OrderDetailResponse.OrderItemResponse>> itemsByOrderId = getItemsByOrderId(orderIds);
            Map<UUID, OrderDetailResponse.PaymentResponse> paymentsByOrderId = getPaymentsByOrderId(orderIds);

            for (OrderEntity order : orders.getContent()) {
                content.add(toOrderListItem(
                        order,
                        itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>()),
                        paymentsByOrderId.get(order.getId()),
                        includeCustomerName
                ));
            }
        }

        return OrderListResponse.builder()
                .content(content)
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    private Page<OrderEntity> findOrders(Specification<OrderEntity> specification, Pageable pageable) {
        List<OrderEntity> orders = findOrderBatch(
                specification,
                Math.toIntExact(pageable.getOffset()),
                pageable.getPageSize(),
                pageable.getSort()
        );

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<OrderEntity> countRoot = countQuery.from(OrderEntity.class);
        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicate);

        long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(orders, pageable, totalElements);
    }

    private List<OrderEntity> findOrderBatch(
            Specification<OrderEntity> specification,
            int offset,
            int limit,
            Sort sort
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderEntity> orderQuery = criteriaBuilder.createQuery(OrderEntity.class);
        Root<OrderEntity> orderRoot = orderQuery.from(OrderEntity.class);
        orderRoot.fetch("user", JoinType.LEFT);
        Predicate orderPredicate = specification.toPredicate(orderRoot, orderQuery, criteriaBuilder);

        List<jakarta.persistence.criteria.Order> orderBy = new ArrayList<>();
        for (Sort.Order sortOrder : sort) {
            if (sortOrder.isAscending()) {
                orderBy.add(criteriaBuilder.asc(orderRoot.get(sortOrder.getProperty())));
            } else {
                orderBy.add(criteriaBuilder.desc(orderRoot.get(sortOrder.getProperty())));
            }
        }

        orderQuery.select(orderRoot)
                .where(orderPredicate)
                .orderBy(orderBy);

        return entityManager.createQuery(orderQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private Map<UUID, List<OrderDetailResponse.OrderItemResponse>> getItemsByOrderId(List<UUID> orderIds) {
        Map<UUID, List<OrderDetailResponse.OrderItemResponse>> itemsByOrderId = new HashMap<>();

        for (OrderItemEntity orderItem : orderItemRepository.findAllByOrderIdInWithProduct(orderIds)) {
            UUID orderId = orderItem.getOrder().getId();
            itemsByOrderId.computeIfAbsent(orderId, ignored -> new ArrayList<>())
                    .add(toOrderItemResponse(orderItem));
        }

        return itemsByOrderId;
    }

    private Map<UUID, OrderDetailResponse.PaymentResponse> getPaymentsByOrderId(List<UUID> orderIds) {
        Map<UUID, OrderDetailResponse.PaymentResponse> paymentsByOrderId = new HashMap<>();

        for (PaymentEntity payment : paymentRepository.findAllByOrderIdInOrderByCreatedAtDesc(orderIds)) {
            UUID orderId = payment.getOrder().getId();
            if (!paymentsByOrderId.containsKey(orderId)) {
                paymentsByOrderId.put(orderId, toPaymentResponse(payment));
            }
        }

        return paymentsByOrderId;
    }

    private OrderListResponse.OrderResponse toOrderListItem(
            OrderEntity order,
            List<OrderDetailResponse.OrderItemResponse> items,
            OrderDetailResponse.PaymentResponse payment,
            boolean includeCustomerName
    ) {
        return OrderListResponse.OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .note(order.getNote())
                .customerName(includeCustomerName ? order.getUser().getFullName() : null)
                .items(items)
                .pricing(OrderDetailResponse.PricingResponse.builder()
                        .subTotal(order.getSubTotal())
                        .discountAmount(order.getDiscountAmount())
                        .shippingFee(order.getShippingFee())
                        .grandTotal(order.getGrandTotal())
                        .build())
                .shipping(OrderDetailResponse.ShippingResponse.builder()
                        .recipientName(order.getRecipientName())
                        .recipientPhone(order.getRecipientPhone())
                        .shippingAddress(order.getShippingAddress())
                        .build())
                .payment(payment)
                .build();
    }

    private List<OrderDetailResponse.OrderItemResponse> toOrderItemResponses(List<OrderItemEntity> orderItems) {
        List<OrderDetailResponse.OrderItemResponse> items = new ArrayList<>();

        for (OrderItemEntity orderItem : orderItems) {
            items.add(toOrderItemResponse(orderItem));
        }

        return items;
    }

    private OrderDetailResponse.OrderItemResponse toOrderItemResponse(OrderItemEntity orderItem) {
        return OrderDetailResponse.OrderItemResponse.builder()
                .orderItemId(orderItem.getId())
                .productVariantId(orderItem.getProductVariant().getId())
                .productName(orderItem.getProductName())
                .productPrimaryImageUrl(orderItem.getProductVariant().getProduct().getPrimaryImageUrl())
                .variantName(orderItem.getVariantName())
                .sku(orderItem.getSku())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }

    private OrderDetailResponse.PaymentResponse toPaymentResponse(PaymentEntity payment) {
        return OrderDetailResponse.PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private List<OrderStatusEnum> parseStatuses(List<String> rawStatuses) {
        List<OrderStatusEnum> statuses = new ArrayList<>();
        if (rawStatuses == null) {
            return statuses;
        }

        for (String rawStatus : rawStatuses) {
            if (rawStatus == null) {
                continue;
            }

            String[] values = rawStatus.split(",");
            for (String value : values) {
                try {
                    statuses.add(OrderStatusEnum.valueOf(value.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    throw new BusinessException(
                            "INVALID_ORDER_STATUS",
                            "Order status is invalid",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        return statuses;
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0 || size < 1) {
            throw new BusinessException(
                    "INVALID_PAGE_REQUEST",
                    "Page must be zero or greater and size must be greater than zero",
                    HttpStatus.BAD_REQUEST
            );
        }

        String field = sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy.trim();
        if (!field.equals("createdAt") && !field.equals("orderedAt")) {
            throw new BusinessException(
                    "INVALID_ORDER_SORT",
                    "sortBy must be createdAt or orderedAt",
                    HttpStatus.BAD_REQUEST
            );
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.valueOf(sortDir == null ? "DESC" : sortDir.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    "INVALID_SORT_DIRECTION",
                    "sortDir must be ASC or DESC",
                    HttpStatus.BAD_REQUEST
            );
        }

        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private OrderEntity getPendingOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(
                        "ORDER_NOT_FOUND",
                        "Order not found",
                        HttpStatus.NOT_FOUND
                ));

        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new BusinessException(
                    "INVALID_ORDER_STATUS",
                    "Only pending orders can be confirmed or rejected",
                    HttpStatus.BAD_REQUEST
            );
        }

        return order;
    }

    private List<CartItemEntity> getCartItems(UserEntity user) {
        Optional<CartEntity> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isEmpty()) {
            throw new BusinessException("CART_NOT_FOUND",
                    "Cart not found",
                    HttpStatus.NOT_FOUND);
        }

        CartEntity cart = cartOpt.get();
        return cartItemRepository.findCartItemsByCartId(cart.getId());
    }

    private UserAddressEntity getUserAddress(String userAddressId, UUID userId) {
        UUID addressId = UUID.fromString(userAddressId);
        Optional<UserAddressEntity> userAddressOpt = userAddressRepository.findByIdAndUserId(addressId, userId);

        if (userAddressOpt.isEmpty()) {
            throw new BusinessException("USER_ADDRESS_NOT_FOUND",
                    "User address not found",
                    HttpStatus.NOT_FOUND);
        }

        return userAddressOpt.get();
    }

    private String buildShippingAddress(UserAddressEntity userAddress) {
        String shippingAddress = userAddress.getDetailAddress();

        if (userAddress.getWard() != null && !userAddress.getWard().isBlank()) {
            shippingAddress = shippingAddress + ", " + userAddress.getWard();
        }

        if (userAddress.getDistrict() != null && !userAddress.getDistrict().isBlank()) {
            shippingAddress = shippingAddress + ", " + userAddress.getDistrict();
        }

        if (userAddress.getProvince() != null && !userAddress.getProvince().isBlank()) {
            shippingAddress = shippingAddress + ", " + userAddress.getProvince();
        }

        return shippingAddress;
    }

    private BigDecimal calculateSubTotal(List<CartItemEntity> cartItems) {
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CartItemEntity cartItem : cartItems) {
            BigDecimal unitPrice = cartItem.getProductVariant().getPrice();
            BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
            BigDecimal itemTotal = unitPrice.multiply(quantity);

            subTotal = subTotal.add(itemTotal);
        }

        return subTotal;
    }

    private BigDecimal calculateDiscountAmount(String discountCode, BigDecimal subTotal) {
        if (discountCode == null || discountCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        Optional<DiscountEntity> discountOpt = discountRepository.findByCode(discountCode);
        if (discountOpt.isEmpty()) {
            throw new BusinessException("DISCOUNT_NOT_FOUND",
                    "Discount not found",
                    HttpStatus.NOT_FOUND);
        }

        DiscountEntity discount = discountOpt.get();
        BigDecimal discountAmount;

        if (discount.getType() == DiscountTypeEnum.PERCENTAGE) {
            discountAmount = subTotal.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (discount.getMaxDiscountAmount() != null) {
                discountAmount = discountAmount.min(discount.getMaxDiscountAmount());
            }
        } else discountAmount = discount.getValue();


        return discountAmount.min(subTotal);
    }

    private void checkInventory(List<CartItemEntity> cartItems) {
        for (CartItemEntity cartItem : cartItems) {
            UUID productVariantId = cartItem.getProductVariant().getId();
            Integer quantityInStock = inventoryRepository.getQuantityInStockByProductVariantId(productVariantId);

            if (quantityInStock < cartItem.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_INVENTORY",
                        "Product variant not enough stock",
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void decreaseInventoryWithLock(List<CartItemEntity> cartItems) {
        for (CartItemEntity cartItem: cartItems) {
            UUID productVariantId = cartItem.getProductVariant().getId();

            List<InventoryEntity> inventories = inventoryRepository
                    .findAllByProductVariantIdForUpdate(productVariantId);

            int totalQuantityInStock = 0;

            for (InventoryEntity inventory: inventories) {
                totalQuantityInStock += inventory.getQuantityInStock();
            }

            if (totalQuantityInStock < cartItem.getQuantity()) {
                throw new BusinessException(
                        "INSUFFICIENT_INVENTORY",
                        "Product variant not enough stock",
                        HttpStatus.BAD_REQUEST
                );
            }

            int remainingQuantity = cartItem.getQuantity();

            for (InventoryEntity inventory : inventories) {
                if (remainingQuantity == 0) {
                    break;
                }

                int availableQuantity = inventory.getQuantityInStock();
                int deductedQuantity = Math.min(availableQuantity,
                        remainingQuantity);

                inventory.setQuantityInStock(availableQuantity -
                        deductedQuantity);
                remainingQuantity -= deductedQuantity;
            }



        }
    }

    private void restoreInventoryWithLock(List<OrderItemEntity> orderItems) {
        for (OrderItemEntity orderItem : orderItems) {
            UUID productVariantId = orderItem.getProductVariant().getId();
            List<InventoryEntity> inventories = inventoryRepository
                    .findAllByProductVariantIdForUpdate(productVariantId);

            if (inventories.isEmpty()) {
                throw new BusinessException(
                        "INVENTORY_NOT_FOUND",
                        "Inventory not found for product variant",
                        HttpStatus.NOT_FOUND
                );
            }

            InventoryEntity inventory = inventories.getFirst();
            inventory.setQuantityInStock(inventory.getQuantityInStock() + orderItem.getQuantity());
        }
    }

    private void decreaseInventory(List<CartItemEntity> cartItems) {
        for (CartItemEntity cartItem : cartItems) {
            UUID productVariantId = cartItem.getProductVariant().getId();
            int remainingQuantity = cartItem.getQuantity();
            List<InventoryEntity> inventories = inventoryRepository.findAllByProductVariantId(productVariantId);

            for (InventoryEntity inventory : inventories) {
                if (remainingQuantity <= 0) {
                    break;
                }

                int availableQuantity = inventory.getQuantityInStock();
                int deductedQuantity = Math.min(availableQuantity, remainingQuantity);
                inventory.setQuantityInStock(availableQuantity - deductedQuantity);
                inventoryRepository.save(inventory);
                remainingQuantity -= deductedQuantity;
            }

            if (remainingQuantity > 0) {
                throw new BusinessException("INSUFFICIENT_INVENTORY",
                        "Product variant not enough stock",
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private PaymentMethodEnum getPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PaymentMethodEnum.COD;
        }
        try {
            return PaymentMethodEnum.valueOf(paymentMethod.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    "INVALID_PAYMENT_METHOD",
                    "Payment method must be COD, ONLINE, or E_WALLET_QR",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private String generateOrderCode() {
        return "ORD-" + LocalDateTime.now().format(CODE_TIME_FORMATTER) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generatePaymentCode() {
        return "PAY-" + LocalDateTime.now().format(CODE_TIME_FORMATTER) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new BusinessException("USER_NOT_FOUND",
                    "User not found",
                    HttpStatus.NOT_FOUND);
        }
        return userOpt.get();
    }
}
