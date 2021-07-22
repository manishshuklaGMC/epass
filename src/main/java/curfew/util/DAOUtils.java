package curfew.util;

import curfew.exception.CurfewPassException;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

/** Created by manish.shukla on 24/09/19. */
public class DAOUtils {

  public static <T> T logAndThrowException(
      String message, Throwable t, Class... checkedExceptions) {
    throw new CurfewPassException(message, t);
  }

  public static Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
    return rs.getLong(columnName) == 0 ? null : rs.getLong(columnName);
  }

  public static Long getLongOrNull(ResultSet rs, int columnIndex) throws SQLException {
    return rs.getLong(columnIndex) == 0 ? null : rs.getLong(columnIndex);
  }

  public static Timestamp getTimestamp(Long millis) {
    return Optional.ofNullable(millis).map(Timestamp::new).orElse(null);
  }

  public static Long getTimeStamp(ResultSet rs, final int columnIndex) throws SQLException {
    return Optional.ofNullable(rs.getTimestamp(columnIndex)).map(Timestamp::getTime).orElse(null);
  }

  public static Long getTimeStamp(ResultSet rs, final String columnName) throws SQLException {
    return Optional.ofNullable(rs.getTimestamp(columnName)).map(Timestamp::getTime).orElse(null);
  }

  public static <T> List<T> getList(ResultSet resultSet, String columnName) throws SQLException {
    List<T> list = new ArrayList<>();

    if (resultSet.getArray(columnName) != null) {
      list = Arrays.asList((T[]) resultSet.getArray(columnName).getArray());
    }

    return list;
  }

  public static Object getJsonbObject(String json) {
    try {
      PGobject pGobject = new PGobject();
      pGobject.setType("jsonb");
      pGobject.setValue(json);
      return pGobject;
    } catch (SQLException var2) {
      throw new RuntimeException(var2);
    }
  }

  public static <T> String getPostgresArrayFormat(List<T> list) {
    return "{"
        + Optional.ofNullable(list)
            .orElse(new ArrayList<>())
            .stream()
            .map(String::valueOf)
            .collect(joining(", "))
        + "}";
  }
}
