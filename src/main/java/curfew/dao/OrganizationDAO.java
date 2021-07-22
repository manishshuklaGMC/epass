package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.model.Organization;
import curfew.model.OrganizationStatus;
import curfew.util.JDBCTemplateWrapper;
import curfew.util.Utils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

/** Created by manish.shukla on 2020/3/25. */
@Service
public class OrganizationDAO {
  private static final String TABLE_ORGANIZATION = "organization";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_NAME = "name";
  private static final String COL_ORG_ID = "orgID";
  private static final String COL_STATUS = "status";
  private static final String COL_ACTIVE_PASS_LIMIT = "active_pass_limit";
  private static final String COL_STATE_ID = "state_id";

  private static final String ALL_COLUMNS =
      String.join(
          ",", COL_ID, COL_CREATED_AT, COL_NAME, COL_ORG_ID, COL_STATUS, COL_ACTIVE_PASS_LIMIT, COL_STATE_ID);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public OrganizationDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Organization> getOrganizationByID(String orgID, Integer stateID) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format("SELECT %s from organization WHERE orgID = '%s' and %s = :%s", ALL_COLUMNS, orgID, COL_STATE_ID, COL_STATE_ID),
            (resultSet, i) -> getOrganization(resultSet),
            ImmutableMap.of(COL_STATE_ID, stateID))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No organization for key " + orgID + " " + t.getLocalizedMessage(),
                    t,
                    EmptyResultDataAccessException.class));
  }

  public CompletionStage<List<Organization>> getAllOrganizations(Integer stateId) {
    return jdbcTemplateWrapper
        .query(
            String.format(
                "select %s, org_count.count as count from "
                    + "organization o  left join (select ac.organizationid, "
                    + "count(*) as count from application a join orders o on o.uuid = a.orderid "
                    + "join account ac on ac.id = o.account_id where a.end_time > now() "
                    + "and o.status = 'processed' and ac.state_id = :%s group by ac.organizationid) as org_count "
                    + "on o.orgid = org_count.organizationid where o.state_id = :%s order by o.%s desc",
                ALL_COLUMNS, COL_STATE_ID, COL_STATE_ID, COL_CREATED_AT),
            (resultSet, i) -> {
              Organization organization = getOrganization(resultSet);
              organization.setActivePassCount(resultSet.getInt("count"));
              return organization;
            },
            ImmutableMap.of(COL_STATE_ID, stateId))
        .exceptionally(
            t ->
                logAndThrowException(
                    "unable to load organizations", t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> updateStatus(String orgID, OrganizationStatus status) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ORGANIZATION, COL_STATUS, COL_STATUS, COL_ORG_ID, COL_ORG_ID),
            ImmutableMap.of(COL_STATUS, status.name(), COL_ORG_ID, orgID))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for " + orgID, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<String> insertOrganization(Organization organization) {
    String key = Utils.getRandomString(32);
    String insertQuery =
        String.format(
            "INSERT INTO organization(%1$s, %2$s, %3$s, %4$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s) ON CONFLICT(%2$s, %4$s) DO NOTHING",
            COL_NAME, COL_ORG_ID, COL_STATUS, COL_STATE_ID);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_NAME, organization.getName());
    values.put(COL_ORG_ID, organization.getOrgID());
    values.put(COL_STATUS, organization.getStatus().name());
    values.put(COL_STATE_ID, organization.getStateId());
    return jdbcTemplateWrapper.update(insertQuery, values).thenApply(__ -> key);
  }

  private Organization getOrganization(ResultSet resultSet) throws SQLException {
    return Organization.builder()
        .id(resultSet.getInt(COL_ID))
        .name(resultSet.getString(COL_NAME))
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .status(OrganizationStatus.valueOf(resultSet.getString(COL_STATUS)))
        .orgID(resultSet.getString(COL_ORG_ID))
        .activePassLimit(resultSet.getInt(COL_ACTIVE_PASS_LIMIT))
        .stateId(resultSet.getInt(COL_STATE_ID))
        .build();
  }

  public CompletionStage<Integer> updateLimit(Integer idOrganization, Integer newLimit) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ORGANIZATION,
                COL_ACTIVE_PASS_LIMIT,
                COL_ACTIVE_PASS_LIMIT,
                COL_ID,
                COL_ID),
            ImmutableMap.of(COL_ACTIVE_PASS_LIMIT, newLimit, COL_ID, idOrganization))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No organaization for " + idOrganization,
                    t,
                    EmptyResultDataAccessException.class));
  }
}
