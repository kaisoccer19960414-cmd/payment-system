package com.example.payment.controller.api;


import com.example.payment.dto.response.UserView;
import com.example.payment.repository.UserRepository;
import com.example.payment.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


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
    public List<UserView> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> userMapper.toView(u))
                .toList();
    }

    @PostMapping("/purchase/{userId}")
    public String purchase(@PathVariable Long userId, Authentication authentication) {
        var loginUser = userRepository.findByName(authentication.getName()).orElseThrow();
        if (!loginUser.getId().equals(userId)) {
            return "自分以外のアカウントで購入することはできません";
        }
        paymentService.purchase(userId, 1L, 1);
        return "購入が完了しました";
    }
}