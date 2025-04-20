package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.SalesPerson;

import java.util.List;
import java.util.Optional;

public interface SalesPersonRepository extends JpaRepository<SalesPerson,Long> {

    List<SalesPerson> findByIsActiveAndIsOccupied(Boolean isActive, Boolean isOccupied);
    Optional<SalesPerson> findByIdAndIsActiveAndIsOccupied(Long Id, Boolean isActive, Boolean isOccupied);
    Optional<SalesPerson> findFirstByIsActiveTrueAndIsOccupiedFalse();
}
