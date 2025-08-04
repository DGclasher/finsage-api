package org.finsage.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

@Service
@Cacheable(value = "stockValuationCache", key = "#symbol")
public class StockValuationClient {

    private final Dotenv dotenv = Dotenv.load();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String BASE_URL = dotenv.get("STOCK_API_BASE_URL", "https://localhost:8000");

    public Double fetchCurrentPrice(String symbol) {
        String url = BASE_URL + "/valuations/get-valuation-verdict?symbol=" + symbol;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.body());

                if (root.has("current_price")) {
                    return root.get("current_price").asDouble();
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}


