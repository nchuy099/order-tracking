package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CartRequest;
import com.nchuy099.ordertracking.dto.response.CartResponse;

public interface CartService {

    CartResponse get();

    void addItem(CartRequest request);

    void updateItemQuantity(CartRequest request);

}
