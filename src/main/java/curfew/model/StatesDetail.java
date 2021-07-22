package curfew.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class StatesDetail {
    private final Map<StateName, State> statesDetail;
    private final Map<Integer, State> statesDetailById;


    public StatesDetail(Map<StateName, State> statesDetail, Map<Integer, State> statesDetailById) {
        this.statesDetail = statesDetail;
        this.statesDetailById = statesDetailById;
    }
}
