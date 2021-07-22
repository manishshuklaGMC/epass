package curfew.dao;

import com.google.gson.Gson;
import curfew.controller.request.OrderType;
import curfew.exception.NotificationException;
import curfew.model.*;
import curfew.service.AuthenticationService;
import curfew.service.OTPService;
import curfew.service.RedisService;
import curfew.util.DefaultTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static curfew.dao.OrderDAO.DEFAULT_PAGE_SIZE;
import static junit.framework.TestCase.assertEquals;

/** Created by manish.shukla on 2020/3/26. */
public class ApplicationDAOTest extends DefaultTest {
  @Autowired public ApplicationDAO applicationDAO;

  @Autowired public OrderDAO orderDAO;

  @Autowired public OrganizationDAO orgDAO;

  @Autowired public OTPService otpService;

  @Autowired public AuthenticationService authenticationService;

  @Autowired public AccountDAO accountsDAO;

  @Autowired public SessionDAO sessionDAO;

  @Autowired public RedisService redisService;

  @Before
  public void setUp() {
    // truncateAll();
  }

  @Test
  public void getApplicationaByID() {
    applicationDAO
        .createApplication(
            Application.builder()
                .applicationType(ApplicationType.person)
                .applicationStatus(ApplicationStatus.accepted)
                .startTime(System.currentTimeMillis())
                .endTime(System.currentTimeMillis())
                .issuerId(10)
                .token("token")
                .orderID("orderID")
                .purpose("for application")
                .entity(
                    new Person(
                        "12345", ProofType.AADHAR, "Mayank", "Natani", "9999999999", "11/07/1994"))
                .build())
        .toCompletableFuture()
        .join();

    System.out.println(
        new Gson()
            .toJson(
                applicationDAO.getApplicationsByOrderID("orderID").toCompletableFuture().join()));
    //    System.out.println(new
    // Gson().toJson(applicationDAO.getApplicationByToken("token").toCompletableFuture().join()));
  }

  @Test
  public void pushOrder() {
    String uuid =
        orderDAO
            .pushOrder(
                Order.builder()
                    .orderStatus(OrderStatus.created)
                    .orderType(OrderType.person)
                    .accountId(5)
                    .requestCount(1)
                    .purpose("purpose hi purpose!")
                    .validTill(System.currentTimeMillis())
                    .build())
            .toCompletableFuture()
            .join();
    System.out.println(uuid);

    //    orderDAO.updateStatus(120, OrderStatus.processed, "processed from
    // test.").toCompletableFuture().join();
    //
    //    orderDAO.updatePDFURL(120, "https://pdf.com").toCompletableFuture().join();

    orderDAO.updateRequestCount(uuid, 30).toCompletableFuture().join();

    System.out.println(
        new Gson()
            .toJson(orderDAO.getOrdersByAccountID(5, null, null).toCompletableFuture().join()));
  }

  @Test
  public void ensurePaginationIsWorkingForGetOrders() {
    createOrders(15);
    List<Order> orders = orderDAO.getOrdersByAccountID(500, 1, 5).toCompletableFuture().join();
    assertEquals(5, orders.size());
    orders = orderDAO.getOrdersByAccountID(500, 2, 5).toCompletableFuture().join();
    assertEquals(5, orders.size());
    orders = orderDAO.getOrdersByAccountID(500, 3, 5).toCompletableFuture().join();
    assertEquals(5, orders.size());
  }

  @Test
  public void ensureDefaultPaginationIsReturnedIfPageSizeNotProvided() {
    createOrders(55);
    List<Order> orders =
        orderDAO.getOrdersByAccountID(500, null, null).toCompletableFuture().join();
    assertEquals(DEFAULT_PAGE_SIZE.intValue(), orders.size());
  }

  @Test
  public void ensureInvalidParametersForPageSizeIsHandledCorrectlyAndReturnsDefault() {
    createOrders(55);
    List<Order> orders = orderDAO.getOrdersByAccountID(500, 1, -5).toCompletableFuture().join();
    assertEquals(DEFAULT_PAGE_SIZE.intValue(), orders.size());
  }

  @Test
  public void ensurePaginationForWorkingGetAllOrders() {
    createOrders(15);
    List<Order> orders = orderDAO.getOrdersAll(1, 10).toCompletableFuture().join();
    assertEquals(10, orders.size());
  }

  @Test
  public void ensureInvalidParametersForPageNumberIsHandledCorrectlyAndReturnsDefault() {
    createOrders(5);
    List<Order> orders = orderDAO.getOrdersByAccountID(500, -1, 5).toCompletableFuture().join();
    assertEquals(5, orders.size());
  }

  public void createOrders(Integer numberOfOrders) {
    for (int i = 0; i < numberOfOrders; i++) {
      orderDAO
          .pushOrder(
              Order.builder()
                  .orderStatus(OrderStatus.created)
                  .orderType(OrderType.person)
                  .accountId(500)
                  .requestCount(1)
                  .purpose("order +" + i)
                  .validTill(System.currentTimeMillis())
                  .build())
          .toCompletableFuture()
          .join();
    }
  }

  @Test
  public void pushOrganization() throws InterruptedException {
    Organization org =
        Organization.builder()
            .name("swiggy")
            .orgID("GSTN123456")
            .status(OrganizationStatus.VERIFIED)
            .build();
    orgDAO.insertOrganization(org).toCompletableFuture().join();
    //    Thread.sleep(1000);
    System.out.println(
        new Gson().toJson(orgDAO.getOrganizationByID("GSTN123456", 1).toCompletableFuture().join()));
  }

  @Test
  public void sendOTP() throws InterruptedException, NotificationException {
    //    authenticationService.addAccount("Mayank Natani", "mayanknatani6@gmail.com", "123456",
    // "ORG:1234", "Org name");
    //    accountsDAO.putAccount(Account.builder()
    //        .name("admin")
    //        .accountIdentifierType(AccountIdentifierType.email)
    //        .accountType(AccountType.admin)
    //        .organizationID("police")
    //        .identifier("admin@police.com")
    //        .status(AccountStatus.VERIFIED)
    //        .passwordHashed("pass")
    //        .build()).toCompletableFuture().join();
    //    String authToken = Utils.getRandomSessionsString();
    //    sessionDAO.createSession(Session.builder()
    //        .sessionStatus(SessionStatus.active)
    //        .authToken(authToken)
    //        .userId(27L)
    //        .build()
    //    ).toCompletableFuture().join();
    //
    // authenticationService.approveAccount(ApproveAccount.builder().authToken("oxxtdnxkpneajyzzupjtwddncqvcpaneadajxsizkwcylzpdcmgasozybbxdyxupbcxwjwextbboizipqxfojhqcomzwetsortfpjeccrmcdlhhwxnywswjzpnkocsiq").email("mayanknatani6@gmail.com").build());
    //    System.out.println(new
    // Gson().toJson(authenticationService.signIn("mayanknatani6@gmail.com",
    // "123456").toCompletableFuture().join()));
    //    otpService.verifyOTPAndCreateAccount()
    //    authenticationService.approveAccount();
    //    otpService.generateOTP("mayanknatani6@gmail.com", AccountIdentifierType.email, "");
    //    otpService.resendOTP("mayanknatani6@gmail.com", AccountIdentifierType.email);
    //
    // otpService.verifyOTPAndCreateAccount(VerifyOTP.builder().identifier("mayanknatani6@gmail.com").otp("650159").accountIdentifierType(AccountIdentifierType.email).build());
    //    otpService.generateOTP("+918297504324", AccountIdentifierType.phone, "public-key");
    //    System.out.println(new
    // Gson().toJson(otpService.verifyOTPAndCreateAccount(VerifyOTP.builder().accountIdentifierType(AccountIdentifierType.phone).identifier("+918297504324").otp("337633").build())));
  }

  @Test
  public void createAdmin() throws InterruptedException, NotificationException {
    //    System.out.println(authenticationService.addAdminAccount("Mayank Natani Admin 1",
    // "mayanknataniadmin@gmail.com", "police wala",
    // "riqfzefkavcoajmcedysyshpkxceydqvsqabobmglvbzwmlcqzlcthphzrnahzewmshemksbyikgqodmgldntdjawrnvmubfqllbzzsjvfvwfvhfzrejsmtvevbttups").getPassword());
    //    System.out.println(new
    // Gson().toJson(authenticationService.signIn("mayanknataniadmin@gmail.com",
    // "R6bQhDbpPoVG").toCompletableFuture().join()));
  }

  @Test
  public void getAllAccountsPendingVerification()
      throws InterruptedException, NotificationException {
    //    GetAllAccountsPendingVerificationResponse res =
    //
    // authenticationService.getAllAccountsPendingVerification("joqzfycmptkwxlakmpicfzimyccduunwjysoysxfebkvdnislkgvgdutknkfziufbstkfrwmwiepuajjnnnnzfvzlbbesckjpesjfcefobnumsudpsubuojdcujglzpt");
    //    System.out.println(new Gson().toJson(res));
  }

  @Test
  public void redis() throws InterruptedException, NotificationException {
    redisService.setKey("key1", "value1");
    System.out.println(redisService.getKey("key1"));
    redisService.setKey("key1", "value2");
    System.out.println(redisService.getKey("key1"));
  }
}
