package com.betwise.repository;

import com.betwise.model.Asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository
        extends JpaRepository<Asset, Long> {

    Optional<Asset> findByTicker(String ticker);

    List<Asset> findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(
            String ticker,
            String name);
}