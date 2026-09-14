package com.betwise.controller;

import com.betwise.dto.AssetResponse;
import com.betwise.service.AssetService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(
            AssetService assetService) {

        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets() {

        return ResponseEntity.ok(
                assetService.getAssets()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                assetService.getAsset(id)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<AssetResponse>> searchAssets(
            @RequestParam String query) {

        return ResponseEntity.ok(
                assetService.searchAssets(query)
        );
    }
}