package curfew.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by manish.shukla on 2020/3/25.
 */
@Builder
@Getter
public class User {
  private final Long id;
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String DOB;
  private final String gender;
  private final String city;
  private final String profession;
  private final String proofId;
  private final ProofType proofType;
  private final RoleType role;
  private final Long createdAt;
  private final Long updatedAt;
  private final UserStatus userStatus;
}
