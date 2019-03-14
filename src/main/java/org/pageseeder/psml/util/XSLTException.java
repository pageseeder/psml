package org.pageseeder.psml.util;

public class XSLTException extends RuntimeException {

  /**
   * As per requirement
   */
  private static final long serialVersionUID = 20190314L;

  public XSLTException() {
    super();
  }

  public XSLTException(String message) {
    super(message);
  }

  public XSLTException(String message, Throwable cause) {
    super(message, cause);
  }

  public XSLTException(Throwable cause) {
    super(cause);
  }


}
