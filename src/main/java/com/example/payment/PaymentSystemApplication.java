package com.example.payment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PaymentSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentSystemApplication.class, args); // ← ここも合わせる
	}

	@Bean
	CommandLineRunner initData(com.example.payment.repository.UserRepository userRepository, com.example.payment.repository.ProductRepository productRepository, PasswordEncoder encoder) {
		return args -> {
			if (userRepository.count() == 0) {
				userRepository.save(new com.example.payment.entity.User(null, "田中", 5000, encoder.encode("password1")));
				userRepository.save(new com.example.payment.entity.User(null, "佐藤", 500, encoder.encode("password2")));
				userRepository.save(new com.example.payment.entity.User(null, "鈴木", 0, encoder.encode("password3")));
			}
			if (productRepository.count() == 0) {
				productRepository.save(new com.example.payment.entity.Product(null, "サンプル商品", 1000, 10, null));
				productRepository.save(new com.example.payment.entity.Product(null, "プレミアム商品", 3000, 5, null));
			}
		};
	}
}
