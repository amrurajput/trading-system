package com.universalbank.trading_system.service;

import com.universalbank.trading_system.constant.TradingAppConstant;
import com.universalbank.trading_system.dto.*;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final ClientRepository clients;
    private final SalesPersonRepository salesRepo;
    private final TraderRepository tradersRepo;
    private final SymbolRepository symbols;
    private final StatusRepository statuses;
    private final StateRepository states;
    private final TradeExecutionRepository execRepo;


    @Transactional
    public OrderSummaryDTO placeNewOrder(PlaceOrderRequest dto) {
        log.info("placeNewOrder: {}", dto);
        Order o = new Order();
        populateLiveOrder(o, dto);
        orderRepo.saveAndFlush(o);
        log.info("New live order {} created", o.getId());
        return toSummary(o);
    }


    @Transactional
    public OrderSummaryDTO updateDraftOrder(PlaceOrderRequest dto, Long draftId) {
        log.info("updateDraftOrder (promote draft={}): {}", draftId, dto);

        // 1) Load the draft
        Order draft = lookup(orderRepo, draftId, "Order");
        if (!draft.getStatus().getId().equals(TradingAppConstant.draftStatus)) {
            throw new IllegalStateException("Order " + draftId + " is not a draft");
        }

        // 2) Populate it as a live order
        populateLiveOrder(draft, dto);

        // 3) Save & return
        Order promoted = orderRepo.saveAndFlush(draft);
        log.info("Draft {} promoted to live order {}", draftId, promoted.getId());
        return toSummary(promoted);
    }


    private void populateLiveOrder(Order o, PlaceOrderRequest dto) {
        // lookups
        Client client = lookup(clients, dto.getClientId(), "Client");
        Symbol symbol = symbols.findByCode(dto.getSymbolId())
                .orElseThrow(() -> new EntityNotFoundException("Symbol '" + dto.getSymbolId() + "' not found"));

        // statuses & states
        Status inProgStatus = lookup(statuses, TradingAppConstant.inProgressStatus, "Status");
        Status pendStatus = lookup(statuses, TradingAppConstant.pendingAssignmentStatus, "Status");
        State desiredState = lookup(states, TradingAppConstant.toBeAssignedTOTraderState, "State");
        State noSpState = lookup(states, TradingAppConstant.SalesPersonNotAvailableState, "State");

        // try SP assignment
        SalesPerson sp = Optional.ofNullable(dto.getSalesPersonId())
                .flatMap(id -> salesRepo.findByIdAndIsActiveAndIsOccupied(id, true, false))
                .orElse(null);

        boolean hasSp = sp != null;
        Status status = hasSp ? inProgStatus : pendStatus;
        State state = hasSp ? desiredState : noSpState;

        // now populate
        o.setClient(client);
        o.setSymbol(symbol);
        o.setSalesPerson(sp);
        o.setQuantity(dto.getQuantity());
        o.setPriceLimit(dto.getPriceLimit());
        o.setNotifyThreshold(dto.getPriceLimit());
        o.setTimeLimit(dto.getTimeLimit());
        o.setOperationType(dto.getOperationType());
        o.setStatus(status);
        o.setState(state);

        // audit
        LocalDateTime now = LocalDateTime.now();
        if (o.getId() == null) {
            o.setCreatedAt(now);
            o.setCreatedBy(dto.getCreatedBy());
            o.setNotificationSent(false);
        } else {
            o.setUpdatedAt(now);
            o.setUpdatedBy(dto.getCreatedBy());
        }

        // post‑assign occupancy if SP was assigned
        if (hasSp) {
            updateSalesOccupancy(sp, inProgStatus);
        }
    }


    @Transactional
    public OrderSummaryDTO createDraftOrder(PlaceOrderRequest dto) {
        log.info("createDraftOrder: {}", dto);

        var client = lookup(clients, dto.getClientId(), "Client");
        var symbol = symbols.findByCode(dto.getSymbolId())
                .orElseThrow(() -> new EntityNotFoundException("Symbol '" + dto.getSymbolId() + "' not found"));
        var draftStatus = lookup(statuses, TradingAppConstant.draftStatus, "Status");
        var draftState = lookup(states, TradingAppConstant.draftState, "State");

        Order o = Order.builder()
                .client(client)
                .symbol(symbol)
                .salesPerson(null)
                .quantity(dto.getQuantity())
                .priceLimit(dto.getPriceLimit())
                .notifyThreshold(dto.getPriceLimit())
                .timeLimit(dto.getTimeLimit())
                .operationType(dto.getOperationType())
                .status(draftStatus)
                .state(draftState)
                .notificationSent(false)
                .createdAt(LocalDateTime.now())
                .createdBy(dto.getCreatedBy())
                .build();

        orderRepo.save(o);
        log.info("Draft order {} created", o.getId());
        return toSummary(o);
    }


    @Transactional
    public OrderSummaryDTO asignTrader(Long traderId, Long orderId) {
        log.info("asignTrader: trader={} order={}", traderId, orderId);

        var order = lookup(orderRepo, orderId, "Order");
        if (!order.getStatus().getId().equals(TradingAppConstant.inProgressStatus) ||
                !order.getState().getId().equals(TradingAppConstant.toBeAssignedTOTraderState)) {
            throw new IllegalStateException("Order not in IN_PROGRESS/TRADER_TO_PICK");
        }

        var trader = lookup(tradersRepo, traderId, "Trader");
        if (!trader.getIsActive()) {
            throw new IllegalStateException("Trader " + traderId + " is inactive");
        }

        // capacity check
        long cnt = orderRepo.findByAssignedTraderIdAndStatus(
                traderId,
                lookup(statuses, TradingAppConstant.inProgressStatus, "Status")
        ).size();
        if (trader.getIsOccupied()) {
            throw new ConstraintViolationException("Trader at capacity " + cnt, null);
        }


        var pendingStatus = lookup(statuses, TradingAppConstant.inProgressStatus, "Status");
        var pickedByTrderState = lookup(states, TradingAppConstant.tradePickedByTraderState, "State");
        order.setAssignedTrader(trader);
        order.setStatus(pendingStatus);
        order.setState(pickedByTrderState);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(trader.getName());
        orderRepo.saveAndFlush(order);
        log.info("Trader {} assigned to order {}", traderId, orderId);

        updateTraderOccupancy(trader);
        return toSummary(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("deleteOrder: id={}", id);

        var order = lookup(orderRepo, id, "Order");
        long st = order.getStatus().getId();
        long stt = order.getState().getId();
        boolean can = st == (TradingAppConstant.draftStatus)
                || (st == (TradingAppConstant.inProgressStatus)
                && stt == (TradingAppConstant.toBeAssignedTOTraderState));
        if (!can) {
            throw new IllegalStateException("Cannot delete order in its current state");
        }

        var sp = order.getSalesPerson();
        var tr = order.getAssignedTrader();
        orderRepo.delete(order);
        orderRepo.flush();
        log.info("Order {} deleted", id);

        if (sp != null) updateSalesOccupancy(sp, lookup(statuses, TradingAppConstant.inProgressStatus, "Status"));
        if (tr != null) updateTraderOccupancy(tr);
    }

    @Transactional
    public TradeExecutionResponse execute(ExecuteOrderRequest dto) {
        log.info("execute: {}", dto);

        var order = lookup(orderRepo, dto.getOrderId(), "Order");
        var trader = lookup(tradersRepo, dto.getTraderId(), "Trader");

        var te = TradeExecution.builder()
                .order(order)
                .executedBy(trader)
                .executedQty(dto.getQty())
                .executedPrice(dto.getPrice())
                .executedAt(LocalDateTime.now())
                .build();
        te = execRepo.save(te);

        // recalc total
        double total = execRepo.findByOrder(order).stream()
                .mapToDouble(TradeExecution::getExecutedQty)
                .sum();
        var newState = total >= order.getQuantity()
                ? states.findById(TradingAppConstant.fullyExcecutedState)
                : states.findById(TradingAppConstant.partialExcecutedState);

        var newStatus = total >= order.getQuantity()
                ? statuses.findById(TradingAppConstant.completedStatus)
                : statuses.findById(TradingAppConstant.inProgressStatus);
        order.setState(newState.get());
        order.setStatus(newStatus.get());
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(trader.getName());
        orderRepo.save(order);

        updateTraderOccupancy(trader);
        log.info("Execution {} saved; order {} state={}", te.getId(), order.getId(), newState.get().getName());

        return new TradeExecutionResponse(
                order.getId(),
                order.getClient().getId(),
                order.getSalesPerson() != null
                        ? order.getSalesPerson().getId()
                        : null,
                te.getExecutedBy().getId(),
                order.getQuantity(),
                te.getExecutedQty(),
                te.getExecutedPrice(),
                te.getExecutedAt(),
                order.getNotifyThreshold(),
                order.getOperationType(),
                order.getStatus().getName(),
                order.getState().getName()
        );
    }

    private <T, ID> T lookup(JpaRepository<T, ID> repo, ID id, String name) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(name + " " + id + " not found"));
    }

    private void updateSalesOccupancy(SalesPerson sp, Status status) {
        long cnt = orderRepo.findBySalesPersonIdAndStatus(sp.getId(), status).size();
        boolean occ = cnt >= TradingAppConstant.NofOrderRequestThreshold;
        if (sp.getIsOccupied() != occ) {
            sp.setIsOccupied(occ);
            salesRepo.saveAndFlush(sp);
        }
    }

    private void updateTraderOccupancy(Trader tr) {
        long cnt = orderRepo.findByAssignedTraderIdAndStatus(
                tr.getId(),
                lookup(statuses, TradingAppConstant.inProgressStatus, "Status")
        ).size();
        boolean occ = cnt >= TradingAppConstant.NofOrderRequestThreshold;
        if (tr.getIsOccupied() != occ) {
            tr.setIsOccupied(occ);
            tradersRepo.saveAndFlush(tr);
        }
    }

    @Transactional(readOnly = true)
    public List<Order> list() {
        log.debug("list()");
        return orderRepo.findAll();
    }


    @Transactional(readOnly = true)
    public Order get(Long id) {
        log.debug("get(id={})", id);
        return lookup(orderRepo, id, "Order");
    }

    public List<SalesPerson> activeSalesPeople() {
        var result = salesRepo.findByIsActiveAndIsOccupied(true, false);
        if (result.size() < 1) {
            log.error("No active Sales Person Available in System to Handle Trade");
        }
        return result;
    }

    public List<Trader> activeTraders() {
        var result = tradersRepo.findByIsActiveTrueAndIsOccupiedFalse();
        if (result.size() < 1) {
            log.error("No active Sales Person Available in System to Handle Trade");
        }
        return result;
    }


    private OrderSummaryDTO toSummary(Order o) {
        return new OrderSummaryDTO(
                o.getId(),
                o.getClient().getId(),
                o.getSalesPerson() != null ? o.getSalesPerson().getId() : null,
                o.getAssignedTrader() != null ? o.getAssignedTrader().getId() : null,
                o.getSymbol().getCode(),
                o.getPriceLimit(),
                o.getQuantity(),
                o.getState().getName(),
                o.getStatus().getName(),
                o.getOperationType()
        );
    }
}

