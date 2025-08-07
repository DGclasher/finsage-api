package org.finsage.api.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final CacheManager cacheManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @CacheEvict(value = "investmentSummary", key = "#userId")
    public void evictInvestmentSummaryCache(String userId) {
        logger.debug("Evicted investment summary cache for user: {}", userId);
    }

    @CacheEvict(value = "stockValuationCache", key = "#symbol")
    public void evictStockValuationCache(String symbol) {
        logger.debug("Evicted stock valuation cache for symbol: {}", symbol);
    }

    @CacheEvict(value = "stockPrices", key = "#symbol")
    public void evictStockPricesCache(String symbol) {
        logger.debug("Evicted stock prices cache for symbol: {}", symbol);
    }

    public void clearAllCaches() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        cacheNames.forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            logger.info("Cleared cache: {}", cacheName);
        });
    }

    public void clearSpecificCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            logger.info("Cleared cache: {}", cacheName);
        } else {
            logger.warn("Cache '{}' not found", cacheName);
        }
    }

    public boolean isRedisConnected() {
        if (redisTemplate == null) {
            logger.debug("RedisTemplate not available (likely in test mode)");
            return false;
        }
        try {
            String response = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equals(response);
        } catch (Exception e) {
            logger.error("Redis connection check failed: {}", e.getMessage());
            return false;
        }
    }

    public Set<String> getAllRedisKeys() {
        if (redisTemplate == null) {
            logger.debug("RedisTemplate not available (likely in test mode)");
            return Set.of();
        }
        try {
            return redisTemplate.keys("*");
        } catch (Exception e) {
            logger.error("Failed to get Redis keys: {}", e.getMessage());
            return Set.of();
        }
    }

    public void deleteAllRedisKeys() {
        if (redisTemplate == null) {
            logger.debug("RedisTemplate not available (likely in test mode)");
            return;
        }
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Deleted {} Redis keys", keys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to delete Redis keys: {}", e.getMessage());
        }
    }

    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
