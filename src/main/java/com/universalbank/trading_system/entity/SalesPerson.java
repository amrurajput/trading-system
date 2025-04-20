package com.universalbank.trading_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesPerson {
    @Id @GeneratedValue private Long id;
    private String name;
    private String desk;
    @ManyToMany
    @JoinTable(name="salesperson_clients",
        joinColumns=@JoinColumn(name="salesperson_id"),
        inverseJoinColumns=@JoinColumn(name="client_id"))
    private Set<Client> clients;
    private Boolean isActive;
    private Boolean isOccupied;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
