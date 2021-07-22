package curfew.controller.response;

import curfew.model.ApplicationStatus;
import curfew.model.ApplicationType;
import curfew.model.Entity;
import curfew.model.Place;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VerifyTokenResponse {
  private final Long applicationID;
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
  private final String signature;
  private final String payload;
  private final String validLocations;
}
