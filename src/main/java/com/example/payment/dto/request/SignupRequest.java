package com.example.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "ユーザー名を入力してください")
    private String name;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 4, message = "パスワードは4文字以上で入力してください")
    private String password;
}