package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.model.AccountIdentifierType;
import curfew.model.TokenApprover;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Service
public class TokenApproversDAO {
  private static final String TABLE_TOKEN_APPROVER = "token_approver";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_ORG_NAME = "org_name";
  private static final String COL_IDENTIFIER = "identifier";
  private static final String COL_IDENTIFIER_TYPE = "identifier_type";
  private static final String ALL_COLUMNS =
      String.join(",", COL_ID, COL_CREATED_AT, COL_ORG_NAME, COL_IDENTIFIER, COL_IDENTIFIER_TYPE);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public TokenApproversDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<TokenApprover> getApprover(String identifier) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %1$s from %2$s WHERE %3$s = :%3$s",
                ALL_COLUMNS, TABLE_TOKEN_APPROVER, COL_IDENTIFIER),
            (resultSet, i) -> getTokenApprover(resultSet),
            ImmutableMap.of(COL_IDENTIFIER, identifier))
        .exceptionally(t -> null);
  }

  public CompletionStage<Void> insert(
      String identifier, AccountIdentifierType accountIdentifierType, String orgName) {
    String insertQuery =
        String.format(
            "INSERT INTO token_approver(%1$s, %2$s, %3$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s) ON CONFLICT(%1$s) DO NOTHING",
            COL_IDENTIFIER, COL_IDENTIFIER_TYPE, COL_ORG_NAME);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_IDENTIFIER, identifier);
    values.put(COL_IDENTIFIER_TYPE, accountIdentifierType.name());
    values.put(COL_ORG_NAME, orgName);
    return jdbcTemplateWrapper.update(insertQuery, values).thenAccept(__ -> {});
  }

  private TokenApprover getTokenApprover(ResultSet resultSet) throws SQLException {
    return TokenApprover.builder()
        .id(resultSet.getLong(COL_ID))
        .identifier(COL_IDENTIFIER)
        .identifierType(AccountIdentifierType.valueOf(resultSet.getString(COL_IDENTIFIER_TYPE)))
        .orgName(COL_ORG_NAME)
        .build();
  }
}
