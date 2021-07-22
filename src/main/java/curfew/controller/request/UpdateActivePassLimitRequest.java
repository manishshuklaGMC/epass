package curfew.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/4/1. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateActivePassLimitRequest {
  private String authToken;
  private Integer newLimit;
  private Integer id;
}
