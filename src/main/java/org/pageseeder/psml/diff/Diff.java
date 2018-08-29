/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.pageseeder.psml.process.util.Files;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Adds <diff> elements to portable PSML files.
 * For more info, see {@link
 * https://dev.pageseeder.com/guide/publishing/ant_api/tasks/task_diff.html}
 *
 * @author Philip Rutherford
 *
 */
public final class Diff {

  /**
   * The logger.
   */
  private Logger logger = null;

  /**
   * The folder containing the documents to process.
   */
  private File src = null;

  /**
   * Where to export it.
   */
  private File dest = null;

  /**
   * Defines the images to process
   */
  private IncludesExcludesMatcher filesMatcher = null;

  /**
   * @param destination the destination to set
   */
  public void setDest(File destination) {
    this.dest = destination;
  }

  /**
   * @param source the src to set
   */
  public void setSrc(File source) {
    this.src = source;
  }

  /**
   * @param log the logger to set
   */
  public void setLogger(Logger log) {
    this.logger = log;
  }

  /**
   * @param matcher the filesMatcher to set
   */
  public void setFilesMatcher(IncludesExcludesMatcher matcher) {
    this.filesMatcher = matcher;
  }

  /**
   * Adds <diff> elements to portable PSML files (only if they have compare <content> elements).
   *
   * @throws DiffException if problem adding the elements.
   */
  public void diff() throws DiffException {

    // parameters validation
    // src
    if (this.src == null)
      throw new DiffException("Src must be specified");
    if (!this.src.exists() || !this.src.isDirectory())
      throw new DiffException("Invalid src location");
    // dest
    if (this.dest == null)
      throw new DiffException("Dest must be specified");
    if (!this.dest.exists() || !this.dest.isDirectory())
      throw new DiffException("Invalid destination folder");

    // make sure there's a logger
    if (this.logger == null) this.logger = LoggerFactory.getLogger(Diff.class);

    // collect files
    this.logger.debug("Collecting PSML files from "+this.src.getAbsolutePath());
    Map<String, File> psml = new HashMap<>();
    collectAll(this.src, psml);

    // loop through file list
    for (String relPath : psml.keySet()) {

      // check pattern matching
      boolean matches = this.filesMatcher == null || !this.filesMatcher.hasPatterns() || this.filesMatcher.matches(relPath);
      if (!matches) continue;

      this.logger.debug("Checking file "+relPath);
      // check if any compare fragments
      Map<String, String> compareFragments;
      try (InputStream input = new FileInputStream(psml.get(relPath))) {
        compareFragments = comparePSML(input);
      } catch (ParserConfigurationException | SAXException | IOException ex) {
        this.logger.error("Failed to parse input file "+relPath+" : "+ex.getMessage());
        throw new DiffException("Failed to parse input file "+relPath+" : "+ex.getMessage(), ex);
      }
      if (compareFragments.isEmpty()) {
        continue;
      }

      this.logger.debug("Diffing file "+relPath);

      FileOutputStream fos = null;
      try (InputStream input = new FileInputStream(psml.get(relPath))) {
        File output = new File(this.dest, relPath);
        // just in case
        output.getParentFile().mkdirs();
        if (!output.exists() && !output.createNewFile())
          throw new DiffException("Failed to create output file "+output.getAbsolutePath());
        fos = new FileOutputStream(output);
        diffPSML(input, new OutputStreamWriter(fos, StandardCharsets.UTF_8), compareFragments);
      } catch (ParserConfigurationException | SAXException | IOException ex) {
        this.logger.error("Failed to create output file: "+ex.getMessage(), ex);
        throw new DiffException("Failed to create output file: "+ex.getMessage(), ex);
      } finally {
        // close streams
        try {
          if (fos != null) fos.close();
        } catch (IOException ex) {
          throw new DiffException("Failed to close output stream: "+ex.getMessage(), ex);
        }
      }

    }
  }

  /**
   * Collects compare fragments in portable PSML.
   *
   * @param in  the PSML document
   *
   * @return map of fragment ID to current fragment with a corresponding <compare> element.
   *
   * @throws ParserConfigurationException if problem getting parser
   * @throws SAXException if problem parsing PSML
   * @throws IOException if problem reading PSML
   */
  public Map<String,String> comparePSML(InputStream in)
      throws ParserConfigurationException, SAXException, IOException {
    CompareHandler handler = new CompareHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    parser.parse(in, handler);
    return handler.getCompareFragments();
  }

  /**
   * Adds <diff> elements for compare fragments in portable PSML.
   *
   * @param in                the PSML document
   * @param out               the result PSML document
   * @param compareFragments  map of fragment ID to current fragment with a corresponding <compare> element.
   *
   * @throws ParserConfigurationException if problem getting parser
   * @throws SAXException if problem parsing PSML
   * @throws IOException if problem reading or writing PSML
   */
  public void diffPSML(InputStream in, Writer out, Map<String,String> compareFragments)
      throws ParserConfigurationException, SAXException, IOException {
    DiffHandler handler = new DiffHandler(out, compareFragments, new PSMLDiffer(4000000));
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    parser.parse(in, handler);
    out.flush();
  }

  /**
   * Collect all the PSML files from the folder provided and its sub-folders.
   *
   * @param file      a file/folder to collect
   * @param psml      the list of PSML files already collected.
   */
  private void collectAll(File file, Map<String, File> psml) {
    collectFiles(file, file, psml, true);
  }

  /**
   * Collect all the PSML files from the folder provided and its sub-folders.
   *
   * @param file        a file/folder to collect
   * @param root        path of root folder, used to compute the relative path
   * @param psml        the list of PSML files already collected.
   * @param isRoot      if the current file is the root folder
   */
  private void collectFiles(File file, File root, Map<String, File> psml, boolean isRoot) {
    if (file.isDirectory()) {
      File[] all = file.listFiles();
      for (File f : all) {
        if (!isRoot || !"META-INF".equals(f.getName())) {
          collectFiles(f, root, psml, false);
        }
      }
    } else if (file.isFile() && file.getName().toLowerCase().endsWith(".psml")) {
      psml.put(Files.computeRelativePath(file, root), file);
    }
  }

}
