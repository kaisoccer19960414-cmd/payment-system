package com.example.payment.mapper;

import com.example.payment.dto.response.UserView;
import com.example.payment.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserView toView(User user) {
        return new UserView(user.getId(), user.getName(), user.getBalance());
    }
}