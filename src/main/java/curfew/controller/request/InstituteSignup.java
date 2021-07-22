package curfew.controller.request;

import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class InstituteSignup {
  private final String email;
  private final String name;
  private final String organizationName;
  private final String cin;
  private final String tin;
  private final String type;
  private final String city;
  private final String password;
}
