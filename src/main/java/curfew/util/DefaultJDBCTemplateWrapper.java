package curfew.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class DefaultJDBCTemplateWrapper extends JDBCTemplateWrapper {

  @Autowired
  public DefaultJDBCTemplateWrapper(
      @Value("${db.noOfThreads}") int noOfThreads,
      @Value("${spring.datasource.driver-class-name}") String driverClass,
      @Value("${spring.datasource.username}") String userName,
      @Value("${spring.datasource.password}") String password,
      @Value("${spring.datasource.url}") String url) {
    super(getDefaultDataSource(driverClass, userName, password, url), noOfThreads);
  }

  public static DataSource getDefaultDataSource(
      String driverClass, String userName, String password, String url) {
    BasicDataSource dataSource = new BasicDataSource();

    dataSource.setDriverClassName(driverClass);
    dataSource.setUsername(userName);
    dataSource.setPassword(password);
    dataSource.setUrl(url);
    dataSource.setMaxIdle(5);
    dataSource.setInitialSize(5);
    dataSource.setValidationQuery("SELECT 1");
    return dataSource;
  }
}
