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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.process.config.XSLTTransformation;
import org.slf4j.Logger;

/**
 * XSLTTransformer is responsible for performing XSLT transformations on a set of input files.
 *
 * <p>It allows configuration of various behaviors such as error handling, validation, and
 * file moving or copying.
 *
 * <p>This class is designed to work with specific XSLT transformation details and a given set
 * of source files.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class XSLTTransformer {

  /**
   * The size of the byte buffer used to copy files.
   */
  private static final int BUFFER_SIZE = 12 * 1024;

  /**
   * The XSLT details
   */
  private final @Nullable XSLTTransformation transformationDetails;

  /**
   * The parent task, for logging
   */
  private @Nullable Logger logger = null;

  /**
   * If the original source should be preserved
   */
  private boolean preserveSrc = false;

  /**
   * If process should fail on error
   */
  private boolean failOnError = true;

  /**
   * If the results should be validated with psml-processed.xsd
   */
  private boolean validate = true;

  /**
   * If untransformed files should be moved to destination
   */
  private boolean moveAll = true;

  /**
   * Build a new transformer.
   *
   * @param xslt the details of the XSLT.
   */
  public XSLTTransformer(@Nullable XSLTTransformation xslt) {
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
   * @param validate if the results should be validated with psml-processed.xsd
   */
  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  /**
   * @param moveall if untransformed files should be moved to destination
   */
  public void setMoveAll(boolean moveall) {
    this.moveAll = moveall;
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
   * @param psmlFiles the map of destination relative path to files to transform
   * @param destinationFolder the destination folder, where the output will be saved
   *
   * @throws ProcessException if anything goes wrong
   */
  public void transform(Map<String, File> psmlFiles, File destinationFolder) throws ProcessException {
    // make sure we've got something to do
    if (this.transformationDetails == null) return;
    // create XSLT template
    File xslt = new File(this.transformationDetails.getXSLT());
    if (!xslt.exists() || !xslt.isFile())
      throw new ProcessException("Invalid XSLT script "+this.transformationDetails.getXSLT());
    // log
    XSLTErrorListener listener = null;
    if (logger != null) {
      logger.debug("Transform: Loading XSLT script {}", xslt.getAbsolutePath());
      listener = new XSLTErrorListener(logger);
    }
    Transformer transformer = XMLUtils.createTransformer(xslt, listener);
    transformer.setErrorListener(listener);
    Map<String, String> params = this.transformationDetails.getParams();
    for (Map.Entry<String, String> p : params.entrySet()) {
      transformer.setParameter(p.getKey(), p.getValue());
    }
    // find schema to validate output
    URL schema = null;
    if (validate) {
      ClassLoader loader = XSLTTransformer.class.getClassLoader();
      schema = loader.getResource(XSLTTransformer.class.getPackage()
              .getName().replace('.', '/') + "/psml-processed.xsd");
    }
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
          logger.debug("Transform: Transforming file {}", relPath);
        // run xslt script
        try {
          XMLUtils.transform(psmlFiles.get(relPath), output, transformer, schema, null, null);
        } catch (ProcessException ex) {
          if (this.failOnError) throw ex;
          else if (this.logger != null) this.logger.error(ex.getMessage());
        }
      } else if (moveAll) {
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

  /**
   * An XSLT error listener .
   *
   * @author Philip Rutherford
   */
  private static class XSLTErrorListener implements ErrorListener {

    /**
     * For logging errors
     */
    private final Logger log;

    /**
     * Creates a new XSLT error listener wrapping the specified listener.
     */
    XSLTErrorListener(Logger log) {
      this.log = log;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      this.log.error("Transformer fatal error: {}", exception.getMessageAndLocation());
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException {
      this.log.warn("Transformer warning: {}", exception.getMessageAndLocation());
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      this.log.error("Transformer error: {}", exception.getMessageAndLocation());
    }
  }

}
