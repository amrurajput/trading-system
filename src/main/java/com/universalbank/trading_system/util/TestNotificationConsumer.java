//package com.universalbank.trading_system.util;
//
//import com.universalbank.trading_system.events.NotificationEvent;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class TestNotificationConsumer {
//
//    private final List<NotificationEvent> events = new ArrayList<>();
//
//    @KafkaListener(topics = "order-thresholds", groupId = "test-group")
//    public void listen(NotificationEvent event) {
//        System.out.println("Received notification: " + event);
//        events.add(event);
//    }
//
//    public List<NotificationEvent> getEvents() {
//        return events;
//    }
//}