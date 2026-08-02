package com.example.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseRequest {

    @NotNull(message = "商品を選択してください")
    private Long productId;

    @NotNull(message = "数量を入力してください")
    @Min(value = 1, message = "数量は1以上を指定してください")
    private Integer quantity;
}