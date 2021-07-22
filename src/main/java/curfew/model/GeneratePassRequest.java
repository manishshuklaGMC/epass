package curfew.model;

import lombok.Builder;

/** Created by manish.shukla on 2020/3/25. */
@Builder
public class GeneratePassRequest {
  private final Long phoneNumber;
  private final String name;
  private final Long startTime;
  private final Long endTime;
}
