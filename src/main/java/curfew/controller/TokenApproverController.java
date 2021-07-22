package curfew.controller;

import curfew.model.Account;
import curfew.service.AuthenticationService;
import curfew.service.TokenApproverService;
import curfew.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class TokenApproverController {
  private final TokenApproverService tokenApproverService;
  private final AuthenticationService authenticationService;
  private int superUserID;

  public TokenApproverController(
      @Value("${superuser.id}") int superUserID,
      TokenApproverService tokenApproverService,
      AuthenticationService authenticationService) {
    this.superUserID = superUserID;
    this.tokenApproverService = tokenApproverService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/uploadTokenApprovers")
  public String uploadTokenApprovers(
      @RequestParam("file") MultipartFile file, @RequestParam("authToken") String authToken) {
    Account account = authenticationService.verifySession(authToken);
    if (account == null || account.getId() != superUserID) {
      throw new RuntimeException("You should be a super user to put token approvers.");
    }
    List<String> lines = Utils.getFileLines(file);
    tokenApproverService.makeEntries(lines);
    return "DONE";
  }
}
