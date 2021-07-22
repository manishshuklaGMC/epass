package curfew.controller.response;

import curfew.model.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class GetAllOrganizationsRespose {
  private List<Organization> organizations;
}
