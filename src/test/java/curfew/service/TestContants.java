package curfew.service;

import curfew.model.Account;
import curfew.model.AccountIdentifierType;
import curfew.model.AccountStatus;
import curfew.model.AccountType;

public class TestContants {
    public static final Account TEST_ACCOUNT_VERIFIED = Account.builder()
            .name("test")
            .identifier("test@email.com")
            .organizationID("org_id")
            .accountIdentifierType(AccountIdentifierType.email)
            .accountType(AccountType.user)
            .passwordHashed("hashed_pass")
            .status(AccountStatus.VERIFIED)
            .build();
    public static final Account TEST_ACCOUNT_UNVERIFIED = Account.builder()
            .name("test")
            .identifier("test@email.com")
            .organizationID("org_id")
            .accountIdentifierType(AccountIdentifierType.email)
            .accountType(AccountType.user)
            .passwordHashed("hashed_pass")
            .status(AccountStatus.UNVERIFIED)
            .build();
    public static final Account TEST_ACCOUNT_POLICE_VERIFICATION_PENDING = Account.builder()
            .name("test")
            .identifier("test@email.com")
            .organizationID("org_id")
            .accountIdentifierType(AccountIdentifierType.email)
            .accountType(AccountType.user)
            .passwordHashed("hashed_pass")
            .status(AccountStatus.POLICE_VERIFICATION_PENDING)
            .build();

    public static final Account ADMIN_ACCOUNT_VERIFIED = Account.builder()
            .name("test")
            .identifier("test@email.com")
            .organizationID("org_id")
            .accountIdentifierType(AccountIdentifierType.email)
            .accountType(AccountType.admin)
            .passwordHashed("hashed_pass")
            .status(AccountStatus.VERIFIED)
            .build();
}
