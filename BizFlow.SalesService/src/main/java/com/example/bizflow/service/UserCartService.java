package com.example.bizflow.service;

import com.example.bizflow.entity.UserCart;
import com.example.bizflow.repository.UserCartRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserCartService {

    private final UserCartRepository userCartRepository;
    private final ObjectMapper objectMapper;

    public UserCartService(UserCartRepository userCartRepository, ObjectMapper objectMapper) {
        this.userCartRepository = userCartRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<UserCartSnapshot> getCartState(Long userId) {
        return userCartRepository.findByUserId(userId)
                .map(this::toSnapshotSafely);
    }

    public UserCartSnapshot saveCartState(Long userId, String username, Object state) {
        UserCart cart = userCartRepository.findByUserId(userId)
                .orElseGet(UserCart::new);
        cart.setUserId(userId);
        cart.setUsername(username);
        cart.setCartJson(toJson(state));
        cart.setUpdatedAt(LocalDateTime.now());
        UserCart saved = userCartRepository.save(cart);
        return toSnapshotSafely(saved);
    }

    public void clearCartState(Long userId) {
        userCartRepository.deleteByUserId(userId);
    }

    private UserCartSnapshot toSnapshotSafely(UserCart cart) {
        Object state = null;
        if (cart.getCartJson() != null && !cart.getCartJson().isBlank()) {
            try {
                state = objectMapper.readValue(cart.getCartJson(), Object.class);
            } catch (JsonProcessingException ignored) {
                state = null;
            }
        }
        return new UserCartSnapshot(cart.getUserId(), cart.getUsername(), state, cart.getUpdatedAt());
    }

    private String toJson(Object state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid cart state payload", e);
        }
    }

    public static class UserCartSnapshot {
        private final Long userId;
        private final String username;
        private final Object state;
        private final LocalDateTime updatedAt;

        public UserCartSnapshot(Long userId, String username, Object state, LocalDateTime updatedAt) {
            this.userId = userId;
            this.username = username;
            this.state = state;
            this.updatedAt = updatedAt;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Object getState() {
            return state;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}
