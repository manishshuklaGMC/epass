package curfew.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Created by manish.shukla on 2020/3/25.
 */
@Builder
@Getter
@AllArgsConstructor
public class Application {
  private final Long id;
  private final ApplicationType applicationType;
  private final String purpose;
  private final ApplicationStatus applicationStatus;
  private final Place fromPlace;
  private final Place toPlace;
  private final Long startTime;
  private final Long endTime;
  private final String token;
  private final Integer issuerId;
  private final Long createdAt;
  private final Entity entity;
  private final String orderID;
  private final String partnerCompany;
  private final String validLocations;
}
