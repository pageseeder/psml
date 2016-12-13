/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.pageseeder.psml.process.config.ManifestDocument;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;
import org.slf4j.Logger;

/**
 * Class responsible for the creation of the manifest file in the export process.
 *
 * @author Jean-Baptiste Reure
 * @version 22/10/2012
 *
 */
public final class ManifestCreator {

  /**
   * The UTF-8 charset
   */
  private static final Charset UTF8 = Charset.forName("UTF-8");

  /**
   * How the xrefs are processed
   */
  private final ManifestDocument manifestDoc;

  /**
   * The parent task, for logging
   */
  private Logger logger = null;

  /**
   * Build a new processor.
   *
   * @param xr the details of the processing.
   */
  public ManifestCreator(ManifestDocument xr) {
    if (xr != null && xr.getFilename() == null)
      throw new IllegalArgumentException("Filename cannot be null");
    this.manifestDoc = xr;
  }

  /**
   * @param log the log to set
   */
  public void setLog(Logger log) {
    this.logger = log;
  }

  /**
   * Create the manifest file.
   *
   * @param psmlFiles         the list of files to include in the manifest
   * @param destinationFolder the destination folder, where the manifest will be saved
   *
   * @return the create manifest file, <code>null</code> if not created
   *
   * @throws ProcessException if anything goes wrong
   */
  public File createManifest(Map<String, File> psmlFiles, File destinationFolder) throws ProcessException {
    // make sure we've got something to do
    if (this.manifestDoc == null) return null;
    String manifestFileName = this.manifestDoc.getFilename()+".psml";
    // log
    logger.info("Manifest-Doc: Creating manifest file "+manifestFileName);
    // finding files to include
    // check if there's any matching to do
    IncludesExcludesMatcher matcher = this.manifestDoc.buildMatcher();
    boolean matching = matcher != null && matcher.hasPatterns();
    List<String> toInclude;
    if (matching) {
      toInclude = new ArrayList<String>();
      // match paths
      for (String path : psmlFiles.keySet()) {
        if (matcher.matches(path)) {
          toInclude.add(path);
        }
      }
    } else {
      toInclude = new ArrayList<String>(psmlFiles.keySet());
    }
    // make sure there are file to include
    if (toInclude.isEmpty()) {
      logger.warn("Manifest file is not created as there are no files to point to");
      return null;
    }
    // sort
    Collections.sort(toInclude, new FileNameComparator(psmlFiles));

    // create manifest file
    File manifest = new File(destinationFolder, manifestFileName);
    if (manifest.exists())
      throw new ProcessException("Manifest file already exists, it will be overwritten");
    // create parent folder
    File dad = manifest.getParentFile();
    if (!dad.exists()) dad.mkdirs();
    if (!dad.exists())
      throw new ProcessException("Failed to create manifest parent folder "+dad.getAbsolutePath());
    try {
      if (!manifest.createNewFile())
        throw new ProcessException("Failed to create manifest file "+manifest.getAbsolutePath());

      // start writing
      FileOutputStream manifestStream = new FileOutputStream(manifest);
      try {
        manifestStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n".getBytes(UTF8));
        manifestStream.write("<document type=\"manifest\" level=\"portable\">\n".getBytes(UTF8));
        manifestStream.write("  <section id=\"xrefs\">\n".getBytes(UTF8));
        manifestStream.write("    <xref-fragment id=\"xrefs\">\n".getBytes(UTF8));
        // add all xrefs
        for (String path : toInclude) {
          String name = psmlFiles.get(path).getName();
          manifestStream.write(("      <blockxref type=\"embed\"").getBytes(UTF8));
          manifestStream.write((" href=\""+path+"\"").getBytes(UTF8));
          manifestStream.write((" frag=\"default\"").getBytes(UTF8));
          manifestStream.write((" reverselink=\"false\" ").getBytes(UTF8));
          manifestStream.write((">"+name+"</blockxref>\n").getBytes(UTF8));
        }
        manifestStream.write("    </xref-fragment>\n".getBytes(UTF8));
        manifestStream.write("  </section>\n".getBytes(UTF8));
        manifestStream.write("</document>\n".getBytes(UTF8));
      } finally {
        manifestStream.close();
      }
    } catch (IOException ex) {
      logger.error("Failed to write manifest file", ex);
      throw new ProcessException("Failed to write to manifest file: "+ex.getMessage(), ex);
    }
    // log
    logger.info("Manifest-Doc: Complete");
    return manifest;
  }

  /**
   * Used to order the files in alphabetical order.
   */
  private static class FileNameComparator implements Comparator<String> {
    /** the list of all PSML files */
    private final Map<String, File> psmlFiles;
    /**
     * @param all the list of all PSML files
     */
    public FileNameComparator(Map<String, File> all) {
      this.psmlFiles = all;
    }
    @Override
    public int compare(String path1, String path2) {
      File file1 = this.psmlFiles.get(path1);
      File file2 = this.psmlFiles.get(path2);
      if (file1 == null || file2 == null)
        throw new IllegalArgumentException("Invalid path has no file");
      return file1.getName().compareTo(file2.getName());
    }
  }
}
