package com.universalbank.trading_system.controller;

import com.universalbank.trading_system.entity.Order;
import com.universalbank.trading_system.entity.SalesPerson;
import com.universalbank.trading_system.service.OrderService;
import com.universalbank.trading_system.service.SalesPersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesPersonController {
    private final OrderService svc;
    private final SalesPersonService service;

    @PutMapping("/assignTrader/{orderId}") // SalesPerson  to update orders to assign trader
    public ResponseEntity<Order> assignTrader( @PathVariable Long id, @RequestParam("traderId") Long traderId) {
        try {
            return new ResponseEntity<>(svc.asignTrader(traderId, id),HttpStatusCode.valueOf(200));
        }
        catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatusCode.valueOf(500));

        }
    }




    @GetMapping("/traders")
    public ResponseEntity<List<SalesPerson>> activeTraders() {
        try {
            return new ResponseEntity(service.activeTraders(), HttpStatusCode.valueOf(200));
        }
        catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatusCode.valueOf(500));

        }
    }





}
