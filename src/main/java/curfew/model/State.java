package curfew.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class State {
    private final Integer id;
    private final StateName stateName;
    private final StateConfig stateConfig;
}
