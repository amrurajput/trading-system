package com.universalbank.trading_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A lightweight view of an Order for API responses.
 */
@Data
@AllArgsConstructor
public class OrderSummaryDTO {
    private Long   orderId;
    private Long   clientId;
    private Long   salesPersonId;   // null if unassigned
    private Long   traderId;        // null if unassigned
    private String symbol;
    private double maxPrice;
    private Integer quantity;
    private String state;           // e.g. "IN_PROGRESS"
    private String status;          // e.g. "TRADER_TO_PICK"
    private String operationType;          // e.g. "TRADER_TO_PICK"
}
