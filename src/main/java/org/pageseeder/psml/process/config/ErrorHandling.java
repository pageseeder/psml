/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * This element is used to customise error handling.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code<error
 *            xrefnotfound="[true|false]"
 *            imagenotfound="[true|false]" />}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>xrefnotfound:  If 'true' log an error if an xref target file is not in the export set - default false.</li>
 *   <li>imagenotfound: If 'true' log an error if a referenced image is not in the export set - default false.</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public final class ErrorHandling {

  /**
   * Whether or not to throw an error for an XRef not found
   */
  private boolean xrefnotfound = false;

  /**
   * Whether or not to throw an error for an image not found
   */
  private boolean imagenotfound = false;

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

}
