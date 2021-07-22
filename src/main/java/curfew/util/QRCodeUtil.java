package curfew.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import curfew.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

@Service
public class QRCodeUtil {

  private static final String issuer = "ePass Karnataka";
  private static final String audience = "Karnataka police";
  private static final String organisation = "Swiggy"; // TODO: fix hard code
  private static final QRCodeWriter qrCodeWriter = new QRCodeWriter();
  private static final String QR_CODE_PATH = "/tmp";
  private static final String QR_IMAGE_FORMAT = "PNG";
  private static final String IMAGE_POST_FIX = "-QRCode.png";
  private static final Integer IMAGE_WIDTH = 350;
  private static final Integer IMAGE_HEIGHT = 350;
  private final String secret;

  @Autowired
  public QRCodeUtil(@Value("${secret}") String secret) {
    File dir = new File(QR_CODE_PATH);
    if (!dir.exists()) dir.mkdirs();
    this.secret = secret;
  }

  public static String writeQR(Application application) throws WriterException, IOException {
    String qcodePath = QR_CODE_PATH + "/" + application.getId() + IMAGE_POST_FIX;
    Utils.deleteFileIfExists(qcodePath);
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix =
        qrCodeWriter.encode(
            application.getToken(), BarcodeFormat.QR_CODE, IMAGE_WIDTH, IMAGE_HEIGHT);
    Path path = FileSystems.getDefault().getPath(qcodePath);
    MatrixToImageWriter.writeToPath(bitMatrix, QR_IMAGE_FORMAT, path);
    return qcodePath;
  }

  private static Vehicle decodeVehicle(DecodedJWT jwt) {
    return new Vehicle(
        jwt.getClaims().get("proofId").asString(),
        ProofType.valueOf(jwt.getClaims().get("identityType").asString()),
        jwt.getClaims().get("registrationNumber").asString(),
        jwt.getClaims().get("vehichleModel").asString());
  }

  private static Person decodePerson(DecodedJWT jwt) {
    return new Person(
        jwt.getClaims().get("proofId").asString(),
        ProofType.valueOf(jwt.getClaims().get("identityType").asString()),
        jwt.getClaims().get("firstName").asString(),
        jwt.getClaims().get("lastName").asString(),
        jwt.getClaims().get("Mobile").asString(),
        jwt.getClaims().get("DOB").asString());
  }

  public String writeQRNew(Application application, String sign)
      throws WriterException, IOException {
    String qcodePath = QR_CODE_PATH + "/" + application.getId() + IMAGE_POST_FIX;
    String token = "";
    if (application.getApplicationType().equals(ApplicationType.person)) {
      token = getJWTTokenForPerson(application, sign);
    } else if (application.getApplicationType().equals(ApplicationType.vehicle)) {
      token = getJWTTokenForVehicle(application, sign);
    }

    BitMatrix bitMatrix =
        qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, IMAGE_WIDTH, IMAGE_HEIGHT);
    Path path = FileSystems.getDefault().getPath(qcodePath);
    MatrixToImageWriter.writeToPath(bitMatrix, QR_IMAGE_FORMAT, path);
    return qcodePath;
  }

  private String getJWTTokenForVehicle(Application application, String sign) {
    Vehicle vehicle = (Vehicle) application.getEntity();
    Algorithm algorithm = Algorithm.HMAC256(secret);
    return JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withIssuedAt(new Date())
        .withClaim("identityType", vehicle.getProofType().toString())
        .withClaim("proofId", vehicle.getProofId().toString())
        .withClaim("registrationNumber", vehicle.getRegistrationNumber())
        .withClaim("vehichleModel", vehicle.getVehicleModel())
        // TODO: Add Phone number
        .withClaim("Organization", organisation)
        .withClaim("token", application.getToken())
        .withClaim("signature", sign)
        .withClaim("applicationType", ApplicationType.vehicle.name())
        // .withClaim("signedPayload", new Gson().toJson(application))
        .withSubject(vehicle.getProofId())
        .sign(algorithm);
  }

  public String getJWTTokenForPerson(Application application, String sign) {
    Person person = (Person) application.getEntity();
    Algorithm algorithm = Algorithm.HMAC256(secret);
    return JWT.create()
        .withIssuer(issuer)
        .withAudience("Karnataka police") // TODO: make this generic
        .withIssuedAt(new Date())
        //            .withExpiresAt(application.getEndTime()) // TODO: Validate this
        .withClaim("identityType", person.getProofType().toString())
        .withClaim("proofId", person.getProofId().toString())
        .withClaim("Mobile", person.getPhoneNumber())
        .withClaim("Reason", application.getPurpose())
        .withClaim("firstName", person.getFirstName())
        .withClaim("lastName", person.getLastName())
        .withClaim("token", application.getToken())
        .withClaim("applicationType", ApplicationType.person.name())
        //    .withClaim("signedPayload", new Gson().toJson(application))
        .withClaim("signature", sign)
        .withClaim("DOB", person.getDOB())
        .withSubject(person.getProofId())
        .sign(algorithm);
  }

  public Application decodeJWT(String jwtToken) {
    Algorithm algorithm = Algorithm.HMAC256(secret);
    JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
    DecodedJWT jwt = verifier.verify(jwtToken);
    if (jwt.getClaims().get("applicationType").asString().equals(ApplicationType.person.name())) {
      Person person = decodePerson(jwt);
      return Application.builder()
          .token(jwt.getClaims().get("token").asString())
          .entity(person)
          .build();
    } else {
      Vehicle vehicle = decodeVehicle(jwt);
      return Application.builder()
          .token(jwt.getClaims().get("token").asString())
          .entity(vehicle)
          .build();
    }
  }

  public String decodeSignature(String jwtToken) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);
      JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
      DecodedJWT jwt = verifier.verify(jwtToken);
      return jwt.getClaims().get("signature").asString();
    } catch (Exception e) {
      return null;
    }
  }

  public String decodePayload(String jwtToken) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(secret);
      JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
      DecodedJWT jwt = verifier.verify(jwtToken);
      return jwt.getClaims().get("signedPayload").asString();
    } catch (Exception e) {
      return null;
    }
  }
}
