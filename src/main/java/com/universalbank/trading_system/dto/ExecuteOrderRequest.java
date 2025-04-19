package com.universalbank.trading_system.dto;

public record ExecuteOrderRequest(
    Long orderId,
    Long traderId,
    double price,
    int qty
) {}
