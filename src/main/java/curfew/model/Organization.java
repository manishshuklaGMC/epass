package curfew.model;

import lombok.*;

/** Created by manish.shukla on 2020/3/25. */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Organization {
  private Integer id;
  private String name;
  private String orgID;
  private Long createdAt;
  private OrganizationStatus status;
  private Integer activePassLimit;
  private Integer activePassCount;
  private Integer stateId;
}
