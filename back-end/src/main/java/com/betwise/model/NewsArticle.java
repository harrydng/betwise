package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_articles")
public class NewsArticle {

    public enum Category {
        MARKETS,
        ECONOMY,
        TECH,
        EARNINGS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false, length = 255)
    private String headline;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 100)
    private String source;

    // Comma-separated, e.g. "NVDA,QQQ". Small and only ever read whole,
    // so it doesn't need its own table.
    @Column(length = 255)
    private String tickers;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    public NewsArticle() {
    }

    public NewsArticle(
            Category category,
            String headline,
            String summary,
            String source,
            String tickers,
            LocalDateTime publishedAt) {
        this.category = category;
        this.headline = headline;
        this.summary = summary;
        this.source = source;
        this.tickers = tickers;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getHeadline() {
        return headline;
    }

    public String getSummary() {
        return summary;
    }

    public String getSource() {
        return source;
    }

    public String getTickers() {
        return tickers;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
