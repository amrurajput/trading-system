package com.universalbank.trading_system.scheduler;

import com.universalbank.trading_system.dto.NotificationEvent;
import com.universalbank.trading_system.entity.Order;
import com.universalbank.trading_system.repository.OrderRepository;
import com.universalbank.trading_system.client.StockExchangeClient;
import com.universalbank.trading_system.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {
    private final OrderRepository orderRepo;
    private final StatusRepository statusRepo;
    private final StockExchangeClient client;
    private final KafkaTemplate<String, NotificationEvent> kafka;

    private static final String TOPIC = "order-thresholds";


    @Scheduled(fixedRate = 60_000)
    public void checkThresholds() {
        log.info("Threshold scheduler started");
       var inProgress = statusRepo.findById(1l).get();
        List<Order> orders = orderRepo.findByStatusAndNotificationSentFalseAndAssignedTraderIsNotNull(inProgress);
        for (Order o : orders) {
            // fetch latest close price (blocking)
            double price = client.fetchLatestClose(o.getSymbol().getCode());
            log.debug("OrderId={} Symbol={} LatestPrice={} NotifyThreshold={} State={}",
                    o.getId(), o.getSymbol().getCode(), price, o.getNotifyThreshold(), o.getState().getName());

            boolean crossed = false;
            String operationType = o.getOperationType();
            if ("BUY".equalsIgnoreCase(operationType)) {
                crossed = price <= o.getNotifyThreshold();
            } else if ("SELL".equalsIgnoreCase(operationType)) {
                crossed = price >= o.getNotifyThreshold();
            } else {
                log.warn("Unknown order operationType {} for order {}", operationType, o.getId());
            }

            if (crossed) {
                NotificationEvent evt = NotificationEvent.builder()
                        .orderId(o.getId())
                        .stockSymbol(o.getSymbol().getCode())
                        .type(o.getOperationType())
                        .threshold(o.getNotifyThreshold())
                        .currentPrice(price)
                        .timestamp(LocalDateTime.now())
                        .build();

                log.info("Threshold crossed for Order id={}. Publishing Kafka event.", o.getId());
                kafka.send(TOPIC, evt);

                // mark notified
                o.setNotificationSent(true);
                orderRepo.save(o);
                log.info("Order id={} marked as notificationSent", o.getId());
            }
        }
    }
}
