package com.universalbank.trading_system.events;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEvent {
    private Long orderId;
    private String stockSymbol;
    private String type;
    private double threshold;
    private double currentPrice;
    private LocalDateTime timestamp;
}
