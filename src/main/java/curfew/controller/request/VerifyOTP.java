package curfew.controller.request;

import curfew.model.AccountIdentifierType;
import curfew.model.AccountType;
import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOTP {
  private String identifier;
  private AccountIdentifierType accountIdentifierType;
  private String otp;
  private AccountType accountType;
  private StateName stateName;
}
