package com.betwise.service;

import com.betwise.exception.ConflictException;
import com.betwise.exception.ResourceNotFoundException;
import com.betwise.dto.*;

import com.betwise.model.Asset;
import com.betwise.model.Holding;
import com.betwise.model.Portfolio;
import com.betwise.model.PortfolioCashEvent;
import com.betwise.model.PriceRange;
import com.betwise.model.Transaction;
import com.betwise.model.User;

import com.betwise.repository.AssetRepository;
import com.betwise.repository.HoldingRepository;
import com.betwise.repository.PortfolioRepository;
import com.betwise.repository.TransactionRepository;

import com.betwise.repository.PortfolioCashEventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

        private final PortfolioRepository portfolioRepository;
        private final HoldingRepository holdingRepository;
        private final TransactionRepository transactionRepository;
        private final AssetRepository assetRepository;
        private final PortfolioCashEventRepository cashEventRepository;
        private final MarketPriceService marketPriceService;
        private final NotificationService notificationService;

        public PortfolioService(
                        PortfolioRepository portfolioRepository,
                        HoldingRepository holdingRepository,
                        TransactionRepository transactionRepository,
                        PortfolioCashEventRepository cashEventRepository,
                        MarketPriceService marketPriceService,
                        AssetRepository assetRepository,
                        NotificationService notificationService) {
                this.portfolioRepository = portfolioRepository;
                this.holdingRepository = holdingRepository;
                this.transactionRepository = transactionRepository;
                this.cashEventRepository = cashEventRepository;
                this.marketPriceService = marketPriceService;
                this.assetRepository = assetRepository;
                this.notificationService = notificationService;
        }

        public PortfolioResponse createPortfolio(
                        User user,
                        CreatePortfolioRequest request) {

                if (request.getName() == null
                                || request.getName().trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Portfolio name cannot be empty");
                }

                if (request.getStartingBalance() == null
                                || request.getStartingBalance()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Starting balance must be greater than zero");
                }

                String name = request.getName().trim();

                if (portfolioRepository.existsByNameAndUserId(
                                name,
                                user.getId())) {
                        throw new ConflictException(
                                        "Portfolio name already exists");
                }

                Portfolio portfolio = new Portfolio(
                                user,
                                name,
                                request.getStartingBalance());

                Portfolio saved = portfolioRepository.save(portfolio);

                return new PortfolioResponse(
                                saved,
                                List.of());
        }

        public List<PortfolioResponse> getPortfolios(
                        User user) {

                return portfolioRepository
                                .findByUserId(user.getId())
                                .stream()
                                .map(this::buildPortfolioResponse)
                                .toList();
        }

        public PortfolioResponse getPortfolio(
                        User user,
                        Long portfolioId) {

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                return buildPortfolioResponse(portfolio);
        }

        public PortfolioResponse updatePortfolio(
                        User user,
                        Long portfolioId,
                        UpdatePortfolioRequest request) {

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                if (request.getName() != null) {

                        String name = request.getName().trim();

                        if (name.isEmpty()) {
                                throw new IllegalArgumentException(
                                                "Portfolio name cannot be empty");
                        }

                        if (!name.equals(portfolio.getName())
                                        && portfolioRepository
                                                        .existsByNameAndUserId(
                                                                        name,
                                                                        user.getId())) {

                                throw new ConflictException(
                                                "Portfolio name already exists");
                        }

                        portfolio.setName(name);
                }

                Portfolio updated = portfolioRepository.save(portfolio);

                return buildPortfolioResponse(updated);
        }

        @Transactional
        public void deletePortfolio(
                        User user,
                        Long portfolioId) {

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                transactionRepository
                                .deleteByPortfolioId(portfolioId);

                holdingRepository
                                .deleteByPortfolioId(portfolioId);

                cashEventRepository.deleteByPortfolioId(portfolioId);

                portfolioRepository.delete(portfolio);
        }

        public List<HoldingResponse> getHoldings(
                        User user,
                        Long portfolioId) {

                getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                return holdingRepository
                                .findByPortfolioId(portfolioId)
                                .stream()
                                .map(holding -> {

                                        BigDecimal currentPrice = marketPriceService
                                                        .getCurrentPrice(
                                                                        holding
                                                                                        .getAsset()
                                                                                        .getTicker());

                                        return new HoldingResponse(
                                                        holding,
                                                        currentPrice);
                                })
                                .toList();
        }

        /**
         * The portfolio's total value over a range, rebuilt by replaying its
         * trades and cash events against each asset's price history. Points
         * before the portfolio existed are left out, and the last point is
         * always the live value.
         */
        public PriceHistoryResponse getPortfolioHistory(
                        User user,
                        Long portfolioId,
                        String rangeLabel) {

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                PriceRange range = PriceRange.fromLabel(rangeLabel);
                LocalDateTime now = LocalDateTime.now();
                List<LocalDateTime> grid = range.timestamps(now);

                List<Transaction> transactions = new ArrayList<>(
                                transactionRepository.findByPortfolioIdOrderByExecutedAtDesc(portfolioId));
                Collections.reverse(transactions);

                List<PortfolioCashEvent> cashEvents = new ArrayList<>(
                                cashEventRepository.findByPortfolioIdOrderByCreatedAtDesc(portfolioId));
                Collections.reverse(cashEvents);

                // Price paths for every asset the portfolio has ever traded,
                // aligned point-for-point with `grid`.
                Map<String, List<PricePointResponse>> prices = new HashMap<>();

                for (Transaction transaction : transactions) {
                        prices.computeIfAbsent(
                                        transaction.getAsset().getTicker(),
                                        ticker -> marketPriceService.getPriceHistory(ticker, range, now));
                }

                List<PricePointResponse> points = new ArrayList<>();
                LocalDateTime createdAt = portfolio.getCreatedAt();

                if (createdAt.isAfter(grid.get(0))) {
                        points.add(new PricePointResponse(
                                        createdAt,
                                        portfolio.getStartingBalance()));
                }

                for (int i = 0; i < grid.size() - 1; i++) {
                        LocalDateTime time = grid.get(i);

                        if (time.isBefore(createdAt)) {
                                continue;
                        }

                        points.add(new PricePointResponse(
                                        time,
                                        valueAt(time, i, portfolio, transactions, cashEvents, prices)));
                }

                points.add(new PricePointResponse(
                                now,
                                buildPortfolioResponse(portfolio).getCurrentPortfolioValue()));

                return new PriceHistoryResponse(range.getLabel(), points);
        }

        private BigDecimal valueAt(
                        LocalDateTime time,
                        int gridIndex,
                        Portfolio portfolio,
                        List<Transaction> transactions,
                        List<PortfolioCashEvent> cashEvents,
                        Map<String, List<PricePointResponse>> prices) {

                BigDecimal cash = portfolio.getStartingBalance();
                Map<String, BigDecimal> quantities = new HashMap<>();

                for (Transaction transaction : transactions) {
                        if (transaction.getExecutedAt().isAfter(time)) {
                                break;
                        }

                        boolean isBuy = transaction.getTransactionType() == Transaction.TransactionType.BUY;
                        String ticker = transaction.getAsset().getTicker();

                        cash = isBuy
                                        ? cash.subtract(transaction.getTotalAmount())
                                        : cash.add(transaction.getTotalAmount());

                        quantities.merge(
                                        ticker,
                                        isBuy ? transaction.getQuantity() : transaction.getQuantity().negate(),
                                        BigDecimal::add);
                }

                for (PortfolioCashEvent event : cashEvents) {
                        if (event.getCreatedAt().isAfter(time)) {
                                break;
                        }

                        cash = event.getEventType() == PortfolioCashEvent.CashEventType.TRANSFER_OUT
                                        ? cash.subtract(event.getAmount())
                                        : cash.add(event.getAmount());
                }

                BigDecimal value = cash;

                for (Map.Entry<String, BigDecimal> holding : quantities.entrySet()) {
                        BigDecimal price = prices.get(holding.getKey()).get(gridIndex).getValue();
                        value = value.add(holding.getValue().multiply(price));
                }

                return value.setScale(2, RoundingMode.HALF_UP);
        }

        public List<TransactionResponse> getTransactions(
                        User user,
                        Long portfolioId) {

                getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                return transactionRepository
                                .findByPortfolioIdOrderByExecutedAtDesc(
                                                portfolioId)
                                .stream()
                                .map(TransactionResponse::new)
                                .toList();
        }

        @Transactional
        public TransactionResponse buy(
                        User user,
                        Long portfolioId,
                        TradeRequest request) {

                validateTrade(request);

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                Asset asset = getAsset(request.getAssetId());

                BigDecimal price = getMarketPrice(asset);

                BigDecimal totalAmount = request.getQuantity()
                                .multiply(price)
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP);

                if (portfolio.getCashBalance()
                                .compareTo(totalAmount) < 0) {

                        throw new IllegalArgumentException(
                                        "Insufficient cash balance");
                }

                Holding holding = holdingRepository
                                .findByPortfolioIdAndAssetId(
                                                portfolioId,
                                                asset.getId())
                                .orElse(null);

                if (holding == null) {

                        holding = new Holding(
                                        portfolio,
                                        asset,
                                        request.getQuantity(),
                                        price);

                } else {

                        BigDecimal oldTotalCost = holding.getQuantity()
                                        .multiply(
                                                        holding.getAverageCost());

                        BigDecimal newTotalCost = request.getQuantity()
                                        .multiply(
                                                        price);

                        BigDecimal newQuantity = holding.getQuantity()
                                        .add(request.getQuantity());

                        BigDecimal newAverageCost = oldTotalCost
                                        .add(newTotalCost)
                                        .divide(
                                                        newQuantity,
                                                        2,
                                                        RoundingMode.HALF_UP);

                        holding.setQuantity(newQuantity);
                        holding.setAverageCost(newAverageCost);
                }

                holdingRepository.save(holding);

                portfolio.setCashBalance(
                                portfolio.getCashBalance()
                                                .subtract(totalAmount));

                portfolioRepository.save(portfolio);

                Transaction transaction = new Transaction(
                                portfolio,
                                asset,
                                Transaction.TransactionType.BUY,
                                request.getQuantity(),
                                price);

                Transaction saved = transactionRepository.save(transaction);

                notificationService.notifyTradeFilled(
                                user,
                                saved.getId(),
                                true,
                                asset.getTicker(),
                                saved.getQuantity(),
                                saved.getPrice(),
                                saved.getTotalAmount());

                return new TransactionResponse(saved);
        }

        @Transactional
        public TransactionResponse sell(
                        User user,
                        Long portfolioId,
                        TradeRequest request) {

                validateTrade(request);

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                Asset asset = getAsset(request.getAssetId());

                Holding holding = holdingRepository
                                .findByPortfolioIdAndAssetId(
                                                portfolioId,
                                                asset.getId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Asset not owned"));

                if (holding.getQuantity()
                                .compareTo(request.getQuantity()) < 0) {

                        throw new IllegalArgumentException(
                                        "Insufficient asset quantity");
                }

                BigDecimal price = getMarketPrice(asset);

                BigDecimal totalAmount = request.getQuantity()
                                .multiply(price)
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP);

                BigDecimal remainingQuantity = holding.getQuantity()
                                .subtract(request.getQuantity());

                if (remainingQuantity.compareTo(
                                BigDecimal.ZERO) == 0) {

                        holdingRepository.delete(holding);

                } else {

                        holding.setQuantity(
                                        remainingQuantity);

                        holdingRepository.save(holding);
                }

                portfolio.setCashBalance(
                                portfolio.getCashBalance()
                                                .add(totalAmount));

                portfolioRepository.save(portfolio);

                Transaction transaction = new Transaction(
                                portfolio,
                                asset,
                                Transaction.TransactionType.SELL,
                                request.getQuantity(),
                                price);

                Transaction saved = transactionRepository.save(transaction);

                notificationService.notifyTradeFilled(
                                user,
                                saved.getId(),
                                false,
                                asset.getTicker(),
                                saved.getQuantity(),
                                saved.getPrice(),
                                saved.getTotalAmount());

                return new TransactionResponse(saved);
        }

        private Portfolio getOwnedPortfolio(
                        Long portfolioId,
                        Long userId) {

                return portfolioRepository
                                .findByIdAndUserId(
                                                portfolioId,
                                                userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Portfolio not found"));
        }

        private Asset getAsset(Long assetId) {

                return assetRepository
                                .findById(assetId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Asset not found"));
        }

        private void validateTrade(
                        TradeRequest request) {

                if (request.getAssetId() == null) {
                        throw new IllegalArgumentException(
                                        "Asset is required");
                }

                if (request.getQuantity() == null
                                || request.getQuantity()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Quantity must be greater than zero");
                }
        }

        // Trades always execute at the server's market price so a client
        // can't choose its own price.
        private BigDecimal getMarketPrice(Asset asset) {

                BigDecimal price = marketPriceService
                                .getCurrentPrice(asset.getTicker());

                if (price == null
                                || price.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "No market price available for "
                                                        + asset.getTicker());
                }

                return price;
        }

        @Transactional
        public void transferCash(
                        User user,
                        Long sourcePortfolioId,
                        TransferCashRequest request) {

                if (request.getAmount() == null
                                || request.getAmount()
                                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Transfer amount must be greater than zero");
                }

                if (sourcePortfolioId.equals(
                                request.getDestinationPortfolioId())) {

                        throw new IllegalArgumentException(
                                        "Cannot transfer to the same portfolio");
                }

                Portfolio source = getOwnedPortfolio(
                                sourcePortfolioId,
                                user.getId());

                Portfolio destination = getOwnedPortfolio(
                                request.getDestinationPortfolioId(),
                                user.getId());

                BigDecimal amount = request.getAmount()
                                .setScale(
                                                2,
                                                RoundingMode.HALF_UP);

                if (source.getCashBalance()
                                .compareTo(amount) < 0) {

                        throw new IllegalArgumentException(
                                        "Insufficient cash balance");
                }

                source.setCashBalance(
                                source.getCashBalance()
                                                .subtract(amount));

                destination.setCashBalance(
                                destination.getCashBalance()
                                                .add(amount));

                portfolioRepository.save(source);
                portfolioRepository.save(destination);

                String referenceId = java.util.UUID.randomUUID().toString();

                PortfolioCashEvent transferOut = new PortfolioCashEvent(
                                source,
                                PortfolioCashEvent.CashEventType.TRANSFER_OUT,
                                amount,
                                referenceId);

                PortfolioCashEvent transferIn = new PortfolioCashEvent(
                                destination,
                                PortfolioCashEvent.CashEventType.TRANSFER_IN,
                                amount,
                                referenceId);

                cashEventRepository.save(transferOut);
                cashEventRepository.save(transferIn);

                notificationService.notifyCashTransfer(
                                user,
                                destination.getId(),
                                amount,
                                source.getName(),
                                destination.getName());
        }

        @Transactional
        public void addLessonReward(
                        User user,
                        Long portfolioId,
                        BigDecimal amount) {

                if (amount == null
                                || amount.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Reward must be greater than zero");
                }

                Portfolio portfolio = getOwnedPortfolio(
                                portfolioId,
                                user.getId());

                BigDecimal reward = amount.setScale(
                                2,
                                RoundingMode.HALF_UP);

                portfolio.setCashBalance(
                                portfolio.getCashBalance()
                                                .add(reward));

                portfolioRepository.save(portfolio);

                PortfolioCashEvent event = new PortfolioCashEvent(
                                portfolio,
                                PortfolioCashEvent.CashEventType.LESSON_REWARD,
                                reward,
                                null);

                cashEventRepository.save(event);
        }

        private PortfolioResponse buildPortfolioResponse(
                        Portfolio portfolio) {

                List<Holding> holdings = holdingRepository
                                .findByPortfolioId(
                                                portfolio.getId());

                List<HoldingResponse> responses = holdings.stream()
                                .map(holding -> {

                                        BigDecimal currentPrice = marketPriceService
                                                        .getCurrentPrice(
                                                                        holding
                                                                                        .getAsset()
                                                                                        .getTicker());

                                        return new HoldingResponse(
                                                        holding,
                                                        currentPrice);
                                })
                                .toList();

                return new PortfolioResponse(
                                portfolio,
                                responses);
        }
}