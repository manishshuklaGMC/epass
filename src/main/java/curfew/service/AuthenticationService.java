package curfew.service;

import com.google.common.collect.ImmutableMap;
import curfew.controller.request.AccountAction;
import curfew.controller.request.ApproveAccount;
import curfew.controller.request.GetUserProfile;
import curfew.controller.response.CreateAdminAccountResponse;
import curfew.controller.response.GetAllAccountsPendingVerificationResponse;
import curfew.controller.response.PutAccountResponse;
import curfew.controller.response.SignInResponse;
import curfew.dao.AccountDAO;
import curfew.dao.OrganizationDAO;
import curfew.dao.SessionDAO;
import curfew.exception.CurfewPassException;
import curfew.exception.NotificationException;
import curfew.model.*;
import curfew.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/** Created by manish.shukla on 2020/3/26. */
@Service
public class AuthenticationService {
  private static final String EMAIL_POLICE_VERIFICATION_TEMPLATE = "email_police_verification.ftl";
  private static final String EMAIL_POLICE_VERIFICATION_SUBJECT =
          "Your Admin Verification Is Completed.";
  private final AccountDAO accountDAO;
  private final OrganizationDAO organizationDAO;
  private final SessionDAO sessionDAO;
  private final OTPService otpService;
  private final NotificationService notificationService;
  private final StatesDetail statesDetail;
  @Value("${superuser.id}")
  private int superUserID;

  public AuthenticationService(
          AccountDAO accountDAO,
          OrganizationDAO organizationDAO,
          SessionDAO sessionDAO,
          OTPService otpService,
          NotificationService notificationService, StatesDetail statesDetail) {
    this.accountDAO = accountDAO;
    this.organizationDAO = organizationDAO;
    this.sessionDAO = sessionDAO;
    this.otpService = otpService;
    this.notificationService = notificationService;
    this.statesDetail = statesDetail;
  }

  private static String encryptPassword(String password) {
    try {
      MessageDigest crypt = MessageDigest.getInstance("SHA-1");
      crypt.reset();
      crypt.update(password.getBytes("UTF-8"));
      return byteToHex(crypt.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new CurfewPassException("Error generating password hash");
    } catch (UnsupportedEncodingException e) {
      throw new CurfewPassException("Error encoding password hash");
    }
  }

  private static String encryptPasswordNew(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  private static boolean verifyPassword(String password, String hash) {
    try {
      return BCrypt.checkpw(password, hash);
    } catch (Exception e) {
      return false;
    }
  }

  private static String byteToHex(final byte[] hash) {
    Formatter formatter = new Formatter();
    for (byte b : hash) {
      formatter.format("%02x", b);
    }
    String result = formatter.toString();
    formatter.close();
    return result;
  }

  public CreateAdminAccountResponse addAdminAccount(
          String name, String email, String organizationName, String authToken, StateName stateName) {
    Account adminAccount = verifySession(authToken);
    if (adminAccount.getId() != superUserID) {
      throw new RuntimeException("Not allowed to create admin users.");
    }
    organizationDAO
            .insertOrganization(
                    Organization.builder()
                            .status(OrganizationStatus.VERIFIED)
                            .orgID(organizationName)
                            .name(organizationName)
                            .stateId(statesDetail.getStatesDetail().get(stateName).getId())
                            .build())
            .toCompletableFuture()
            .join();
    Account account = accountDAO.getAccountByIdentifier(email, statesDetail.getStatesDetail().get(stateName).getId()).toCompletableFuture().join();
    String password = Utils.getRandomString(12);
    if (account != null) {
      accountDAO
              .updateAccount(
                      Account.builder()
                              .accountType(AccountType.admin)
                              .status(AccountStatus.VERIFIED)
                              .accountIdentifierType(AccountIdentifierType.email)
                              .organizationID(organizationName)
                              .identifier(email)
                              .name(name)
                              .passwordHashed(getHash(password))
                              .createdAt(account.getCreatedAt())
                              .stateId(statesDetail.getStatesDetail().get(stateName).getId())
                              .build())
              .toCompletableFuture()
              .join();
    } else {
      accountDAO
              .putAccount(
                      Account.builder()
                              .accountType(AccountType.admin)
                              .status(AccountStatus.VERIFIED)
                              .accountIdentifierType(AccountIdentifierType.email)
                              .organizationID(organizationName)
                              .identifier(email)
                              .name(name)
                              .passwordHashed(getHash(password))
                              .stateId(statesDetail.getStatesDetail().get(stateName).getId())
                              .build())
              .toCompletableFuture()
              .join();
    }
    return CreateAdminAccountResponse.builder().password(password).build();
  }

  public PutAccountResponse addAccount(
          String name, String email, String password, String organizationID, String organizationName, StateName stateName)
          throws NotificationException {
    Account account = accountDAO.getAccountByIdentifier(email, statesDetail.getStatesDetail().get(stateName).getId()).toCompletableFuture().join();
    if (account != null) {
      if (account.getStatus() == AccountStatus.POLICE_VERIFICATION_PENDING) {
        throw new RuntimeException("Admin verification is pending.");
      } else if (account.getStatus() == AccountStatus.UNVERIFIED) {
        otpService.generateOTP(email, AccountIdentifierType.email, account.getAccountType(), "", account.getStateId());
        throw new RuntimeException("Please verify your email.");
      } else {
        throw new RuntimeException("Account already exists.");
      }
    }

    organizationDAO
            .insertOrganization(
                    Organization.builder()
                            .name(organizationName)
                            .orgID(organizationID)
                            .status(OrganizationStatus.UNVERIFIED)
                            .stateId(statesDetail.getStatesDetail().get(stateName).getId())
                            .build())
            .toCompletableFuture()
            .join();

    accountDAO
            .putAccount(
                    Account.builder()
                            .name(name)
                            .identifier(email)
                            .organizationID(organizationID)
                            .accountIdentifierType(AccountIdentifierType.email)
                            .accountType(AccountType.user)
                            .passwordHashed(getHash(password))
                            .status(AccountStatus.UNVERIFIED)
                            .stateId(statesDetail.getStatesDetail().get(stateName).getId())
                            .build())
            .toCompletableFuture()
            .join();

    otpService.generateOTP(email, AccountIdentifierType.email, AccountType.user, "", statesDetail.getStatesDetail().get(stateName).getId());
    return new PutAccountResponse("Account Created");
  }

  public void approveAccount(ApproveAccount approveAccount) throws NotificationException {
    Account account =
            accountDAO
                    .getAccountByAuthToken(approveAccount.getAuthToken())
                    .toCompletableFuture()
                    .join();
    if (account.getAccountType() != AccountType.admin) {
      throw new RuntimeException("Not allowed to approve an account.");
    }
    Account accountTobeApproved =
            accountDAO.getAccountByID(approveAccount.getRequesterAccountId()).toCompletableFuture().join();
    if (accountTobeApproved == null) {
      throw new RuntimeException("Account doesn't exist.");
    }
    if(account.getStateId() != accountTobeApproved.getStateId()) {
      throw new RuntimeException("Not allowed to approve account of this state.");
    }
    // TODO: 08/04/20 check if otp verification is done.
    if (approveAccount.getAccountAction()!= null &&
        approveAccount.getAccountAction().equals(AccountAction.DECLINE)) {
      accountDAO
          .updateStatus(accountTobeApproved.getId(), AccountStatus.DECLINED)
          .toCompletableFuture()
          .join();
      return;
    }
    accountDAO
            .updateStatus(accountTobeApproved.getId(), AccountStatus.VERIFIED)
            .toCompletableFuture()
            .join();
    organizationDAO
            .updateStatus(accountTobeApproved.getOrganizationID(), OrganizationStatus.VERIFIED)
            .toCompletableFuture()
            .join();
    notificationService.sendEmail(
            EMAIL_POLICE_VERIFICATION_TEMPLATE,
            EMAIL_POLICE_VERIFICATION_SUBJECT,
            accountTobeApproved.getIdentifier(),
            ImmutableMap.of(),
            account.getStateId());
  }

  // TODO:
  private String getHash(String key) {
    return encryptPasswordNew(key);
  }

  public void updatePassword(String email, String password, String authToken) {
    Account account = verifySession(authToken);
    if (!account.getIdentifier().equals(email)) {
      throw new CurfewPassException("You are not allowed to update password for this account");
    }
    accountDAO.updatePassword(account.getId(), getHash(password)).toCompletableFuture().join();
  }

  public CompletionStage<SignInResponse> signIn(
          String email, String password, AccountType accountType, StateName stateName) {
    String sessionID = Utils.getRandomSessionsString();
    Integer stateID = statesDetail.getStatesDetail().get(stateName).getId();
    return accountDAO
            .getAccountByEmailAndPassword(email, stateID)
            .thenCompose(
                    account -> {
                      System.out.println(account.getIdentifier());
                      if (!(verifyPassword(password, account.getPasswordHashed())
                              || encryptPassword(password).equals(account.getPasswordHashed()))) {
                        throw new CurfewPassException("Invalid password");
                      }
                      if (account.getStatus() == AccountStatus.POLICE_VERIFICATION_PENDING) {
                        throw new CurfewPassException("Please wait for admin verification.");
                      }
                      if (account.getStatus() == AccountStatus.UNVERIFIED) {
                        try {
                          otpService.generateOTP(
                                  email, AccountIdentifierType.email, account.getAccountType(), "", account.getStateId());
                        } catch (NotificationException e) {
                          throw new CurfewPassException("Verification email sending failed.");
                        }
                        throw new CurfewPassException("Please verify your email.");
                      }
                      if (account.getAccountType() != accountType) {
                        throw new CurfewPassException(
                                "Account is a " + account.getAccountType() + " asked for " + accountType);
                      }
                      return sessionDAO
                              .createSession(
                                      Session.builder()
                                              .authToken(sessionID)
                                              .sessionStatus(SessionStatus.active)
                                              .userId(account.getId().longValue())
                                              .build())
                              .thenApply(__ -> account);
                    })
            .thenApply(
                    account -> {
                      Organization organization =
                              organizationDAO
                                      .getOrganizationByID(account.getOrganizationID(), account.getStateId())
                                      .toCompletableFuture()
                                      .join();
                      return new SignInResponse(
                              sessionID,
                              account.getId(),
                              account.getName(),
                              organization.getOrgID(),
                              organization.getName());
                    });
  }

  public Account verifySession(String authToken) {
    return sessionDAO
            .getSessionByAuthToken(authToken)
            .thenCompose(session -> accountDAO.getAccountByAuthToken(authToken))
            .toCompletableFuture()
            .join();
  }

  public GetAllAccountsPendingVerificationResponse getAllAccountsPendingVerification(
          String authToken) {
    Account account = verifySession(authToken);
    if (account.getAccountType() != AccountType.admin) {
      throw new RuntimeException("Not Allowed to fetch all account pending verification.");
    }
    List<Account> accounts =
            accountDAO
                    .getAllAccountsByStatus(AccountStatus.POLICE_VERIFICATION_PENDING, account.getStateId())
                    .toCompletableFuture()
                    .join();
    List<AccountInfo> accountInfos =
            accounts
                    .stream()
                    .map(
                            acc -> {
                              Organization org =
                                      organizationDAO
                                              .getOrganizationByID(acc.getOrganizationID(), acc.getStateId())
                                              .toCompletableFuture()
                                              .join();
                              return AccountInfo.builder()
                                      .email(acc.getIdentifier())
                                      .name(acc.getName())
                                      .orgID(acc.getOrganizationID())
                                      .orgName(org.getName())
                                      .id(acc.getId())
                                      .build();
                            })
                    .collect(Collectors.toList());
    return GetAllAccountsPendingVerificationResponse.builder().accounts(accountInfos).build();
  }

  public CompletionStage<GetUserProfile> getRequesterUserProfile(String authToken) {
    Account userAccount = verifySession(authToken);
    return organizationDAO
            .getOrganizationByID(userAccount.getOrganizationID(), userAccount.getStateId())
            .thenApply(
                    organization -> {
                      return new GetUserProfile(
                              userAccount.getName(),
                              (userAccount.getAccountIdentifierType() == AccountIdentifierType.email)
                                      ? userAccount.getIdentifier()
                                      : "",
                              organization.getName(),
                              organization.getOrgID(),
                              statesDetail.getStatesDetailById().get(userAccount.getStateId()).getStateName()
                      );
                    });
  }

  public GetUserProfile getApproverUserProfile(Account userAccount) {
    GetUserProfile userProfile = new GetUserProfile();
    userProfile.setName(userAccount.getName());
    if (userAccount.getAccountIdentifierType() == AccountIdentifierType.email) {
      userProfile.setEmail(userAccount.getIdentifier());
    }
    userProfile.setStateName(statesDetail.getStatesDetailById().get(userAccount.getStateId()).getStateName());
    return userProfile;
  }

  public Void invalidateSessions(String identifier, StateName stateName) {
    return accountDAO.getAccountByEmailAndPassword(identifier,
        statesDetail.getStatesDetail().get(stateName).getId())
    .thenCompose(account -> sessionDAO.invalidateSessions(account.getId()))
        .thenAccept(__->{})
        .toCompletableFuture()
        .join();
  }
}
