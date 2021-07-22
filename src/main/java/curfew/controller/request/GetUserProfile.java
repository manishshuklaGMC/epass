package curfew.controller.request;

import curfew.model.StateName;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetUserProfile {
  private String name;
  private String email;
  private String OrgName;
  private String OrgId;
  private StateName stateName;
}
