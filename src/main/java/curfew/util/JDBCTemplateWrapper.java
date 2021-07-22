package curfew.util;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class JDBCTemplateWrapper {
  public final NamedParameterJdbcTemplate jdbcTemplate;
  private final ExecutorService executorService;

  public JDBCTemplateWrapper(DataSource dataSource, int noOfThreads) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.executorService = Executors.newFixedThreadPool(noOfThreads);
  }

  public CompletionStage<Integer> updateWithGeneratedKeys(
      String sql, Map<String, ?> paramsMap, KeyHolder keyHolder) {
    SqlParameterSource paramsSource = new MapSqlParameterSource(paramsMap);
    return supplyAsync(() -> jdbcTemplate.update(sql, paramsSource, keyHolder), executorService);
  }

  public CompletionStage<Integer> updateWithGeneratedKeysWithColumns(
      String sql, Map<String, ?> paramsMap, KeyHolder keyHolder, String columns[]) {
    SqlParameterSource paramsSource = new MapSqlParameterSource(paramsMap);
    return supplyAsync(
        () -> jdbcTemplate.update(sql, paramsSource, keyHolder, columns), executorService);
  }

  public CompletionStage<Integer> update(String sql, Map<String, ?> paramsMap) {
    return supplyAsync(() -> jdbcTemplate.update(sql, paramsMap), executorService);
  }

  public <T> CompletionStage<List<T>> query(String sql, RowMapper<T> rm, Map<String, ?> paramsMap) {
    return supplyAsync(() -> jdbcTemplate.query(sql, paramsMap, rm), executorService);
  }

  public <T> CompletionStage<Optional<T>> queryOptionalRow(
      String sql, RowMapper<T> rm, Map<String, ?> paramsMap) {
    return querySingleRow(sql, rm, paramsMap)
        .thenApply(Optional::of)
        .exceptionally(
            e -> {
              throw new CompletionException(e);
            });
  }

  public <T> CompletionStage<Optional<T>> queryAnyRow(
      String sql, RowMapper<T> rm, Map<String, ?> paramsMap) {
    return query(sql, rm, paramsMap)
        .thenApply(
            rows -> {
              return rows.stream().findFirst();
            })
        .exceptionally(
            e -> {
              throw new CompletionException(e);
            });
  }

  public <T> CompletionStage<T> querySingleRow(
      String sql, RowMapper<T> rm, Map<String, ?> paramsMap) {
    return supplyAsync(() -> jdbcTemplate.queryForObject(sql, paramsMap, rm), executorService);
  }

  public CompletionStage<int[]> batchUpdate(String sql, SqlParameterSource[] batchArgs) {
    return supplyAsync(() -> jdbcTemplate.batchUpdate(sql, batchArgs), executorService);
  }
}
