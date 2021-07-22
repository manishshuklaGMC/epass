package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.model.Session;
import curfew.model.SessionStatus;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.model.SessionStatus.expired;
import static curfew.util.DAOUtils.logAndThrowException;

@Service
public class SessionDAO {
  private static final String TABLE_SESSION = "session";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_VALID_TILL = "valid_till";
  private static final String COL_AUTH_TOKEN = "auth_token";
  private static final String COL_SESSION_STATUS = "session_status";
  private static final String COL_USER_ID = "user_id";

  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_CREATED_AT,
          COL_VALID_TILL,
          COL_AUTH_TOKEN,
          COL_SESSION_STATUS,
          COL_USER_ID);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public SessionDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Session> getSession(Long id) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s", ALL_COLUMNS, TABLE_SESSION, COL_ID, COL_ID),
            (resultSet, i) -> getSession(resultSet),
            ImmutableMap.of(COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + id, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Session> getSessionByAuthToken(String authToken) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s and valid_till > now()",
                ALL_COLUMNS, TABLE_SESSION, COL_AUTH_TOKEN, COL_AUTH_TOKEN),
            (resultSet, i) -> getSession(resultSet),
            ImmutableMap.of(COL_AUTH_TOKEN, authToken))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No session with token " + authToken, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<String> createSession(Session session) {
    String insertQuery =
        String.format(
            "INSERT INTO session(%1$s, %2$s, %3$s) " + "VALUES (:%1$s, :%2$s, :%3$s)",
            COL_AUTH_TOKEN, COL_SESSION_STATUS, COL_USER_ID);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_AUTH_TOKEN, session.getAuthToken());
    values.put(COL_SESSION_STATUS, session.getSessionStatus().name());
    values.put(COL_USER_ID, session.getUserId());
    return jdbcTemplateWrapper
        .update(insertQuery, values)
        .thenApply(
            __ -> {
              return session.getAuthToken();
            });
  }

  public CompletionStage<Void> invalidateSessions(Integer accountID) {
    String insertQuery =
        String.format(
            "UPDATE  session set %1$s = :%1$s where %2$s = :%2$s)",
            COL_SESSION_STATUS, COL_USER_ID);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_SESSION_STATUS, expired.name());
    values.put(COL_USER_ID, accountID);
    return jdbcTemplateWrapper
        .update(insertQuery, values)
        .thenAccept(__ -> {});
  }

  private Session getSession(ResultSet resultSet) throws SQLException {
    return Session.builder()
        .id(resultSet.getLong(COL_ID))
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .validTill(resultSet.getTimestamp(COL_VALID_TILL).getTime())
        .authToken(resultSet.getString(COL_AUTH_TOKEN))
        .sessionStatus(SessionStatus.valueOf(resultSet.getString(COL_SESSION_STATUS)))
        .userId(resultSet.getLong(COL_USER_ID))
        .build();
  }
}
