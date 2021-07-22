package curfew.model;

/** Created by manish.shukla on 2020/3/25. */
public enum RoleType {
  ADMIN("admin"),
  USER("user");
  //  MEDICAL,
  //  DEFENCE,
  //  ADMINISTRATION,
  //  ESSENTIAL_SERVICE_CLASS1,
  //  ESSENTIAL_SERVICE_CLASS2,
  //  ESSENTIAL_SERVICE_CLASS3,
  //  PUBLIC

  private String userType;

  RoleType(String userType) {
    this.userType = userType;
  }

  public String getUserType() {
    return userType;
  }
}
