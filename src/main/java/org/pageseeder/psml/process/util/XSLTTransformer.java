/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.transform.Transformer;

import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.process.config.XSLTTransformation;
import org.slf4j.Logger;

/**
 * @author Jean-Baptiste Reure
 * @version 22/10/2012
 *
 */
public final class XSLTTransformer {

  /**
   * The size of the byte buffer used to copy files.
   */
  private static final int BUFFER_SIZE = 12 * 1024;

  /**
   * The XSLT details
   */
  private final XSLTTransformation transformationDetails;

  /**
   * The parent task, for logging
   */
  private Logger logger = null;

  /**
   * If original source should be preserved
   */
  private boolean preserveSrc = false;

  /**
   * If process should fail on error
   */
  private boolean failOnError = true;

  /**
   * Build a new transformer.
   *
   * @param xslt the details of the XSLT.
   */
  public XSLTTransformer(XSLTTransformation xslt) {
    if (xslt != null && xslt.getXSLT() == null)
      throw new IllegalArgumentException("XSLT script cannot be null");
    this.transformationDetails = xslt;
  }

  /**
   * @param preserve the preserveSrc to set
   */
  public void setPreserveSrc(boolean preserve) {
    this.preserveSrc = preserve;
  }

  /**
   * @param failonerror if the process should fail on error
   */
  public void setFailOnError(boolean failonerror) {
    this.failOnError = failonerror;
  }

  /**
   * @return the path to the XSLT script
   */
  public String getXSLT() {
    return this.transformationDetails == null ? "No Script" : this.transformationDetails.getXSLT();
  }

  /**
   * @param log the log to set
   */
  public void setLog(Logger log) {
    this.logger = log;
  }

  /**
   * Perform the XSLT transformation.
   *
   * @param psmlFiles the list of files to transform
   * @param destinationFolder the destination folder, where the output will be saved
   * @param element name of the ant element, used for warning
   *
   * @throws ProcessException if anything goes wrong
   */
  public void transform(Map<String, File> psmlFiles, File destinationFolder, String element) throws ProcessException {
    // make sure we've got something to do
    if (this.transformationDetails == null) return;
    // create XSLT template
    File xslt = new File(this.transformationDetails.getXSLT());
    if (!xslt.exists() || !xslt.isFile())
      throw new ProcessException("Invalid XSLT script "+this.transformationDetails.getXSLT());
    // log
    if (logger != null)
      logger.debug("Transform: Loading XSLT script "+xslt.getAbsolutePath());
    Transformer transformer = XMLUtils.createTransformer(xslt);
    Map<String, String> params = this.transformationDetails.getParams();
    for (String p : params.keySet()) {
      transformer.setParameter(p, params.get(p));
    }
    // find schema to validate output
    ClassLoader loader = XSLTTransformer.class.getClassLoader();
    URL schema = loader.getResource(XSLTTransformer.class.getPackage()
        .getName().replace('.', '/') + "/psml-processed.xsd");
    // build the file pattern matcher
    IncludesExcludesMatcher matcher = this.transformationDetails.buildMatcher();
    // loop through file list
    for (String relPath : psmlFiles.keySet()) {
      // check pattern matching
      boolean transform = matcher == null || !matcher.hasPatterns() || matcher.matches(relPath);
      // create output file
      File output;
      try {
        output = new File(destinationFolder, relPath);
        // just in case
        output.getParentFile().mkdirs();
        if (transform) {
          if (!output.exists() && !output.createNewFile())
            throw new ProcessException("Failed to create output file "+output.getAbsolutePath());
        }
      } catch (IOException e) {
        throw new ProcessException("XRefs error: Failed to create temp file: "+e.getMessage(), e);
      }
      // run transform now
      if (transform) {
        // log
        if (logger != null)
          logger.debug("Transform: Transforming file "+relPath);
        // run xslt script
        try {
          XMLUtils.transform(psmlFiles.get(relPath), output, transformer, schema, null, null);
        } catch (ProcessException ex) {
          if (this.failOnError) throw ex;
          else if (this.logger != null) this.logger.error(ex.getMessage());
        }
      } else {
        // move/copy it then
        moveFile(psmlFiles.get(relPath), output);
      }
    }
    // log
    if (logger != null)
      logger.debug("Transform: Complete");
  }
  /**
   * Copy or move the file, depending on the preservesrc flag
   *
   * @param from the original file
   * @param to   the target file
   *
   * @throws ProcessException If moving/copying the file failed
   */
  private void moveFile(File from, File to) throws ProcessException {
    to.getParentFile().mkdirs();
    if (this.preserveSrc) {
      // copy file
      try {
        FileInputStream fis = new FileInputStream(from);
        FileOutputStream fos = new FileOutputStream(to);
        try {
          int read;
          byte[] buffer = new byte[BUFFER_SIZE];
          while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
          }
        } finally {
          fis.close();
          fos.close();
        }
      } catch (IOException ex) {
        throw new ProcessException("Failed to copy file "+from.getAbsolutePath()+" to "+to.getAbsolutePath(), ex);
      }
    } else {
      try {
        java.nio.file.Files.move(from.toPath(), to.toPath());
      } catch (IOException ex) {
        throw new ProcessException("Failed to move file "+from.getAbsolutePath()+" to "+to.getAbsolutePath(), ex);
      }
    }
  }


}
