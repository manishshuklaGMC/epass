package curfew.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Base64;

@Log
@Service
public class DHEService {

  private final String algorithm = "ECDH";

  @Value("${forwardsecrecy.dhe.provider:BC}")
  String provider;

  public String getSharedSecret(PrivateKey ourPrivatekey, PublicKey remotePublicKey)
      throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
    KeyAgreement ecdhKeyAgreement = KeyAgreement.getInstance(algorithm, provider);
    ecdhKeyAgreement.init(ourPrivatekey);
    ecdhKeyAgreement.doPhase(remotePublicKey, true);
    final byte[] secretKey = ecdhKeyAgreement.generateSecret();
    return Base64.getEncoder().encodeToString(secretKey);
  }
}
