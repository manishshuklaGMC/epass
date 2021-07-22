package curfew.service;

import curfew.util.DefaultTest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

public class KeyStoreServiceTest extends DefaultTest {

  @Autowired private KeyStoreService keyStoreService;

  @Autowired private ECCService eccService;

  @BeforeEach
  public void beforeClass() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public void test() throws Exception {
    eccService.serializeKeyPair();
    assertTrue(true);
  }
}
