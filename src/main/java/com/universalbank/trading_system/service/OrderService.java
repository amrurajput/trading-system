package com.universalbank.trading_system.service;
import com.universalbank.trading_system.constant.TradingAppConstant;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import com.universalbank.trading_system.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final ClientRepository clientsRepo;
    private final SalesPersonRepository salesRepo;
    private final TraderRepository tradersRepo;
    private final SymbolRepository symbolsRepo;
    private final StatusRepository statusesRepo;
    private final StateRepository statesRepo;
    private final TradeExecutionRepository execRepo;

    public Order placeNewOrder(PlaceOrderRequest dto) {


        List<String> errors = new ArrayList<>();

        Optional<Client> clientOpt = clientsRepo.findById(dto.getClientId());
        Optional<SalesPerson> spOpt = salesRepo.findByIdAndIsActiveAndIsOccupied(dto.getSalesPersonId(),true, false);
        Optional<Symbol>  symbolOpt = symbolsRepo.findByCode(dto.getSymbolId());
        Optional<Status> statusOpt = statusesRepo.findById(dto.getStatusId());
        Optional<State>  stateOpt  = statesRepo.findById(dto.getStateId());

        if (clientOpt.isEmpty())   errors.add("clientId "   + dto.getClientId() + " not found in system");
        if (spOpt.isEmpty())       errors.add("salesPersonId " + dto.getSalesPersonId() + " not found in system");
        if (symbolOpt.isEmpty())   errors.add("symbolId '"  + dto.getSymbolId() + "' not found in system");
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(
                    "Validation failed: " + String.join("; ", errors), Collections.emptySet()
            );
        }

        Client client        = clientOpt.get();
        SalesPerson salesP   = spOpt.get();
        Symbol symbol        = symbolOpt.get();

        Order o = Order.builder()
                .client(client)
                .salesPerson(salesP)
                .symbol(symbol)
                .quantity(dto.getQuantity())
                .priceLimit(dto.getPriceLimit())
                .notifyThreshold(dto.getPriceLimit())
                .timeLimit(dto.getTimeLimit())
                .operationType(dto.getOperationType())
                .status(statusOpt.get())
                .state(stateOpt.get())
                .notificationSent(false)
                .createdAt(LocalDateTime.now())
                .createdBy(dto.getCreatedBy())
                .build();

        var result = orderRepo.saveAndFlush(o);


        var  salesPersonNoOfOrders =  orderRepo.findBySalesPersonIdAndStatus(salesP.getId(),statusOpt.get());
        if(salesPersonNoOfOrders.size() > TradingAppConstant.clientNoOfRequestThreshold) {
            salesP.setIsOccupied(true);
            salesRepo.saveAndFlush(salesP);
        }

        return result;
    }
    public List<Order> list() { return orderRepo.findAll(); }
    public Order get(Long id) { return orderRepo.findById(id).orElseThrow(); }



    @Transactional
    public Order updateDraftOrder(PlaceOrderRequest dto, Long orderId) throws Exception {
        // 1) Fetch and verify draft status
        Order existing = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order " + orderId + " not found"));
        if (existing.getStatus().getId() != TradingAppConstant.draftStatus) {
            throw new Exception("Only draft orders (statusId=0) can be updated");
        }

        // 2) Gather validation errors
        List<String> errors = new ArrayList<>();
        Optional<Client>     clientOpt = clientsRepo.findById(dto.getClientId());
        Optional<SalesPerson> spOpt     = salesRepo.findByIdAndIsActiveAndIsOccupied(
                dto.getSalesPersonId(), true, false);
        Optional<Symbol>     symbolOpt = symbolsRepo.findByCode(dto.getSymbolId());
        Optional<State>      stateOpt  = statesRepo.findById(TradingAppConstant.inProgressStatus);
        Optional<Status>     statusOpt = statusesRepo.findById(TradingAppConstant.traderToPickState);

        if (clientOpt.isEmpty())   errors.add("clientId "    + dto.getClientId() + " not found");
        if (spOpt.isEmpty())       errors.add("salesPersonId " + dto.getSalesPersonId() + " not available");
        if (symbolOpt.isEmpty())   errors.add("symbolId '"    + dto.getSymbolId() + "' not found");
        if (stateOpt.isEmpty())    errors.add("stateId "     + dto.getStateId() + " not found");
        if (statusOpt.isEmpty())   errors.add("statusId "    + dto.getStatusId() + " not found");

        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(
                    "Validation failed: " + String.join("; ", errors),
                    Collections.emptySet()
            );
        }

        // 3) Apply updates
        existing.setClient(clientOpt.get());
        existing.setSalesPerson(spOpt.get());
        existing.setSymbol(symbolOpt.get());
        existing.setQuantity(dto.getQuantity());
        existing.setPriceLimit(dto.getPriceLimit());
        existing.setNotifyThreshold(dto.getPriceLimit());
        existing.setTimeLimit(dto.getTimeLimit());
        existing.setOperationType(dto.getOperationType());
        existing.setState(stateOpt.get());
        existing.setStatus(statusOpt.get());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(dto.getCreatedBy());

        // 4) Save & flush
        Order updated = orderRepo.saveAndFlush(existing);

        // 5) Occupancy logic: count this salesPerson’s orders at the chosen status
        long count = orderRepo
                .findBySalesPersonIdAndStatus(spOpt.get().getId(), statusOpt.get())
                .size();
        if (count > TradingAppConstant.clientNoOfRequestThreshold) {
            SalesPerson sp = spOpt.get();
            sp.setIsOccupied(true);
            salesRepo.saveAndFlush(sp);
        }

        return updated;
    }

    @Transactional
    public Order asignTrader(Long traderId, Long orderId) {
        // 1) Fetch the order and verify it’s awaiting trader assignment
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order " + orderId + " not found"));
        Long inProgressStatusId   = TradingAppConstant.inProgressStatus;
        Long traderToPickStateId  = TradingAppConstant.traderToPickState;
        if (order.getStatus().getId() != inProgressStatusId ||
                order.getState().getId()  != traderToPickStateId) {
            throw new IllegalStateException(
                    "Only orders with status=IN_PROGRESS and state=TRADER_TO_PICK can be assigned");
        }

        // 2) Fetch the trader
        Trader trader = tradersRepo.findById(traderId)
                .orElseThrow(() -> new EntityNotFoundException("Trader " + traderId + " not found"));
    // check trader is active or not

        if (trader.getIsActive()) {
            throw new ConstraintViolationException(
                    "Trader " + traderId +
                            " is not active ",
                    Collections.emptySet()
            );
        }
        // 3) Capacity check before assignment
        Status inProgressStatus = statusesRepo.findById((long)inProgressStatusId)
                .orElseThrow(() -> new EntityNotFoundException("Status IN_PROGRESS not defined"));
        long activeCount = orderRepo
                .findByAssignedTraderIdAndStatus(traderId, inProgressStatus)
                .size();
        if (trader.getIsOccupied() ) {
            throw new ConstraintViolationException(
                    "Trader " + traderId +
                            " already has " + activeCount +
                            " active orders (max = " + TradingAppConstant.clientNoOfRequestThreshold + ")",
                    Collections.emptySet()
            );
        }

        // 4) Assign & audit
        order.setAssignedTrader(trader);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(trader.getName());
        Order updatedOrder = orderRepo.saveAndFlush(order);

        // 5) Post‑assignment: update trader’s occupancy
        long newCount = orderRepo
                .findByAssignedTraderIdAndStatus(traderId, inProgressStatus)
                .size();
        if (newCount >= TradingAppConstant.clientNoOfRequestThreshold) {
            trader.setIsOccupied(true);
        } else {
            trader.setIsOccupied(false);
        }
        tradersRepo.saveAndFlush(trader);

        return updatedOrder;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        // 1) Fetch
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order " + orderId + " not found"));

        long draftStatusId        = TradingAppConstant.draftStatus;
        long inProgressStatusId   = TradingAppConstant.inProgressStatus;
        long traderToPickStateId  = TradingAppConstant.traderToPickState;

        long currentStatusId = order.getStatus().getId();
        long currentStateId  = order.getState().getId();

        // 2) Check allowed for deletion
        boolean isDraft        = (currentStatusId == draftStatusId);
        boolean isPendingPick  = (currentStatusId == inProgressStatusId
                && currentStateId == traderToPickStateId);

        if (!isDraft && !isPendingPick) {
            throw new IllegalStateException(
                    "Cannot delete order in status=" + currentStatusId +
                            " and state=" + currentStateId);
        }

        // Capture references for occupancy update
        SalesPerson sp = order.getSalesPerson();
        Trader      tr = order.getAssignedTrader();

        // 3) Delete
        orderRepo.delete(order);
        orderRepo.flush();

        // 4) Update SalesPerson occupancy
        long spCount = orderRepo
                .findBySalesPersonIdAndStatus(sp.getId(),
                        statusesRepo.findById((long)inProgressStatusId)
                                .orElseThrow(() -> new EntityNotFoundException("Status IN_PROGRESS not defined")))
                .size();

        if (spCount < TradingAppConstant.clientNoOfRequestThreshold) {
            sp.setIsOccupied(false);
            salesRepo.saveAndFlush(sp);
        }

        // 5) Update Trader occupancy (if one was assigned)
        if (tr != null) {
            long trCount = orderRepo
                    .findByAssignedTraderIdAndStatus(tr.getId(),
                            statusesRepo.findById((long)inProgressStatusId)
                                    .orElseThrow(() -> new EntityNotFoundException("Status IN_PROGRESS not defined")))
                    .size();

            if (trCount < TradingAppConstant.clientNoOfRequestThreshold) {
                tr.setIsOccupied(false);
                tradersRepo.saveAndFlush(tr);
            }
        }
    }

    @Transactional
    public TradeExecution execute(ExecuteOrderRequest dto) {
        // 1) Load order and trader
        Order order = get(dto.getOrderId());
        Trader trader = tradersRepo.findById(dto.getTraderId())
                .orElseThrow(() -> new EntityNotFoundException("Trader " + dto.getTraderId() + " not found"));

        // 2) Persist this execution
        TradeExecution te = TradeExecution.builder()
                .order(order)
                .executedBy(trader)
                .executedQty(dto.getQty())
                .executedPrice(dto.getPrice())
                .executedAt(LocalDateTime.now())
                .build();
        te = execRepo.save(te);

        // 3) Recompute cumulative executed quantity
        List<TradeExecution> allExecs = execRepo.findByOrder(order);
        double totalExecuted = allExecs.stream()
                .mapToDouble(TradeExecution::getExecutedQty)
                .sum();

        // 4) Decide new state
        State newState;
        if (totalExecuted >= order.getQuantity()) {
            newState = statesRepo.findByName("COMPLETED");
        } else {
            newState = statesRepo.findByName("PARTIALLY_COMPLETED");
        }
        order.setState(newState);
        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(trader.getName());

        // 5) Persist order update
        orderRepo.save(order);

        // 6) Update trader occupancy: count remaining in-progress orders
        Status inProgressStatus = statusesRepo.findById(
                (long) TradingAppConstant.inProgressStatus
        ).orElseThrow(() -> new EntityNotFoundException("Status IN_PROGRESS not defined"));

        long activeCount = orderRepo
                .findByAssignedTraderIdAndStatus(trader.getId(), inProgressStatus)
                .size();

        // If they've dropped below the threshold, clear their occupied flag
        if (activeCount < TradingAppConstant.clientNoOfRequestThreshold && trader.getIsOccupied()) {
            trader.setIsOccupied(false);
            tradersRepo.saveAndFlush(trader);
        }

        return te;
    }
}
