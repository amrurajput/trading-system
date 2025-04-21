package com.universalbank.trading_system.controller;

import com.universalbank.trading_system.dto.NotificationEvent;
import com.universalbank.trading_system.util.TestNotificationConsumer;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Deque;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestNotificationConsumer listener;

    @Operation(summary = "List last captured NotificationEvents")
    @GetMapping("/messages")
    public Deque<NotificationEvent> messages() {
        return listener.getEvents();
    }
}
