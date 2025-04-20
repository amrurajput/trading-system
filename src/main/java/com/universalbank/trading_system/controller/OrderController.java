package com.universalbank.trading_system.controller;
import com.universalbank.trading_system.dto.*;
import com.universalbank.trading_system.entity.*;
import com.universalbank.trading_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService svc;

    @PostMapping("/placeOrder")
    public ResponseEntity<Order> placeNewOrder(@RequestBody PlaceOrderRequest dto) {
        try {
            return new ResponseEntity<>(svc.placeNewOrder(dto),HttpStatusCode.valueOf(200));
        }
        catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatusCode.valueOf(500));

        }
    }

    @PutMapping("/updateDraftOrder/{orderId}") // client to update orders to assign trader
    public ResponseEntity<Order> updateDraftOrder(@RequestBody PlaceOrderRequest dto, @PathVariable Long id) {
      try {
          return new ResponseEntity<>(svc.updateDraftOrder(dto, id),HttpStatusCode.valueOf(200));
    }
        catch (Exception e) {
        return new ResponseEntity(e.getMessage(), HttpStatusCode.valueOf(500));

        }
    }


    @DeleteMapping("/deleteOrder/{orderId}") // salesPerson to assign trader
    public ResponseEntity<String> deleteOrder( @PathVariable Long id) {

        try {
            svc.deleteOrder(id);
            return new ResponseEntity<>("SuccessFully Deleted Order With ID "+id,HttpStatusCode.valueOf(200));
        }
        catch (Exception e) {
            return new ResponseEntity("Error Occurred  while deleting Order With ID "+id + " :: "+ e.getMessage(), HttpStatusCode.valueOf(500));

        }
    }

    @GetMapping
    public List<Order> list() {
        return svc.list();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return svc.get(id);
    }


}
