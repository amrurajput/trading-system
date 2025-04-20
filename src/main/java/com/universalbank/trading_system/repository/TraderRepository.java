package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Trader;

import java.util.List;
import java.util.Optional;

public interface TraderRepository extends JpaRepository<Trader,Long> {
    Optional<Trader> findFirstByIsActiveTrueAndIsOccupiedFalse();
    List<Trader> findByIsActiveTrueAndIsOccupiedFalse();
}
