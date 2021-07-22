package curfew.cron;

import curfew.model.Order;
import curfew.model.OrderStatus;
import curfew.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@ConditionalOnProperty(value = "cronInstance", havingValue = "true", matchIfMissing = false)
public class CronJobs {
  private static final Logger logger = LoggerFactory.getLogger(CronJobs.class);
  private final OrderService orderService;

  @Value("${cronInstance}")
  private boolean isCronInstance;

  public CronJobs(OrderService orderService) {
    this.orderService = orderService;
  }

  @Scheduled(fixedDelay = 5000, initialDelay = 5000)
  public void cronJobSch() {

    if (!isCronInstance) {
      logger.debug("Not a cron instance");
      return;
    }

    logger.debug("Running the cron");

    try {
      Integer pageNumber = 0;
      Integer pageSize = Integer.MAX_VALUE;
      List<Order> orders = orderService.getAllOrders(pageNumber, pageSize);
      Collections.sort(orders);
      logger.debug("Found " + orders.size() + " orders for creating applications");
      for (Order order : orders) {
        // with status " + order.getOrderStatus().name());
        if (order.getOrderStatus() == OrderStatus.approved) {
          try {
            logger.debug("Creating order applications : " + order.getId());
            orderService.createOrderApplications(order);
            logger.debug("Created order applications : " + order.getId());
          } catch (Exception e) {
            orderService.updateOrderStatus(
                order.getId(), OrderStatus.failed, e.getLocalizedMessage());
            logger.debug(
                "Error creating application order : " + order.getId() + e.getLocalizedMessage());
          }
        }
      }
      orders = orderService.getAllOrders(pageNumber, pageSize);
      Collections.sort(orders);
      logger.info("Found " + orders.size() + " orders for processing");
      for (Order order : orders) {
        // status " + order.getOrderStatus().name());
        if (order.getOrderStatus() == OrderStatus.processing) {
          try {
            logger.info("Processing order : " + order.getId());
            orderService.processOrder(order.getId());
            logger.info("Processed Order : " + order.getId());
          } catch (Exception e) {
            orderService.updateOrderStatus(
                order.getId(), OrderStatus.failed, e.getLocalizedMessage());
            logger.error("Error processing order : " + order.getId() + e.getLocalizedMessage());
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error getting list of orders " + e.getLocalizedMessage());
    }
  }
}
