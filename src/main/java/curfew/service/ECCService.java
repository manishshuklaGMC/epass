package curfew.service;

import curfew.exception.CurfewPassException;
import curfew.model.DHPublicKey;
import curfew.model.KeyMaterial;
import curfew.model.SecretKeyPair;
import curfew.model.SerializedKeyPair;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.spec.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@Service
public class ECCService {

  @Value("${forwardsecrecy.ecc.curve:curve25519}")
  String curve;

  @Value("${forwardsecrecy.ecc.algorithm:EC}")
  String algorithm;

  @Value("${forwardsecrecy.ecc.keyDerivationAlgorithm:ECDH}")
  String keyDerivationAlgorithm;

  @Value("${forwardsecrecy.ecc.provider:BC}")
  String provider;

  @Value("${forwardsecrecy.ecc.keyExpiryHrs:24}")
  int keyExpiry;

  @Autowired DHEService dheService;

  public SerializedKeyPair getKeyPair()
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

    final KeyPair kp = this.generateKey();
    final String privateKey = this.getPEMEncodedStream(kp.getPrivate(), true);
    final String publicKey = this.getPEMEncodedStream(kp.getPublic(), false);
    Date date = new Date();
    Calendar cl = Calendar.getInstance();
    cl.setTime(date);
    cl.add(Calendar.HOUR, keyExpiry);
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df =
        new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    String expiryAsISO = df.format(cl.getTime());
    final DHPublicKey dhPublicKey = new DHPublicKey(expiryAsISO, "", publicKey);
    final KeyMaterial keyMaterial = new KeyMaterial(keyDerivationAlgorithm, curve, "", dhPublicKey);
    final SerializedKeyPair serializedKeyPair = new SerializedKeyPair(privateKey, keyMaterial);
    return serializedKeyPair;
  }

  // TODO: This is the method used for serializing the keypair.  It can be changed in production!!
  public void serializeKeyPair() throws Exception {
    final KeyPair keyPair = this.generateKey();
    FileOutputStream fileOutputStream = new FileOutputStream(new File("keypair"));
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream o = new ObjectOutputStream(b);
    o.writeObject(keyPair);
    b.writeTo(fileOutputStream);
    fileOutputStream.close();
  }

  private KeyPair generateKey()
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    KeyPairGenerator kpg;
    kpg = KeyPairGenerator.getInstance(algorithm, provider);

    X9ECParameters ecP = CustomNamedCurves.getByName(curve);
    ECParameterSpec ecSpec = EC5Util.convertToSpec(ecP);
    kpg.initialize(ecSpec);

    final KeyPair kp = kpg.genKeyPair();
    log.info("Key pair generated " + kp.getPublic().getAlgorithm());
    return kp;
  }

  public Key getPEMDecodedStream(final String pemEncodedKey)
      throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

    boolean privateKey = false;
    String encodedKey = "";

    if (pemEncodedKey.startsWith("-----BEGIN PRIVATE KEY-----")) {
      privateKey = true;
      encodedKey =
          pemEncodedKey
              .replaceAll("-----BEGIN PRIVATE KEY-----", "")
              .replaceAll("-----END PRIVATE KEY-----", "");
    } else {
      encodedKey =
          pemEncodedKey
              .replaceAll("-----BEGIN PUBLIC KEY-----", "")
              .replaceAll("-----END PUBLIC KEY-----", "");
    }

    final byte[] pkcs8EncodedKey = Base64.getDecoder().decode(encodedKey);

    KeyFactory factory = KeyFactory.getInstance(algorithm, provider);
    log.trace("Successfully initialised the key factory");

    if (privateKey) {
      log.trace("Its a private key");
      KeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedKey);
      // This does not mean the key is in correct format. If you receive invalid key spec error then
      // the encoding is not correct.
      log.trace("PKCS8 decoded");
      return factory.generatePrivate(keySpec);
    }
    log.trace("Its a public key");
    KeySpec keySpec = new X509EncodedKeySpec(pkcs8EncodedKey);
    // This does not mean the key is in correct format. If you receive invalid key spec error then
    // the encoding is not correct.
    log.trace("X509 decoded");
    return factory.generatePublic(keySpec);
  }

  public String getPEMEncodedStream(final Key key, boolean privateKey) {

    final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key.getEncoded());
    final StringBuilder sb = new StringBuilder();
    final String keyType = privateKey ? "PRIVATE" : "PUBLIC";
    sb.append("-----BEGIN " + keyType + " KEY-----");
    sb.append(new String(Base64.getEncoder().encode(pkcs8KeySpec.getEncoded())));
    sb.append("-----END " + keyType + " KEY-----");
    return sb.toString();
  }

  public String getSharedKey(SecretKeyPair spec) {
    try {
      log.info("Generate Shared Secret");
      log.trace("Get PrivateKey");
      final Key ourPrivateKey = this.getPEMDecodedStream(spec.getOurPrivateKey());
      final Key ourPublicKey = this.getPEMDecodedStream(spec.getRemotePublicKey());
      log.trace("Got the key decoded. Lets generate secret key");
      final String secretKey =
          dheService.getSharedSecret((PrivateKey) ourPrivateKey, (PublicKey) ourPublicKey);
      return secretKey;
    } catch (NoSuchProviderException
        | NoSuchAlgorithmException
        | InvalidKeyException
        | InvalidKeySpecException ex) {
      log.error("Unable to generate shared key", ex);
      throw new CurfewPassException("Unable to generate sharedkey from the keypair", ex);
    }
  }
}
