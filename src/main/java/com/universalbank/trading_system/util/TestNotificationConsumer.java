package com.universalbank.trading_system.util;

import com.universalbank.trading_system.dto.NotificationEvent;
import com.universalbank.trading_system.dto.NotificationEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Slf4j

public class TestNotificationConsumer {

    @Getter
    private final Deque<NotificationEvent> events = new ConcurrentLinkedDeque<>();

    @KafkaListener(
            topics          = "order-thresholds",
            groupId         = "notification-group",
            containerFactory= "notificationListenerContainerFactory"
    )
    public void onNotification(NotificationEvent event) {
        log.info("Captured Kafka event: {}", event);
        if (events.size() >= 100) events.removeFirst();
        events.addLast(event);
    }
}