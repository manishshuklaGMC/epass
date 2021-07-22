package curfew.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Created by manish.shukla on 2020/4/1. */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetOrganizationDetailsRequest {
  private String authToken;
}
