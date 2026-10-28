package com.betwise.service;

import com.betwise.dto.NewsArticleResponse;
import com.betwise.model.NewsArticle;
import com.betwise.repository.NewsArticleRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsService {

    private final NewsArticleRepository newsArticleRepository;

    public NewsService(
            NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    /** Newest first. Pass a category such as "markets" to filter. */
    public List<NewsArticleResponse> getNews(String category) {

        List<NewsArticle> articles;

        if (category == null || category.isBlank()) {
            articles = newsArticleRepository.findAllByOrderByPublishedAtDesc();
        } else {
            articles = newsArticleRepository.findByCategoryOrderByPublishedAtDesc(
                    parseCategory(category));
        }

        return articles.stream()
                .map(NewsArticleResponse::new)
                .toList();
    }

    private static NewsArticle.Category parseCategory(String category) {
        try {
            return NewsArticle.Category.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown news category: " + category);
        }
    }
}
