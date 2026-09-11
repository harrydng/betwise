package com.betwise.dto;

import com.betwise.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private LocalDate dateOfBirth;

    private User.ExperienceLevel experienceLevel;
    private User.InvestmentGoal investmentGoal;
    private User.RiskTolerance riskTolerance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.dateOfBirth = user.getDateOfBirth();
        this.experienceLevel = user.getExperienceLevel();
        this.investmentGoal = user.getInvestmentGoal();
        this.riskTolerance = user.getRiskTolerance();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public User.ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public User.InvestmentGoal getInvestmentGoal() {
        return investmentGoal;
    }

    public User.RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}