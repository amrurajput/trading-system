package com.universalbank.trading_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

//    @NotNull(message = "Sales Person ID is required")
    private Long salesPersonId;

    private Long traderId;

    @NotBlank(message = "Symbol must not be blank")
    private String symbolId;

    @NotBlank(message = "Operation type is required")
    private String operationType;

    @Min(value = 0, message = "Quantity must be zero or positive")
    private int quantity;

    @Min(value = 0, message = "Price limit must be zero or positive")
    private double priceLimit;

    @NotNull(message = "Time limit is required")
    private LocalDateTime timeLimit;

    private Long stateId;


    private Long statusId;


    private String createdBy;


}
