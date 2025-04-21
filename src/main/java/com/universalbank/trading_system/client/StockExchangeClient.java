package com.universalbank.trading_system.client;

import com.universalbank.trading_system.dto.PricePoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Component
@Slf4j
public class StockExchangeClient {
    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StockExchangeClient(WebClient.Builder builder,
                               @Value("${alphavantage.api.url}") String baseUrl,
                               @Value("${alphavantage.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = builder
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(config -> config.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    /**
     * Returns a Mono that, when subscribed or blocked, fetches the latest 1-min bar for the symbol.
     */
    public Mono<PricePoint> getLatestPricePoint(String symbol) {
        log.debug("Invoking getLatestPricePoint for symbol={}", symbol);
        return webClient.get()
                .uri(uri -> uri.path("/query")
                        .queryParam("function", "TIME_SERIES_INTRADAY")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1min")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseLatestPricePoint)
                .doOnError(ex -> log.error("Error fetching intraday for {}: {}", symbol, ex.getMessage()));
    }

    /**
     * Helper to block and return just the close price (or zero on error).
     */
    public double fetchLatestClose(String symbol) {
        try {
            PricePoint pp = getLatestPricePoint(symbol)
                    .doOnNext(p -> log.debug("Fetched PricePoint: {}", p))
                    .block();
            if (pp == null) {
                log.warn("No price point for symbol={}", symbol);
                return 0.0;
            }
            return pp.getClose();
        } catch (Exception ex) {
            log.error("Blocking fetch failed for {}: {}", symbol, ex.getMessage());
            return 0.0;
        }
    }

    private PricePoint parseLatestPricePoint(String rawJson) {
        try {
            JsonNode root = mapper.readTree(rawJson);
            JsonNode series = root.path("Time Series (1min)");
            Iterator<String> it = series.fieldNames();
            String latestTs = null;
            while (it.hasNext()) {
                String ts = it.next();
                if (latestTs == null || ts.compareTo(latestTs) > 0) {
                    latestTs = ts;
                }
            }
            JsonNode data = series.path(latestTs);
            //LocalDateTime timestamp = LocalDateTime.parse(latestTs, TS_FORMAT);
            double open   = data.path("1. open").asDouble();
            double high   = data.path("2. high").asDouble();
            double low    = data.path("3. low").asDouble();
            double close  = data.path("4. close").asDouble();
            long   volume = data.path("5. volume").asLong();
            return new PricePoint(LocalDateTime.now(), open, high, low, close, volume);
        } catch (Exception e) {
            log.error("Failed to parse JSON for latest price", e);
            throw new RuntimeException("Invalid price data", e);
        }
    }
}
