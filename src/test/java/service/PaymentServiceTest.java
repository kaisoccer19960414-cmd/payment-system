package com.example.payment.service;

import com.example.payment.entity.Product;
import com.example.payment.entity.User;
import com.example.payment.repository.OrderRepository;
import com.example.payment.repository.ProductRepository;
import com.example.payment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PaymentServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private com.example.payment.service.PaymentService paymentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // Given: 残高1000円のユーザーと、在庫10個・価格1000円の商品を用意
        User user = userRepository.save(new User(null, "テスト太郎", 1000, "dummy", "USER"));
        userId = user.getId();

        Product product = productRepository.save(new Product(null, "テスト商品", 1000, 10, null));
        productId = product.getId();
    }

    // --- No.1: 残高が価格より十分多い ---
    @Test
    void 残高が十分にあれば購入に成功し残高が正しく減る() {
        // Given: 残高1000円、価格1000円、数量1個(setUpの状態そのまま)

        // When
        paymentService.purchase(userId, productId, 1);

        // Then
        User updated = userRepository.findById(userId).orElseThrow();
        assertEquals(0, updated.getBalance());
    }

    // --- No.2: 残高がちょうど価格と同額(境界値) ---
    @Test
    void 残高がちょうど価格と同額なら成功し残高が0円になる() {
        // Given: setUpで既に残高1000円=価格1000円ぴったり

        // When
        paymentService.purchase(userId, productId, 1);

        // Then
        User updated = userRepository.findById(userId).orElseThrow();
        assertEquals(0, updated.getBalance());
    }

    // --- No.3: 残高が価格より1円少ない(境界値・異常系) ---
    @Test
    void 残高が1円足りないだけで購入に失敗し残高が変わらない() {
        // Given: 残高を999円に設定(価格1000円まで1円足りない)
        User user = userRepository.findById(userId).orElseThrow();
        user.setBalance(999);
        userRepository.save(user);

        // When / Then
        assertThrows(com.example.payment.exception.PaymentException.class,
                () -> paymentService.purchase(userId, productId, 1));

        User unchanged = userRepository.findById(userId).orElseThrow();
        assertEquals(999, unchanged.getBalance());
    }

    // --- No.4: 在庫が数量より十分多い ---
    @Test
    void 在庫が十分にあれば購入に成功し在庫が正しく減る() {
        // Given: setUpで在庫10個、数量2個購入
        User user = userRepository.findById(userId).orElseThrow();
        user.setBalance(2000); // 2個分の金額を確保
        userRepository.save(user);

        // When
        paymentService.purchase(userId, productId, 2);

        // Then
        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals(8, updated.getStock());
    }

    // --- No.5: 在庫がちょうど数量と同数(境界値) ---
    @Test
    void 在庫がちょうど数量と同数なら成功し在庫が0になる() {
        // Given: 在庫を1個に設定、数量1個購入
        Product product = productRepository.findById(productId).orElseThrow();
        product.setStock(1);
        productRepository.save(product);

        // When
        paymentService.purchase(userId, productId, 1);

        // Then
        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals(0, updated.getStock());
    }

    // --- No.6: 在庫が数量より1個少ない(境界値・異常系) ---
    @Test
    void 在庫が1個足りないだけで購入に失敗し在庫が変わらない() {
        // Given: 在庫を1個に設定、数量2個を買おうとする
        Product product = productRepository.findById(productId).orElseThrow();
        product.setStock(1);
        productRepository.save(product);

        // When / Then
        assertThrows(com.example.payment.exception.PaymentException.class,
                () -> paymentService.purchase(userId, productId, 2));

        Product unchanged = productRepository.findById(productId).orElseThrow();
        assertEquals(1, unchanged.getStock());
    }

    // --- No.7: 存在しないユーザーIDを指定(異常系) ---
    @Test
    void 存在しないユーザーIDを指定すると例外が発生する() {
        long invalidUserId = -1L;

        assertThrows(NoSuchElementException.class,
                () -> paymentService.purchase(invalidUserId, productId, 1));
    }

    // --- No.8: 存在しない商品IDを指定(異常系) ---
    @Test
    void 存在しない商品IDを指定すると例外が発生する() {
        long invalidProductId = -1L;

        assertThrows(NoSuchElementException.class,
                () -> paymentService.purchase(userId, invalidProductId, 1));
    }

    // --- No.10: 注文と注文明細の金額整合性 ---
    @Test
    void 購入成功時にOrderとOrderItemの金額が正しく紐づく() {
        // Given: 残高1000円、価格1000円、数量1個

        // When
        paymentService.purchase(userId, productId, 1);

        // Then
        var orders = orderRepository.findByUserIdWithItems(userId);
        var order = orders.get(0);

        assertEquals(1000, order.getTotalPrice());
        assertEquals(1, order.getItems().size());
        assertEquals(productId, order.getItems().get(0).getProductId());
        assertEquals(1000, order.getItems().get(0).getPrice());
        assertEquals(1, order.getItems().get(0).getQuantity());
    }
}