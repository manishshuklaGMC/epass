package curfew.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAdminAccountResponse {
  private final String password;
}
