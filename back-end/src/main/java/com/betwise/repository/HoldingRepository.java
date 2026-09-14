package com.betwise.repository;

import com.betwise.model.Holding;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository
        extends JpaRepository<Holding, Long> {

    List<Holding> findByPortfolioId(Long portfolioId);

    Optional<Holding> findByPortfolioIdAndAssetId(
            Long portfolioId,
            Long assetId
    );

    void deleteByPortfolioId(Long portfolioId);
}