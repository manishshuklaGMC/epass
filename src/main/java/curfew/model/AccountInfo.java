package curfew.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountInfo {
  private String name;
  private String email;
  private String orgID;
  private String orgName;
  private Integer id;
}
