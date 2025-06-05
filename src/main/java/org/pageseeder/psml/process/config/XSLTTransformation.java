/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a mechanism to process PSML files with XSLT.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code <pretransform
 *             xslt="[pre XSLT path]"
 *             includes="[patterns]"
 *             excludes="[patterns]">
 *    <include name="[name]" />
 *    <exclude name="[name]" />
 *    <param name="[x]" expression="[y]" />
 *   ...
 * </pretransform>
 *   or
 * <posttransform
 *             xslt="[pre XSLT path]"
 *             includes="[patterns]"
 *             excludes="[patterns]">
 *    <include name="[name]" />
 *    <exclude name="[name]" />
 *    <param name="[x]" expression="[y]" />
 *   ...
 * </posttransform>}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>xslt: Path to XSLT file to apply to all PS XML files before/after all other processing (must produce valid PSML).</li>
 *   <li>param elements: parameters to pass to the XSLT script.</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public final class XSLTTransformation extends IncludeExcludeConfig {

  /**
   * Path to XSLT script
   */
  private String xslt = null;

  /**
   * List of facets to filter the documents
   */
  private final Map<String, String> params = new HashMap<>();

  /**
   * @param x the path to XSLT script
   */
  public void setXSLT(String x) {
    this.xslt = x;
  }

  /**
   * @return the path to the xslt script
   */
  public String getXSLT() {
    return this.xslt;
  }

  /**
   * @return the params
   */
  public Map<String, String> getParams() {
    return this.params;
  }

  /**
   * Add a new XSL parameter.
   * @param name  the param name
   * @param value the param value
   */
  public void setParam(String name, String value) {
    this.params.put(name, value);
  }

}
