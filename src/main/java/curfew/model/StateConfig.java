package curfew.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StateConfig {
    private String emailFromId;
    private String emailFromName;
    private String issuingAuthorityDisclaimer;
    private String helplineFooter;
    private String passTitleImageFileURL;
}
