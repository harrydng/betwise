package com.betwise.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
        name = "cash_balance",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal cashBalance;

    @Column(
        name = "starting_balance",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal startingBalance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Portfolio() {
    }

    public Portfolio(
        User user,
        String name,
        BigDecimal startingBalance
    ) {
        this.user = user;
        this.name = name;
        this.startingBalance = startingBalance;
        this.cashBalance = startingBalance;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }
}