package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.TradeExecution;
public interface TradeExecutionRepository extends JpaRepository<TradeExecution,Long> {}
