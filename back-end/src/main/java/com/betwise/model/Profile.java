package com.betwise.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String riskTolerance;

    private String experienceLevel;

    private String goal;

    public Profile() {
    }

    // profile struct
    public Profile(
        Long userId,
        String riskTolerance,
        String experienceLevel,
        String goal
    ) {
        this.userId = userId;
        this.riskTolerance = riskTolerance;
        this.experienceLevel = experienceLevel;
        this.goal = goal;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRiskTolerance() {
        return riskTolerance;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public String getGoal() {
        return goal;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRiskTolerance(String riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }
}
