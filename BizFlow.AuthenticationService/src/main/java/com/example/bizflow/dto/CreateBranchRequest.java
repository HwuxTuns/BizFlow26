package com.example.bizflow.dto;

public class CreateBranchRequest {
    private String name;
    private String address;
    private String phone;
    private String email;
    private Long ownerId;

    public CreateBranchRequest() {}

    public CreateBranchRequest(String name, String address, String phone, String email, Long ownerId) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.ownerId = ownerId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
