package curfew.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import curfew.model.ErrorInfo;
import curfew.model.KeyMaterial;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.lang.Nullable;

@ToString(includeFieldNames = true)
@Data
public class SerializedKeyPair {
  @NonNull private final String privateKey;

  @NonNull
  @JsonProperty("KeyMaterials")
  KeyMaterial keyMaterials;

  @Nullable ErrorInfo errorInfo;
}
