package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.config.CustomUserDetailService;
import com.nchuy099.ordertracking.common.StockStatusEnum;
import com.nchuy099.ordertracking.dto.request.CartRequest;
import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.response.CartResponse;
import com.nchuy099.ordertracking.entity.*;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.*;
import com.nchuy099.ordertracking.service.CartService;
import com.nchuy099.ordertracking.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int LIMITED_STOCK = 10;

    private final ProductVariantRepository productVariantRepository;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final InventoryRepository inventoryRepository;

    @Override
    public CartResponse get() {
        //Get user & get cart by user id
        UserEntity userEntity = getCurrentUserEntity();

        Optional<CartEntity> cartOpt = cartRepository.findByUserId(userEntity.getId());

        CartEntity cart;
        if (cartOpt.isEmpty()) {
            cart = new CartEntity();
            cart.setUser(userEntity);
            cartRepository.save(cart);

            return CartResponse.builder()
                    .cartItems(new ArrayList<>())
                    .build();
        }
        else cart = cartOpt.get();

        // lấy cart item cùng thông tin liên quan bằng hql
        List<CartItemEntity> cartItems = cartItemRepository.findCartItemsByCartId(cart.getId());


        // Build response
        List<CartResponse.CartItemResponse> cartItemResponses = new ArrayList<>();

        for (CartItemEntity cartItem: cartItems) {
            Integer quantityInStock = inventoryRepository.getQuantityInStockByProductVariantId(
                    cartItem.getProductVariant().getId());

            CartResponse.CartItemResponse cartItemResponse = CartResponse.CartItemResponse.builder()
                    .productId(cartItem.getProductVariant().getProduct().getId().toString())
                    .productName(cartItem.getProductVariant().getProduct().getName())
                    .productVariantSku(cartItem.getProductVariant().getSku())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProductVariant().getPrice())
                    .stockStatus(getStockStatus(quantityInStock))
                    .build();
            cartItemResponses.add(cartItemResponse);
        }

        return CartResponse.builder()
                .cartItems(cartItemResponses)
                .build();
    }

    private StockStatusEnum getStockStatus(Integer quantityInStock) {
        if (quantityInStock == null || quantityInStock <= 0)  return StockStatusEnum.OUT_OF_STOCK;
        if (quantityInStock < LIMITED_STOCK) return StockStatusEnum.LIMITED_STOCK;
        return StockStatusEnum.IN_STOCK;
    }



    @Transactional
    @Override
    public void addItem(CartRequest request) {

        // Check product variant
        UUID productVariantId = UUID.fromString(request.getProductVariantId());
        Optional<ProductVariantEntity> variantOpt = productVariantRepository.findById(productVariantId);

        if (variantOpt.isEmpty()) {
            throw new BusinessException("PRODUCT_VARIANT_NOT_FOUND",
                    "Product Variant not found",
                    HttpStatus.NOT_FOUND);
        }

        // Check cart nếu chưa có thì tạo mới
        UserEntity user = getCurrentUserEntity();

        Optional<CartEntity> cartOpt = cartRepository.findByUserId(user.getId());

        CartEntity cart;
        if (cartOpt.isEmpty()) {
            cart = new CartEntity();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        else cart = cartOpt.get();


        // Check cart item nếu chưa có thì tạo mới
        Optional<CartItemEntity> cartItemEntityOpt = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), productVariantId);

        CartItemEntity cartItem;
        if (cartItemEntityOpt.isEmpty()) {
            checkInventory(productVariantId, request.getQuantity());
            cartItem = CartItemEntity.builder()
                    .productVariant(variantOpt.get())
                    .quantity(request.getQuantity())
                    .cart(cart)
                    .build();
        } else {
            cartItem = cartItemEntityOpt.get();
            Integer newQuantity = cartItem.getQuantity() + request.getQuantity();
            checkInventory(productVariantId, newQuantity);
            cartItem.setQuantity(newQuantity);
        }
        cartItemRepository.save(cartItem);


    }

    @Override
    public void updateItemQuantity(CartRequest request) {
        // Check product variant

        UUID productVariantId = UUID.fromString(request.getProductVariantId());
        Optional<ProductVariantEntity> variantOpt = productVariantRepository.findById(productVariantId);

        if (variantOpt.isEmpty()) {
            throw new BusinessException("PRODUCT_VARIANT_NOT_FOUND",
                    "Product Variant not found",
                    HttpStatus.NOT_FOUND);
        }

        // check cart
        UserEntity user = getCurrentUserEntity();

        Optional<CartEntity> cartOpt = cartRepository.findByUserId(user.getId());
        if (cartOpt.isEmpty()) {
            throw new BusinessException("CART_NOT_FOUND",
                    "Cart not found",
                    HttpStatus.NOT_FOUND);
        }


        // check cart item
        Optional<CartItemEntity> cartItemEntityOpt = cartItemRepository.findByCartIdAndProductVariantId(
                cartOpt.get().getId(), productVariantId);

        if (cartItemEntityOpt.isEmpty()) {
            throw new BusinessException("CART_ITEM_NOT_FOUND",
                    "Cart item not found",
                    HttpStatus.NOT_FOUND);
        }

        CartItemEntity cartItem = cartItemEntityOpt.get();
        Integer newQuantity = cartItem.getQuantity() + request.getQuantity();
        checkInventory(productVariantId, newQuantity);
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);


    }

    private void checkInventory(UUID productVariantId, Integer quantity) {
        Integer quantityInStock = inventoryRepository.getQuantityInStockByProductVariantId(productVariantId);
        if (quantityInStock < quantity) {
            throw new BusinessException("INSUFFICIENT_INVENTORY",
                    "Product variant not enough stock",
                    HttpStatus.BAD_REQUEST);
        }
    }


    private UserEntity getCurrentUserEntity() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

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
