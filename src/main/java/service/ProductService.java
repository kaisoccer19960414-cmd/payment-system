package com.example.payment.service;

import com.example.payment.dto.request.ProductCreateRequest;
import com.example.payment.dto.request.ProductUpdateRequest;
import com.example.payment.dto.request.StockAdjustRequest;
import com.example.payment.entity.Product;
import com.example.payment.repository.ProductRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product register(ProductCreateRequest request) {
        Product product = new Product(
                null,
                request.getName(),
                request.getPrice(),
                request.getStock(),
                null,
                true
        );
        return productRepository.save(product);
    }

    public ProductUpdateRequest getUpdateRequest(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName(product.getName());
        request.setPrice(product.getPrice());
        return request;
    }

    @Transactional
    public Product update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Product adjustStock(Long id, StockAdjustRequest request) {
        try {
            Product product = productRepository.findById(id).orElseThrow();
            int newStock = product.getStock() + request.getQuantity();

            if (newStock < 0) {
                throw new IllegalStateException("在庫がマイナスになるため変更できません(現在庫: " + product.getStock() + ")");
            }

            product.setStock(newStock);
            return productRepository.saveAndFlush(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("他の操作と競合しました。もう一度お試しください");
        }
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setActive(false);
        productRepository.save(product);
    }
}