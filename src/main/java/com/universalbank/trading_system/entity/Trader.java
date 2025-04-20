package com.universalbank.trading_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Trader {
    @Id @GeneratedValue private Long id;
    private String name;
    private String specialty;
    private Boolean isActive;
    private Boolean isOccupied;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
