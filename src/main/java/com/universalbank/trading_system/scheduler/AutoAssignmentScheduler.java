package com.universalbank.trading_system.scheduler;

import com.universalbank.trading_system.constant.TradingAppConstant;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoAssignmentScheduler {

    private final OrderRepository        orderRepo;
    private final SalesPersonRepository  salesRepo;
    private final TraderRepository       tradersRepo;
    private final StatusRepository       statusesRepo;
    private final StateRepository        statesRepo;

    /**
     * Runs every minute.  If the feature flag is off, does nothing.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoAssign() {
        if (!TradingAppConstant.autoAssignEnabled) {
            log.debug("Auto‑assign is disabled");
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(TradingAppConstant.autoAssignDelayMinutes);

        // 1) New orders with no salesPerson
        List<Order> unclaimedSales = orderRepo
                .findBySalesPersonIsNullAndCreatedAtBefore(cutoff);

        for (Order o : unclaimedSales) {
            Optional<SalesPerson> optSp = salesRepo
                    .findFirstByIsActiveTrueAndIsOccupiedFalse();
            if (optSp.isPresent()) {
                SalesPerson sp = optSp.get();
                o.setSalesPerson(sp);
                o.setUpdatedAt(LocalDateTime.now());
                o.setUpdatedBy("SYSTEM‑AUTO");
                orderRepo.saveAndFlush(o);

                // update occupancy if needed
                long cnt = orderRepo
                        .findBySalesPersonIdAndStatus(
                                sp.getId(),
                                statusesRepo.findById(TradingAppConstant.inProgressStatus)
                                        .orElseThrow()
                        ).size();
                if (cnt > TradingAppConstant.clientNoOfRequestThreshold) {
                    sp.setIsOccupied(true);
                    salesRepo.saveAndFlush(sp);
                }
                log.info("Auto‑assigned SalesPerson {} to Order {}", sp.getId(), o.getId());
            }
        }

        // 2) In‑progress orders waiting for trader
        Status  inProgStatus   = statusesRepo.findById(TradingAppConstant.inProgressStatus)
                .orElseThrow();
        State   pickState      = statesRepo.findById(TradingAppConstant.traderToPickState)
                .orElseThrow();
        List<Order> unclaimedTraders = orderRepo
                .findByAssignedTraderIsNullAndStatusAndStateAndUpdatedAtBefore(
                        inProgStatus, pickState, cutoff);

        for (Order o : unclaimedTraders) {
            Optional<Trader> optTr = tradersRepo
                    .findFirstByIsActiveTrueAndIsOccupiedFalse();
            if (optTr.isPresent()) {
                Trader tr = optTr.get();
                o.setAssignedTrader(tr);
                o.setUpdatedAt(LocalDateTime.now());
                o.setUpdatedBy("SYSTEM‑AUTO");
                orderRepo.saveAndFlush(o);

                // update occupancy for trader
                long cnt = orderRepo
                        .findByAssignedTraderIdAndStatus(
                                tr.getId(),
                                inProgStatus
                        ).size();
                tr.setIsOccupied(cnt >= TradingAppConstant.clientNoOfRequestThreshold);
                tradersRepo.saveAndFlush(tr);

                log.info("Auto‑assigned Trader {} to Order {}", tr.getId(), o.getId());
            }
        }
    }
}
