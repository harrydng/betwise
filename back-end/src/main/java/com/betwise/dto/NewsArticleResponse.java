package com.betwise.dto;

import com.betwise.model.NewsArticle;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class NewsArticleResponse {

    private Long id;
    private NewsArticle.Category category;
    private String headline;
    private String summary;
    private String source;
    private List<String> tickers;
    private LocalDateTime publishedAt;

    public NewsArticleResponse(NewsArticle article) {
        this.id = article.getId();
        this.category = article.getCategory();
        this.headline = article.getHeadline();
        this.summary = article.getSummary();
        this.source = article.getSource();
        this.publishedAt = article.getPublishedAt();

        String rawTickers = article.getTickers();

        this.tickers = rawTickers == null || rawTickers.isBlank()
                ? List.of()
                : Arrays.stream(rawTickers.split(","))
                        .map(String::trim)
                        .filter(ticker -> !ticker.isEmpty())
                        .toList();
    }

    public Long getId() {
        return id;
    }

    public NewsArticle.Category getCategory() {
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

    public List<String> getTickers() {
        return tickers;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
