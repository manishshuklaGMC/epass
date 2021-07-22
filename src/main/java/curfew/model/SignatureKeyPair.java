package curfew.model;

import java.security.PrivateKey;
import java.security.PublicKey;

public class SignatureKeyPair {
  private final PublicKey publicKey;
  private final PrivateKey privateKey;

  public SignatureKeyPair(PublicKey publicKey, PrivateKey privateKey) {
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }
}
