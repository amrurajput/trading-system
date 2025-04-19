package com.universalbank.trading_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TradeExecution {
    @Id @GeneratedValue private Long id;
    @ManyToOne @JoinColumn(name="order_id") private Order order;
    @ManyToOne @JoinColumn(name="trader_id") private Trader executedBy;
    private int executedQty;
    private double executedPrice;
    private LocalDateTime executedAt;
}
