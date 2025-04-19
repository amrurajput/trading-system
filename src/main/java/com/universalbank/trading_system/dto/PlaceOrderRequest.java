package com.universalbank.trading_system.dto;
import java.time.LocalDateTime;

public record PlaceOrderRequest(
    Long clientId,
    Long salesPersonId,
    Long traderId,
    String symbolId,
    String operationType,
    int quantity,
    double priceLimit,
    LocalDateTime timeLimit,
    Long stateId,
    Long statusId,
    double notifyThreshold,
    String createdBy
) {}
