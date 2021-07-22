package curfew.dao;

import curfew.util.DefaultTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Created by manish.shukla on 2020/3/25. */
public class UserDAOTest extends DefaultTest {
  @Autowired public UserDAO userDAO;

  @Test
  public void getUserID() throws Exception {}

  @Before
  public void setUp() {
    // truncateAll();
  }

  @Test
  public void addUser() throws Exception {
    //    User rule =
    //        User.builder()
    //            .firstName("test")
    //            .build();
    //    userDAO.getUserByID(1L).toCompletableFuture().join();
  }
}
