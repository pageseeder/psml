package org.pageseeder.psml.util;

import org.slf4j.Logger;

import java.util.Objects;

/**
 * A logger-based implementation tracks warnings and errors using a provided {@link Logger}.
 *
 * <p>An internal flag is maintained to keep track of whether any errors have been logged.
 * This allows the consumer of this class to query the existence of logged errors.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class DiagnosticLogger implements DiagnosticCollector {

  private final Logger logger;

  public DiagnosticLogger(Logger logger) {
    this.logger = Objects.requireNonNull(logger);
  }

  @Override
  public void log(boolean isError, String message) {
    if (isError) this.error(message);
    else this.warn(message);
  }

  @Override
  public void warn(String message) {
    this.logger.warn(message);
  }

  @Override
  public void error(String message) {
    this.logger.error(message);
  }

}
