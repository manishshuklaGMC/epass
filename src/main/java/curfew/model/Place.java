package curfew.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Place {
  private final String city;
  private final Long pinCode;
  private final Float latitude;
  private final Float longitude;
}
