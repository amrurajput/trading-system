package com.universalbank.trading_system.repository;
import com.universalbank.trading_system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.TradeExecution;

import java.util.List;

public interface TradeExecutionRepository extends JpaRepository<TradeExecution,Long> {

    List<TradeExecution> findByOrder(Order order);
}
