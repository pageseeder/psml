package org.pageseeder.psml.util;

/**
 * Interface that tracks warnings and errors.
 *
 * <p>It provides methods to log warnings and errors, check if errors have been recorded,
 * and potentially store diagnostic messages for future processing.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
@FunctionalInterface
public interface DiagnosticCollector {

  /**
   * Logs a message as an error or warning based on the specified flag.
   *
   * @param isError A boolean indicating whether the message is an error. If {@code true}, the
   *                message is treated as an error; otherwise, it is treated as a warning.
   * @param message The message to log. This should provide details about the warning or error.
   */
  void log(boolean isError, String message);

  /**
   * Logs a warning message.
   *
   * @param message The warning message to be logged. This should provide a clear description of the
   *                issue for which the warning is being raised.
   */
  default void warn(String message) {
    log(false, message);
  }

  /**
   * Logs an error message.
   *
   * @param message The error message to be logged. This should clearly describe the problem encountered.
   */
  default void error(String message) {
    log(true, message);
  }
}
