package curfew.controller;

import curfew.controller.request.GetOrganizationDetailsRequest;
import curfew.controller.request.UpdateActivePassLimitRequest;
import curfew.controller.response.GetAllOrganizationsRespose;
import curfew.controller.response.PutOrganizationResponse;
import curfew.exception.CurfewPassException;
import curfew.model.Account;
import curfew.model.AccountType;
import curfew.model.Organization;
import curfew.service.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/** Created by manish.shukla on 2020/4/1. */
@RestController
public class OrganizationController {
  private final OrganizationService organizationService;
  private final AuthenticationService authenticationService;

  public OrganizationController(
      OrganizationService organizationService, AuthenticationService authenticationService) {
    this.organizationService = organizationService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/addOrganization")
  public PutOrganizationResponse putOrganization(@RequestBody Organization organization) {
    return new PutOrganizationResponse(organizationService.putOrganization(organization));
  }

  @PostMapping("/getOrganization")
  public CompletableFuture<Organization> getOrganaizationDetails(
      @RequestBody GetOrganizationDetailsRequest getOrgRequest) {
    Account account = authenticationService.verifySession(getOrgRequest.getAuthToken());
    return organizationService.getOrganization(account.getOrganizationID(), account.getStateId()).toCompletableFuture();
  }

  @PostMapping("/getAllOrganizations")
  public CompletableFuture<GetAllOrganizationsRespose> listOrganizations(
      @RequestBody GetOrganizationDetailsRequest getOrgRequest) {
    Account account = authenticationService
            .verifySession(getOrgRequest.getAuthToken());
    if (account.getAccountType()
        .equals(AccountType.admin)) {
      return organizationService
          .getAllOrganizations(account.getStateId())
          .thenApply(GetAllOrganizationsRespose::new)
          .toCompletableFuture();
    } else {
      throw new CurfewPassException("Usage restricted to admin users");
    }
  }

  @PostMapping("/setPassLimit")
  public CompletionStage<String> setActivePassLimit(
      @RequestBody UpdateActivePassLimitRequest updateActivePassLimitRequest) {
    if (authenticationService
        .verifySession(updateActivePassLimitRequest.getAuthToken())
        .getAccountType()
        .equals(AccountType.admin)) {
      return organizationService
          .updateActivePassLimit(
              updateActivePassLimitRequest.getId(),
              updateActivePassLimitRequest.getNewLimit())
          .thenApply(__ -> "done");
    } else {
      throw new CurfewPassException("Usage restricted to admin users");
    }
  }
}
