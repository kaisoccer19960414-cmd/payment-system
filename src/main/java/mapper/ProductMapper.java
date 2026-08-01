package com.example.payment.mapper;

import com.example.payment.dto.response.ProductView;
import com.example.payment.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductView toView(Product product) {
        return new ProductView(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}