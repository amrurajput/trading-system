package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Trader;
public interface TraderRepository extends JpaRepository<Trader,Long> {}
