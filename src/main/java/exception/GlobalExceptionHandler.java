package com.example.payment.exception;

import com.example.payment.exception.PaymentException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public Object handleNotFound(Model model, HttpServletRequest request) {
        return respond("指定されたユーザーが見つかりません", model, request);
    }

    @ExceptionHandler(PaymentException.class)
    public Object handlePayment(PaymentException e, Model model, HttpServletRequest request) {
        return respond(e.getMessage(), model, request);
    }

    private Object respond(String message, Model model, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
        }
        model.addAttribute("message", message);
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException e, Model model, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("入力内容に誤りがあります");
        return respond(message, model, request);
    }
}