package com.betwise.dto;

import com.betwise.model.User;

import java.time.LocalDate;

public class UpdateUserRequest {

    private String name;
    private LocalDate dateOfBirth;

    private User.ExperienceLevel experienceLevel;
    private User.InvestmentGoal investmentGoal;
    private User.RiskTolerance riskTolerance;

    public UpdateUserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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