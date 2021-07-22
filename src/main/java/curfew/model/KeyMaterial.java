package curfew.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import curfew.model.DHPublicKey;
import lombok.*;

@ToString(includeFieldNames = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyMaterial {
  @NonNull String cryptoAlg;
  @NonNull String curve;
  @NonNull String params;

  @NonNull
  @JsonProperty("DHPublicKey")
  DHPublicKey dhPublicKey;
}
