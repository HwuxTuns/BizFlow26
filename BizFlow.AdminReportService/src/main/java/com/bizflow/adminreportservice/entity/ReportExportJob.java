package com.bizflow.adminreportservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_export_jobs")
public class ReportExportJob {

	public enum JobType {
		SALES,
		LOW_STOCK
	}

	public enum Status {
		QUEUED,
		PROCESSING,
		SUCCEEDED,
		FAILED
	}

	@Id
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private JobType jobType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private Status status;

	@Column(length = 16, nullable = false)
	private String format;

	@Column
	private LocalDate fromDate;

	@Column
	private LocalDate toDate;

	@Column
	private Integer threshold;

	@Column
	private Integer limitValue;

	@Column(length = 255)
	private String filename;

	@Column(length = 255)
	private String contentType;

	@Lob
	@Column(columnDefinition = "LONGBLOB")
	private byte[] resultBytes;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String errorMessage;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime startedAt;

	@Column
	private LocalDateTime finishedAt;

	@PrePersist
	void prePersist() {
		if (this.id == null) this.id = UUID.randomUUID().toString();
		if (this.createdAt == null) this.createdAt = LocalDateTime.now();
		if (this.status == null) this.status = Status.QUEUED;
	}

	public String getId() {
		return id;
	}

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Integer getLimitValue() {
		return limitValue;
	}

	public void setLimitValue(Integer limitValue) {
		this.limitValue = limitValue;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getResultBytes() {
		return resultBytes;
	}

	public void setResultBytes(byte[] resultBytes) {
		this.resultBytes = resultBytes;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(LocalDateTime finishedAt) {
		this.finishedAt = finishedAt;
	}
}
