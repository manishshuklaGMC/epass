package curfew.dao;

import curfew.util.DefaultTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Created by manish.shukla on 2020/3/26. */
public class AccountDAOTest extends DefaultTest {

  @Autowired private AccountDAO accountDAO;

  @Test
  public void getAccountByID() throws Exception {}

  @Test
  public void putAccount() throws Exception {
    //    accountDAO
    //        .putAccount(
    //            Account.builder()
    //                .passwordHashed("dfsdf")
    //                .accountIdentifierType(AccountIdentifierType.email)
    //                .identifier("xyz@1223")
    //                .name("123")
    //                .organizationID(1)
    //                .status(AccountStatus.VERIFIED)
    //                .build())
    //        .toCompletableFuture()
    //        .join();
    //    System.out.println(new
    // Gson().toJson(accountDAO.getAccountByPhoneNumber("xyz@1223").toCompletableFuture().join()));
  }

  @Test
  public void getAccountByEmailAndPassword() {
    //    System.out.println(accountDAO.getAccountByEmailAndPassword("manish@gmail.com",
    // "").toCompletableFuture().join());
  }
}
