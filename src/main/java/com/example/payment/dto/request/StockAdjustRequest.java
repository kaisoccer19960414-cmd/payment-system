package com.example.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustRequest {

    @NotNull(message = "増減数を入力してください")
    private Integer quantity;
}