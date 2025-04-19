package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Order;
import com.universalbank.trading_system.entity.Status;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByStatusAndNotificationSentFalse(Status status);
}
