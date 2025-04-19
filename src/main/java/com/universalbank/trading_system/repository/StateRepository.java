package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.State;
public interface StateRepository extends JpaRepository<State,Long> {
    State findByName(String name);
}
