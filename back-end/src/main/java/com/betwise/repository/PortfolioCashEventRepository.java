package com.betwise.repository;

import com.betwise.model.PortfolioCashEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioCashEventRepository
        extends JpaRepository<PortfolioCashEvent, Long> {

    List<PortfolioCashEvent>
            findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}