package com.betwise.repository;

import com.betwise.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByPortfolioIdOrderByExecutedAtDesc(
            Long portfolioId
    );

    void deleteByPortfolioId(Long portfolioId);
}