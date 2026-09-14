package com.betwise.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_cash_events")
public class PortfolioCashEvent {

    public enum CashEventType {
        TRANSFER_IN,
        TRANSFER_OUT,
        LESSON_REWARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private CashEventType eventType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /*
     * Used to connect TRANSFER_OUT and TRANSFER_IN records
     * belonging to the same transfer.
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PortfolioCashEvent() {
    }

    public PortfolioCashEvent(
            Portfolio portfolio,
            CashEventType eventType,
            BigDecimal amount,
            String referenceId) {

        this.portfolio = portfolio;
        this.eventType = eventType;
        this.amount = amount;
        this.referenceId = referenceId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public CashEventType getEventType() {
        return eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}