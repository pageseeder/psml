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
   * @return the relative path if files are related, <code>null</code> otherwise.
   */
  public static String computeRelativePath(File file, File root) {
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
    return null;
  }
}
