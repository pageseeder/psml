/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;


/**
 * Represent an ANT include or exclude.
 *
 * <p>Only attribute <code>name</code> is supported.
 *
 * @author Jean-Baptiste Reure
 * @version 1.6.0
 * @since 1.0
 */
public final class IncludeExclude {

  /**
   * include name.
   */
  private String name;

  /**
   * if this is to include or exclude documents
   */
  private final boolean isInclude;

  /**
   * @param name Name to include
   * @return a new include instance
   */
  public static IncludeExclude createInclude(String name) {
    return new IncludeExclude(name, true);
  }

  /**
   * @param name Name to exclude
   * @return a new exclude instance
   */
  public static IncludeExclude createExclude(String name) {
    return new IncludeExclude(name, false);
  }

  /**
   * @return a new include instance
   */
  public static IncludeExclude createInclude() {
    return new IncludeExclude(null, true);
  }

  /**
   * @return a new exclude instance
   */
  public static IncludeExclude createExclude() {
    return new IncludeExclude(null, false);
  }

  /**
   * @param include true if it's an include; false for an exclude.
   */
  private IncludeExclude(String name, boolean include) {
    this.name = name;
    this.isInclude = include;
  }

  /**
   * @param name the new include name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the include name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return <code>true</code> if include, <code>false</code> if exclude
   */
  public boolean isInclude() {
    return this.isInclude;
  }

}
