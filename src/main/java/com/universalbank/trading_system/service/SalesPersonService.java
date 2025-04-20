package com.universalbank.trading_system.service;

import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesPersonService {
    private final TraderRepository traderRepository;


    public List<Trader> activeTraders() {
        var result = traderRepository.findByIsActiveTrueAndIsOccupiedFalse();
        if(result.size() < 1) {
            log.error("No active Sales Person Available in System to Handle Trade");
        }
        return result;
    }


}
