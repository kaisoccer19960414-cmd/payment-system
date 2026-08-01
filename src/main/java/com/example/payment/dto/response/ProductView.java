package com.example.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductView {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
}