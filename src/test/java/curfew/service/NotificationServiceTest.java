package curfew.service;

import curfew.exception.NotificationException;
import curfew.util.DefaultTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NotificationServiceTest extends DefaultTest {

  @Autowired private NotificationService notificationService;

  // Just to not run on every mvn clean install
  @Ignore
  @Test
  public void sendEmail() throws NotificationException {
    Map<String, String> inputs = new HashMap<>();
    inputs.put("otp", "123456");
    notificationService.sendEmail(
        "email_verification.ftl", "Test Email", "mayanknatani6@gmail.com", inputs, 1);
  }

  // Just to not run on every mvn clean install
  @Ignore
  @Test
  public void sendSMS() {
    Map<String, String> inputs = new HashMap<>();
    inputs.put("firstname", "Bob");
    notificationService.sendSMS("sms.ftl", "+918297504324", inputs);
    log.info("Sending SMS successful");
  }
}
