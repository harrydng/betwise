package com.betwise.repository;
import com.betwise.model.Portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository
        extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserId(Long userId);

    Optional<Portfolio> findByIdAndUserId(
            Long id,
            Long userId);

    boolean existsByNameAndUserId(
            String name,
            Long userId);
}