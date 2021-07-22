package curfew.model;

import curfew.controller.request.OrderType;
import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class Order implements Comparable<Order> {
  private final Integer id;
  private final Integer accountId;
  private final String orgName;
  private final String requester;
  private final OrderStatus orderStatus;
  private final OrderType orderType;
  private final Integer requestCount;
  private final Long createdAt;
  private final Long updatedAt;
  private final String pdfUrl;
  private final String uuid;
  private final String zipFileURL;
  private final String reason;
  private final String purpose;
  private final Long validTill;

  @Override
  public int compareTo(Order o) {
    return this.getRequestCount() - o.getRequestCount();
  }
}
