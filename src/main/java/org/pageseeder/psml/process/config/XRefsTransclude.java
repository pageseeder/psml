/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * Defines how to traverse XRefs, including the XRefs types and some patterns to include/exclude.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code <xrefs
 *       types="[xref types]"
 *       levels="[true|false]"
 *       xrefragment="[include,exclude,only]"
 *       includes="[patterns]"
 *       excludes="[patterns]">
 *   <include name="[name]" />
 *   <exclude name="[name]" />
 * </xrefs> }</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>types:        Comma separated list of xref types to process in included files
 *                     (i.e. transclude, embed ) - default none. </li>
 *   <li>levels:       Whether to modify heading levels based on XRef level attribute - default true. </li>
 *   <li>xreffragment: Defines how to handle XRefs in an {@code <xref-fragment>} element.
 *                     Possible values are "include" (process XRefs in an xref-fragment),
 *                     "exclude" (don't process XRefs in an xref-fragment) and
 *                     "only" (process only XRefs in an xref-fragment). Default is "include". </li>
 *   <li>includes:     A comma-separated list of patterns matching documents/folders to include.
 *                     When not specified, all documents/folders are included.
 *                     The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                     *.psml,archive,folder1/*.psml,** /*.psml</li>
 *   <li>excludes:     A comma-separated list of patterns matching documents/folders to exclude.
 *                     When not specified, no documents/folders are excluded.
 *                     The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                     _local/**,_external/**</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public final class XRefsTransclude extends IncludeExcludeConfig {

  public enum XREF_IN_XREF_FRAGMENT {
    
    /** Process xrefs inside {@code <xref-fragment>} */
    INCLUDE,
    
    /** Don't process xrefs inside {@code <xref-fragment>} */
    EXCLUDE,
    
    /** Process only xrefs inside {@code <xref-fragment>} */
    ONLY;
    
    /**
     * Returns the XREF_IN_XREF_FRAGMENT corresponding to the given name.
     *
     * @param name The name of XREF_IN_XREF_FRAGMENT (include, exclude or only).
     * 
     * @return The corresponding XREF_IN_XREF_FRAGMENT.
     */
    public static XREF_IN_XREF_FRAGMENT forName(String name) {
      for (XREF_IN_XREF_FRAGMENT n : values()) {
        if (n.name().equalsIgnoreCase(name)) return n;
      }
      throw new IllegalArgumentException("Invalid xreffragment attribute value: " + name);
    }    
  }
  
  /**
   * List of XRefs types to match.
   */
  private String types = null;

  /**
   * How to handle xrefs in an xref-fragment.
   */
  private XREF_IN_XREF_FRAGMENT xRefFragment = XREF_IN_XREF_FRAGMENT.INCLUDE;

  /**
   * Whether to process xref levels
   */
  private boolean levels = true;

  /**
   * @param t the types to set
   */
  public void setTypes(String t) {
    this.types = t;
  }

  /**
   * @param xf the xRefFragment to set
   */
  public void setXRefsInXRefFragment(XREF_IN_XREF_FRAGMENT xf) {
    this.xRefFragment = xf;
  }

  /**
   * @param s the name of xRefFragment to set  (include, exclude or only)
   */
  public void setXRefFragment(String s) {
    this.xRefFragment = XREF_IN_XREF_FRAGMENT.forName(s);
  }

  /**
   * @param b the levels to set
   */
  public void setLevels(Boolean b) {
    this.levels = b;
  }

  /**
   * @return <code>true</code> if the XRefs in an xref-fragment are included
   */
  public boolean includeXRefsInXRefFragment() {
    return this.xRefFragment == XREF_IN_XREF_FRAGMENT.INCLUDE;
  }

  /**
   * @return <code>true</code> if the XRefs in an xref-fragment are excluded
   */
  public boolean excludeXRefsInXRefFragment() {
    return this.xRefFragment == XREF_IN_XREF_FRAGMENT.EXCLUDE;
  }

  /**
   * @return <code>true</code> if only the XRefs in an xref-fragment are included
   */
  public boolean onlyXRefsInXRefFragment() {
    return this.xRefFragment == XREF_IN_XREF_FRAGMENT.ONLY;
  }

  /**
   * @return the types
   */
  public String getTypes() {
    return this.types;
  }

  /**
   * @return the levels
   */
  public boolean getLevels() {
    return this.levels;
  }

}
