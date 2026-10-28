package com.betwise.controller;

import com.betwise.dto.NewsArticleResponse;
import com.betwise.service.NewsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(
            NewsService newsService) {

        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<List<NewsArticleResponse>> getNews(
            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(
                newsService.getNews(category)
        );
    }
}
