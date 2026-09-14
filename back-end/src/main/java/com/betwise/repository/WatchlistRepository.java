package com.betwise.repository;

import com.betwise.model.Watchlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository
        extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserId(Long userId);

    Optional<Watchlist> findByIdAndUserId(
            Long id,
            Long userId
    );

    boolean existsByNameAndUserId(
            String name,
            Long userId
    );
}