package curfew.model;

public enum ProofType {
  AADHAR("aadhar"),
  DL("dl"),
  PAN("pan"),
  ORG("org"),
  RC("rc"),
  PASSPORT("passport");

  private String proofType;

  ProofType(String proofType) {
    this.proofType = proofType;
  }

  public String getProofType() {
    return proofType;
  }
}
