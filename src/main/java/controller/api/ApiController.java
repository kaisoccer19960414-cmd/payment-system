package com.example.payment.controller.api;


import com.example.payment.dto.request.PurchaseRequest;
import com.example.payment.dto.response.UserView;
import com.example.payment.repository.UserRepository;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final com.example.payment.mapper.UserMapper userMapper;

    public ApiController(UserRepository userRepository, PaymentService paymentService, com.example.payment.mapper.UserMapper userMapper) {
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public Page<UserView> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toView);
    }

    @PostMapping("/purchase/{userId}")
    public String purchase(@PathVariable Long userId,
                           @Valid @RequestBody PurchaseRequest request,
                           Authentication authentication) {
        var loginUser = userRepository.findByName(authentication.getName()).orElseThrow();
        if (!loginUser.getId().equals(userId)) {
            return "自分以外のアカウントで購入することはできません";
        }
        paymentService.purchase(userId, request.getProductId(), request.getQuantity());
        return "購入が完了しました";
    }
}