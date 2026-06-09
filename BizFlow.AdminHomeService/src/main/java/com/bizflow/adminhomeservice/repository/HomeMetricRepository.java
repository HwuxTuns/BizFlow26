package com.bizflow.adminhomeservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bizflow.adminhomeservice.entity.HomeMetric;

@Repository
public interface HomeMetricRepository extends JpaRepository<HomeMetric, Long> {
}
