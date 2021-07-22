package curfew.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/3/26. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignInResponse {
  private String authToken;
  private Integer accountID;
  private String accountName;
  private String organizationID;
  private String organizationName;
}
