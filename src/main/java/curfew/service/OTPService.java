package curfew.service;

import com.google.common.collect.ImmutableMap;
import curfew.controller.request.VerifyOTP;
import curfew.controller.response.VerifyOTPResponse;
import curfew.dao.AccountDAO;
import curfew.dao.OTPDAO;
import curfew.dao.SessionDAO;
import curfew.exception.CurfewPassException;
import curfew.exception.NotificationException;
import curfew.model.*;
import curfew.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OTPService {

  private static final String OTP_SMS_TEMPLATE = "otp_sms.ftl";
  private static final String EMAIL_VERIFICATION_TEMPLATE = "email_verification.ftl";
  private static final String EMAIL_VERIFICATION_SUBJECT = "Please verify your email address.";
  private final OTPDAO otpdao;
  private final NotificationService notificationService;
  private final AccountDAO accountDAO;
  private final SessionDAO sessionDAO;
  private final TokenApproverService tokenApproverService;

  @Value("${server.public_key}")
  private String serverPublicKey;

  private final StatesDetail statesDetail;

  public OTPService(
          OTPDAO otpdao,
          NotificationService notificationService,
          AccountDAO accountDAO,
          SessionDAO sessionDAO,
          TokenApproverService tokenApproverService, StatesDetail statesDetail) {
    this.otpdao = otpdao;
    this.notificationService = notificationService;
    this.accountDAO = accountDAO;
    this.sessionDAO = sessionDAO;
    this.tokenApproverService = tokenApproverService;
    this.statesDetail = statesDetail;
  }

  public void generateOTP(
      String identifier,
      AccountIdentifierType accountIdentifierType,
      AccountType accountType,
      String publicKey,
      Integer stateId)
      throws NotificationException {
    if (accountType == AccountType.police) {
      validatePoliceAccount(identifier);
    }
    String otpStr = Utils.getRandomNumberString();
    OTP storedOTP = otpdao.getLatestOTP(identifier).toCompletableFuture().join();
    if (storedOTP != null && storedOTP.getOtpStatus().equals(OTPStatus.invalid)) {
      throw new CurfewPassException("user blocked");
    }
    if (storedOTP == null
        || storedOTP.getValidTill() < System.currentTimeMillis()
        || storedOTP.getOtpStatus() == OTPStatus.verified) {
      otpdao
          .pushOTP(OTP.builder().otp(otpStr).identifier(identifier).publicKey(publicKey).build())
          .toCompletableFuture()
          .join();
      sendOTP(identifier, accountIdentifierType, otpStr, stateId);
    } else {
      sendOTP(identifier, accountIdentifierType, storedOTP.getOtp(), stateId);
    }
  }

  private TokenApprover validatePoliceAccount(String identifier) {
    TokenApprover tokenApprover = tokenApproverService.getEntryOrNull(identifier);
    if (tokenApprover == null) {
      throw new CurfewPassException("Not a token approver");
    }
    return tokenApprover;
  }

  private void sendOTP(
      String identifier, AccountIdentifierType accountIdentifierType, String otpStr, Integer stateID)
      throws NotificationException {
    if (accountIdentifierType == AccountIdentifierType.phone) {
      notificationService.sendSMS(OTP_SMS_TEMPLATE, identifier, ImmutableMap.of("otp", otpStr));
    } else if (accountIdentifierType == AccountIdentifierType.email) {
      notificationService.sendEmail(
          EMAIL_VERIFICATION_TEMPLATE,
          EMAIL_VERIFICATION_SUBJECT,
          identifier,
          ImmutableMap.of("otp", otpStr),
          stateID);
    } else {
      throw new RuntimeException("Account identifier type is not supported");
    }
  }

  public void resendOTP(String identifier, AccountIdentifierType accountIdentifierType, StateName stateName)
      throws NotificationException {
    OTP storedOTP = otpdao.getLatestOTP(identifier).toCompletableFuture().join();
    if (storedOTP != null
        && storedOTP.getValidTill() > System.currentTimeMillis()
        && storedOTP.getOtpStatus().equals(OTPStatus.unverified)) {
      sendOTP(identifier, accountIdentifierType, storedOTP.getOtp(),
              statesDetail.getStatesDetail().get(stateName).getId());
    } else {
      throw new RuntimeException("Previous OTP has expired, please request a new one.");
    }
  }

  public VerifyOTPResponse verifyOTPAndCreateAccount(VerifyOTP payload) {
    validateOTP(payload);
    if (payload.getAccountIdentifierType() == AccountIdentifierType.phone) {
      try {
        TokenApprover approver = validatePoliceAccount(payload.getIdentifier());
        accountDAO
            .putAccount(
                Account.builder()
                    .accountIdentifierType(AccountIdentifierType.phone)
                    .identifier(payload.getIdentifier())
                    .accountType(AccountType.police)
                    .name(payload.getIdentifier())
                    .passwordHashed("")
                    .status(AccountStatus.VERIFIED)
                    .organizationID(approver.getOrgName())
                        .stateId(statesDetail.getStatesDetail().get(payload.getStateName()).getId())
                    .build())
            .toCompletableFuture()
            .join();
      } catch (Exception e) {
        throw new CurfewPassException("user is blocked, please contact support");
      }
    } else if (payload.getAccountIdentifierType() == AccountIdentifierType.email) {
      Account account =
          accountDAO.getAccountByIdentifier(payload.getIdentifier(), statesDetail.getStatesDetail().get(payload.getStateName()).getId()).toCompletableFuture().join();
      if (account.getStatus() == AccountStatus.UNVERIFIED) {
        accountDAO
            .updateStatus(account.getId(), AccountStatus.POLICE_VERIFICATION_PENDING)
            .toCompletableFuture()
            .join();
      }
    } else {
      throw new CurfewPassException("OTP service not supported apart from mobileNumber and email.");
    }
    Account account =
        accountDAO.getAccountByIdentifier(payload.getIdentifier(), statesDetail.getStatesDetail().get(payload.getStateName()).getId()).toCompletableFuture().join();
    String authToken = Utils.getRandomSessionsString();
    sessionDAO
        .createSession(
            Session.builder()
                .sessionStatus(SessionStatus.active)
                .authToken(authToken)
                .userId(account.getId().longValue())
                .build())
        .toCompletableFuture()
        .join();
    return VerifyOTPResponse.builder().authToken(authToken).publicKey(serverPublicKey).build();
  }

  private void validateOTP(VerifyOTP payload) {
    OTP storedOTP = otpdao.getLatestOTP(payload.getIdentifier()).toCompletableFuture().join();
    if (storedOTP == null) {
      throw new CurfewPassException(
          String.format("No OTP Request for this identifier %s", payload.getIdentifier()));
    }
    if (storedOTP.getValidTill() < System.currentTimeMillis()
        || !storedOTP.getOtpStatus().equals(OTPStatus.unverified)) {
      throw new CurfewPassException("OTP has expired");
    }
    if (!storedOTP.getOtp().equals(payload.getOtp())) {
      if (storedOTP.getVerificationTrialCount() >= 4) {
        otpdao
            .updateCount(payload, OTPStatus.invalid, storedOTP.getVerificationTrialCount() + 1)
            .toCompletableFuture()
            .join();
        throw new CurfewPassException("Exceeded max retry count");
      }
      otpdao
          .updateCount(payload, storedOTP.getOtpStatus(), storedOTP.getVerificationTrialCount() + 1)
          .toCompletableFuture()
          .join();
      throw new CurfewPassException("Invalid OTP");
    }
    otpdao
        .updateCount(payload, OTPStatus.verified, storedOTP.getVerificationTrialCount() + 1)
        .toCompletableFuture()
        .join();
  }
}
