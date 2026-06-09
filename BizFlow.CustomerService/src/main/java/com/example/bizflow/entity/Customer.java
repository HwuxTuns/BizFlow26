package com.example.bizflow.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== THÔNG TIN CƠ BẢN =====
    @Column(nullable = false)
    private String name;

    private String phone;
    private String email;
    private String address;

    // Link to authentication user (optional). When present, points are tied to this user.
    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "username", unique = true)
    private String username;

    // ===== TÍCH ĐIỂM =====
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(name = "monthly_points", nullable = false)
    private Integer monthlyPoints = 0;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier = CustomerTier.DONG;

    // ===== THÔNG TIN MỞ RỘNG =====
    private LocalDate dob;
    private String cccd;
    private String gender;

    public Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.totalPoints = 0;
        this.monthlyPoints = 0;
        this.tier = CustomerTier.DONG;
    }

    // ===== HELPER METHOD =====
    public void addPoints(int points) {
        if (points <= 0) {
            return;
        }

        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (monthlyPoints == null) {
            monthlyPoints = 0;
        }

        totalPoints += points;
        monthlyPoints += points;
    }

    @PrePersist
    public void ensureDefaults() {
        if (totalPoints == null) {
            totalPoints = 0;
        }
        if (monthlyPoints == null) {
            monthlyPoints = 0;
        }
        if (tier == null) {
            tier = CustomerTier.DONG;
        }
    }

    // ===== GETTERS =====
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public Integer getMonthlyPoints() {
        return monthlyPoints;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getCccd() {
        return cccd;
    }

    public String getGender() {
        return gender;
    }

    // ===== SETTERS =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setMonthlyPoints(Integer monthlyPoints) {
        this.monthlyPoints = monthlyPoints;
    }

    public void setTier(CustomerTier tier) {
        this.tier = tier;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
