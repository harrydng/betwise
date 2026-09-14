package com.betwise.dto;

import com.betwise.model.User;

import java.time.LocalDate;

public class CreateUserRequest {

    private String name;
    private String email;
    private String password;
    private LocalDate dateOfBirth;

    private User.ExperienceLevel experienceLevel;
    private User.InvestmentGoal investmentGoal;
    private User.RiskTolerance riskTolerance;

    public CreateUserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public User.ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(User.ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public User.InvestmentGoal getInvestmentGoal() {
        return investmentGoal;
    }

    public void setInvestmentGoal(User.InvestmentGoal investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public User.RiskTolerance getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(User.RiskTolerance riskTolerance) {
        this.riskTolerance = riskTolerance;
    }
}