package curfew.dao;

import curfew.util.DefaultTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Created by manish.shukla on 2020/3/26. */
public class OrganizationDAOTest extends DefaultTest {
  @Autowired private OrganizationDAO organizationDAO;

  @Test
  public void getOrganizationByKey() {
        System.out.println(
        organizationDAO.getOrganizationByID("test",1).toCompletableFuture().join());
  }
}
