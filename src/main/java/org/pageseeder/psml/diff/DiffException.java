package org.pageseeder.psml.diff;

public class DiffException extends Exception {

  private static final long serialVersionUID = 1L;

  public DiffException() {
  }

  public DiffException(String message) {
    super(message);
  }

  public DiffException(Throwable cause) {
    super(cause);
  }

  public DiffException(String message, Throwable cause) {
    super(message, cause);
  }

}
