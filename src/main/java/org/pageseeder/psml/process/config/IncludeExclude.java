/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;


/**
 * Represent an ANT include or exclude.
 * Only attribute <code>name</code> is supported currently.
 *
 * @author Jean-Baptiste Reure
 * @version 04/05/2015
 */
public final class IncludeExclude {
  /**
   * include name.
   */
  private String _name;

  /**
   * if this is to include or exclude documents
   */
  private final boolean _isInclude;

  /**
   * @return a new include instance
   */
  public static IncludeExclude createInclude(String name) {
    return new IncludeExclude(name, true);
  }

  /**
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
   * @param inc if it's an include or an exclude
   */
  private IncludeExclude(String name, boolean inc) {
    this._name = name;
    this._isInclude = inc;
  }

  /**
   * @param pname the new include name.
   */
  public void setName(String pname) {
    this._name = pname;
  }

  /**
   * @return the include name.
   */
  public String getName() {
    return _name;
  }

  /**
   * @return <code>true</code> if include, <code>false</code> if exclude
   */
  public boolean isInclude() {
    return this._isInclude;
  }

}
