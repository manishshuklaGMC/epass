package curfew.exception;

public class InvalidOTPError extends RuntimeException {
  public InvalidOTPError(String message) {
    super(message);
  }
}
