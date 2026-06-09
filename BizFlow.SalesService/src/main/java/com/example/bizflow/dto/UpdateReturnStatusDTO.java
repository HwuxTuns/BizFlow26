package com.example.bizflow.dto;

public class UpdateReturnStatusDTO {
    private String status; // APPROVED, REJECTED, COMPLETED
    private String adminNote;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
}
