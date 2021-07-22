package curfew.controller.request;

import curfew.model.AccountType;
import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignIn {
  private String email;
  private String password;
  private AccountType accountType;
  private StateName stateName;
}
