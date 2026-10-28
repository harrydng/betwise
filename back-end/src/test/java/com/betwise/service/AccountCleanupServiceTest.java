package com.betwise.service;

import com.betwise.model.Portfolio;
import com.betwise.model.Watchlist;
import com.betwise.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCleanupServiceTest {

    @Mock private PortfolioRepository portfolioRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private HoldingRepository holdingRepository;
    @Mock private PortfolioCashEventRepository cashEventRepository;
    @Mock private WatchlistRepository watchlistRepository;
    @Mock private WatchlistItemRepository watchlistItemRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private QuizProgressRepository quizProgressRepository;
    @Mock private QuizAnswerRepository quizAnswerRepository;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private AccountCleanupService accountCleanupService;

    @Test
    void deletesChildRowsBeforeTheirParents() {

        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getId()).thenReturn(10L);

        Watchlist watchlist = mock(Watchlist.class);
        when(watchlist.getId()).thenReturn(20L);

        when(portfolioRepository.findByUserId(1L)).thenReturn(List.of(portfolio));
        when(watchlistRepository.findByUserId(1L)).thenReturn(List.of(watchlist));

        accountCleanupService.deleteEverythingOwnedBy(1L);

        InOrder portfolioOrder = inOrder(
                transactionRepository, holdingRepository, cashEventRepository, portfolioRepository);

        portfolioOrder.verify(transactionRepository).deleteByPortfolioId(10L);
        portfolioOrder.verify(holdingRepository).deleteByPortfolioId(10L);
        portfolioOrder.verify(cashEventRepository).deleteByPortfolioId(10L);
        portfolioOrder.verify(portfolioRepository).delete(portfolio);

        InOrder watchlistOrder = inOrder(watchlistItemRepository, watchlistRepository);

        watchlistOrder.verify(watchlistItemRepository).deleteByWatchlistId(20L);
        watchlistOrder.verify(watchlistRepository).delete(watchlist);

        verify(quizAnswerRepository).deleteByUserId(1L);
        verify(quizProgressRepository).deleteByUserId(1L);
        verify(lessonProgressRepository).deleteByUserId(1L);
        verify(notificationRepository).deleteByUserId(1L);
    }
}
