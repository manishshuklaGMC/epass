package curfew.dao;

import curfew.model.Session;
import curfew.model.SessionStatus;
import curfew.util.DefaultTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionDAOTest extends DefaultTest {
  @Autowired private SessionDAO sessionDAO;

  @Test
  public void getSession() {}

  @Test
  public void createSession() {
    Session session =
        Session.builder()
            .sessionStatus(SessionStatus.active)
            .authToken("dfsdfdsfsdf")
            .userId(1l)
            .build();
    sessionDAO.createSession(session).toCompletableFuture().join();
  }
}
