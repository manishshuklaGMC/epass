package curfew.util;

import curfew.exception.CurfewPassException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Slf4j
public class AESEncryptor {

  private static final String transformation = "AES/ECB/PKCS5Padding";
  private static final String algorithm = "AES";

  public static String encrypt(String strToEncrypt, String secretKey) {
    try {
      Optional<SecretKeySpec> secretKeySpec = buildSecretKeySpec(secretKey);
      if (secretKeySpec.isPresent()) {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec.get());
        return Base64.getEncoder()
            .encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
      }

    } catch (Exception e) {
      log.error(
          "Error while encrypting the string ["
              + strToEncrypt
              + "] with secret key ["
              + secretKey
              + "]",
          e);
      throw new CurfewPassException("Error while encrypting the string [" + strToEncrypt + "]", e);
    }
    return null;
  }

  private static Optional<SecretKeySpec> buildSecretKeySpec(String myKey) {
    byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
    SecretKeySpec secretKey = new SecretKeySpec(key, algorithm);
    return Optional.of(secretKey);
  }
}
