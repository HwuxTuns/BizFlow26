package com.example.bizflow.repository;

import com.example.bizflow.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    Optional<Shelf> findByProductId(Long productId);
    List<Shelf> findAllByOrderByUpdatedAtDesc();
    List<Shelf> findByQuantityLessThan(int threshold);
}
