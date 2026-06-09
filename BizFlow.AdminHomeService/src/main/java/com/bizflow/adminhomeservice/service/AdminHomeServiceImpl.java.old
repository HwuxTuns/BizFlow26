package com.bizflow.adminhomeservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.adminhomeservice.dto.HomeMetricDto;
import com.bizflow.adminhomeservice.entity.HomeMetric;
import com.bizflow.adminhomeservice.repository.HomeMetricRepository;

@Service
public class AdminHomeServiceImpl implements AdminHomeService {

    private final HomeMetricRepository homeMetricRepository;

    public AdminHomeServiceImpl(HomeMetricRepository homeMetricRepository) {
        this.homeMetricRepository = homeMetricRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeMetricDto> getDashboardMetrics() {
        return homeMetricRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private HomeMetricDto toDto(HomeMetric metric) {
        return new HomeMetricDto(
                metric.getId(),
                metric.getMetricName(),
                metric.getDescription(),
                metric.getMetricValue(),
                metric.getUpdatedAt()
        );
    }
}
