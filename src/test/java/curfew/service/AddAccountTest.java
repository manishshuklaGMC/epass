package curfew.service;

import curfew.controller.response.PutAccountResponse;
import curfew.dao.AccountDAO;
import curfew.dao.OrganizationDAO;
import curfew.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static curfew.model.StateName.HARYANA;
import static curfew.service.TestContants.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AddAccountTest {
    @Rule
    public ExpectedException expectThrown = ExpectedException.none();

    @Mock
    AccountDAO accountDAO;

    @Mock
    OrganizationDAO organizationDAO;

    @Mock
    OTPService otpService;

    @InjectMocks
    AuthenticationService authenticationService;

    String email;
    Account testAccount;
    Class<? extends Exception> expectedException;
    String expectedMessage;
    public AddAccountTest(String email, Account testAccount, Class<? extends Exception> expectedException,
                          String expectedMessage) {
        this.email = email;
        this.testAccount = testAccount;
        this.expectedException = expectedException;
        this.expectedMessage = expectedMessage;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { TEST_ACCOUNT_VERIFIED.getIdentifier(), TEST_ACCOUNT_VERIFIED, RuntimeException.class, "Account already exists." },
                { TEST_ACCOUNT_UNVERIFIED.getIdentifier(), TEST_ACCOUNT_UNVERIFIED, RuntimeException.class, "Please verify your email." },
                { TEST_ACCOUNT_POLICE_VERIFICATION_PENDING.getIdentifier(), TEST_ACCOUNT_POLICE_VERIFICATION_PENDING, RuntimeException.class, "Admin verification is pending." },
                { "test@email.com", TEST_ACCOUNT_UNVERIFIED, null, "Account Created"}

        });
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws Throwable{
        if (expectedException != null) {
            expectThrown.expect(expectedException);
            expectThrown.expectMessage(expectedMessage);

            when(accountDAO.getAccountByIdentifier(email, 1))
                    .thenReturn(CompletableFuture.completedFuture(testAccount));

            PutAccountResponse response = authenticationService.addAccount(
                    testAccount.getName(),
                    testAccount.getIdentifier(),
                    testAccount.getPasswordHashed(),
                    testAccount.getOrganizationID(),
                    "testOrg",
                    HARYANA);
        } else {
            when(accountDAO.getAccountByIdentifier(email, 1))
                    .thenReturn(CompletableFuture.completedFuture(null));

            when(organizationDAO.insertOrganization(any(Organization.class)))
                    .thenReturn(CompletableFuture.completedFuture("abc"));
            when(accountDAO.putAccount(any(Account.class)))
                    .thenReturn(CompletableFuture.completedFuture(1));

            PutAccountResponse response = authenticationService.addAccount(
                    testAccount.getName(),
                    email,
                    testAccount.getPasswordHashed(),
                    testAccount.getOrganizationID(),
                    "testOrg",
                    HARYANA);
            assertEquals(expectedMessage, response.getMessage());
        }
    }
}
