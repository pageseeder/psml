/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;

/**
 * Include exclude config details.
 *
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 0.5.0
 */
public abstract class IncludeExcludeConfig {

  /**
   * List of single files to include or exclude
   */
  private final List<IncludeExclude> includeExcludes = new ArrayList<>();

  /**
   * List of include patterns
   */
  private final List<String> includes = new ArrayList<>();

  /**
   * List of exclude patterns
   */
  private final List<String> excludes = new ArrayList<>();

  /**
   * @param toAdd the object to add
   */
  public void addIncludeExclude(IncludeExclude toAdd) {
    this.includeExcludes.add(toAdd);
  }

  /**
   * @param name the name to include
   */
  public void addInclude(String name) {
    this.includeExcludes.add(IncludeExclude.createInclude(name));
  }

  /**
   * @param name the name to exclude
   */
  public void addExclude(String name) {
    this.includeExcludes.add(IncludeExclude.createExclude(name));
  }

  /**
   * @return the new include object
   */
  public IncludeExclude createInclude() {
    IncludeExclude inc = IncludeExclude.createInclude();
    this.includeExcludes.add(inc);
    return inc;
  }

  /**
   * @return the new exclude object
   */
  public IncludeExclude createExclude() {
    IncludeExclude exc = IncludeExclude.createExclude();
    this.includeExcludes.add(exc);
    return exc;
  }

  /**
   * @param exc the excludes to set
   */
  public void setExcludes(@Nullable String exc) {
    if (exc != null)
      this.excludes.addAll(Arrays.asList(exc.split(",")));
  }

  /**
   * @param inc the includes to set
   */
  public void setIncludes(@Nullable String inc) {
    if (inc != null)
      this.includes.addAll(Arrays.asList(inc.split(",")));
  }

  /**
   * @param exc the excludes to add
   */
  public void addExcludes(Collection<String> exc) {
    this.excludes.addAll(exc);
  }

  /**
   * @return the includeExcludes
   */
  public List<IncludeExclude> getIncludeExcludes() {
    return this.includeExcludes;
  }

  /**
   * @return the includes
   */
  public List<String> getIncludes() {
    return this.includes;
  }

  /**
   * @return the excludes
   */
  public List<String> getExcludes() {
    return this.excludes;
  }

  /**
   * @param inc the includes to add
   */
  public void addIncludes(Collection<String> inc) {
    this.includes.addAll(inc);
  }

  /**
   * Build a matcher based on the includes and excludes patterns.
   *
   * @return the matcher if there are any patterns, <code>null</code> otherwise.
   */
  public @Nullable IncludesExcludesMatcher buildMatcher() {
    IncludesExcludesMatcher matcher = new IncludesExcludesMatcher();
    matcher.addIncludePatterns(this.includes);
    matcher.addExcludePatterns(this.excludes);
    for (IncludeExclude ie : this.includeExcludes) {
      if (ie.isInclude()) matcher.addIncludePattern(ie.getName());
      else matcher.addExcludePattern(ie.getName());
    }
    if (!matcher.hasPatterns()) return null;
    return matcher;
  }

}
