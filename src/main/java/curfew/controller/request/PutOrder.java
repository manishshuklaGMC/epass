package curfew.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PutOrder {
  private MultipartFile file;
  private OrderType orderType;
  private String authToken;
  private String purpose;
  private Long validTill;
}
