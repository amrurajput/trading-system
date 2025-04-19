package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Symbol;

import java.util.Optional;


public interface SymbolRepository extends JpaRepository<Symbol,Long> {


    Optional<Symbol> findByCode(String s);
}
