package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.model.*;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

/** Created by manish.shukla on 2020/3/25. */
@Service
public class AccountDAO {
  private static final String TABLE_ACCOUNT = "account";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_NAME = "name";
  private static final String COL_IDENTIFIER = "identifier";
  private static final String COL_IDENTIFIER_TYPE = "account_identifier_type";
  private static final String COL_PASSWORD_HASH = "passwordhashed";
  private static final String COL_STATUS = "status";
  private static final String COL_STATE_ID = "state_id";

  private static final String COL_ORG_ID = "organizationid";
  private static final String COL_ORGANIZATION_ID = "organization_id";
  private static final String COL_ACCOUNT_TYPE = "account_type";
  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_CREATED_AT,
          COL_NAME,
          COL_IDENTIFIER,
          COL_IDENTIFIER_TYPE,
          COL_PASSWORD_HASH,
          COL_ORG_ID,
          COL_ACCOUNT_TYPE,
          COL_STATUS,
          COL_STATE_ID
      );

  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public AccountDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Account> getAccountByID(Integer id) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s", ALL_COLUMNS, TABLE_ACCOUNT, COL_ID, COL_ID),
            (resultSet, i) -> getAccount(resultSet),
            ImmutableMap.of(COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for id" + id + t.getLocalizedMessage(), t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Account> getAccountByAuthToken(String authToken) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " select a.* from session s join account a on a.id = s.user_id  where auth_token = :%s",
                "authToken"),
            (resultSet, i) -> getAccount(resultSet),
            ImmutableMap.of("authToken", authToken))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for session " + authToken + t.getLocalizedMessage(),
                    t,
                    EmptyResultDataAccessException.class));
  }

  public CompletionStage<Account> getAccountByEmailAndPassword(String email, Integer stateId) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s and %s = :%s",
                ALL_COLUMNS, TABLE_ACCOUNT, COL_IDENTIFIER, COL_IDENTIFIER, COL_STATE_ID, COL_STATE_ID),
            (resultSet, i) -> getAccount(resultSet),
            ImmutableMap.of(COL_IDENTIFIER, email, COL_STATE_ID, stateId))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for email " + email, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Account> getAccountByIdentifier(String identifier, Integer stateId) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s and %s = :%s",
                ALL_COLUMNS, TABLE_ACCOUNT, COL_IDENTIFIER, COL_IDENTIFIER, COL_STATE_ID, COL_STATE_ID),
            (resultSet, i) -> getAccount(resultSet),
            ImmutableMap.of(COL_IDENTIFIER, identifier, COL_STATE_ID, stateId))
        .exceptionally(t -> null);
  }

  public CompletionStage<Integer> updateStatus(Integer id, AccountStatus status) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ACCOUNT, COL_STATUS, COL_STATUS, COL_ID, COL_ID),
            ImmutableMap.of(COL_STATUS, status.name(), COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for " + id, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> updatePassword(Integer id, String passwordHash) {
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s WHERE %s = :%s",
                TABLE_ACCOUNT,
                COL_PASSWORD_HASH,
                COL_PASSWORD_HASH,
                COL_ID,
                COL_ID),
            ImmutableMap.of(COL_PASSWORD_HASH, passwordHash, COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for " + id +t.getLocalizedMessage(), t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<List<Account>> getAllAccountsByStatus(AccountStatus accountStatus, Integer stateId) {
    return jdbcTemplateWrapper
        .query(
            String.format(
                " SELECT %s from %s WHERE %s = :%s and %s = :%s",
                ALL_COLUMNS, TABLE_ACCOUNT, COL_STATUS, COL_STATUS, COL_STATE_ID, COL_STATE_ID),
            (resultSet, i) -> getAccount(resultSet),
            ImmutableMap.of(COL_STATUS, accountStatus.name(), COL_STATE_ID, stateId))
        .exceptionally(t -> new ArrayList<>());
  }

  public CompletionStage<Integer> updateAccount(Account account) {
    Map<String, Object> args = new HashMap<>();
    args.put(COL_STATUS, account.getStatus().name());
    args.put(COL_ORG_ID, account.getOrganizationID());
    args.put(COL_ACCOUNT_TYPE, account.getAccountType().name());
    args.put(COL_NAME, account.getName());
    args.put(COL_IDENTIFIER, account.getIdentifier());
    args.put(COL_PASSWORD_HASH, account.getPasswordHashed());
    args.put(COL_STATE_ID, account.getStateId());
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s, %s = :%s, %s = :%s, %s = :%s, %s = :%s WHERE %s = :%s and %s = :%s ",
                TABLE_ACCOUNT,
                COL_STATUS,
                COL_STATUS,
                COL_ORG_ID,
                COL_ORG_ID,
                COL_ACCOUNT_TYPE,
                COL_ACCOUNT_TYPE,
                COL_NAME,
                COL_NAME,
                COL_PASSWORD_HASH,
                COL_PASSWORD_HASH,
                COL_IDENTIFIER,
                COL_IDENTIFIER,
                COL_STATE_ID,
                COL_STATE_ID
            ),
            args)
        .exceptionally(
            t ->
                logAndThrowException(
                    "No account for " + account.getIdentifier(),
                    t,
                    EmptyResultDataAccessException.class));
  }

  public CompletionStage<Integer> putAccount(Account account) {
    // TODO : fix type
    String insertQuery =
        String.format(
            "INSERT INTO account(%1$s, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s, :%5$s, :%6$s, :%7$s, :%8$s) ON CONFLICT(%2$s, %8$s) DO NOTHING;",
            COL_NAME,
            COL_IDENTIFIER,
            COL_IDENTIFIER_TYPE,
            COL_PASSWORD_HASH,
            COL_ORG_ID,
            COL_ACCOUNT_TYPE,
            COL_STATUS,
            COL_STATE_ID);

    Map<String, Object> values = new HashMap<>();
    values.put(COL_NAME, account.getName());
    values.put(COL_IDENTIFIER, account.getIdentifier());
    values.put(COL_IDENTIFIER_TYPE, account.getAccountIdentifierType().name());
    values.put(COL_PASSWORD_HASH, account.getPasswordHashed());
    values.put(COL_ORG_ID, account.getOrganizationID());
    values.put(COL_ACCOUNT_TYPE, account.getAccountType().name());
    values.put(COL_STATUS, account.getStatus().name());
    values.put(COL_STATE_ID, account.getStateId());
    return jdbcTemplateWrapper.update(insertQuery, values);
  }

  private Account getAccount(ResultSet resultSet) throws SQLException {
    return Account.builder()
        .id(resultSet.getInt(COL_ID))
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .name(resultSet.getString(COL_NAME))
        .identifier(resultSet.getString(COL_IDENTIFIER))
        .accountIdentifierType(
            AccountIdentifierType.valueOf(resultSet.getString(COL_IDENTIFIER_TYPE)))
        .passwordHashed(resultSet.getString(COL_PASSWORD_HASH))
        .accountType(AccountType.valueOf(resultSet.getString(COL_ACCOUNT_TYPE)))
        .organizationID(resultSet.getString(COL_ORG_ID))
        .status(AccountStatus.valueOf(resultSet.getString(COL_STATUS)))
        .stateId(resultSet.getInt(COL_STATE_ID))
        .build();
  }
}
