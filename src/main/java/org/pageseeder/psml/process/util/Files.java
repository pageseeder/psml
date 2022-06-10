/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import java.io.File;
import java.io.IOException;

/**
 * A general utility class.
 *
 * @author Jean-Baptiste Reure
 * @version 27 April 2012
 */
public final class Files {

  /** utility class. */
  private Files() {
  }

  /**
   * Compute the relative path of the file provided.
   * Path separators are replaced with '/'.
   *
   * @param file the file to compute the relative path for
   * @param root the root that the path will be relative to
   *
   * @return the relative path between file and root
   *
   * @throws IllegalArgumentException if file is not a descendant of root
   */
  public static String computeRelativePath(File file, File root) throws IllegalArgumentException {
    String fpath;
    try {
      fpath = file.getCanonicalPath();
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to compute file's canonical path: "+ex.getMessage(), ex);
    }
    String rpath;
    try {
      rpath = root.getCanonicalPath();
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to compute root's canonical path: "+ex.getMessage(), ex);
    }
    if (fpath.equals(rpath)) return "";
    if (fpath.startsWith(rpath+File.separator))
      return fpath.substring(rpath.length()+1).replace(File.separatorChar, '/');
    // sanitise paths
    int i = 0;
    while (i < rpath.length() && i < fpath.length()) {
      if (rpath.charAt(i) != fpath.charAt(i)) break;
      i++;
    }
    throw new IllegalArgumentException("The path " + File.separatorChar + fpath.substring(i) +
            " is outside the src path " + File.separatorChar + rpath.substring(i));
  }
}
