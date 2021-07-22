package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.model.ProofType;
import curfew.model.RoleType;
import curfew.model.User;
import curfew.model.UserStatus;
import curfew.util.JDBCTemplateWrapper;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

@Service
public class UserDAO {
  private static final String TABLE_USER = "users";
  private static final String COL_ID = "id";
  private static final String COL_LAST_NAME = "last_name";
  private static final String COL_FIRST_NAME = "first_name";
  private static final String COL_PHONE_NUMBER = "phone_number";
  private static final String COL_DOB = "date_of_birth";
  private static final String COL_CITY = "city";
  private static final String COL_PROFESSION = "profession";
  private static final String COL_PROOF_ID = "proof_id";
  private static final String COL_PROOF_TYPE = "proof_type";
  private static final String COL_ROLE_TYPE = "role_type";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_UPDATED_AT = "updated_at";
  private static final String COL_STATUS = "status";

  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_LAST_NAME,
          COL_FIRST_NAME,
          COL_PHONE_NUMBER,
          COL_DOB,
          COL_CITY,
          COL_PROFESSION,
          COL_PROOF_ID,
          COL_PROOF_TYPE,
          COL_ROLE_TYPE,
          COL_CREATED_AT,
          COL_UPDATED_AT,
          COL_STATUS);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public UserDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<User> getUserByID(Long id) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %s from %s WHERE %s = :%s", ALL_COLUMNS, TABLE_USER, COL_ID, COL_ID),
            (resultSet, i) -> getUser(resultSet),
            ImmutableMap.of(COL_ID, id))
        .exceptionally(
            t ->
                logAndThrowException(
                    "No rule with id " + id, t, EmptyResultDataAccessException.class));
  }

  public CompletionStage<Void> createUser(User user) {
    String insertQuery =
        String.format(
            "INSERT INTO users(%1$s, %2$s, %3$s, %4$s, %5$s, %6$s, %7$s, %8$s, %9$s, %10$s, %11$s, %12$s) "
                + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s, :%5$s, :%6$s, :%7$s, :%8$s, :%9$s, %10$s, %11$s, %12$s)",
            COL_LAST_NAME,
            COL_FIRST_NAME,
            COL_PHONE_NUMBER,
            COL_DOB,
            COL_CITY,
            COL_PROFESSION,
            COL_PROOF_ID,
            COL_PROOF_TYPE,
            COL_ROLE_TYPE,
            COL_CREATED_AT,
            COL_UPDATED_AT,
            COL_STATUS);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_LAST_NAME, user.getLastName());
    values.put(COL_FIRST_NAME, user.getFirstName());
    values.put(COL_PHONE_NUMBER, user.getPhoneNumber());
    values.put(COL_DOB, user.getDOB());
    values.put(COL_CITY, user.getCity());
    values.put(COL_PROFESSION, user.getProfession());
    values.put(COL_PROOF_ID, user.getProofId());
    values.put(COL_PROOF_TYPE, user.getProofType().name());
    values.put(COL_ROLE_TYPE, user.getRole().name());
    values.put(COL_CREATED_AT, user.getCreatedAt());
    values.put(COL_UPDATED_AT, user.getUpdatedAt());
    values.put(COL_STATUS, user.getUserStatus().name());
    return jdbcTemplateWrapper.update(insertQuery, values).thenAccept(__ -> {});
  }

  private User getUser(ResultSet resultSet) throws SQLException {
    return User.builder()
        .id(resultSet.getLong(COL_ID))
        .lastName(resultSet.getString(COL_LAST_NAME))
        .firstName(resultSet.getString(COL_FIRST_NAME))
        .phoneNumber(resultSet.getString(COL_PHONE_NUMBER))
        .DOB(resultSet.getString(COL_DOB))
        .city(resultSet.getString(COL_CITY))
        .profession(resultSet.getString(COL_PROFESSION))
        .proofId(resultSet.getString(COL_PROOF_ID))
        .proofType(ProofType.valueOf(resultSet.getString(COL_PROOF_TYPE)))
        .role(RoleType.valueOf(resultSet.getString(COL_ROLE_TYPE)))
        .createdAt(resultSet.getLong(COL_CREATED_AT))
        .updatedAt(resultSet.getLong(COL_UPDATED_AT))
        .userStatus(UserStatus.valueOf(resultSet.getString(COL_STATUS)))
        .build();
  }

  public String getPublicKeyForUser(Long userId) {
    throw new NotImplementedException();
  }
}
