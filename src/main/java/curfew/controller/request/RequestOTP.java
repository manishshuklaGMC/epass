package curfew.controller.request;

import curfew.model.AccountIdentifierType;
import curfew.model.AccountType;
import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestOTP {
  private String identifier;
  private String publicKey;
  private AccountIdentifierType accountIdentifierType;
  private AccountType accountType;
  private StateName stateName;
}
