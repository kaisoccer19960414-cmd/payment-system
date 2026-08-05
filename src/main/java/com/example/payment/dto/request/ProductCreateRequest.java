package com.example.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateRequest {

    @NotBlank(message = "商品名を入力してください")
    private String name;

    @NotNull(message = "価格を入力してください")
    @Min(value = 1, message = "価格は1円以上を指定してください")
    private Integer price;

    @NotNull(message = "在庫数を入力してください")
    @Min(value = 0, message = "在庫数は0以上を指定してください")
    private Integer stock;
}