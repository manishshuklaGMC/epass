package curfew.controller;

import curfew.controller.request.*;
import curfew.controller.response.CreateAdminAccountResponse;
import curfew.controller.response.GetAllAccountsPendingVerificationResponse;
import curfew.controller.response.PutAccountResponse;
import curfew.controller.response.SignInResponse;
import curfew.dao.OrganizationDAO;
import curfew.exception.CurfewPassException;
import curfew.model.Account;
import curfew.model.AccountIdentifierType;
import curfew.service.AuthenticationService;
import curfew.util.Utils;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final OrganizationDAO organizationDAO;

  public AuthenticationController(
      AuthenticationService authenticationService, OrganizationDAO organizationDAO) {
    this.authenticationService = authenticationService;
    this.organizationDAO = organizationDAO;
  }

  @RequestMapping(
    value = "/createAccount",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public PutAccountResponse createAccount(@RequestBody PutAccount request) {
    if (!Utils.isValid(request.getEmail())) {
      throw new CurfewPassException("Invalid email");
    }
    try {
      return authenticationService.addAccount(
          request.getName(),
          request.getEmail(),
          request.getPassword(),
          request.getOrgID(),
          request.getOrgName(),
              request.getStateName());
    } catch (Exception e) {
      throw new CurfewPassException("Error Creating account " + e.getLocalizedMessage());
    }
  }

  @RequestMapping(
    value = "/createAdminAccount",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public CreateAdminAccountResponse createAdminAccount(@RequestBody CreateAdminAccount request) {
    try {
      return authenticationService.addAdminAccount(
          request.getName(), request.getEmail(), request.getOrgName(), request.getAuthToken(), request.getStateName());
    } catch (Exception e) {
      throw new CurfewPassException("Error Creating account " + e.getLocalizedMessage());
    }
  }

  @RequestMapping(
    value = "/signin",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public SignInResponse signin(@RequestBody SignIn request) {
    try {
      return authenticationService
          .signIn(request.getEmail(), request.getPassword(), request.getAccountType(), request.getStateName())
          .toCompletableFuture()
          .join();
    } catch (Exception e) {
      throw new CurfewPassException("Error Signing in " + e.getLocalizedMessage());
    }
  }

  @RequestMapping(
    value = "/updatePassword",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public String updatePassword(@RequestBody UpdatePassword updatePassword) {
    try {
      authenticationService.updatePassword(
          updatePassword.getEmail(), updatePassword.getPassword(), updatePassword.getAuthToken());
      return "DONE";
    } catch (Exception e) {
      throw new CurfewPassException("Error Creating account" + e.getLocalizedMessage());
    }
  }

  @RequestMapping(
    value = "/approveAccount",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public String approveAccount(@RequestBody ApproveAccount request) {
    try {
      authenticationService.approveAccount(request);
      return "approved";
    } catch (Exception e) {
      throw new CurfewPassException("Error Creating account" + e.getLocalizedMessage());
    }
  }

  @RequestMapping(
    value = "/getAllAccountsPendingVerification",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public GetAllAccountsPendingVerificationResponse getAllAccountsPendingVerification(
      @RequestBody GetAllAccountsPendingVerification request) {
    try {
      return authenticationService.getAllAccountsPendingVerification(request.getAuthToken());
    } catch (Exception e) {
      throw new CurfewPassException("Error Creating account" + e.getLocalizedMessage());
    }
  }

  @PostMapping("/getRequesterUserProfile")
  public GetUserProfile getRequesterUserProfile(@RequestParam("authToken") String authToken) {
    return authenticationService.getRequesterUserProfile(authToken).toCompletableFuture().join();
  }

  @PostMapping("/getApproverUserProfile")
  public GetUserProfile getApproverUserProfile(@RequestParam("authToken") String authToken) {
    Account userAccount = authenticationService.verifySession(authToken);
    return authenticationService.getApproverUserProfile(userAccount);
  }
}
