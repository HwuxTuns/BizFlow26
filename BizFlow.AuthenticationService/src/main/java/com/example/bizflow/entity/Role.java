package com.example.bizflow.entity;

public enum Role {
    ADMIN("Quản trị viên"),
    OWNER("Chủ cửa hàng"),
    MANAGER("Quản lý"),
    EMPLOYEE("Nhân viên");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
