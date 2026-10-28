package com.betwise.service;

import com.betwise.model.Portfolio;
import com.betwise.model.Watchlist;
import com.betwise.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Removes everything a user owns so the user row itself can be deleted.
 * Every table that references the user (directly or through a portfolio
 * or watchlist) has a non-null foreign key, so children must go first.
 */
@Service
public class AccountCleanupService {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioCashEventRepository cashEventRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizProgressRepository quizProgressRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final NotificationRepository notificationRepository;

    public AccountCleanupService(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            HoldingRepository holdingRepository,
            PortfolioCashEventRepository cashEventRepository,
            WatchlistRepository watchlistRepository,
            WatchlistItemRepository watchlistItemRepository,
            LessonProgressRepository lessonProgressRepository,
            QuizProgressRepository quizProgressRepository,
            QuizAnswerRepository quizAnswerRepository,
            NotificationRepository notificationRepository) {

        this.portfolioRepository = portfolioRepository;
        this.transactionRepository = transactionRepository;
        this.holdingRepository = holdingRepository;
        this.cashEventRepository = cashEventRepository;
        this.watchlistRepository = watchlistRepository;
        this.watchlistItemRepository = watchlistItemRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.quizProgressRepository = quizProgressRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void deleteEverythingOwnedBy(Long userId) {

        for (Portfolio portfolio : portfolioRepository.findByUserId(userId)) {
            transactionRepository.deleteByPortfolioId(portfolio.getId());
            holdingRepository.deleteByPortfolioId(portfolio.getId());
            cashEventRepository.deleteByPortfolioId(portfolio.getId());
            portfolioRepository.delete(portfolio);
        }

        for (Watchlist watchlist : watchlistRepository.findByUserId(userId)) {
            watchlistItemRepository.deleteByWatchlistId(watchlist.getId());
            watchlistRepository.delete(watchlist);
        }

        quizAnswerRepository.deleteByUserId(userId);
        quizProgressRepository.deleteByUserId(userId);
        lessonProgressRepository.deleteByUserId(userId);
        notificationRepository.deleteByUserId(userId);
    }
}
