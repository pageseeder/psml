package org.pageseeder.psml.util;

/**
 * A no-op implementation of the warning collection mechanism.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class NilDiagnosticCollector implements DiagnosticCollector {

  @Override
  public void log(boolean isError, String message) {
    // Do nothing
  }
}
