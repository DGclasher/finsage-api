package org.finsage.api.controllers;

import lombok.RequiredArgsConstructor;
import org.finsage.api.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cache")
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    private final CacheService cacheService;

    @PostMapping("/purge")
    public ResponseEntity<Map<String, String>> purgeAllCaches() {
        try {
            cacheService.clearAllCaches();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All caches purged successfully");
            response.put("purgedCaches", String.join(", ", cacheService.getCacheNames()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error purging caches: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to purge caches: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/purge/{cacheName}")
    public ResponseEntity<Map<String, String>> purgeSpecificCache(@PathVariable String cacheName) {
        try {
            if (cacheService.getCacheNames().contains(cacheName)) {
                cacheService.clearSpecificCache(cacheName);

                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Cache '" + cacheName + "' purged successfully");

                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Cache '" + cacheName + "' not found");

                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error purging cache {}: {}", cacheName, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to purge cache '" + cacheName + "': " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshCache() {
        try {
            cacheService.clearAllCaches();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cache refreshed successfully. Next requests will populate fresh data.");

            logger.info("Cache refresh completed");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error refreshing cache: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to refresh cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        try {
            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("availableCaches", cacheService.getCacheNames());
            cacheInfo.put("totalRedisKeys", cacheService.getAllRedisKeys().size());
            cacheInfo.put("redisConnected", cacheService.isRedisConnected());

            return ResponseEntity.ok(cacheInfo);
        } catch (Exception e) {
            logger.error("Error getting cache info: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get cache info: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/redis/keys")
    public ResponseEntity<Map<String, String>> deleteAllRedisKeys() {
        try {
            int keyCount = cacheService.getAllRedisKeys().size();
            cacheService.deleteAllRedisKeys();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Deleted " + keyCount + " Redis keys");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting Redis keys: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to delete Redis keys: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/evict/investment-summary/{userId}")
    public ResponseEntity<Map<String, String>> evictInvestmentSummaryCache(@PathVariable String userId) {
        try {
            cacheService.evictInvestmentSummaryCache(userId);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Investment summary cache evicted for user: " + userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error evicting investment summary cache for user {}: {}", userId, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to evict investment summary cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/evict/stock-valuation/{symbol}")
    public ResponseEntity<Map<String, String>> evictStockValuationCache(@PathVariable String symbol) {
        try {
            cacheService.evictStockValuationCache(symbol);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Stock valuation cache evicted for symbol: " + symbol);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error evicting stock valuation cache for symbol {}: {}", symbol, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to evict stock valuation cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
