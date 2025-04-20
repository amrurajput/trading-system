package com.universalbank.trading_system.controller;

import com.universalbank.trading_system.dto.ExecuteOrderRequest;
import com.universalbank.trading_system.entity.TradeExecution;
import com.universalbank.trading_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trader")
@RequiredArgsConstructor
public class TraderController {

    private final OrderService svc;
    @PostMapping("/execute")
    public TradeExecution execute(@RequestBody ExecuteOrderRequest dto) {
        return svc.execute(dto);
    }
}
