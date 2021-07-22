package curfew.controller;

import curfew.controller.response.SigningKeyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping(
    value = "/getSigningKey",
    consumes = {"application/JSON"}
  )
  // TODO: Authorize that the user can make this request!!!!
  public SigningKeyResponse createAccount(@RequestParam Long userId) {
    String key = userService.getEncryptedSigningKey(userId);
    return new SigningKeyResponse(key);
  }
}
