package com.example.bizflow.dto;

public class BranchSummary {
    private Long id;
    private String name;
    private String ownerName;
    private Boolean isActive;
    private String address;

    public BranchSummary() {
    }

    public BranchSummary(Long id, String name, String ownerName, Boolean isActive, String address) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
        this.isActive = isActive;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
