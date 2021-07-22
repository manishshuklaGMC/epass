package curfew.service;

import curfew.model.SecretKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;

@Service
public class KeyStoreService {

  @Autowired private KeyPair keyPair;

  @Autowired private ECCService eccService;

  public String generateSharedKey(String mobilePublicKey) {
    SecretKeyPair spec =
        new SecretKeyPair(
            mobilePublicKey, eccService.getPEMEncodedStream(keyPair.getPrivate(), true));
    return eccService.getSharedKey(spec);
  }
}
