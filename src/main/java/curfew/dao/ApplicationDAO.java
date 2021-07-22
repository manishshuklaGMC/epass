package curfew.dao;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import curfew.model.*;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

/** Created by manish.shukla on 2020/3/25. */
@Service
public class ApplicationDAO {
  private static final String TABLE_APPLICATION = "application";
  private static final String COL_ID = "id";
  private static final String COL_APPLICATION_STATUS = "status";
  private static final String COL_PURPOSE = "purpose";
  private static final String COL_APPLICATION_TYPE = "type";
  private static final String COL_START_TIME = "start_time";
  private static final String COL_END_TIME = "end_time";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_TOKEN = "token";
  private static final String COL_ISSUER_ID = "issuer_id";
  private static final String COL_ENTITY = "entity";
  private static final String COL_ORDER_ID = "orderid";
  private static final String COL_PARTNER_COMPANY = "partner_company";
  private static final String COL_VALID_LOCATIONS = "valid_locations";
  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_APPLICATION_STATUS,
          COL_PURPOSE,
          COL_APPLICATION_TYPE,
          COL_START_TIME,
          COL_END_TIME,
          COL_CREATED_AT,
          COL_TOKEN,
          COL_ISSUER_ID,
          COL_ENTITY,
          COL_ORDER_ID,
          COL_PARTNER_COMPANY,
          COL_VALID_LOCATIONS);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public ApplicationDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Integer> getActiveApplicationCount(String organizationID, Integer stateID) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " select count(*) from application a join orders o on o.uuid = a.orderid join "
                    + "account ac on ac.id = o.account_id where ac.organizationid = :%s and ac.state_id = :%s and end_time > now() "
                    + "and (o.status = 'processing' or o.status = 'processed' or o.status = 'approved');",
                "organizationID", "stateID"),
            (resultSet, i) -> resultSet.getInt("count"),
            ImmutableMap.of("organizationID", organizationID, "stateID", stateID))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No organization with id " + organizationID,
                    t,
                    EmptyResultDataAccessException.class));
  }

  public CompletionStage<Application> getApplicationByID(Long id) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s",
                ALL_COLUMNS, TABLE_APPLICATION, COL_ID, COL_ID),
            (resultSet, i) -> getApplication(resultSet),
            ImmutableMap.of(COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + id, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Application> getApplicationByToken(String token) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE LOWER(%s) = LOWER(:%s)",
                ALL_COLUMNS, TABLE_APPLICATION, COL_TOKEN, COL_TOKEN),
            (resultSet, i) -> getApplication(resultSet),
            ImmutableMap.of(COL_TOKEN, token))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + token, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<List<Application>> getApplicationsByOrderID(String orderID) {
    return jdbcTemplateWrapper
        .query(
            String.format(
                " SELECT %s from %s WHERE %s = :%s",
                ALL_COLUMNS, TABLE_APPLICATION, COL_ORDER_ID, COL_ORDER_ID),
            (resultSet, i) -> getApplication(resultSet),
            ImmutableMap.of(COL_ORDER_ID, orderID))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + orderID, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Void> createApplication(Application application) {

    String insertQuery =
        String.format(
            "INSERT INTO application(%1$s, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s, :%5$s, :%6$s, :%7$s, :%8$s, :%9$s, :%10$s, :%11$s)",
            COL_APPLICATION_STATUS,
            COL_PURPOSE,
            COL_APPLICATION_TYPE,
            COL_START_TIME,
            COL_END_TIME,
            COL_TOKEN,
            COL_ISSUER_ID,
            COL_ENTITY,
            COL_ORDER_ID,
            COL_PARTNER_COMPANY,
            COL_VALID_LOCATIONS);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_APPLICATION_STATUS, application.getApplicationStatus().name());
    values.put(COL_PURPOSE, application.getPurpose());
    values.put(COL_APPLICATION_TYPE, application.getApplicationType().name());
    values.put(
        COL_START_TIME,
        application.getStartTime() == null ? null : new Timestamp(application.getStartTime()));
    values.put(
        COL_END_TIME,
        application.getEndTime() == null ? null : new Timestamp(application.getEndTime()));
    values.put(COL_TOKEN, application.getToken());
    values.put(COL_ISSUER_ID, application.getIssuerId());
    values.put(COL_ENTITY, new Gson().toJson(application.getEntity()));
    values.put(COL_ORDER_ID, application.getOrderID());
    values.put(COL_PARTNER_COMPANY, application.getPartnerCompany());
    values.put(COL_VALID_LOCATIONS, application.getValidLocations());
    return jdbcTemplateWrapper.update(insertQuery, values).thenAccept(__ -> {});
  }

  private Application getApplication(ResultSet resultSet) throws SQLException {
    ApplicationType applicationType =
        ApplicationType.valueOf(resultSet.getString(COL_APPLICATION_TYPE));
    Entity entity =
        (applicationType == ApplicationType.person)
            ? new Gson().fromJson(resultSet.getString(COL_ENTITY), Person.class)
            : new Gson().fromJson(resultSet.getString(COL_ENTITY), Vehicle.class);
    return Application.builder()
        .id(resultSet.getLong(COL_ID))
        .applicationType(ApplicationType.valueOf(resultSet.getString(COL_APPLICATION_TYPE)))
        .applicationStatus(ApplicationStatus.valueOf(resultSet.getString(COL_APPLICATION_STATUS)))
        .purpose(resultSet.getString(COL_PURPOSE))
        .issuerId(resultSet.getInt(COL_ISSUER_ID))
        .startTime(
            resultSet.getTimestamp(COL_START_TIME) == null
                ? null
                : resultSet.getTimestamp(COL_START_TIME).getTime())
        .endTime(
            resultSet.getTimestamp(COL_END_TIME) == null
                ? null
                : resultSet.getTimestamp(COL_END_TIME).getTime())
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .token(resultSet.getString(COL_TOKEN))
        .entity(entity)
        .orderID(resultSet.getString(COL_ORDER_ID))
        .partnerCompany(resultSet.getString(COL_PARTNER_COMPANY))
        .validLocations(resultSet.getString(COL_VALID_LOCATIONS))
        .build();
  }
}
