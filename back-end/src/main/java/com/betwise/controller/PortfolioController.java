package com.betwise.controller;

import com.betwise.dto.*;
import com.betwise.model.User;
import com.betwise.service.PortfolioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(
            PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            Authentication authentication,
            @RequestBody CreatePortfolioRequest request) {

        User user = (User) authentication.getPrincipal();

        PortfolioResponse response = portfolioService.createPortfolio(
                user,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                portfolioService.getPortfolios(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                portfolioService.getPortfolio(
                        user,
                        id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdatePortfolioRequest request) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                portfolioService.updatePortfolio(
                        user,
                        id,
                        request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        portfolioService.deletePortfolio(
                user,
                id);

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping("/{id}/holdings")
    public ResponseEntity<List<HoldingResponse>> getHoldings(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                portfolioService.getHoldings(
                        user,
                        id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                portfolioService.getTransactions(
                        user,
                        id));
    }

    @PostMapping("/{id}/buy")
    public ResponseEntity<TransactionResponse> buy(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TradeRequest request) {

        User user = (User) authentication.getPrincipal();

        TransactionResponse response = portfolioService.buy(
                user,
                id,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{id}/sell")
    public ResponseEntity<TransactionResponse> sell(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TradeRequest request) {

        User user = (User) authentication.getPrincipal();

        TransactionResponse response = portfolioService.sell(
                user,
                id,
                request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<Void> transferCash(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody TransferCashRequest request) {

        User user = (User) authentication.getPrincipal();

        portfolioService.transferCash(
                user,
                id,
                request);

        return ResponseEntity.noContent().build();
    }
}