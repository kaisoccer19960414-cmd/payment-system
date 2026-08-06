package com.example.payment.controller.web;

import com.example.payment.dto.request.SignupRequest;
import com.example.payment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        if (userService.existsByName(signupRequest.getName())) {
            model.addAttribute("error", "このユーザー名は既に使われています");
            return "signup";
        }

        userService.register(signupRequest);
        return "redirect:/login?registered";
    }
}