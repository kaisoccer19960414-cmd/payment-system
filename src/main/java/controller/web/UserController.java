package com.example.payment.controller.web;

import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
import com.example.payment.exception.PaymentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.payment.dto.response.UserView;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import com.example.payment.dto.request.PurchaseRequest;


import java.util.concurrent.*;//test

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final com.example.payment.service.PaymentService paymentService;
    private final com.example.payment.mapper.UserMapper userMapper;
    private final com.example.payment.repository.ProductRepository productRepository;
    private final com.example.payment.mapper.ProductMapper productMapper;
    private final com.example.payment.repository.OrderRepository orderRepository;

    // コンストラクタインジェクション
    public UserController(UserRepository userRepository, com.example.payment.service.PaymentService paymentService, com.example.payment.mapper.UserMapper userMapper, com.example.payment.repository.ProductRepository productRepository, com.example.payment.mapper.ProductMapper productMapper, com.example.payment.repository.OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.userMapper = userMapper;
        this.productRepository=productRepository;
        this.productMapper=productMapper;
        this.orderRepository = orderRepository;


    }

    @GetMapping("/")
    public String index() {
        return "redirect:/users";
    }


    @GetMapping("/admin/products")
    public String adminProducts(Model model) {
        var products = productRepository.findByActiveTrue().stream()
                .map(productMapper::toView)
                .toList();
        model.addAttribute("products", products);
        return "admin-products";
    }


    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserView> users = userRepository.findAll().stream()
                .map(u -> userMapper.toView(u))
                .toList();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(required = false) String keyword, Model model) {
        List<com.example.payment.entity.Product> productList;
        if (keyword != null && !keyword.isBlank()) {
            productList = productRepository.findByActiveTrueAndNameContaining(keyword);
        } else {
            productList = productRepository.findByActiveTrue();
        }

        var products = productList.stream()
                .map(productMapper::toView)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("指定された商品が見つかりません"));
        model.addAttribute("product", productMapper.toView(product));
        return "product-detail";
    }

    @GetMapping("/purchase/{userId}")
    public String purchasePage(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("指定されたユーザーが見つかりません"));
        UserView userView = userMapper.toView(user);

        var products = productRepository.findByActiveTrue().stream()
                .map(productMapper::toView)
                .toList();

        model.addAttribute("user", userView);
        model.addAttribute("products", products);
        return "purchase";
    }

    @PostMapping("/purchase/{userId}/confirm")
    public String confirmPurchase(@PathVariable Long userId,
                                  @Valid PurchaseRequest request,
                                  Authentication authentication,
                                  Model model) {
        User loginUser = userRepository.findByName(authentication.getName()).orElseThrow();
        if (!loginUser.getId().equals(userId)) {
            throw new PaymentException("自分以外のアカウントで購入することはできません");
        }

        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new java.util.NoSuchElementException("指定された商品が見つかりません"));
        int totalPrice = product.getPrice() * request.getQuantity();

        model.addAttribute("userId", userId);
        model.addAttribute("productName", product.getName());
        model.addAttribute("price", product.getPrice());
        model.addAttribute("quantity", request.getQuantity());
        model.addAttribute("productId", request.getProductId());
        model.addAttribute("totalPrice", totalPrice);
        return "purchase-confirm";
    }

    @PostMapping("/purchase/{userId}")
    public String doPurchase(@PathVariable Long userId,
                             @Valid PurchaseRequest request,
                             Authentication authentication) {
        User loginUser = userRepository.findByName(authentication.getName()).orElseThrow();

        if (!loginUser.getId().equals(userId)) {
            throw new PaymentException("自分以外のアカウントで購入することはできません");
        }

        paymentService.purchase(userId, request.getProductId(), request.getQuantity());
        return "redirect:/users";
    }


    //test
    @GetMapping("/test/race-product")
    public String testRaceProduct() throws InterruptedException {
        List<Long> userIds = List.of(13L, 14L, 15L);
        ExecutorService executor = Executors.newFixedThreadPool(userIds.size());
        CountDownLatch latch = new CountDownLatch(userIds.size());

        for (Long uid : userIds) {
            executor.submit(() -> {
                try {
                    paymentService.purchase(uid, 1L, 1); // ← 修正: 商品ID=1を1個
                } catch (Exception e) {
                    System.out.println("失敗(" + uid + "): " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        return "redirect:/users";
    }

    @GetMapping("/orders/{userId}")
    public String listOrders(@PathVariable Long userId, Model model) {
        var orders = orderRepository.findByUserIdWithItems(userId);
        model.addAttribute("orders", orders);
        return "orders";
    }

}