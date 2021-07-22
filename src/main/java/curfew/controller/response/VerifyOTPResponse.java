package curfew.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyOTPResponse {
  private final String authToken;
  private final String publicKey;
}
