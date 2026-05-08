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

  /**
   * Turn the path provided into a relative export path (including /local or /external if required_).
   *
   * @param fullpath    the full path to relativise
   * @param parent      the parent folder
   * @param exportCtxt  the general context
   * @param groupCtxt   the group context
   * @param extfolders  the format N [= 0 "_", gt 0 ""][local|external][le 1 "", gt 1 "N"]
   * @param siteprefix  the pageseeder site prefix (e.g. /ps)
   *
   * @return the relative path.
   */
  public static String relativiseFullPath(String fullpath, String parent,
        String exportCtxt, String groupCtxt, int extfolders, String siteprefix) {
    String path = fullpath.replaceAll("\\.xml$", ".psml");
    String parentFolder  = parent.replaceFirst("/$", "")+'/';
    String exportContext = exportCtxt.replaceFirst("/$", "")+'/';
    String groupContext  = groupCtxt.replaceFirst("/$", "")+'/';
    boolean parentInContext   = parentFolder.startsWith(exportContext);
    boolean parentInGroupCtxt = parentFolder.startsWith(groupContext);
    boolean targetInContext   = fullpath.startsWith(exportContext);
    boolean targetInGroupCtxt = fullpath.startsWith(groupContext);
    String prefix = extfolders > 0 ? "" : "_";
    String suffix = extfolders <= 1 ? "" : Integer.toString(extfolders);

    String dad;
    String extraFolder;
    // if in same context, just strip context at the beginning
    if (targetInContext) {
      if (parentInContext) // within same context, use direct access
        return relativise(path.substring(exportContext.length()),
                parentFolder.substring(exportContext.length()));
      // parent in _local or _external folder?
      if (parentInGroupCtxt) {
        dad = parentFolder.substring(groupContext.length());
      } else {
        // remove site prefix
        if (parentFolder.startsWith(siteprefix+'/')) {
          parentFolder = parentFolder.substring(siteprefix.length()+1);
        }
        dad = parentFolder;
      }
      dad = "extra/" + dad; // extra/ (+1 ../) for _external or _local folder
      extraFolder = "";
      path = path.substring(exportContext.length());
      // same for group context
    } else if (targetInGroupCtxt) {
      // parent in _local as well?
      if (!parentInContext && parentInGroupCtxt) // within same context, use direct access
        return relativise(path.substring(groupContext.length()),
                parentFolder.substring(groupContext.length()));
      // parent in context or in _external?
      if (parentInContext) {
        dad = parentFolder.substring(exportContext.length());
      } else {
        // if we're here: parent is in _external folder
        // remove site prefix
        if (parentFolder.startsWith(siteprefix+'/')) {
          parentFolder = parentFolder.substring(siteprefix.length()+1);
        }
        dad = "extra/" + parentFolder; // extra/ (+1 ../) for _external folder
      }
      extraFolder = prefix + "local" + suffix + "/";
      path = path.substring(groupContext.length());
    } else if (!parentInContext && !parentInGroupCtxt) // parent and target are both in _external
      // within same context, use direct access
      return relativise(path, parentFolder);
    else {
      // if we're here, target is in _external folder
      // remove site prefix
      if (path.startsWith(siteprefix+'/')) {
        path = path.substring(siteprefix.length()+1);
      }
      // parent is either in context or in _local
      if (parentInContext) {
        dad = parentFolder.substring(exportContext.length());
      } else {
        dad = "extra/" + parentFolder.substring(groupContext.length()); // extra/ (+1 ../) for _local folder
      }
      extraFolder = prefix + "external" + suffix + "/";
    }
    String href = "";
    for (int i = 0; !dad.isEmpty() && i < dad.split("/").length; i++) {
      href += "../";
    }
    return href + extraFolder + path;
  }

  /**
   * Direct relativisation:
   * path              parent                result
   * /a/b/c/d          /a/b/c                d
   * /a/b/c/d          /a/b                  c/d
   * /a/b/c/d          /a/b/e                ../c/d
   * /a/b/c/d          /a/e/f/g              ../../../b/c/d
   *
   * @param path    the target
   * @param parent  the current context
   *
   * @return  the relative path
   */
  static String relativise(String path, String parent) {
    // shortcut
    if (parent.isEmpty()) return path;
    // shortcut, if target is in parent
    if (path.startsWith(parent+'/')) return path.substring(parent.length()+1);
    // check relative path
    String[] pathElements   = path.split("/");
    String[] parentElements = parent.split("/");
    // keep track of elements left in parents that are not in target
    int left = parentElements.length - pathElements.length + 1;
    // keep last element of path (filename)
    for (int i = 0; i < pathElements.length-1; i++) {
      if (i >= parentElements.length) {
        // no more elements in target
        break;
      } else if (pathElements[i].equals(parentElements[i])) {
        // match, just strip it
        path = path.substring(pathElements[i].length()+1);
      } else {
        // no more matching, stop here
        left = parentElements.length - i;
        break;
      }
    }
    // if there are still elements left in parent, include them
    for (int i = 0; i < left; i++) {
      path = "../" + path;
    }
    return path;
  }
}
