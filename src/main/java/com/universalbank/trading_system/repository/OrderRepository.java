package com.universalbank.trading_system.repository;
import com.universalbank.trading_system.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Order;
import com.universalbank.trading_system.entity.Status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByStatusAndNotificationSentFalse(Status status);
    List<Order> findBySalesPersonIdAndStatus(Long salesPersonId, Status status);

    Collection<Object> findByAssignedTraderIdAndStatus(Long traderId, Status inProgressStatus);

    // un‑assigned new orders
    List<Order> findBySalesPersonIsNullAndStateAndCreatedAtBefore(
            State state,
            LocalDateTime cutoff
    );

    // un‑assigned in‑progress orders waiting for a trader
    List<Order> findByAssignedTraderIsNullAndStatusAndStateAndUpdatedAtBefore(
            Status status, State state, LocalDateTime cutoff);

    List<Order> findByStatusAndNotificationSentFalseAndAssignedTraderIsNotNull(Status status);
}
