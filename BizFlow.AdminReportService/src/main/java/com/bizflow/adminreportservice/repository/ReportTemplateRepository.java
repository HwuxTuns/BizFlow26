package com.bizflow.adminreportservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bizflow.adminreportservice.entity.ReportTemplate;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
}
