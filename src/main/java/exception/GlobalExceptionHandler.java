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
}