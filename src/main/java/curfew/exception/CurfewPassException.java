package curfew.exception;

public class CurfewPassException extends CurfewPassBaseException {
  public CurfewPassException(String message) {
    super(message);
  }

  public CurfewPassException(String message, String displayMessage) {
    super(message, displayMessage);
  }

  public CurfewPassException(String message, Throwable cause) {
    super(message, cause);
  }

  public CurfewPassException(String message, String displayMessage, Throwable cause) {
    super(message, displayMessage, cause);
  }
}
