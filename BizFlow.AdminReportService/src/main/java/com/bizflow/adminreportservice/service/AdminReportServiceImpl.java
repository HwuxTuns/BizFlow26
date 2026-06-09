package com.bizflow.adminreportservice.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bizflow.adminreportservice.dto.ReportDto;
import com.bizflow.adminreportservice.entity.ReportTemplate;
import com.bizflow.adminreportservice.exception.ReportNotFoundException;
import com.bizflow.adminreportservice.repository.ReportTemplateRepository;

@Service
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportTemplateRepository repository;

    public AdminReportServiceImpl(ReportTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDto> listReports() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportDto triggerReport(Long id) {
        ReportTemplate template = repository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException(id));
        template.setLastRun(Instant.now());
        template.setStatus("RUNNING");
        ReportTemplate saved = repository.save(template);
        return toDto(saved);
    }

    private ReportDto toDto(ReportTemplate template) {
        return new ReportDto(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getFrequency(),
                template.getLastRun(),
                template.getStatus()
        );
    }
}
