/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * This element is used to customise error handling.
 *
 * <p>Used to represent the inner ANT element:<p>
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
 *
 */
public final class WarningHandling {

  /**
   * Whether or not to throw a warning for an XRef not found
   */
  private boolean xrefnotfound = true;

  /**
   * Whether or not to throw a warning when an XRef target is ambiguous
   */
  private boolean xrefambiguous = true;

  /**
   * Whether or not to throw a warning for an image not found
   */
  private boolean imagenotfound = true;

  /**
   * @param imagenf the imagenotfound to set
   */
  public void setImageNotFound(boolean imagenf) {
    this.imagenotfound = imagenf;
  }

  /**
   * @param xrefnf the xrefnotfound to set
   */
  public void setXrefNotFound(boolean xrefnf) {
    this.xrefnotfound = xrefnf;
  }

  /**
   * @param xrefa the xrefambiguous to set
   */
  public void setXrefAmbiguous(boolean xrefa) {
    this.xrefambiguous = xrefa;
  }

  /**
   * @return the imagenotfound
   */
  public boolean getImageNotFound() {
    return this.imagenotfound;
  }

  /**
   * @return the xrefnotfound
   */
  public boolean getXrefNotFound() {
    return this.xrefnotfound;
  }

  /**
   * @return the xrefambiguous
   */
  public boolean getXrefAmbiguous() {
    return this.xrefambiguous;
  }

}
