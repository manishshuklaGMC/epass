package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.controller.request.OrderType;
import curfew.model.Order;
import curfew.model.OrderStatus;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

/** Created by manish.shukla on 2020/3/25. */
@Service
public class OrderDAO {
  public static final Integer DEFAULT_PAGE_SIZE = Integer.MAX_VALUE;
  private static final String TABLE_ORDER = "orders";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_ORDER_STATUS = "status";
  private static final String COL_ORDER_TYPE = "type";
  private static final String COL_REQUEST_COUNT = "request_count";
  private static final String COL_UPDATED_AT = "updated_at";
  private static final String COL_ACCOUNT_ID = "account_id";
  private static final String COL_PDF_URL = "pdf_url";
  private static final String COL_ORDER_UUID = "uuid";
  private static final String COL_ZIP_FILE_URL = "zip_file_url";
  private static final String COL_REASON = "reason";
  private static final String COL_PURPOSE = "purpose";
  private static final String COL_VALID_TILL = "validTill";
  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_CREATED_AT,
          COL_ORDER_STATUS,
          COL_ORDER_TYPE,
          COL_REQUEST_COUNT,
          COL_UPDATED_AT,
          COL_ACCOUNT_ID,
          COL_PDF_URL,
          COL_ORDER_UUID,
          COL_REASON,
          COL_PURPOSE,
          COL_ZIP_FILE_URL,
          COL_VALID_TILL);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public OrderDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Integer> updateStatus(
      Integer orderId, OrderStatus orderStatus, String reason) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s, %s = :%s WHERE %s = :%s",
                TABLE_ORDER,
                COL_ORDER_STATUS,
                COL_ORDER_STATUS,
                COL_REASON,
                COL_REASON,
                COL_ID,
                COL_ID),
            ImmutableMap.of(
                COL_ORDER_STATUS,
                orderStatus.name(),
                COL_ID,
                orderId,
                COL_REASON,
                (reason == null) ? "" : reason))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No Order for order id" + orderId, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> updateStatusByUUID(
      String uuid, OrderStatus orderStatus, String reason) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s, %s = :%s WHERE %s = :%s",
                TABLE_ORDER,
                COL_ORDER_STATUS,
                COL_ORDER_STATUS,
                COL_REASON,
                COL_REASON,
                COL_ORDER_UUID,
                COL_ORDER_UUID),
            ImmutableMap.of(
                COL_ORDER_STATUS,
                orderStatus.name(),
                COL_ORDER_UUID,
                uuid,
                COL_REASON,
                (reason == null) ? "" : reason))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No Order for order id" + uuid, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> updateRequestCount(String uuid, int count) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ORDER, COL_REQUEST_COUNT, COL_REQUEST_COUNT, COL_ORDER_UUID, COL_ORDER_UUID),
            ImmutableMap.of(COL_ORDER_UUID, uuid, COL_REQUEST_COUNT, count))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No Order for order uuid" + uuid, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> updatePDFURL(Integer orderId, String url) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                " UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ORDER, COL_PDF_URL, COL_PDF_URL, COL_ID, COL_ID),
            ImmutableMap.of(COL_PDF_URL, url, COL_ID, orderId))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No Order for order id" + orderId, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<List<Order>> getOrdersAll(Integer pageNumber, Integer pageSize) {
    return jdbcTemplateWrapper
        .query(
            String.format(
                " SELECT %s from %s order by created_at desc  offset %d limit %d",
                ALL_COLUMNS, TABLE_ORDER, getRecordOffset(pageNumber), getRecordLimit(pageSize)),
            (resultSet, i) -> getOrder(resultSet),
            ImmutableMap.of())
        .exceptionally(
            t -> {
              StringWriter sw = new StringWriter();
              t.printStackTrace(new PrintWriter(sw));
              return logAndThrowException(
                  String.format("No orders for account %s", sw.toString()),
                  t,
                  EmptyResultDataAccessException.class);
            });
  }

  public CompletionStage<List<Order>> getOrdersByState(Integer pageNumber, Integer pageSize, Integer stateID) {
    return jdbcTemplateWrapper
            .query(
                    String.format(
                            " SELECT o.*, oz.name as organization_name, a.identifier as identifier from orders o join account a on a.id = o.account_id " +
                                    "join organization oz on oz.orgID = a.organizationid and oz.state_id = a.state_id"+
                                    " where a.state_id = %d" +
                                    " order by o.created_at desc  offset %d limit %d",
                            stateID , getRecordOffset(pageNumber), getRecordLimit(pageSize)),
                    (resultSet, i) -> {
                      Order order = getOrder(resultSet);
                      return Order.builder()
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
                              .orgName(resultSet.getString("organization_name"))
                              .requester(resultSet.getString("identifier"))
                              .purpose(order.getPurpose())
                              .reason(order.getReason())
                              .validTill(order.getValidTill())
                              .build();
                      },
                    ImmutableMap.of())
            .exceptionally(
                    t -> {
                      StringWriter sw = new StringWriter();
                      t.printStackTrace(new PrintWriter(sw));
                      return logAndThrowException(
                              String.format("No orders for account %s", sw.toString()),
                              t,
                              EmptyResultDataAccessException.class);
                    });
  }

  public CompletionStage<List<Order>> getOrdersByAccountID(
      Integer accountID, Integer pageNumber, Integer pageSize) {
    return jdbcTemplateWrapper
        .query(
            String.format(
                " SELECT %s from %s WHERE %s = :%s order by created_at desc offset %d limit %d",
                ALL_COLUMNS,
                TABLE_ORDER,
                COL_ACCOUNT_ID,
                COL_ACCOUNT_ID,
                getRecordOffset(pageNumber),
                getRecordLimit(pageSize)),
            (resultSet, i) -> getOrder(resultSet),
            ImmutableMap.of(COL_ACCOUNT_ID, accountID))
        .exceptionally(
            t -> {
              StringWriter sw = new StringWriter();
              t.printStackTrace(new PrintWriter(sw));
              return logAndThrowException(
                  "No rule with account " + accountID + sw.toString(),
                  t,
                  EmptyResultDataAccessException.class);
            });
  }

  private int getRecordOffset(Integer pageNumber) {
    return pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
  }

  private Integer getRecordLimit(Integer pageSize) {
    return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
  }

  public CompletionStage<Order> getOrder(Integer orderID) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s", ALL_COLUMNS, TABLE_ORDER, COL_ID, COL_ID),
            (resultSet, i) -> getOrder(resultSet),
            ImmutableMap.of(COL_ID, orderID))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + orderID, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Order> getOrderByUUID(String uuid) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s",
                ALL_COLUMNS, TABLE_ORDER, COL_ORDER_UUID, COL_ORDER_UUID),
            (resultSet, i) -> getOrder(resultSet),
            ImmutableMap.of(COL_ORDER_UUID, uuid))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + uuid, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<String> pushOrder(Order order) {
    String uuid = UUID.randomUUID().toString();
    String insertQuery =
        String.format(
            "INSERT INTO orders (%1$s, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s, :%5$s, :%6$s, :%7$s, :%8$s)",
            COL_ACCOUNT_ID,
            COL_ORDER_STATUS,
            COL_ORDER_TYPE,
            COL_REQUEST_COUNT,
            COL_UPDATED_AT,
            COL_ORDER_UUID,
            COL_PURPOSE,
            COL_VALID_TILL);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_ACCOUNT_ID, order.getAccountId());
    values.put(COL_ORDER_STATUS, order.getOrderStatus().name());
    values.put(COL_ORDER_TYPE, order.getOrderType().name());
    values.put(COL_REQUEST_COUNT, order.getRequestCount());
    values.put(COL_UPDATED_AT, new Timestamp(System.currentTimeMillis()));
    values.put(COL_ORDER_UUID, uuid);
    values.put(COL_PURPOSE, order.getPurpose());
    values.put(COL_VALID_TILL, new Timestamp(order.getValidTill()));
    return jdbcTemplateWrapper.update(insertQuery, values).thenApply(__ -> uuid);
  }

  public CompletionStage<Void> addZipFileURL(Integer orderID, String s3URL, OrderStatus status) {
    String insertQuery =
        String.format(
            "update orders set %1$s = :%1$s, %2$s = :%2$s where %3$s = :%3$s",
            COL_ZIP_FILE_URL, COL_ORDER_STATUS, COL_ID);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_ID, orderID);
    values.put(COL_ZIP_FILE_URL, s3URL);
    values.put(COL_ORDER_STATUS, status.name());
    return jdbcTemplateWrapper.update(insertQuery, values).thenAccept(__ -> {});
  }

  private Order getOrder(ResultSet resultSet) throws SQLException {
    return Order.builder()
        .id(resultSet.getInt(COL_ID))
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .orderStatus(OrderStatus.valueOf(resultSet.getString(COL_ORDER_STATUS)))
        .orderType(OrderType.valueOf(resultSet.getString(COL_ORDER_TYPE)))
        .requestCount(resultSet.getInt(COL_REQUEST_COUNT))
        .updatedAt(resultSet.getTimestamp(COL_UPDATED_AT).getTime())
        .accountId(resultSet.getInt(COL_ACCOUNT_ID))
        .pdfUrl(resultSet.getString(COL_PDF_URL))
        .uuid(resultSet.getString(COL_ORDER_UUID))
        .zipFileURL(resultSet.getString(COL_ZIP_FILE_URL))
        .reason(resultSet.getString(COL_REASON))
        .purpose(resultSet.getString(COL_PURPOSE))
        .validTill(resultSet.getTimestamp(COL_VALID_TILL).getTime())
        .build();
  }
}
