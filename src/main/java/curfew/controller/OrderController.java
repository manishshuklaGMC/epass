package curfew.controller;

import com.google.zxing.WriterException;
import com.itextpdf.text.DocumentException;
import curfew.controller.request.*;
import curfew.controller.response.GetOrdersResponse;
import curfew.controller.response.ProcessOrderResponse;
import curfew.exception.CurfewPassException;
import curfew.model.Account;
import curfew.model.AccountType;
import curfew.model.Order;
import curfew.model.OrderStatus;
import curfew.service.AuthenticationService;
import curfew.service.OrderService;
import curfew.util.Utils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class OrderController {
  private static final Long DAY_IN_MILLIS = 60L * 1000 * 60 * 24 - 1;
  private OrderService orderService;
  private AuthenticationService authenticationService;
  // subtracting 1 milisecond to make sure it's still falls inside the same day.

  public OrderController(OrderService orderService, AuthenticationService authenticationService) {
    this.orderService = orderService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/processOrder")
  public ProcessOrderResponse processOrder(@RequestBody ProcessOrder processOrder) {
    if (authenticationService
        .verifySession(processOrder.getAuthToken())
        .getAccountType()
        .equals(AccountType.admin)) {
      try {
        return new ProcessOrderResponse(orderService.processOrder(processOrder.getOrderID()));
      } catch (IOException | URISyntaxException | WriterException | DocumentException e) {
        throw new RuntimeException(e);
      }
    }
    throw new CurfewPassException("invalid token");
  }

  @PostMapping("/downloadQRCodes")
  public ProcessOrderResponse downloadQrCodes(@RequestBody ProcessOrder processOrder) {
    Account account = authenticationService.verifySession(processOrder.getAuthToken());
    if (account.getId() != null) {
      try {
        return new ProcessOrderResponse(
            orderService
                .getOrderFileQRCodes(
                    processOrder.getOrderID(), account.getId(), account.getAccountType())
                .toCompletableFuture()
                .join());
      } catch (Exception e) {
        throw new CurfewPassException(e.getLocalizedMessage());
      }
    }
    throw new CurfewPassException("invalid token");
  }

  @PostMapping("/downloadOrderFile")
  public ProcessOrderResponse downLoadOrderFile(@RequestBody ProcessOrder processOrder) {
    Account account = authenticationService.verifySession(processOrder.getAuthToken());
    if (account.getId() != null) {
      try {
        return new ProcessOrderResponse(
            orderService
                .getOrderFile(processOrder.getOrderID(), account.getId(), account.getAccountType())
                .toCompletableFuture()
                .join());
      } catch (Exception e) {
        throw new CurfewPassException(e.getLocalizedMessage());
      }
    }
    throw new CurfewPassException("invalid token");
  }

  @PostMapping("/createOrder")
  public void createOrder(
      @RequestParam("file") MultipartFile file,
      @RequestParam("orderType") String orderType,
      @RequestParam("purpose") String purpose,
      @RequestParam("authToken") String authToken,
      @RequestParam(value = "validTillDate", required = false) String validTillDate) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    long validTill = 0;
    try {
      validTill =
          dateFormat
                  .parse(dateFormat.format(Utils.addDays(new Date(System.currentTimeMillis()), 7)))
                  .getTime()
              + DAY_IN_MILLIS;
      // to make sure he gets till the end of the day printed on his pass.
    } catch (ParseException e) {
      // will never happen
      throw new CurfewPassException("Date parsing failed");
    }
    if (validTillDate != null) {
      try {
        validTill = dateFormat.parse(validTillDate).getTime() + DAY_IN_MILLIS;
      } catch (ParseException e) {
        throw new CurfewPassException(
            "Please use yyyy-MM-dd as date format, given " + validTillDate);
      }
    }
    List<String> lines = Utils.getFileLines(file);

    if (lines.size() > 10000) {
      throw new CurfewPassException("Number of applications in one order cannot exceed 10,000");
    }
    // TODO: Make validTill read from the file only
    orderService.putOrderAndValidate(
        PutOrder.builder()
            .file(file)
            .orderType(OrderType.valueOf(orderType))
            .authToken(authToken)
            .purpose(purpose)
            .validTill(validTill)
            .build(),
        lines);
  }

  @PostMapping("/createOrderApplications")
  public String createOrderApplications(@RequestBody CreateOrderApplications request) {
    if (!authenticationService
        .verifySession(request.getAuthToken())
        .getAccountType()
        .equals(AccountType.admin)) {
      throw new CurfewPassException("invalid token");
    }

    Order order = orderService.getOrder(request.getOrderID());
    if (order.getOrderStatus() != OrderStatus.approved) {
      throw new CurfewPassException("Order must be approved but " + order.getOrderStatus().name());
    }

    try {
      orderService.createOrderApplications(order);
    } catch (IOException e) {
      orderService.updateOrderStatus(order.getId(), OrderStatus.failed, e.getLocalizedMessage());
      throw new CurfewPassException("Error creating order " + e.getLocalizedMessage());
    }

    return "DONE";
  }

  @PostMapping("/approveOrder")
  public String approveOrder(@RequestBody ApproveOrder request) {

    if (authenticationService
        .verifySession(request.getAuthToken())
        .getAccountType()
        .equals(AccountType.admin)) {
      OrderAction orderAction = request.getOrderAction();

      Order order = orderService.getOrder(request.getOrderID());
      if (order.getOrderStatus() != OrderStatus.created) {
        throw new CurfewPassException("Order is already " + order.getOrderStatus().name());
      }

      if (orderAction == OrderAction.ACCEPT) {
        orderService.updateOrderStatus(
            request.getOrderID(), OrderStatus.approved, request.getReason());
        try {
          return orderService.processOrder(request.getOrderID());
        } catch (IOException | WriterException | URISyntaxException | DocumentException e) {
          throw new CurfewPassException("Failed to process order " + e.getLocalizedMessage());
        }
      }

      if (orderAction == OrderAction.DECLINE) {
        orderService.updateOrderStatus(
            request.getOrderID(), OrderStatus.declined, request.getReason());
        return "DECLINED";
      }
    }
    throw new CurfewPassException("invalid token");
  }

  @PostMapping(value = "/getOrders")
  public GetOrdersResponse getOrders(@RequestBody GetOrders request) {
    if (authenticationService
        .verifySession(request.getAuthToken())
        .getId()
        .equals(request.getAccountID())) {
      return new GetOrdersResponse(
          orderService.getOrders(
              request.getAccountID(), request.getPageNumber(), request.getPageSize()));
    } else {
      throw new CurfewPassException("invalid token");
    }
  }

  @PostMapping("/getAllOrders")
  public GetOrdersResponse getAllOrders(@RequestBody GetOrders request) {
    Account account = authenticationService
            .verifySession(request.getAuthToken());
    if (account
        .getAccountType()
        .equals(AccountType.admin)) {
      return new GetOrdersResponse(
          orderService.getAllOrdersByState(request.getPageNumber(), request.getPageSize(), account.getStateId()));
    }
    throw new CurfewPassException("invalid token");
  }
}
