package com.universalbank.trading_system.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.universalbank.trading_system.entity.Client;
public interface ClientRepository extends JpaRepository<Client,Long> {}
