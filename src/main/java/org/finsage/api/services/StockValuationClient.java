package org.finsage.api.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class StockValuationClient {

    private static final Logger logger = LoggerFactory.getLogger(StockValuationClient.class);
    
    private final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private final HttpClient httpClient;
    private final String BASE_URL;
    private final ObjectMapper objectMapper;

    public StockValuationClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.BASE_URL = dotenv.get("STOCK_API_BASE_URL", "https://localhost:8000");
        this.objectMapper = new ObjectMapper();
    }

    @Cacheable(value = "stockValuationCache", key = "#symbol")
    public Double fetchCurrentPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            logger.warn("Symbol is null or empty");
            return null;
        }

        String url = BASE_URL + "/valuations/get-valuation-verdict?symbol=" + symbol;
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("User-Agent", "FinSage-API/1.0")
                    .GET()
                    .build();

            logger.debug("Fetching stock price for symbol: {} from URL: {}", symbol, url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());

                if (root.has("current_price")) {
                    Double price = root.get("current_price").asDouble();
                    logger.debug("Successfully fetched price for {}: {}", symbol, price);
                    return price;
                } else {
                    logger.warn("Response for symbol {} does not contain 'current_price' field", symbol);
                }
            } else {
                logger.warn("HTTP error {} when fetching price for symbol {}: {}", 
                           response.statusCode(), symbol, response.body());
            }
        } catch (ConnectException e) {
            logger.error("Connection failed when fetching price for symbol {}: {}", symbol, e.getMessage());
        } catch (SocketTimeoutException e) {
            logger.error("Timeout when fetching price for symbol {}: {}", symbol, e.getMessage());
        } catch (IOException e) {
            logger.error("IO error when fetching price for symbol {}: {}", symbol, e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Request interrupted when fetching price for symbol {}: {}", symbol, e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            logger.error("Unexpected error when fetching price for symbol {}: {}", symbol, e.getMessage(), e);
        }
        
        logger.debug("Returning null price for symbol: {}", symbol);
        return null;
    }
}


