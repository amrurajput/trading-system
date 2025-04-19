package com.universalbank.trading_system.controller;
import com.universalbank.trading_system.dto.*;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService svc;

    @PostMapping
    public Order place(@RequestBody PlaceOrderRequest dto) {
        return svc.place(dto);
    }

    @GetMapping
    public List<Order> list() {
        return svc.list();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping("/execute")
    public TradeExecution execute(@RequestBody ExecuteOrderRequest dto) {
        return svc.execute(dto);
    }
}
