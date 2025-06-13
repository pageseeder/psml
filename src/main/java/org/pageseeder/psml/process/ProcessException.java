package org.pageseeder.psml.process;

/**
 * Represents a generic exception that occurs during a process. This exception serves
 * as a base class for more specific process-related exceptions.
 *
 * <p>ProcessException is a custom exception that extends the standard Java Exception class.
 * It provides constructors for creating instances with specific messages or causes.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 1.0
 * @since 1.0
 */
public class ProcessException extends Exception {

  private static final long serialVersionUID = 1L;

  public ProcessException() {
  }

  /**
   * Constructs a new ProcessException with the specified detail message.
   *
   * @param message the detail message, which provides additional information about the exception.
   *                The message can be retrieved later using the {@code getMessage()} method.
   */
  public ProcessException(String message) {
    super(message);
  }

  /**
   * Constructs a new ProcessException with the specified cause.
   *
   * @param cause the cause of the exception, which can provide additional context
   *              for the error. The cause can be retrieved later using the
   *              {@code getCause()} method.
   */
  public ProcessException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new ProcessException with the specified detail message and cause.
   *
   * @param message the detail message, which provides additional information about the exception.
   *                The message can be retrieved later using the {@code getMessage()} method.
   * @param cause the cause of the exception, which can provide additional context
   *              for the error. The cause can be retrieved later using the
   *              {@code getCause()} method.
   */
  public ProcessException(String message, Throwable cause) {
    super(message, cause);
  }

}
