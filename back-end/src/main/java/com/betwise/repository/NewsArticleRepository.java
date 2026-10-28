package com.betwise.repository;

import com.betwise.model.NewsArticle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsArticleRepository
        extends JpaRepository<NewsArticle, Long> {

    List<NewsArticle> findAllByOrderByPublishedAtDesc();

    List<NewsArticle> findByCategoryOrderByPublishedAtDesc(
            NewsArticle.Category category
    );
}
