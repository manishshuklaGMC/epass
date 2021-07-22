package curfew.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import curfew.exception.CurfewPassException;
import curfew.exception.NotificationException;
import curfew.model.StateConfig;
import curfew.model.StatesDetail;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

  @Autowired Configuration freemarkerConfiguration;
  @Autowired private SendGrid sendGrid;
  @Value("${twilio.auth.token}")
  private String twilioAuthToken;
  @Value("${twilio.account.sid}")
  private String twilioAccountSid;
  @Value("${twilio.from.phone}")
  private String fromPhoneNumber;

  private final StatesDetail statesDetail;

  public NotificationService(StatesDetail statesDetail) {
    this.statesDetail = statesDetail;
  }
//  @Value("${email.from.address}")
//  private String emailFromAddress;
//  @Value("${email.from.name}")
//  private String emailFromName;

  public void sendSMS(String templateFileName, String toPhoneNumber, Map<String, String> inputs) {
    Twilio.init(twilioAccountSid, twilioAuthToken);

    Message message =
        Message.creator(
                new PhoneNumber(toPhoneNumber), // to
                new PhoneNumber(fromPhoneNumber), // from
                getContentFromTemplate(inputs, templateFileName))
            .create();

    log.info("SMS sent successfully to [" + message.getTo() + "]");
  }

  public void sendEmail(
      String templateFileName, String emailSubject, String email, Map<String, String> inputs, Integer stateID)
      throws NotificationException {
    StateConfig stateConfig = statesDetail.getStatesDetailById().get(stateID).getStateConfig();
    Email from = new Email(stateConfig.getEmailFromId(), stateConfig.getEmailFromName());
    Email to = new Email(email);
    Content content = new Content("text/html", getContentFromTemplate(inputs, templateFileName));

    Mail mail = new Mail(from, emailSubject, to, content);

    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      Response response = sendGrid.api(request);
      log.trace(
          "Email sent.  The response status code ["
              + response.getStatusCode()
              + "].  Headers ["
              + response.getHeaders()
              + "]");
    } catch (IOException ex) {
      log.error("Cannot send email.", ex);
      throw new NotificationException("Cannot send email to user [" + email + "]", ex);
    }
  }

  public String getContentFromTemplate(Map<String, String> map, String templateFileName) {
    try {
      Template template = freemarkerConfiguration.getTemplate(templateFileName);
      return FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
    } catch (Exception e) {
      throw new CurfewPassException(
          "Unable to generate content from template [" + templateFileName + "]", e);
    }
  }
}
