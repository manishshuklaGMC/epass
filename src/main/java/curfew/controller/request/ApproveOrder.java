package curfew.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOrder {
  private Integer orderID;
  private OrderAction orderAction;
  private String reason;
  private String authToken;
}
