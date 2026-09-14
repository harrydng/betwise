package com.betwise.service;

import com.betwise.dto.AssetResponse;
import com.betwise.model.Asset;
import com.betwise.repository.AssetRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final MarketPriceService marketPriceService;

    public AssetService(
            AssetRepository assetRepository,
            MarketPriceService marketPriceService) {

        this.assetRepository = assetRepository;
        this.marketPriceService = marketPriceService;
    }

    public List<AssetResponse> getAssets() {

        return assetRepository
                .findAll()
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public AssetResponse getAsset(Long id) {

        Asset asset =
                assetRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Asset not found"
                                )
                        );

        return buildResponse(asset);
    }

    public List<AssetResponse> searchAssets(
            String query) {

        return assetRepository
                .findByTickerContainingIgnoreCaseOrNameContainingIgnoreCase(
                        query,
                        query
                )
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private AssetResponse buildResponse(
            Asset asset) {

        BigDecimal currentPrice =
                marketPriceService
                        .getCurrentPrice(
                                asset.getTicker()
                        );

        return new AssetResponse(
                asset,
                currentPrice
        );
    }
}