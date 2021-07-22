package curfew.model;

import lombok.Getter;

@Getter
public abstract class Entity {
  private final String proofId;
  private final ProofType proofType;

  protected Entity(String proofId, ProofType proofType) {
    this.proofId = proofId;
    this.proofType = proofType;
  }
}
