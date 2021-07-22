package curfew.controller.request;

import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/3/26. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PutAccount {
  private String name;
  private String email;
  private String password;
  private String orgID;
  private String orgName;
  private StateName stateName;
}
