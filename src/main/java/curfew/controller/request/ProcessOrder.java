package curfew.controller.request;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProcessOrder {
  private Integer orderID;
  private String authToken;

  public ProcessOrder(Integer orderID, String authToken) {
    this.orderID = orderID;
    this.authToken = authToken;
  }

  public Integer getOrderID() {
    return orderID;
  }

  public String getAuthToken() {
    return authToken;
  }
}
