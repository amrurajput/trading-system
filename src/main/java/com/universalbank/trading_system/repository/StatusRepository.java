package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Status;
public interface StatusRepository extends JpaRepository<Status,Long> {
    Status findByName(String name);
}
