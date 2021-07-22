package curfew.dao;

import com.google.gson.Gson;
import curfew.model.Session;
import curfew.model.State;
import curfew.util.JDBCTemplateWrapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Created by manish.shukla on 2020/4/9.
 */
@Service
public class StateDAO {
  private static final String TABLE_STATE= "state";
  private static final String COL_ID = "id";
  private static final String COL_CREATED_AT = "created_at";
  private static final String COL_CONFIG = "config";
  private static final String COL_NAME = "name";

  private static final String ALL_COLUMNS =
      String.join(
          ",",
          COL_ID,
          COL_CREATED_AT,
          COL_CONFIG,
          COL_NAME);
  private final JDBCTemplateWrapper jdbcTemplateWrapper;

  public StateDAO(JDBCTemplateWrapper jdbcTemplateWrapper) {
    this.jdbcTemplateWrapper = jdbcTemplateWrapper;
  }

  public CompletionStage<Void> addState(State state) {
    String insertQuery =
        String.format(
            "INSERT INTO %1$s (%2$s, %3$s) " + "VALUES (:%2$s, :%3$s::json)",
            TABLE_STATE, COL_NAME, COL_CONFIG);
    Map<String, Object> values = new HashMap<>();
    values.put(COL_NAME, state.getStateName().name());
    values.put(COL_CONFIG, new Gson().toJson(state.getStateConfig()));
    return jdbcTemplateWrapper
        .update(insertQuery, values)
        .thenAccept(__->{});
  }
}
