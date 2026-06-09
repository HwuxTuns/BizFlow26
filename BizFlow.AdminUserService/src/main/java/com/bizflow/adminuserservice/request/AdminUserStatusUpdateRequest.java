package com.bizflow.adminuserservice.request;

import jakarta.validation.constraints.NotNull;

public class AdminUserStatusUpdateRequest {

    @NotNull
    private Boolean enabled;

    private String note;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
