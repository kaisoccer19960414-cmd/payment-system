package com.example.payment.service;

import com.example.payment.dto.request.SignupRequest;
import com.example.payment.entity.User;
import com.example.payment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existsByName(String name) {
        return userRepository.findByName(name).isPresent();
    }

    @Transactional
    public User register(SignupRequest request) {
        User user = new User(
                null,
                request.getName(),
                0,
                passwordEncoder.encode(request.getPassword()),
                "USER"
        );
        return userRepository.save(user);
    }
}