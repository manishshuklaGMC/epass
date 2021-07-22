package curfew.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString(includeFieldNames = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DHPublicKey {
  @NonNull String expiry;
  // Dont ask me why this is capital. I am just blindly following the spec ;)
  @NonNull
  @JsonProperty("Parameter")
  String parameter;

  @NonNull
  @JsonProperty("KeyValue")
  String keyValue;
}
