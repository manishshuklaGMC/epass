package curfew.util;

import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import curfew.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.io.IOException;
import java.net.URISyntaxException;

/** Created by manish.shukla on 2020/3/26. */
public class S3UtilTest extends DefaultTest {
  @Autowired public S3Util s3Util;
  @Autowired public OrderService orderService;

  @Test
  public void testGetSignedURL() {
    System.out.println(s3Util.getSignedURL("test.txt").toCompletableFuture().join());
  }


  @Test
  public void fetch() {
    System.out.println(s3Util.getFileStream("test.txt").toCompletableFuture().join());
  }
}
