/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 1.6.9
 * @since 1.0.0
 */
public final class IncludesExcludesMatcher {

  /**
   * A list of patterns matching documents' paths to exclude.
   */
  private final List<String> excludePatterns = new ArrayList<>();

  /**
   * A list of patterns matching documents' paths to include.
   */
  private final List<String> includePatterns = new ArrayList<>();

  /**
   * Adds some patterns to include.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param patterns the list of patterns to include.
   */
  public void addIncludePatterns(@Nullable List<String> patterns) {
    if (patterns == null) return;
    for (String inc : patterns) {
      if (!inc.isEmpty()) this.includePatterns.add(createRegex(inc));
    }
  }

  /**
   * Adds some patterns to include.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param patterns the list of patterns to include.
   */
  public void addIncludePatterns(String @Nullable [] patterns) {
    if (patterns == null) return;
    for (String inc : patterns) {
      if (!inc.isEmpty()) this.includePatterns.add(createRegex(inc));
    }
  }

  /**
   * Add a pattern to include.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param pattern the pattern to include.
   */
  public void addIncludePattern(@Nullable String pattern) {
    if (pattern == null) return;
    this.includePatterns.add(createRegex(pattern));
  }

  /**
   * Adds a list of patterns to exclude.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param patterns the list of patterns to exclude.
   */
  public void addExcludePatterns(@Nullable List<String> patterns) {
    if (patterns == null) return;
    for (String exc : patterns) {
      if (!exc.isEmpty()) this.excludePatterns.add(createRegex(exc));
    }
  }

  /**
   * Adds a list of patterns to exclude.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param patterns the list of patterns to exclude.
   */
  public void addExcludePatterns(String @Nullable [] patterns) {
    if (patterns == null) return;
    for (String exc : patterns) {
      if (!exc.isEmpty()) this.excludePatterns.add(createRegex(exc));
    }
  }

  /**
   * Adds a pattern to exclude.
   *
   * <p>Does nothing if the parameter is <code>null</code>.
   *
   * @param pattern the pattern to exclude.
   */
  public void addExcludePattern(@Nullable String pattern) {
    if (pattern == null) return;
    this.excludePatterns.add(createRegex(pattern));
  }

  /**
   * Indicates whether the specified path is specifically excluded based on the exclusion patterns.
   *
   * @param path the path to test
   * @return <code>true</code> if excluded;
   *         <code>false</code> otherwise or if <code>null</code>.
   */
  public boolean isExcluded(@Nullable String path) {
    // match path with patterns, if one matches then excluded!
    if (path != null) for (String exc : this.excludePatterns) {
      if (path.matches(exc)) return true;
    }
    // either there is no path or they passed the exclusion test (or there was no test) so not excluded!
    return false;
  }

  /**
   * Indicates whether the specified path is specifically included based on the included patterns.
   *
   * @param path the path to test
   *
   * @return <code>true</code> if included;
   *         <code>false</code> otherwise or if <code>null</code>.
   */
  public boolean isIncluded(@Nullable String path) {
    // match patterns
    if (!this.includePatterns.isEmpty()) {
      // there are patterns to match, if no path, then not good!
      if (path == null) return false;
      // ok match path with all patterns, if one matches then it's all good!
      for (String inc : this.includePatterns) {
        if (path.matches(inc)) return true;
      }
    } else {
      return true;
    }
    // if we're here then none of the matchers matched so no good!
    return false;
  }

  /**
   * Checks if the path specified is valid according to this matcher.
   * This is the same as calling {@code isIncluded(path) && !isExcluded(path)}
   *
   * @param path the path to test
   *
   * @return <code>true</code> if path matches, <code>false</code> otherwise.
   */
  public boolean matches(String path) {
    return isIncluded(path) && !isExcluded(path);
  }

  /**
   * Indicates whether any include or exclude patterns have been specified.
   *
   * @return <code>true</code> if either list of patterns has elements;
   *         <code>false</code> if both lists are empty.
   */
  public boolean hasPatterns() {
    return !this.includePatterns.isEmpty() || !this.excludePatterns.isEmpty();
  }

  /**
   * Generate the regular expression corresponding to the specified pattern.
   *
   * @param pattern the pattern
   * @return the corresponding regular expression.
   */
  static String createRegex(String pattern) {
    return "^"+pattern.replaceAll("[\\.\\(\\)\\[\\]\\^\\$\\+]", "\\\\$0")
                      .replace("**", "\\u0000DS\\u0000")
                      .replace("*", "([^/]*?)")
                      .replace("\\u0000DS\\u0000", "(.*?)")+"$";
  }

}
