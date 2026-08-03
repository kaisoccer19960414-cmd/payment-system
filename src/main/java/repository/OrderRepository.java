package com.example.payment.repository;

import com.example.payment.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("select distinct o from Order o join fetch o.items where o.userId = :userId")
    List<Order> findByUserIdWithItems(Long userId);
}