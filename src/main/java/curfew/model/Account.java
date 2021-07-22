package curfew.model;

import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class Account {
  private final Integer id;
  private final String name;
  private final String identifier;
  private final String organizationID;
  private final AccountIdentifierType accountIdentifierType;
  private final String passwordHashed;
  private final Long createdAt;
  private final AccountType accountType;
  private final AccountStatus status;
  private final Integer stateId;
}
