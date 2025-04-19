package com.universalbank.trading_system.service;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import com.universalbank.trading_system.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repo;
    private final ClientRepository clients;
    private final SalesPersonRepository sales;
    private final TraderRepository traders;
    private final SymbolRepository symbols;
    private final StatusRepository statuses;
    private final StateRepository states;
    private final TradeExecutionRepository execRepo;

    public Order place(PlaceOrderRequest dto) {
        Order o = Order.builder()
            .client(clients.findById(dto.clientId()).orElseThrow())
            .salesPerson(sales.findById(dto.salesPersonId()).orElseThrow())
            .assignedTrader(traders.findById(dto.traderId()).orElseThrow())
            .symbol(symbols.findByCode(dto.symbolId()).orElseThrow())
            .quantity(dto.quantity())
            .priceLimit(dto.priceLimit())
            .timeLimit(dto.timeLimit())
                .operationType(dto.operationType())
            .status(statuses.findById(dto.statusId()).get())
            .state(states.findById(dto.stateId()).get())
            .notifyThreshold(dto.notifyThreshold())
            .notificationSent(false)
            .createdAt(LocalDateTime.now())
            .createdBy(dto.createdBy())
            .build();
        return repo.save(o);
    }

    public List<Order> list() { return repo.findAll(); }
    public Order get(Long id) { return repo.findById(id).orElseThrow(); }

    @Transactional
    public TradeExecution execute(ExecuteOrderRequest dto) {
        Order o = get(dto.orderId());
        o.setState(states.findByName("COMPLETED"));
        repo.save(o);
        TradeExecution te = TradeExecution.builder()
            .order(o)
            .executedBy(traders.findById(dto.traderId()).orElseThrow())
            .executedQty(dto.qty())
            .executedPrice(dto.price())
            .executedAt(LocalDateTime.now())
            .build();
        return execRepo.save(te);
    }
}
