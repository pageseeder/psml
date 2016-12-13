/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * Generates auto-numbering in PSML files.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code<number
 *       numberconfig="[numbering config path]"
 *       includes="[patterns]"
 *       excludes="[patterns]">
 *   <include name="[name]" />
 *   <exclude name="[name]" />
 * </numberconfig>}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>numberconfig: Path to numbering config file for numbering included files (required)</li>
 *   <li>includes: A comma-separated list of patterns matching documents/folders to include.
 *                 When not specified, all documents/folders are included.
 *                 The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                   *.psml,archive,folder1/*.psml,** /*.psml</li>
 *   <li>excludes: A comma-separated list of patterns matching documents/folders to exclude.
 *                 When not specified, no documents/folders are excluded.
 *                 The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                   _local/**,_external/**</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public final class Numbering extends IncludeExcludeConfig {

  /**
   * Path of numbering config file
   */
  private String numberconfig = null;

  /**
   * @param nconfig the numberconfig to set
   */
  public void setNumberConfig(String nconfig) {
    this.numberconfig = nconfig;
  }

  /**
   * @return the numberconfig
   */
  public String getNumberConfig() {
    return this.numberconfig;
  }

}
