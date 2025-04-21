package com.universalbank.trading_system.controller;

import com.universalbank.trading_system.dto.ExecuteOrderRequest;
import com.universalbank.trading_system.dto.OrderSummaryDTO;
import com.universalbank.trading_system.dto.PlaceOrderRequest;
import com.universalbank.trading_system.dto.TradeExecutionResponse;
import com.universalbank.trading_system.entity.Order;
import com.universalbank.trading_system.entity.SalesPerson;
import com.universalbank.trading_system.entity.TradeExecution;
import com.universalbank.trading_system.entity.Trader;
import com.universalbank.trading_system.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Endpoints for creating, updating, and querying orders")
@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new live order",
            description = "Creates a live trading order. If a valid salesperson ID is provided and available, assigns immediately; otherwise enters PENDING_ASSIGNMENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/place")
    public OrderSummaryDTO placeOrder(
            @Parameter(description = "Order placement details", required = true)
            @Valid @RequestBody PlaceOrderRequest dto
    ) {
        log.debug("placeOrder: {}", dto);
        return orderService.placeNewOrder(dto);
    }

    @Operation(summary = "Create a draft order",
            description = "Creates an order in DRAFT status, no salesperson or trader assigned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Draft created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/draft")
    public OrderSummaryDTO createDraft(
            @Parameter(description = "Draft order details", required = true)
            @Valid @RequestBody PlaceOrderRequest dto
    ) {
        log.debug("createDraft: {}", dto);
        return orderService.createDraftOrder(dto);
    }

    @Operation(summary = "Promote a draft to a live order",
            description = "Converts a draft order (status=DRAFT) to a live order, applying same logic as placeOrder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Draft promoted successfully"),
            @ApiResponse(responseCode = "400", description = "Order not in DRAFT status or invalid params")
    })
    @PutMapping("/draft/{id}/promote")
    public OrderSummaryDTO promoteDraft(
            @Parameter(description = "Draft order ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Live order details", required = true) @Valid @RequestBody PlaceOrderRequest dto
    ) {
        log.debug("promoteDraft id={} dto={}", id, dto);
        return orderService.updateDraftOrder(dto, id);
    }

    @Operation(summary = "Assign a trader to an in-progress order",
            description = "Assigns the given trader to an order currently in IN_PROGRESS and TRADER_TO_PICK state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trader assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or capacity reached")
    })
    @PutMapping("/{orderId}/assign-trader")
    public OrderSummaryDTO assignTrader(
            @Parameter(description = "Order ID", required = true) @PathVariable("orderId") Long orderId,
            @Parameter(description = "Trader ID", required = true) @RequestParam Long traderId
    ) {
        log.debug("assignTrader orderId={} traderId={}", orderId, traderId);
        return orderService.asignTrader(traderId, orderId);
    }

    @Operation(summary = "Execute an order (partial or full)",
            description = "Records execution. Updates order state to PARTIALLY_COMPLETED or COMPLETED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Execution recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid execution request")
    })
    @PostMapping("/execute")
    public TradeExecutionResponse executeOrder(
            @Parameter(description = "Execution details", required = true) @Valid @RequestBody ExecuteOrderRequest dto
    ) {
        log.debug("executeOrder: {}", dto);
        return orderService.execute(dto);
    }

    @Operation(summary = "Delete an order",
            description = "Deletes a draft or an order pending trader assignment (IN_PROGRESS & TRADER_TO_PICK).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete the order in its current state")
    })
    @DeleteMapping("/{id}")
    public void deleteOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable("id") Long id
    ) {
        log.debug("deleteOrder id={}", id);
        orderService.deleteOrder(id);
    }

    @Operation(summary = "List all orders",
            description = "Returns all orders in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders returned")
    })
    @GetMapping
    public List<Order> listOrders() {
        log.debug("listOrders");
        return orderService.list();
    }

    @Operation(summary = "Get a specific order",
            description = "Retrieves an order by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order returned"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public Order getOrder(
            @Parameter(description = "Order ID", required = true) @PathVariable("id") Long id
    ) {
        log.debug("getOrder id={}", id);
        return orderService.get(id);
    }


    @Operation(summary = "List of available salesPeople",
            description = "Returns all available salesPeople in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of Available salesPeople returned")
    })
    @GetMapping("/salesPerson")
    public List<SalesPerson> salesPeople() {
        log.debug("/salesPerson");
        return orderService.activeSalesPeople();
    }

    @Operation(summary = "List of available traders",
            description = "Returns all available traders in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of Available traders returned")
    })
    @GetMapping("/traders")
    public List<Trader> traders() {
        log.debug("/traders");
        return orderService.activeTraders();
    }
}
