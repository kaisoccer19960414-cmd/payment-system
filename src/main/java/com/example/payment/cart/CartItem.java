package com.example.payment.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private String productName;
    private Integer price;
    private Integer quantity;
}