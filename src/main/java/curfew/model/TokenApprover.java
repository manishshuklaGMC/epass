package curfew.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenApprover {
  private final Long id;
  private final String identifier;
  private final AccountIdentifierType identifierType;
  private final String orgName;
}
