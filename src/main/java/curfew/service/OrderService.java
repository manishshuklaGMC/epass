package curfew.service;

import com.google.gson.Gson;
import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import curfew.controller.request.OrderType;
import curfew.controller.request.PutOrder;
import curfew.dao.AccountDAO;
import curfew.dao.ApplicationDAO;
import curfew.dao.OrderDAO;
import curfew.dao.OrganizationDAO;
import curfew.exception.CurfewPassException;
import curfew.kafka.SimpleProducer;
import curfew.model.*;
import curfew.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.SignatureException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static curfew.model.OrderStatus.processed;
import static curfew.util.ParameterCheckUtil.*;

@Service
public class OrderService {
  private static final String TMP_FOLDER = "/tmp";
  private final PDFUtils2 pdfUtils;
  private final SignatureService signatureService;
  private final QRCodeUtil qrCodeUtil;
  private final AuthenticationService authenticationService;
  private final ExecutorService executorService = Executors.newFixedThreadPool(50);
  private final SimpleProducer simpleProducer;
  public static final Logger logger = LoggerFactory.getLogger(OrderService.class);
  private OrderDAO orderDAO;
  private ApplicationDAO applicationDAO;
  private S3Util s3Util;
  private AccountDAO accountDAO;
  private OrganizationDAO organizationDAO;
  @Value("${passTitleImageFileURL}")
  private String passTitleImageFileURL;

  @Value("${purpose}")
  private String purpose;

  public OrderService(
      OrderDAO orderDAO,
      ApplicationDAO applicationDAO,
      S3Util s3Util,
      AccountDAO accountDAO,
      OrganizationDAO organizationDAO,
      SignatureService signatureService,
      QRCodeUtil qrCodeUtil,
      PDFUtils2 pdfUtils,
      AuthenticationService authenticationService,
      SimpleProducer simpleProducer) {
    this.simpleProducer = simpleProducer;
    File dir = new File(TMP_FOLDER);
    if (!dir.exists()) dir.mkdirs();
    this.orderDAO = orderDAO;
    this.applicationDAO = applicationDAO;
    this.s3Util = s3Util;
    this.accountDAO = accountDAO;
    this.organizationDAO = organizationDAO;
    this.signatureService = signatureService;
    this.qrCodeUtil = qrCodeUtil;
    this.authenticationService = authenticationService;
    this.pdfUtils = pdfUtils;
  }

  private static Entity getEntityForRow(String[] values, OrderType orderType) {
    Entity entity = null;
    if (orderType == OrderType.person) {
      if (values.length == 6) {
        // old CSV
        // mayank,natani,orgID,phoneNumber
        String firstName = checkLength(checkEmptyOrNull(cleanName(values[0])), 40);
        String lastName = checkLength(checkEmptyOrNull(cleanName(values[1])), 40);
        String phoneNumber = cleanMobileNumber(values[2]);
        entity =
            new Person(
                Utils.getRandomNumberString(), ProofType.ORG, firstName, lastName, phoneNumber, "");
      } else {
        throw new CurfewPassException("Malformed file");
      }
    } else if (orderType == OrderType.vehicle) {
      String vehicleNumber = checkLength(checkEmptyOrNull(values[0]), 20);
      String vehicleRegistrationNumber = checkLength(checkEmptyOrNull(values[1]), 20);
      String vehicleModel = checkLength(checkEmptyOrNull(values[2]), 20);
      entity = new Vehicle(vehicleNumber, ProofType.RC, vehicleRegistrationNumber, vehicleModel);
    } else {
      throw new CurfewPassException("Unsupported order type");
    }
    return entity;
  }

  public String putOrder(PutOrder order, Integer accountID) {
    int requestCount = 0;

    return orderDAO
        .pushOrder(
            Order.builder()
                .accountId(accountID)
                .orderType(order.getOrderType())
                .orderStatus(OrderStatus.created)
                .requestCount(requestCount)
                .purpose(purpose) // TODO: Hack to give hardcoded purpose.
                .validTill(order.getValidTill())
                .build())
        .toCompletableFuture()
        .join();
  }

  public Integer updateOrderStatus(Integer orderID, OrderStatus orderStatus, String reason) {
    return orderDAO.updateStatus(orderID, orderStatus, reason).toCompletableFuture().join();
  }

  public List<Order> getOrders(Integer accountID, Integer pageNumber, Integer pageSize) {
    return orderDAO
        .getOrdersByAccountID(accountID, pageNumber, pageSize)
        .toCompletableFuture()
        .join();
  }

  public Order getOrder(Integer orderID) {
    return orderDAO.getOrder(orderID).toCompletableFuture().join();
  }

  public List<Order> getAllOrders(Integer pageNumber, Integer pageSize) {
    List<Order> orders = orderDAO.getOrdersAll(pageNumber, pageSize).toCompletableFuture().join();
    List<Order> finalOrders = new ArrayList<>();
    for (Order order : orders) {
      Account account =
          accountDAO.getAccountByID(order.getAccountId()).toCompletableFuture().join();
      if (account == null) {
        finalOrders.add(order);
      } else {
        Organization org =
            organizationDAO
                .getOrganizationByID(account.getOrganizationID(), account.getStateId())
                .toCompletableFuture()
                .join();
        finalOrders.add(
            Order.builder()
                .orderType(order.getOrderType())
                .uuid(order.getUuid())
                .requestCount(order.getRequestCount())
                .pdfUrl(order.getPdfUrl())
                .accountId(order.getAccountId())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .id(order.getId())
                .updatedAt(order.getUpdatedAt())
                .zipFileURL(order.getZipFileURL())
                .orgName(org.getName())
                .requester(account.getIdentifier())
                .purpose(order.getPurpose())
                .reason(order.getReason())
                .validTill(order.getValidTill())
                .build());
      }
    }
    return finalOrders;
  }

  public List<Order> getAllOrdersByState(Integer pageNumber, Integer pageSize, Integer stateId) {
    return orderDAO.getOrdersByState(pageNumber, pageSize, stateId).toCompletableFuture().join();
  }

  private String getTitleImage() throws IOException {
    URL url = new URL(passTitleImageFileURL);
    String localFileName = TMP_FOLDER + "/" + "pass_title.png";
    Utils.deleteFileIfExists(localFileName);
    InputStream is = url.openStream();
    OutputStream os = new FileOutputStream(localFileName);
    byte[] b = new byte[2048];
    int length;
    while ((length = is.read(b)) != -1) {
      os.write(b, 0, length);
    }
    is.close();
    os.close();
    return localFileName;
  }

  public String processOrder(Integer orderID)
      throws IOException, WriterException, URISyntaxException, DocumentException {
    Order order = orderDAO.getOrder(orderID).toCompletableFuture().join();
    if (order.getOrderStatus() == processed) {
      return order.getZipFileURL();
    }
    if (order.getOrderStatus() != OrderStatus.processing) {
      throw new CurfewPassException("Order is not processing");
    }
    List<Application> applications =
        applicationDAO.getApplicationsByOrderID(order.getUuid()).toCompletableFuture().join();
    Account account = accountDAO
            .getAccountByID(order.getAccountId())
            .toCompletableFuture()
            .join();
    Organization organization =
        organizationDAO
            .getOrganizationByID(account.getOrganizationID(), account.getStateId())
            .toCompletableFuture()
            .join();
    logger.debug("Starting to process " + System.currentTimeMillis());
    List<CompletableFuture<String>> pdfFutures = new ArrayList<>();
    for (Application application : applications) {
      CompletableFuture<String> pdfFileFuture =
          CompletableFuture.supplyAsync(
              () -> {
                String sign = null;
                try {
                  String signPayload = new Gson().toJson(application);
                  sign = signatureService.sign(signPayload);
                } catch (SignatureException e) {
                  sign = null;
                }
                try {
                  // push to kafka, fire and forget.
                  simpleProducer.postObjectInDefaultTopic(application.getToken(), application);
                } catch (Exception e) {
                  System.out.println("Sending message to kafka failed " + e.getLocalizedMessage());
                }
                try {
                  return pdfUtils.getPDFFile(
                      application, qrCodeUtil.writeQRNew(application, sign), organization.getName(), organization.getStateId());
                } catch (IOException | DocumentException | URISyntaxException | WriterException e) {
                  throw new RuntimeException(e);
                }
              },
              executorService);
      pdfFutures.add(pdfFileFuture);
    }

    List<String> pdfFilePathList =
        CompletableFuture.allOf(pdfFutures.toArray(new CompletableFuture[0]))
            .thenApply(
                aVoid -> {
                  return pdfFutures
                      .stream()
                      .map(CompletableFuture::join)
                      .collect(Collectors.toList());
                })
            .toCompletableFuture()
            .join();

    Set<String> pdfFilePaths = new HashSet<>(pdfFilePathList);
    //    String mergedPdf = "src/main/data/" + orderID + "_qr_codes.pdf";
    //    MergePdf.mergePDFs(pdfFilePaths, mergedPdf);
    String zipFile = TMP_FOLDER + "/" + "epass-" + orderID + ".zip";
    ZipFiles.zipFiles(pdfFilePaths, zipFile);
    logger.debug("Starting S3 upload " + System.currentTimeMillis());
    URL s3URL =
        s3Util.uploadDocumentSync(
            new FileInputStream(zipFile), "application/zip", Date.valueOf(LocalDate.MAX), zipFile);
    logger.debug("Finished S3 upload " + System.currentTimeMillis());
    orderDAO
        .addZipFileURL(orderID, s3URL.toString(), OrderStatus.processed)
        .toCompletableFuture()
        .join();
    for (String pdfFilePath : pdfFilePaths) {
      File file = new File(pdfFilePath);
      Files.deleteIfExists(file.toPath());
    }
    File file = new File(zipFile);
    Files.deleteIfExists(file.toPath());
    return s3URL.toString();
  }

  public void putOrderAndValidate(PutOrder putOrder, List<String> lines) {
    Account account = authenticationService.verifySession(putOrder.getAuthToken());
    if (account.getAccountType() != AccountType.user) {
      throw new CurfewPassException(
          String.format(
              "Has to be a user to be able to upload order provided %s",
              account.getAccountType().name()));
    }
    CompletionStage<Integer> activePassFuture =
        applicationDAO.getActiveApplicationCount(account.getOrganizationID(), account.getStateId());
    organizationDAO
        .getOrganizationByID(account.getOrganizationID(), account.getStateId())
        .thenCombine(
            activePassFuture,
            (organization, count) -> {
              if (organization.getActivePassLimit() < lines.size() + count) {
                throw new CurfewPassException(
                    "Active pass limit breached, limit="
                        + organization.getActivePassLimit()
                        + "currently active = "
                        + count);
              }
              return null;
            })
        .toCompletableFuture()
        .join();
    String orderUUID = putOrder(putOrder, account.getId());
    try {
      s3Util.uploadDocumentSync(
          putOrder.getFile().getInputStream(), "text/csv", null, orderUUID + "_order_file.csv");
    } catch (IOException e) {
      throw new CurfewPassException("Error in S3 file upload : " + e.getLocalizedMessage());
    }
    try {
      for (String line : lines.subList(1, lines.size())) {
        // to validate the file
        String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        for (int i = 0; i < cols.length; i++) {
          cols[i] = StringUtils.strip(cols[i], "\" ");
        }
        getDate(cols[3]); // check if date is correctly formatted.
        checkLength(cols[cols.length - 2], 40); // check length of partner company
        checkLength(cols[cols.length - 1], 40); // check length of valid locations
        Entity entity = getEntityForRow(cols, putOrder.getOrderType());
      }
      // fixme: make the status depending on the org(if it is auto approval or not.)
      orderDAO.updateStatusByUUID(
          orderUUID, OrderStatus.approved, "pre approved for organiazation");
      Integer requestcount = (lines.size() > 0) ? lines.size() - 1 : 0;
      orderDAO.updateRequestCount(orderUUID, requestcount).toCompletableFuture().join();
    } catch (Exception ex) {
      orderDAO.updateStatusByUUID(
          orderUUID,
          OrderStatus.invalid_file,
          String.format("invalid file %s", ex.getLocalizedMessage()));
      throw new CurfewPassException(
          String.format("Error validating file %s ", ex.getLocalizedMessage()));
    }
  }

  public void createOrderApplications(Order order) throws IOException {
    if (order.getOrderStatus() != OrderStatus.approved) {
      throw new CurfewPassException("Order is not approved");
    }
    InputStream orderFileStream =
        s3Util.getFileStream(order.getUuid() + "_order_file.csv").toCompletableFuture().join();
    BufferedReader br = new BufferedReader(new InputStreamReader(orderFileStream));
    String line;
    List<String> lines = new ArrayList<>();
    while ((line = br.readLine()) != null) {
      lines.add(line);
    }
    List<Application> applications =
        getApplications(
            order.getOrderType(), order.getAccountId(), order.getUuid(), order.getPurpose(), lines);
    for (Application application : applications) {
      applicationDAO.createApplication(application).toCompletableFuture().join();
    }
    updateOrderStatus(order.getId(), OrderStatus.processing, "created applications for order");
  }

  public List<Application> getApplications(
      OrderType orderType, Integer issuerId, String orderID, String purpose, List<String> lines) {
    List<Application> applications = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      // todo: Preferrably use a real parsing library here instead of regex
      String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      for (int i = 0; i < cols.length; i++) {
        cols[i] = StringUtils.strip(cols[i], "\" ");
      }
      Application.ApplicationBuilder applicationBuilder =
          Application.builder()
              .applicationType(ApplicationType.person)
              .applicationStatus(ApplicationStatus.accepted)
              .issuerId(issuerId)
              .orderID(orderID)
              .purpose(purpose)
              .token(Utils.getRandomString(6))
              .partnerCompany(checkLength(cols[cols.length - 2], 40))
              .validLocations(checkLength(cols[cols.length - 1], 40));
      Entity entity = getEntityForRow(cols, orderType);
      long date = getDate(cols[3]);
      ApplicationType applicationType =
          orderType == OrderType.person ? ApplicationType.person : ApplicationType.vehicle;
      applications.add(
          applicationBuilder.entity(entity).applicationType(applicationType).endTime(date).build());
    }
    return applications;
  }

  public CompletionStage<String> getOrderFileQRCodes(
      Integer orderID, Integer accountID, AccountType accountType) {
    return orderDAO
        .getOrder(orderID)
        .thenCompose(
            order -> {
              if ((order.getAccountId().equals(accountID) || accountType == AccountType.admin)
                  && order.getOrderStatus().equals(processed)) {
                return s3Util.getSignedURL(TMP_FOLDER + "/" + "epass-" + orderID + ".zip");
              }
              throw new CurfewPassException(
                  String.format(
                      "Order couldn't be downloaded account ID %s, caller %s, ordeID %s",
                      order.getAccountId(), accountID, orderID));
            })
        .thenApply(URL::toString);
  }

  public CompletionStage<String> getOrderFile(
      Integer orderID, Integer accountID, AccountType accountType) {
    return orderDAO
        .getOrder(orderID)
        .thenCompose(
            order -> {
              if ((order.getAccountId().equals(accountID) || accountType == AccountType.admin)) {
                return s3Util.getSignedURL(order.getUuid() + "_order_file.csv");
              }
              throw new CurfewPassException(
                  String.format(
                      "Order couldn't be downloaded account ID %s, caller %s, ordeID %s",
                      order.getAccountId(), accountID, orderID));
            })
        .thenApply(URL::toString);
  }
}
