package curfew.controller.response;

import curfew.model.StateName;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class FetchStateListResponse {
    private final Map<Integer ,StateName> stateMap;
}
