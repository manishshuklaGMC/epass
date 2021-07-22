package curfew.controller;

import curfew.controller.request.RequestOTP;
import curfew.controller.request.ResendOTP;
import curfew.controller.request.VerifyOTP;
import curfew.controller.response.VerifyOTPResponse;
import curfew.exception.NotificationException;
import curfew.model.StatesDetail;
import curfew.service.AuthenticationService;
import curfew.service.OTPService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OTPController {
  private final OTPService otpService;
  private final StatesDetail statesDetail;
  private final AuthenticationService authenticationService;

  public OTPController(OTPService otpService, StatesDetail statesDetail,
                       AuthenticationService authenticationService) {
    this.otpService = otpService;
    this.statesDetail = statesDetail;
    this.authenticationService = authenticationService;
  }

  @RequestMapping(
    value = "/requestOTP",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public void requestOTP(@RequestBody RequestOTP request) throws NotificationException {
    try {
      authenticationService.invalidateSessions(request.getIdentifier(), request.getStateName());
    } catch (Exception e) {
      System.out.println("couldn't invalidate session");
    }
    otpService.generateOTP(
        request.getIdentifier(),
        request.getAccountIdentifierType(),
        request.getAccountType(),
        request.getPublicKey(),
        statesDetail.getStatesDetail().get(request.getStateName()).getId());
  }

  @RequestMapping(
    value = "/verifyOTP",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public VerifyOTPResponse verifyOTP(@RequestBody VerifyOTP request) {
    return otpService.verifyOTPAndCreateAccount(request);
  }

  @RequestMapping(
    value = "/resendOTP",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public void resendOTP(@RequestBody ResendOTP request) throws NotificationException {
    otpService.resendOTP(request.getIdentifier(), request.getAccountIdentifierType(), request.getStateName());
  }
}
