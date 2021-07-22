package curfew.controller.request;

import curfew.model.StateName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAdminAccount {
  private String name;
  private String email;
  private String orgName;
  private String authToken;
  private StateName stateName;
}
