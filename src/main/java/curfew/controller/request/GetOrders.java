package curfew.controller.request;

import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class GetOrders {
  private final Integer accountID;
  private final String authToken;
  private final Integer pageNumber;
  private final Integer pageSize;
}
