package com.universalbank.trading_system.service;

import com.universalbank.trading_system.constant.TradingAppConstant;
import com.universalbank.trading_system.dto.ExecuteOrderRequest;
import com.universalbank.trading_system.dto.PlaceOrderRequest;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final SalesPersonRepository salesRepo;




    public List<SalesPerson> activeSalesPeople() {
        var result = salesRepo.findByIsActiveAndIsOccupied(true, false);
        if(result.size() < 1) {
            log.error("No active Sales Person Available in System to Handle Trade");
        }
        return result;
    }



}
