package curfew.model;

import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class OTP {
  private final Long id;
  private final Long createdAt;
  private final Long validTill;
  private final String otp;
  private final String identifier;
  private final AccountIdentifierType accountIdentifierType;
  private final String publicKey;
  private final OTPStatus otpStatus;
  private final Integer verificationTrialCount;
}
