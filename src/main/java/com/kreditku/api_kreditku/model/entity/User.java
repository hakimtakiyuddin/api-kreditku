package com.kreditku.api_kreditku.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    private String id; // UUID as String

    @Column(unique = true)
    private String email; // Optional, for future auth

    private int dailyMessageCount;

    private LocalDate lastResetDate;

    private LocalDateTime createdAt;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastResetDate = LocalDate.now();
        this.dailyMessageCount = 0;
    }

    public User(String id) {
        this();
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDailyMessageCount() {
        return dailyMessageCount;
    }

    public void setDailyMessageCount(int dailyMessageCount) {
        this.dailyMessageCount = dailyMessageCount;
    }

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public void incrementMessageCount() {
        this.dailyMessageCount++;
    }

    public void resetDailyCount() {
        this.dailyMessageCount = 0;
        this.lastResetDate = LocalDate.now();
    }
}
