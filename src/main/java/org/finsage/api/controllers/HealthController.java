package org.finsage.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final DataSource dataSource;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // Check database connectivity
        Map<String, Object> dbHealth = checkDatabaseHealth();
        health.put("database", dbHealth);

        // Check Redis connectivity
        Map<String, Object> redisHealth = checkRedisHealth();
        health.put("redis", redisHealth);

        // Determine overall status
        boolean isHealthy = "UP".equals(dbHealth.get("status")) && "UP".equals(redisHealth.get("status"));
        health.put("status", isHealthy ? "UP" : "DOWN");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        return ResponseEntity.ok(checkDatabaseHealth());
    }

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> redisHealth() {
        return ResponseEntity.ok(checkRedisHealth());
    }

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage(), e);
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        return dbHealth;
    }

    private Map<String, Object> checkRedisHealth() {
        Map<String, Object> redisHealth = new HashMap<>();
        if (redisTemplate == null) {
            redisHealth.put("status", "DISABLED");
            redisHealth.put("message", "Redis is not configured (likely in test mode)");
            return redisHealth;
        }

        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            redisHealth.put("status", "PONG".equals(pong) ? "UP" : "DOWN");
            redisHealth.put("response", pong);
        } catch (Exception e) {
            logger.error("Redis health check failed: {}", e.getMessage(), e);
            redisHealth.put("status", "DOWN");
            redisHealth.put("error", e.getMessage());
        }
        return redisHealth;
    }
}
