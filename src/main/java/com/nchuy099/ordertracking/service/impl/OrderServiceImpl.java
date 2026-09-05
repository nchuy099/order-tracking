package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.DiscountTypeEnum;
import com.nchuy099.ordertracking.common.OrderStatusEnum;
import com.nchuy099.ordertracking.common.PaymentMethodEnum;
import com.nchuy099.ordertracking.common.PaymentStatusEnum;
import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.entity.*;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.*;
import com.nchuy099.ordertracking.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        checkInventory(cartItems);

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
                .status(OrderStatusEnum.AWAITING_PAYMENT)
                .recipientName(userAddress.getRecipientName())
                .recipientPhone(userAddress.getRecipientPhone())
                .shippingAddress(buildShippingAddress(userAddress))
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .grandTotal(grandTotal)
                .note(request.getNote())
//                .orderedAt(LocalDateTime.now())
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
                .status(PaymentStatusEnum.UNPAID)
                .amount(grandTotal)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();
        paymentRepository.save(payment);

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

    private PaymentMethodEnum getPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PaymentMethodEnum.COD;
        }
        return PaymentMethodEnum.valueOf(paymentMethod);
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
