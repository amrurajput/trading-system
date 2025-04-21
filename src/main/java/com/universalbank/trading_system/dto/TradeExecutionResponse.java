package com.universalbank.trading_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TradeExecutionResponse {
    private Long   orderId;
    private Long   clientId;
    private Long   salesPersonId;
    private Long   executedById;
    private int    totalQty;
    private int    executedQty;
    private double executedPrice;
    private LocalDateTime executedTime;
    private double minAmount;
    private String type;
    private String orderStatus;
    private String orderState;

}
