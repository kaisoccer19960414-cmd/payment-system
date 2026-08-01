package com.example.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserView {
    private Long id;
    private String name;
    private Integer balance;
}