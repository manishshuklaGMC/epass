package curfew.dao;

import com.google.common.collect.ImmutableMap;
import curfew.controller.request.VerifyOTP;
import curfew.model.OTP;
import curfew.model.OTPStatus;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static curfew.util.DAOUtils.logAndThrowException;

/** Created by manish.shukla on 2020/3/25. */
@Service
public class OTPDAO {
  private static final String TABLE_OTP = "otp";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_VALID_TILL = "valid_till";
  private static final String COL_OTP = "otp";
  private static final String COL_IDENTIFIER = "identifier";
  private static final String COL_IDENTIFIER_TYPE = "identifier_type";
  private static final String COL_PUBLIC_KEY = "public_key";
  private static final String COL_STATUS = "status";
  private static final String COL_TRY_COUNT = "try_count";
  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_CREATED_AT,
          COL_VALID_TILL,
          COL_OTP,
          COL_IDENTIFIER,
          COL_PUBLIC_KEY,
          COL_IDENTIFIER_TYPE,
          COL_STATUS,
          COL_TRY_COUNT);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public OTPDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<OTP> getLatestOTP(String identifier) {
    return jdbcTemplateWrapper
        .querySingleRow(
            String.format(
                " SELECT %1$s from %2$s WHERE %3$s = :%3$s order by %4$s desc limit 1",
                ALL_COLUMNS, TABLE_OTP, COL_IDENTIFIER, COL_VALID_TILL),
            (resultSet, i) -> getOTP(resultSet),
            ImmutableMap.of(OTPDAO.COL_IDENTIFIER, identifier))
        .exceptionally(t -> {
          System.out.println(t.getLocalizedMessage());
          return null;});
  }

  public CompletionStage<Void> pushOTP(OTP otp) {
    String insertQuery =
        String.format(
            "INSERT INTO otp(%1$s, %2$s, %3$s, %4$s) " + "VALUES (:%1$s, :%2$s, :%3$s, :%4$s)",
            COL_IDENTIFIER, COL_OTP, COL_PUBLIC_KEY, COL_IDENTIFIER_TYPE);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_IDENTIFIER, otp.getIdentifier());
    values.put(COL_OTP, otp.getOtp());
    values.put(COL_PUBLIC_KEY, otp.getPublicKey());
    values.put(COL_IDENTIFIER_TYPE, otp.getAccountIdentifierType());
    return jdbcTemplateWrapper.update(insertQuery, values).thenAccept(__ -> {});
  }

  private OTP getOTP(ResultSet resultSet) throws SQLException {
    return OTP.builder()
        .id(resultSet.getLong(COL_ID))
        .createdAt(resultSet.getTimestamp(COL_CREATED_AT).getTime())
        .validTill(resultSet.getTimestamp(COL_VALID_TILL).getTime())
        .otp(resultSet.getString(COL_OTP))
        .identifier(COL_IDENTIFIER)
        .publicKey(resultSet.getString(COL_PUBLIC_KEY))
        .otpStatus(OTPStatus.valueOf(resultSet.getString(COL_STATUS)))
        .verificationTrialCount(resultSet.getInt(COL_TRY_COUNT))
        .build();
  }

  public CompletionStage<Void> updateCount(VerifyOTP payload, OTPStatus otpStatus, Integer count) {
    Map<String, Object> args = new HashMap<>();
    args.put(COL_STATUS, otpStatus.name());
    args.put(COL_IDENTIFIER, payload.getIdentifier());
    args.put(COL_TRY_COUNT, count);
    return jdbcTemplateWrapper
        .update(
            String.format(
                "UPDATE %s SET %s = :%s, %s = :%s WHERE %s = :%s",
                TABLE_OTP,
                COL_STATUS,
                COL_STATUS,
                COL_TRY_COUNT,
                COL_TRY_COUNT,
                COL_IDENTIFIER,
                COL_IDENTIFIER),
            args)
        .thenAccept(__ -> {})
        .exceptionally(
            t -> logAndThrowException("No otp found ", t, EmptyResultDataAccessException.class));
  }
}
