package com.betwise.service;

import com.betwise.exception.ResourceNotFoundException;
import com.betwise.dto.AssetResponse;
import com.betwise.dto.PriceHistoryResponse;
import com.betwise.dto.PricePointResponse;
import com.betwise.model.Asset;
import com.betwise.model.PriceRange;
import com.betwise.repository.AssetRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class AssetService {

    // Every other point of the 1D series is plenty for a list-row chart.
    private static final int SPARKLINE_STRIDE = 2;

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

        return buildResponse(getAssetEntity(id));
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

    public PriceHistoryResponse getHistory(
            Long id,
            String rangeLabel) {

        Asset asset = getAssetEntity(id);
        PriceRange range = PriceRange.fromLabel(rangeLabel);

        List<PricePointResponse> points = marketPriceService.getPriceHistory(
                asset.getTicker(),
                range,
                LocalDateTime.now());

        return new PriceHistoryResponse(range.getLabel(), points);
    }

    private Asset getAssetEntity(Long id) {

        return assetRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Asset not found"
                        )
                );
    }

    private AssetResponse buildResponse(
            Asset asset) {

        String ticker = asset.getTicker();

        List<PricePointResponse> today = marketPriceService.getPriceHistory(
                ticker,
                PriceRange.DAY,
                LocalDateTime.now());

        List<BigDecimal> sparkline = IntStream
                .range(0, today.size())
                .filter(i -> i % SPARKLINE_STRIDE == 0 || i == today.size() - 1)
                .mapToObj(i -> today.get(i).getValue())
                .toList();

        return new AssetResponse(
                asset,
                marketPriceService.getCurrentPrice(ticker),
                marketPriceService.getDayChangePercent(ticker),
                sparkline
        );
    }
}
