package curfew.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

@ToString(includeFieldNames = true)
@Data
public class ErrorInfo {
  @Nullable private String errorCode;
  @Nullable private String errorMessage;
  @Nullable private ErrorInfo errorInfo;
}
