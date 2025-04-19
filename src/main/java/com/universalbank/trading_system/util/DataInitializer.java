package com.universalbank.trading_system.util;

import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final StatusRepository statusRepo;
    private final StateRepository stateRepo;
    private final ClientRepository clientRepo;
    private final TraderRepository traderRepo;
    private final SymbolRepository symbolRepo;
    private final SalesPersonRepository salesRepo;

    @Override
    public void run(String... args) {
        seedStatus();
        seedState();
        seedClientsTradersSalespeople();
        seedSymbols();
    }

    private void seedStatus() {
        if (statusRepo.count() == 0) {
            statusRepo.save(Status.builder()
                    .name("PENDING").id(1l).description("Order pending")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            statusRepo.save(Status.builder()
                    .name("COMPLETED").id(2l).description("Order completed")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            log.info("✔ Seeded Status");
        }
    }

    private void seedState() {
        if (stateRepo.count() == 0) {
            stateRepo.save(State.builder()
                    .name("Trader To Pick").id(1l).description("Trader To Pick")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            stateRepo.save(State.builder()
                    .name("Picketd By Trader").id(2l).description("Picketd By Trader")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            log.info("✔ Seeded State");
        }
    }

    private void seedClientsTradersSalespeople() {
        if (clientRepo.count() == 0) {
            var c1 = clientRepo.save(Client.builder()
                    .name("Alice Smith").email("alice@example.com").phone("555-0101")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            var c2 = clientRepo.save(Client.builder()
                    .name("Bob Johnson").email("bob@example.com").phone("555-0202")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            var c3 = clientRepo.save(Client.builder()
                    .name("Charlie Davis").email("charlie@example.com").phone("555-0303")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());

            var t1 = traderRepo.save(Trader.builder()
                    .name("Trader Joe").specialty("Equities")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());
            var t2 = traderRepo.save(Trader.builder()
                    .name("Trader Jane").specialty("Fixed Income")
                    .createdAt(LocalDateTime.now()).createdBy("system").build());

            var s1 = SalesPerson.builder()
                    .name("Sales Sam").desk("Desk A")
                    .createdAt(LocalDateTime.now()).createdBy("system").build();
            var s2 = SalesPerson.builder()
                    .name("Sales Sally").desk("Desk B")
                    .createdAt(LocalDateTime.now()).createdBy("system").build();

            // link salespeople to clients
            s1.setClients(Set.of(c1, c2));
            s2.setClients(Set.of(c3));
            salesRepo.saveAll(Set.of(s1, s2));

            log.info("✔ Seeded Clients, Traders & SalesPeople");
        }
    }
    private void seedSymbols() {
        if (symbolRepo.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            symbolRepo.save(Symbol.builder()
                    .code("IBM")
                    .description("International Business Machines")
                    .createdAt(now).createdBy("system")
                    .updatedAt(now).updatedBy("system")
                    .build());
            symbolRepo.save(Symbol.builder()
                    .code("SEPC")
                    .description("Springfield Renewables")
                    .createdAt(now).createdBy("system")
                    .updatedAt(now).updatedBy("system")
                    .build());
            log.info("✔ Seeded Symbol table with IBM and SEPC");
        }
    }
}
