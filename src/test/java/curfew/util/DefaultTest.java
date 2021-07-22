package curfew.util;

/** Created by manish.shukla on 2020/3/25. */
import com.google.common.collect.ImmutableMap;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(value = {"environmentName=test111"})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class DefaultTest {

  public static final Integer DEFAULT_USER_FIRST_NAME = 1;
  public static final String DEFAULT_USER_LAST_NAME = "test";

  @Autowired protected JDBCTemplateWrapper jdbcTemplateWrapper;

  protected void truncateAll() {
    jdbcTemplateWrapper
        .update("truncate users CASCADE", ImmutableMap.of())
        .toCompletableFuture()
        .join();
  }

  protected void insertDefaultUser() {
    jdbcTemplateWrapper
        .update(
            "INSERT INTO users(first_name, last_name) VALUES "
                + "("
                + DEFAULT_USER_FIRST_NAME
                + ", '"
                + DEFAULT_USER_LAST_NAME
                + ") ON CONFLICT DO NOTHING",
            ImmutableMap.of())
        .toCompletableFuture()
        .join();
  }
}
