package curfew.service;

import curfew.model.SignatureKeyPair;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Signature;
import java.security.SignatureException;

/** Created by manish.shukla on 2020/3/29. */
@Service
public class SignatureService {
  private static String SIGNATURE_ALGORITHM = "SHA256withDSA";
  private static String ENCODING = "UTF-8";
  @Autowired private SignatureKeyPair keyPair;

  public String sign(String message) throws SignatureException {
    try {
      Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
      sign.initSign(keyPair.getPrivateKey());
      sign.update(message.getBytes(ENCODING));
      return new String(Base64.encodeBase64(sign.sign()), ENCODING);
    } catch (Exception ex) {
      throw new SignatureException(ex);
    }
  }

  public boolean verify(String message, String signature) throws SignatureException {
    try {
      Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
      sign.initVerify(keyPair.getPublicKey());
      sign.update(message.getBytes(ENCODING));
      return sign.verify(Base64.decodeBase64(signature.getBytes(ENCODING)));
    } catch (Exception ex) {
      throw new SignatureException(ex);
    }
  }
}
