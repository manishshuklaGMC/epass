package curfew.controller;

import curfew.controller.request.AddStateRequest;
import curfew.controller.response.FetchStateListResponse;
import curfew.dao.StateDAO;
import curfew.exception.CurfewPassException;
import curfew.model.Account;
import curfew.model.State;
import curfew.model.StateName;
import curfew.model.StatesDetail;
import curfew.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@RestController
public class StateController {
  private final StatesDetail statesDetail;
  private final StateDAO stateDAO;
  private final AuthenticationService authenticationService;

  @Value("${superuser.id}")
  private int superUserID;

  public StateController(
      StatesDetail statesDetail, StateDAO stateDAO, AuthenticationService authenticationService) {
    this.statesDetail = statesDetail;
    this.stateDAO = stateDAO;
    this.authenticationService = authenticationService;
  }

  @PostMapping(value = "/fetchStateList")
  FetchStateListResponse fetchStateList() {
    Map<Integer, StateName> responseMap = new HashMap<>();
    for (Map.Entry<Integer, State> stateById : statesDetail.getStatesDetailById().entrySet()) {
      responseMap.put(stateById.getKey(), stateById.getValue().getStateName());
    }

    return FetchStateListResponse.builder().stateMap(responseMap).build();
  }

  @PostMapping(value = "/addState")
  public CompletionStage<String> addNewState(@RequestBody AddStateRequest request) {
    Account adminAccount = authenticationService.verifySession(request.getAuthToken());
    if (adminAccount.getId() != superUserID) {
      throw new CurfewPassException("Not allowed to add state.");
    }
    return stateDAO
        .addState(
            State.builder()
                .stateName(request.getStateName())
                .stateConfig(request.getStateConfig())
                .build())
        .thenApply(__ -> "added");
  }
}
