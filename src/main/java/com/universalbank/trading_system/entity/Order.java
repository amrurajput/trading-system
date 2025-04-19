package com.universalbank.trading_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name="client_id")      private Client client;
    @ManyToOne @JoinColumn(name="salesperson_id") private SalesPerson salesPerson;
    @ManyToOne @JoinColumn(name="trader_id")      private Trader assignedTrader;
    @ManyToOne @JoinColumn(name="symbol_id")      private Symbol symbol;
    @Positive private int quantity;
    @Positive private double priceLimit;
    private String operationType;
    private LocalDateTime timeLimit;
    @ManyToOne @JoinColumn(name="status_id")      private Status status;
    @ManyToOne @JoinColumn(name="state_id")       private State state;
    private double notifyThreshold;
    private boolean notificationSent=false;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
