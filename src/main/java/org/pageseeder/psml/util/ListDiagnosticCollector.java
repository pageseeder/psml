package org.pageseeder.psml.util;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation collects warnings and errors in memory using lists.
 *
 * <h2>Usage Notes</h2>
 * This implementation is designed to store diagnostic messages in memory, making it suitable
 * for cases where diagnostic information needs to be accumulated and retrieved for further processing.
 *
 * <h2>Thread Safety</h2>
 * This class is not thread-safe. If multiple threads are expected to write or read warning/error messages,
 * appropriate external synchronization must be applied.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class ListDiagnosticCollector implements DiagnosticCollector {

  private final List<String> warnings = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();

  @Override
  public void log(boolean isError, String message) {
    if (isError) this.error(message);
    else this.warn(message);
  }

  @Override
  public void warn(String message) {
    this.warnings.add(message);
  }

  @Override
  public void error(String message) {
    this.errors.add(message);
  }

  /**
   * Retrieves the list of warning messages collected.
   *
   * @return a list of strings representing the warning messages.
   */
  public List<String> getWarnings() {
    return this.warnings;
  }

  /**
   * Retrieves the list of error messages collected.
   *
   * @return a list of strings representing the collected error messages.
   */
  public List<String> getErrors() {
    return this.errors;
  }

}
