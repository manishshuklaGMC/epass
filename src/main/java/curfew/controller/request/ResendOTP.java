package curfew.controller.request;

import curfew.model.AccountIdentifierType;
import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOTP {
  private String identifier;
  private AccountIdentifierType accountIdentifierType;
  private StateName stateName;
}
