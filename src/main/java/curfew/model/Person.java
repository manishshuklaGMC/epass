package curfew.model;

import lombok.Getter;

@Getter
public class Person extends Entity {
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String DOB;

  public Person(
      String proofId,
      ProofType proofType,
      String firstName,
      String lastName,
      String phoneNumber,
      String DOB) {
    super(proofId, proofType);
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.DOB = DOB;
  }
}
