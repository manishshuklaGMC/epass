package curfew.controller.request;

import curfew.model.StateConfig;
import curfew.model.StateName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by manish.shukla on 2020/4/9.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AddStateRequest {
  private StateName stateName;
  private StateConfig stateConfig;
  private String authToken;
}
