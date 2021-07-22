package curfew.model;

import lombok.Builder;
import lombok.Getter;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
public class Session {
  private final Long id;
  private final Long createdAt;
  private final Long validTill;
  private final String authToken;
  private final SessionStatus sessionStatus;
  private final Long userId;
}
