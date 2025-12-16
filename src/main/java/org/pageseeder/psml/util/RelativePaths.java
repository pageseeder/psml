/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class to compute the paths between files.
 *
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 1.7.0
 */
public final class RelativePaths {

  /** utility class. */
  private RelativePaths() {
  }

  /**
   * Compute the relative path of the file provided.
   *
   * <p>This method invokes the <code>File#getCanonicalFile</code> on each parameter first
   * before calling the {@link #compute(File, File)} method.
   *
   * <p>Path separators are replaced with '/'.
   *
   * @param file the file to compute the relative path for
   * @param root the root that the path will be relative to
   *
   * @return the relative path between file and root
   *
   * @throws IllegalArgumentException if the file is not a descendant of the specified root
   */
  public static String computeCanonical(File file, File root) {
    try {
      return compute(file.getCanonicalFile(), root.getCanonicalFile());
    } catch (IOException ex) {
      throw new IllegalArgumentException("Failed to compute file's canonical path: " + ex.getMessage(), ex);
    }
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
  public static String compute(File file, File root) {
    final Path filePath = file.toPath();
    final Path rootPath = root.toPath();

    if (filePath.equals(rootPath)) return "";

    // Fast descendant check; also avoids IllegalArgumentException from relativize across different roots.
    if (!filePath.startsWith(rootPath)) {
      throw new IllegalArgumentException("The path " + File.separatorChar + filePath +
          " is outside the src path " + File.separatorChar + rootPath);
    }

    final String relative = rootPath.relativize(filePath).toString();
    return relative.replace(File.separatorChar, '/');
  }

}
