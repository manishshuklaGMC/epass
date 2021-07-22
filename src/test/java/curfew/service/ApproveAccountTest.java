package curfew.service;

import curfew.controller.request.ApproveAccount;
import curfew.dao.AccountDAO;
import curfew.dao.OrganizationDAO;
import curfew.exception.NotificationException;
import curfew.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static curfew.service.TestContants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class ApproveAccountTest {
    @Rule
    public ExpectedException expectThrown = ExpectedException.none();

    @Mock
    AccountDAO accountDAO;

    @Mock
    OrganizationDAO organizationDAO;

    @Mock
    NotificationService notificationService;

    @InjectMocks
    AuthenticationService authenticationService;

    String approverAuthToken;
    String requesterMail;
    Account approverAccount;
    Account requesterAccount;
    Class<? extends Exception> expectedException;
    String expectedMessage;
    public ApproveAccountTest(String approverAuthToken, String requesterMail, Account approverAccount, Account requesterAccount,
                              Class<? extends Exception> expectedException, String expectedMessage) {
        this.approverAuthToken = approverAuthToken;
        this.requesterMail = requesterMail;
        this.approverAccount = approverAccount;
        this.requesterAccount = requesterAccount;
        this.expectedException = expectedException;
        this.expectedMessage = expectedMessage;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "ADMIN_TOKEN", TEST_ACCOUNT_POLICE_VERIFICATION_PENDING.getIdentifier(), ADMIN_ACCOUNT_VERIFIED, TEST_ACCOUNT_POLICE_VERIFICATION_PENDING, null, null },
                { "USER_TOKEN", TEST_ACCOUNT_POLICE_VERIFICATION_PENDING.getIdentifier(), TEST_ACCOUNT_VERIFIED, TEST_ACCOUNT_POLICE_VERIFICATION_PENDING, RuntimeException.class, "Not allowed to approve a account." },
                { "ADMIN_TOKEN", TEST_ACCOUNT_POLICE_VERIFICATION_PENDING.getIdentifier(), ADMIN_ACCOUNT_VERIFIED, null, RuntimeException.class, "Account doesn't exist." },
                { "ADMIN_TOKEN", TEST_ACCOUNT_VERIFIED.getIdentifier(), ADMIN_ACCOUNT_VERIFIED, TEST_ACCOUNT_UNVERIFIED, RuntimeException.class, "OTP verification pending." },
        });
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws NotificationException {
        when(accountDAO.getAccountByAuthToken(approverAuthToken))
                .thenReturn(CompletableFuture.completedFuture(approverAccount));
        when(accountDAO.getAccountByIdentifier(requesterMail, 1))
                .thenReturn(CompletableFuture.completedFuture(requesterAccount));
        when(accountDAO.updateStatus(1, AccountStatus.VERIFIED))
                .thenReturn(CompletableFuture.completedFuture(1));
        if (requesterAccount != null)
            when(organizationDAO.updateStatus(requesterAccount.getOrganizationID(), OrganizationStatus.VERIFIED))
                    .thenReturn(CompletableFuture.completedFuture(1));

        if (expectedException != null) {
            expectThrown.expect(expectedException);
            expectThrown.expectMessage(expectedMessage);
        }

        authenticationService.approveAccount(
                ApproveAccount.builder()
                .authToken(approverAuthToken)
                .requesterAccountId(1)
                .build()
        );
    }
}
