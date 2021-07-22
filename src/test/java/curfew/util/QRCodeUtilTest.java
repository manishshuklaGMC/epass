package curfew.util;

import com.google.zxing.WriterException;
import curfew.model.Application;
import curfew.model.ApplicationType;
import curfew.model.Person;
import curfew.model.ProofType;
import curfew.service.SignatureService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QRCodeUtilTest extends DefaultTest {
  @Autowired SignatureService signatureService;

  @Autowired QRCodeUtil qrCodeUtil;

  @Test
  public void shouldGenerateAQRCodeWithTheDetails() throws IOException, WriterException {
    //    String signature = signatureService.signPayload(createApplicationForPerson());
    String path = qrCodeUtil.writeQRNew(createApplicationForPerson(), "fdgd");
    assertTrue(path.contains("198-QRCode.png"));
  }

  @Test
  public void testJWTToken() throws IOException, WriterException {
    String path = qrCodeUtil.getJWTTokenForPerson(createApplicationForPerson(), "gdd");
    Application decoded = qrCodeUtil.decodeJWT(path);
    assertEquals(
        decoded.getEntity().getProofId(), createApplicationForPerson().getEntity().getProofId());
  }

  private Application createApplicationForPerson() {
    return Application.builder()
        .id(198L)
        .entity(new Person("1234", ProofType.AADHAR, "First", "Last", "098765432", "01011970"))
        .applicationType(ApplicationType.person)
        .purpose("Medicines")
        .token("123456")
        .build();
  }
}
