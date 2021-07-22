package curfew.controller.response;

import curfew.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class GetOrdersResponse {
  private final List<Order> orders;
}
