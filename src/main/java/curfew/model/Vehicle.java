package curfew.model;

import lombok.Getter;

@Getter
public class Vehicle extends Entity {
  private final String registrationNumber;
  private final String vehicleModel;

  public Vehicle(
      String proofId, ProofType proofType, String registrationNumber, String vehicleModel) {
    super(proofId, proofType);
    this.registrationNumber = registrationNumber;
    this.vehicleModel = vehicleModel;
  }
}
