package com.example.payment.service;

import com.example.payment.entity.Product;
import com.example.payment.entity.User;
import com.example.payment.repository.OrderItemRepository;
import com.example.payment.repository.OrderRepository;
import com.example.payment.repository.ProductRepository;
import com.example.payment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class RandomChaosTest {

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
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    void ランダムな購入を大量実行しても整合性が壊れない() throws InterruptedException {
        // 準備:3ユーザー(初期残高はバラバラでもOK)、2商品
        User userA = userRepository.save(new User(null, "テストA", 500000, "dummy", "USER"));
        User userB = userRepository.save(new User(null, "テストB", 50000, "dummy", "USER"));
        User userC = userRepository.save(new User(null, "テストC", 50000, "dummy", "USER"));

        List<Long> userIds = List.of(userA.getId(), userB.getId(), userC.getId());

        // 「誰がいくらから始めたか」を、保存した値から直接記録する(書き写さない)
        Map<Long, Integer> initialBalances = new HashMap<>();
        initialBalances.put(userA.getId(), userA.getBalance());
        initialBalances.put(userB.getId(), userB.getBalance());
        initialBalances.put(userC.getId(), userC.getBalance());

        Product productX = productRepository.save(new Product(null, "商品X", 1000, 100, null));
        Product productY = productRepository.save(new Product(null, "商品Y", 3000, 100, null));

        List<Long> productIds = List.of(productX.getId(), productY.getId());

        Map<Long, Integer> initialStocks = new HashMap<>();
        initialStocks.put(productX.getId(), productX.getStock());
        initialStocks.put(productY.getId(), productY.getStock());

        int threadCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Random random = new Random();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Long uid = userIds.get(random.nextInt(userIds.size()));
                    Long pid = productIds.get(random.nextInt(productIds.size()));
                    int quantity = random.nextInt(3) + 1;
                    paymentService.purchase(uid, pid, quantity);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // 検証1:各ユーザーが使った金額の合計 == order_itemsの金額合計
        int totalSpent = userIds.stream()
                .mapToInt(id -> initialBalances.get(id) - userRepository.findById(id).orElseThrow().getBalance())
                .sum();

        int totalOrdered = orderItemRepository.findAll().stream()
                .filter(item -> userIds.contains(item.getOrder().getUserId()))
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        System.out.println("===== テスト結果 =====");
        System.out.println("購入成功件数      : " + successCount.get());
        System.out.println("購入失敗件数      : " + failCount.get());

        // 検証2:各商品の在庫減少数 == その商品のorder_items数量合計
        for (Long pid : productIds) {
            Product product = productRepository.findById(pid).orElseThrow();
            int consumed = initialStocks.get(pid) - product.getStock();

            int ordered = orderItemRepository.findAll().stream()
                    .filter(item -> item.getProductId().equals(pid))
                    .mapToInt(com.example.payment.entity.OrderItem::getQuantity)
                    .sum();

            System.out.println(product.getName());
            System.out.println("  初期在庫 : " + initialStocks.get(pid));
            System.out.println("  販売数   : " + consumed);
            System.out.println("  残在庫   : " + product.getStock() + (consumed == ordered ? " ← OK" : " ← NG"));

            assertEquals(consumed, ordered, "商品" + pid + "の在庫減少数と注文数量が一致しない");
        }

        System.out.println("売上合計          : ¥" + totalOrdered);
        System.out.println("注文テーブル件数  : " + orderRepository.count());

        assertEquals(totalSpent, totalOrdered, "支払った金額と注文明細の合計が一致しない");
    }
}