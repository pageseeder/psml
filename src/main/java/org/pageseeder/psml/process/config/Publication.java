/*
 * Copyright (c) 1999-2018 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * Generates auto-numbering and TOC.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code<publication
 *       config="[publication config path]"
 *       rootfile="[path relative to src]"
 *       generatetoc="[true|false]" />}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>config:      Path to publication config file (required)</li>
 *   <li>rootfile:    Path to root file of publication relative to src</li>
 *   <li>generatetoc: Whether to generate the TOC</li>
 * </ul>
 *
 * @author Philip Rutherford
 *
 */
public final class Publication {

  /**
   * Path of publication config file
   */
  private String config = null;

  /**
   * Path of root file
   */
  private String rootfile = null;

  /**
   * Whether to generate the TOC.
   */
  private boolean generatetoc = false;

  /**
   * @param pconfig the config path to set
   */
  public void setConfig(String pconfig) {
    this.config = pconfig;
  }

  /**
   * @return the config path
   */
  public String getConfig() {
    return this.config;
  }

  /**
   * @param rfile the config path to set
   */
  public void setRootfile(String rfile) {
    this.rootfile = rfile;
  }

  /**
   * @return the config path
   */
  public String getRootfile() {
    return this.rootfile;
  }

  /**
   * @param toc Whether to generate the TOC
   */
  public void setGeneratetoc(boolean toc) {
    this.generatetoc = toc;
  }

  /**
   * @return the config path
   */
  public boolean isTOCGenerated() {
    return this.generatetoc;
  }

}
