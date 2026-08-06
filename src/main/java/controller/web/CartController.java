package com.example.payment.controller.web;

import com.example.payment.cart.Cart;
import com.example.payment.cart.CartItem;
import com.example.payment.entity.User;
import com.example.payment.repository.ProductRepository;
import com.example.payment.repository.UserRepository;
import com.example.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@Controller
public class CartController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public CartController(ProductRepository productRepository,
                          UserRepository userRepository,
                          PaymentService paymentService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam Integer quantity,
                            HttpSession session) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("指定された商品が見つかりません"));
        Cart cart = getCart(session);
        cart.add(new CartItem(product.getId(), product.getName(), product.getPrice(), quantity));
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        Cart cart = getCart(session);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "cart";
    }

    @PostMapping("/cart/{index}/remove")
    public String removeFromCart(@PathVariable int index, HttpSession session) {
        getCart(session).remove(index);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(Authentication authentication, HttpSession session) {
        User loginUser = userRepository.findByName(authentication.getName()).orElseThrow();
        Cart cart = getCart(session);

        for (CartItem item : cart.getItems()) {
            paymentService.purchase(loginUser.getId(), item.getProductId(), item.getQuantity());
        }

        cart.clear();
        return "redirect:/orders/" + loginUser.getId();
    }
}