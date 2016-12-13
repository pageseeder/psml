/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

/**
 * Creates a PSML document of type "manifest" from the manifest.xml containing an {@code<xref>}
 * of type "embed" for each PSML file, in alphabetical order.
 * The generated file is included in PSML files for subsequent processing.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code<manifestdoc
 *             filename="[filename]"
 *             includes="[patterns]"
 *             excludes="[patterns]">
 *   <include name="[name]" />
 *   <exclude name="[name]" />
 * </manifestdoc>}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>filename: Filename for generated PSML document (e.g. manifest), required.</li>
 *   <li>includes: A comma-separated list of patterns matching documents/folders to include.
 *                 When not specified, all documents/folders are included.
 *                 The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                   *.psml,archive,folder1/*.psml,** /*.psml</li>
 *   <li>excludes: A comma-separated list of patterns matching documents/folders to exclude.
 *                 When not specified, no documents/folders are excluded.
 *                 The format is similar to other ant tasks file pattern selection. Here are some examples:
 *                   _local/**,_external/**</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public final class ManifestDocument extends IncludeExcludeConfig {

  /**
   * Name of manifest file
   */
  private String filename = null;

  /**
   * @return the name of the manidest.
   */
  public String getFilename() {
    return this.filename;
  }

  /**
   * @param fname the name of the manifest file to create.
   */
  public void setFilename(String fname) {
    this.filename = fname;
  }

}
