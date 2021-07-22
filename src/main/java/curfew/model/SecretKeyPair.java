package curfew.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class SecretKeyPair {

  @NonNull String remotePublicKey;
  @NonNull String ourPrivateKey;
}
