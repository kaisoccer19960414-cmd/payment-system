package com.example.payment.controller.web;

import com.example.payment.dto.request.ProductCreateRequest;
import com.example.payment.dto.request.ProductUpdateRequest;
import com.example.payment.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.example.payment.dto.request.StockAdjustRequest;
import com.example.payment.exception.ProductException;

@Controller
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/admin/products/new")
    public String newForm(Model model) {
        model.addAttribute("productCreateRequest", new ProductCreateRequest());
        return "admin-product-form";
    }

    @PostMapping("/admin/products")
    public String create(@Valid @ModelAttribute ProductCreateRequest productCreateRequest,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin-product-form";
        }
        productService.register(productCreateRequest);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ProductUpdateRequest request = productService.getUpdateRequest(id);
        model.addAttribute("productUpdateRequest", request);
        model.addAttribute("productId", id);
        return "admin-product-edit-form";
    }

    @PostMapping("/admin/products/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ProductUpdateRequest productUpdateRequest,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            return "admin-product-edit-form";
        }
        productService.update(id, productUpdateRequest);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/{id}/stock")
    public String stockForm(@PathVariable Long id, Model model) {
        var product = productService.getProduct(id);
        model.addAttribute("stockAdjustRequest", new StockAdjustRequest());
        model.addAttribute("productId", id);
        model.addAttribute("productName", product.getName());
        model.addAttribute("currentStock", product.getStock());
        return "admin-product-stock-form";
    }

    @PostMapping("/admin/products/{id}/stock")
    public String adjustStock(@PathVariable Long id,
                              @Valid @ModelAttribute StockAdjustRequest stockAdjustRequest,
                              BindingResult bindingResult,
                              Model model) {
        var product = productService.getProduct(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("productName", product.getName());
            model.addAttribute("currentStock", product.getStock());
            return "admin-product-stock-form";
        }

        try {
            productService.adjustStock(id, stockAdjustRequest);
        } catch (ProductException e) {
            model.addAttribute("productId", id);
            model.addAttribute("productName", product.getName());
            model.addAttribute("currentStock", product.getStock());
            model.addAttribute("error", e.getMessage());
            return "admin-product-stock-form";
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}