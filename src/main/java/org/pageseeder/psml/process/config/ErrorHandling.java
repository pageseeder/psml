/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * This element is used to customise error handling.
 *
 * <p>Used to represent the inner ANT element:
 * <pre>{@code <error
 *            xrefnotfound="[true|false]"
 *            xrefambiguous="[true|false]"
 *            imagenotfound="[true|false]" />}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>xrefnotfound:  If 'true' log an error if an xref target file is not in the export set - default false.</li>
 *   <li>xrefambiguous:  If 'true' log an error if an xref target is ambiguous - default false.</li>
 *   <li>imagenotfound: If 'true' log an error if a referenced image is not in the export set - default false.</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 0.5.0
 */
public final class ErrorHandling {

  /**
   * Whether to throw an error for an XRef not found
   */
  private boolean xrefNotFound = false;

  /**
   * Whether to throw an error when an XRef target is ambiguous
   */
  private boolean xrefAmbiguous = false;

  /**
   * Whether to throw an error for an image not found
   */
  private boolean imageNotFound = false;

  /**
   * Sets whether an error should be logged when a referenced image is not found.
   *
   * @param notFound true to log an error for a missing image, false otherwise
   */
  public void setImageNotFound(boolean notFound) {
    this.imageNotFound = notFound;
  }

  /**
   * Sets whether an error should be logged when an xref is not found.
   *
   * @param notFound true to log an error for a missing xref, false otherwise
   */
  public void setXrefNotFound(boolean notFound) {
    this.xrefNotFound = notFound;
  }

  /**
   * Sets whether an error should be logged when an xref is ambiguous.
   *
   * @param notFound true to log an error for an ambiguous xref, false otherwise
   */
  public void setXrefAmbiguous(boolean notFound) {
    this.xrefAmbiguous = notFound;
  }

  /**
   * @return true if an error should be logged when a referenced image is not found.
   */
  public boolean getImageNotFound() {
    return this.imageNotFound;
  }

  /**
   * @return true if an error should be logged when an xref is not found.
   */
  public boolean getXrefNotFound() {
    return this.xrefNotFound;
  }

  /**
   * @return true if an error should be logged when an ambiguous xref is found.
   */
  public boolean getXrefAmbiguous() {
    return this.xrefAmbiguous;
  }

}
