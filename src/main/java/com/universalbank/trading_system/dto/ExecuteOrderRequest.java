package com.universalbank.trading_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class ExecuteOrderRequest {

private Long orderId;
private Long traderId;
private  double price;
private int qty;
}
