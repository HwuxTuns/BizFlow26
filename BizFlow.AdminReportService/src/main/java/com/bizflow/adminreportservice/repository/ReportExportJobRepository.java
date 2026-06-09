package com.bizflow.adminreportservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bizflow.adminreportservice.entity.ReportExportJob;

public interface ReportExportJobRepository extends JpaRepository<ReportExportJob, String> {
}
