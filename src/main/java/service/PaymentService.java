package com.example.payment.service;

import com.example.payment.entity.Order;
import com.example.payment.entity.OrderItem;
import com.example.payment.entity.Product;
import com.example.payment.entity.User;
import com.example.payment.exception.PaymentException;
import com.example.payment.repository.OrderRepository;
import com.example.payment.repository.ProductRepository;
import com.example.payment.repository.UserRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public PaymentService(UserRepository userRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void purchase(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow();

        Product product;
        try {
            product = productRepository.findById(productId).orElseThrow();
            if (product.getStock() < quantity) {
                log.warn("在庫不足のため購入失敗: userId={}, productId={}", userId, productId);
                throw new PaymentException("在庫がありません");
            }
            product.setStock(product.getStock() - quantity);
            productRepository.saveAndFlush(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("在庫の楽観ロック競合: userId={}, productId={}", userId, productId);
            throw new PaymentException("在庫の確保に失敗しました。もう一度お試しください");
        }

        int totalPrice = product.getPrice() * quantity;

        if (user.getBalance() < totalPrice) {
            log.warn("残高不足のため購入失敗: userId={}", userId);
            throw new PaymentException("残高が不足しています");
        }
        user.setBalance(user.getBalance() - totalPrice);
        userRepository.save(user);

        Order order = new Order(userId, totalPrice, LocalDateTime.now());
        order.addItem(new OrderItem(productId, quantity, product.getPrice()));
        orderRepository.save(order);

        log.info("購入成功: userId={}, productId={}, quantity={}, totalPrice={}", userId, productId, quantity, totalPrice);
    }
}