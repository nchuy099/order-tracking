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

    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.ZERO;
    private static final DateTimeFormatter CODE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DiscountRepository discountRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public OrderSummaryResponse getSummary(OrderSummaryRequest request) {
        UserEntity user = getCurrentUserEntity();
        List<CartItemEntity> cartItems = getCartItems(user);
        BigDecimal subTotal = calculateSubTotal(cartItems);
        BigDecimal discountAmount = calculateDiscountAmount(request.getDiscountCode(), subTotal);
        BigDecimal shippingFee = request.getShippingFee() == null ? DEFAULT_SHIPPING_FEE : request.getShippingFee();

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
        UserEntity user = getCurrentUserEntity();
        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("CART_NOT_FOUND",
                        "Cart not found",
                        HttpStatus.NOT_FOUND));
        List<CartItemEntity> cartItems = cartItemRepository.findCartItemsByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BusinessException("CART_EMPTY",
                    "Cart is empty",
                    HttpStatus.BAD_REQUEST);
        }

        checkInventory(cartItems);

        BigDecimal subTotal = calculateSubTotal(cartItems);
        BigDecimal discountAmount = calculateDiscountAmount(request.getDiscountCode(), subTotal);
        BigDecimal shippingFee = request.getShippingFee() == null ? DEFAULT_SHIPPING_FEE : request.getShippingFee();
        BigDecimal grandTotal = subTotal.subtract(discountAmount).add(shippingFee);

        OrderEntity order = OrderEntity.builder()
                .code(generateOrderCode())
                .status(OrderStatusEnum.AWAITING_PAYMENT)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .shippingAddress(request.getShippingAddress())
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .grandTotal(grandTotal)
                .note(request.getNote())
                .orderedAt(LocalDateTime.now())
                .user(user)
                .build();
        orderRepository.save(order);

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

        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .paymentCode(generatePaymentCode())
                .method(getPaymentMethod(request.getPaymentMethod()))
                .status(PaymentStatusEnum.UNPAID)
                .amount(grandTotal)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();
        paymentRepository.save(payment);

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
        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("CART_NOT_FOUND",
                        "Cart not found",
                        HttpStatus.NOT_FOUND));
        return cartItemRepository.findCartItemsByCartId(cart.getId());
    }

    private BigDecimal calculateSubTotal(List<CartItemEntity> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItem.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscountAmount(String discountCode, BigDecimal subTotal) {
        if (discountCode == null || discountCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        DiscountEntity discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new BusinessException("DISCOUNT_NOT_FOUND",
                        "Discount not found",
                        HttpStatus.NOT_FOUND));

        BigDecimal discountAmount;
        if (discount.getType() == DiscountTypeEnum.PERCENTAGE) {
            discountAmount = subTotal.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (discount.getMaxDiscountAmount() != null) {
                discountAmount = discountAmount.min(discount.getMaxDiscountAmount());
            }
        } else {
            discountAmount = discount.getValue();
        }

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
