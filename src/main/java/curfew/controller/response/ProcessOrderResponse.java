package curfew.controller.response;

public class ProcessOrderResponse {
  private final String processedS3URL;

  public ProcessOrderResponse(String processedS3URL) {
    this.processedS3URL = processedS3URL;
  }

  public String getProcessedS3URL() {
    return processedS3URL;
  }
}
