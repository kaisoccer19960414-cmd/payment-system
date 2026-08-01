package com.example.payment.service;

import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private com.example.payment.service.PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User(null, "テスト太郎", 1000));
        userId = user.getId();
    }

    @Test
    void 残高が足りていれば購入に成功し残高が減る() {
        paymentService.purchase(userId);

        User updated = userRepository.findById(userId).orElseThrow();
        assertEquals(0, updated.getBalance());
    }

    @Test
    void 残高が足りなければ例外が発生し残高は変わらない() {
        User user = userRepository.findById(userId).orElseThrow();
        user.setBalance(500);
        userRepository.save(user);

        assertThrows(IllegalStateException.class, () -> paymentService.purchase(userId));

        User unchanged = userRepository.findById(userId).orElseThrow();
        assertEquals(500, unchanged.getBalance());
    }
}