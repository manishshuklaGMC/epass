package curfew.controller.request;

import curfew.model.OrgType;
import curfew.model.Person;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PutOrganization {
  private final String name;
  private final OrgType type;
  private final String city;
  private final Person authorizedPerson;
}
