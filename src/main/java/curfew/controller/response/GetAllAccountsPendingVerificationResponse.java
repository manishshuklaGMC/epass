package curfew.controller.response;

import curfew.model.AccountInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetAllAccountsPendingVerificationResponse {
  private final List<AccountInfo> accounts;
}
