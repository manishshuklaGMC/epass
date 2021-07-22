package curfew.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApproveAccount {
  private Integer requesterAccountId;
  private String authToken;
  private AccountAction accountAction;
}
