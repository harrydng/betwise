package com.betwise.controller;

import com.betwise.dto.AddWatchlistItemRequest;
import com.betwise.dto.CreateWatchlistRequest;
import com.betwise.dto.UpdateWatchlistRequest;
import com.betwise.dto.WatchlistResponse;

import com.betwise.model.User;
import com.betwise.service.WatchlistService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(
            WatchlistService watchlistService
    ) {
        this.watchlistService = watchlistService;
    }

    @PostMapping
    public ResponseEntity<WatchlistResponse> createWatchlist(
            Authentication authentication,
            @RequestBody CreateWatchlistRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        WatchlistResponse response =
                watchlistService.createWatchlist(
                        user,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlists(
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                watchlistService.getWatchlists(user)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchlistResponse> getWatchlist(
            Authentication authentication,
            @PathVariable Long id
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                watchlistService.getWatchlist(
                        user,
                        id
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WatchlistResponse> updateWatchlist(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateWatchlistRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                watchlistService.updateWatchlist(
                        user,
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWatchlist(
            Authentication authentication,
            @PathVariable Long id
    ) {

        User user =
                (User) authentication.getPrincipal();

        watchlistService.deleteWatchlist(
                user,
                id
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<WatchlistResponse> addItem(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody AddWatchlistItemRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        WatchlistResponse response =
                watchlistService.addItem(
                        user,
                        id,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{watchlistId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long watchlistId,
            @PathVariable Long itemId
    ) {

        User user =
                (User) authentication.getPrincipal();

        watchlistService.removeItem(
                user,
                watchlistId,
                itemId
        );

        return ResponseEntity.noContent().build();
    }
}