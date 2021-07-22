package curfew.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/3/26. */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PutAccountResponse {
  private String message;
}
