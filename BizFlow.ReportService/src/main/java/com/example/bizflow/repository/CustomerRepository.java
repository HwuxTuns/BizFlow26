package com.example.bizflow.repository;

import com.example.bizflow.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Count all customers
    @Override
    long count();
}
