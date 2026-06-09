package com.example.bizflow.repository;

import com.example.bizflow.entity.UserCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCartRepository extends JpaRepository<UserCart, Long> {
    Optional<UserCart> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
