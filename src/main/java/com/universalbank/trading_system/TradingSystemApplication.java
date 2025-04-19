package com.universalbank.trading_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingSystemApplication.class, args);
    }
}
