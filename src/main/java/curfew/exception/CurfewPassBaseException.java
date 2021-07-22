package curfew.exception;

/** Created by manish.shukla on 2020/3/25. */
public class CurfewPassBaseException extends RuntimeException {
  private String displayMessage;

  public CurfewPassBaseException(String message) {
    super(message);
  }

  public CurfewPassBaseException(Throwable cause) {
    super(cause);
  }

  public CurfewPassBaseException(String message, String displayMessage) {
    super(message);
    this.displayMessage = displayMessage;
  }

  protected CurfewPassBaseException(String message, Throwable cause) {
    super(message, cause);
  }

  protected CurfewPassBaseException(String message, String displayMessage, Throwable cause) {
    super(message, cause);
    this.displayMessage = displayMessage;
  }

  public String getDisplayMessage() {
    return displayMessage;
  }
}
