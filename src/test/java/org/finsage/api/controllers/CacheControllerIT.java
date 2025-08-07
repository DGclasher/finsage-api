package org.finsage.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CacheControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = getJwtToken("john.doe@example.com", "password");
    }

    private String getJwtToken(String email, String password) throws Exception {
        var loginRequest = Map.of("email", email, "password", password);

        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(responseBody);
        return response.get("token").asText();
    }

    private RequestPostProcessor bearerToken() {
        return request -> {
            request.addHeader("Authorization", "Bearer " + jwtToken);
            return request;
        };
    }

    @Test
    void shouldGetCacheInfo() throws Exception {
        mockMvc.perform(get("/api/v1/cache/info")
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCaches").exists())
                .andExpect(jsonPath("$.totalRedisKeys").exists())
                .andExpect(jsonPath("$.redisConnected").exists());
    }

    @Test
    void shouldRefreshCache() throws Exception {
        mockMvc.perform(post("/api/v1/cache/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldPurgeAllCaches() throws Exception {
        mockMvc.perform(post("/api/v1/cache/purge")
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldEvictInvestmentSummaryCache() throws Exception {
        mockMvc.perform(post("/api/v1/cache/evict/investment-summary/test-user-id")
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldEvictStockValuationCache() throws Exception {
        mockMvc.perform(post("/api/v1/cache/evict/stock-valuation/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").exists());
    }
}
