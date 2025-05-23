package com.universalbank.trading_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.universalbank.trading_system")
public class TradingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingSystemApplication.class, args);
    }
}
