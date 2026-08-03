package com.example.payment.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    // --- No.9: 数量が0(境界値・異常系) ---
    @Test
    void 数量が0だとバリデーションエラーになる() {
        // Given
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(0);

        // When
        Set<ConstraintViolation<PurchaseRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    // --- No.9-2: 数量が負の値(異常系) ---
    @Test
    void 数量がマイナスだとバリデーションエラーになる() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(-1);

        Set<ConstraintViolation<PurchaseRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    // --- No.9-3: 数量が1(境界値・正常系) ---
    @Test
    void 数量が1ならバリデーションを通過する() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        Set<ConstraintViolation<PurchaseRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // --- No.9-4: 商品IDがnull(異常系) ---
    @Test
    void 商品IDがnullだとバリデーションエラーになる() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(null);
        request.setQuantity(1);

        Set<ConstraintViolation<PurchaseRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}