/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * This element is used to customise error handling.
 *
 * <p>Used to represent the inner ANT element:
 * <pre>{@code <warning
 *            xrefnotfound="[true|false]"
 *            xrefambiguous="[true|false]"
 *            imagenotfound="[true|false]" />}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>xrefnotfound:  If 'true' log a warning if an xref target file is not in the export set - default true.</li>
 *   <li>xrefambiguous: If 'true' log a warning if an xref target is ambiguous - default true.</li>
 *   <li>imagenotfound: If 'true' log a warning if a referenced image is not in the export set - default true.</li>
 * </ul>
 *
 * @author Philip Rutherford
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 0.5.0
 */
public final class WarningHandling {

  /**
   * Whether to throw a warning for an XRef not found
   */
  private boolean xrefNotFound = true;

  /**
   * Whether to throw a warning when an XRef target is ambiguous
   */
  private boolean xrefAmbiguous = true;

  /**
   * Whether to throw a warning for an image not found
   */
  private boolean imageNotFound = true;

  /**
   * Sets whether a warning should be logged when a referenced image is not found.
   *
   * @param notFound true to log a warning for a missing image, false otherwise
   */
  public void setImageNotFound(boolean notFound) {
    this.imageNotFound = notFound;
  }

  /**
   * Sets whether a warning should be logged when an xref is not found.
   *
   * @param notFound true to log a warning for a missing xref, false otherwise
   */
  public void setXrefNotFound(boolean notFound) {
    this.xrefNotFound = notFound;
  }

  /**
   * Sets whether a warning should be logged when an xref is ambiguous.
   *
   * @param notFound true to log a warning for an ambiguous xref, false otherwise
   */
  public void setXrefAmbiguous(boolean notFound) {
    this.xrefAmbiguous = notFound;
  }

  /**
   * @return true if a warning should be logged when a referenced image is not found.
   */
  public boolean getImageNotFound() {
    return this.imageNotFound;
  }

  /**
   * @return true if a warning should be logged when an xref is not found.
   */
  public boolean getXrefNotFound() {
    return this.xrefNotFound;
  }

  /**
   * @return true if a warning should be logged when an ambiguous xref is found.
   */
  public boolean getXrefAmbiguous() {
    return this.xrefAmbiguous;
  }

}
