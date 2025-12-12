/*
 *  Copyright (c) 2019 Allette Systems pty. ltd.
 */
package org.pageseeder.psml.split;

import org.pageseeder.psml.util.XSLT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Templates;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Split a PSML document into multiple documents.
 *
 * @author Philip Rutherford
 */
public final class PSMLSplitter {

  /**
   * The builder
   */
  private final Builder builder;

  /**
   * A writer to store the log
   */
  private final Logger logger;

  /**
   * Constructs a new instance of PSMLSplitter.
   *
   * <p>The source, destination, and config files must not be null.</p>
   *
   * @param producer The builder instance containing necessary configuration.
   * @param log      The logger used for logging messages during the operation of PSMLSplitter.
   *                 If not explicitly provided, a default logger will be used.
   * @throws NullPointerException If any of the required fields from the builder (source, destination,
   *                              or config) are null.
   */
  private PSMLSplitter(Builder producer, Logger log) {
    Objects.requireNonNull(producer.source(), "source is null");
    Objects.requireNonNull(producer.destination(), "destination is null");
    Objects.requireNonNull(producer.config(), "config is null");
    this.builder = producer;
    this.logger = Objects.requireNonNull(log);
  }

  /**
   * Split the psml.
   *
   * @throws IOException If an error occurred while moving the file
   */
  public void process() throws IOException {

    // Find destination folder and filename
    File source = this.builder.source();
    File destination;
    String name = this.builder.destination().getName();
    if (name.endsWith(".psml")) {
      destination = this.builder.destination().getParentFile();
    } else {
      destination = this.builder.destination();
      name = source.getName();
    }

    // Ensure that output folder exists
    if (!destination.exists()) {
      destination.mkdirs();
    }

    // Parse templates
    Templates pre1 = XSLT.getTemplatesFromResource("org/pageseeder/psml/split/pre-split1.xsl");
    Templates pre2 = XSLT.getTemplatesFromResource("org/pageseeder/psml/split/pre-split2.xsl");
    Templates split = XSLT.getTemplatesFromResource("org/pageseeder/psml/split/split.xsl");
    String outuri = destination.toURI().toString();

    // Move the media files
    this.logger.info("PSML Splitter: Moving media files");
    String mediaFolderName = this.builder.media() == null ? "images" : this.builder.media();
    File mediaFolder = new File(source.getParentFile(), mediaFolderName);
    if (mediaFolder.exists()) {
      java.nio.file.Files.move(mediaFolder.toPath(), new File(destination, mediaFolderName).toPath());
    }

    // Initiate parameters
    Map<String, String> parameters = new HashMap<>();
    parameters.put("_outputfolder", outuri);
    parameters.put("_outputfilename", name);
    parameters.put("_mediafoldername", mediaFolderName);
    parameters.put("_configfileurl", this.builder.config().toURI().toString());

    // Add custom parameters
    parameters.putAll(this.builder.params());

    // Pre-split 1
    this.logger.info("PSML Splitter: First pre-process");
    File preSplit1 = new File(this.builder.working(), "pre-split1.xml");
    XSLT.transform(source, preSplit1, pre1, parameters);

    // Pre-split 2
    this.logger.info("PSML Splitter: Second pre-process");
    File preSplit2 = new File(this.builder.working(), "pre-split2.xml");
    XSLT.transform(preSplit1, preSplit2, pre2, parameters);

    // Split files
    this.logger.info("PSML Splitter: Splitting PSML");
    XSLT.transform(preSplit2, new File(destination, name), split, parameters);

  }

  // Helpers
  // ----------------------------------------------------------------------------------------------

  public static class Builder {

    /**
     * The PageSeeder documents to export.
     * <p>The source should point to the main PSML document.
     *
     */
    private File source;

    /**
     * The Word document to generate.
     */
    private File destination;

    /**
     * The name of the working directory
     */
    private File working;

    /**
     * The configuration.
     */
    private File config;

    /**
     * The media files folder location.
     */
    private String media;

    /**
     * List of custom parameters specified that can be specified from the command-line
     */
    private Map<String, String> params;

    /**
     *  For logging
     */
    private Logger logger;

    /**
     * @return the srouce
     */
    private File source() {
      return this.source;
    }

    /**
     * @return destination
     */
    private File destination() {
      if (this.destination == null) {
        this.destination = new File(this.source.getParentFile(), "output.psml");
      }
      return this.destination;
    }

    /**
     * @return working
     */
    private File working() {
      if (this.working == null) {
        String tmp = "split-" + System.currentTimeMillis();
        this.working = new File(System.getProperty("java.io.tmpdir"), tmp);
      }
      if (!this.working.exists()) {
        this.working.mkdirs();
      }
      return this.working;
    }

    /**
     * @return the configuration file
     */
    private File config() {
      // check whether the file is exist
      if (this.config != null && this.config.exists()) {
        return this.config;
      } else {
        return null;
      }
    }

    /**
     * @return the media folder
     */
    private String media() {
      return this.media;
    }

    /**
     * @return the custom parameters for XSLT
     */
    private Map<String, String> params() {
      if (this.params == null) {
        this.params = new HashMap<>();
      }
      return this.params;
    }

    /**
     * @param log  for logging
     * @return {@link Builder}
     */
    public Builder log(Logger log) {
      this.logger = log;
      return this;
    }

    /**
     * @param source set the source
     * @return {@link Builder}
     */
    public Builder source(File source) {
      this.source = source;
      return this;
    }

    /**
     * @param destination set the destination
     * @return {@link Builder}
     */
    public Builder destination(File destination) {
      this.destination = destination;
      return this;
    }

    /**
     * @param working set the working folder
     * @return {@link Builder}
     */
    public Builder working(File working) {
      this.working = working;
      return this;
    }

    /**
     * @param config set the configuration file
     * @return {@link Builder}
     */
    public Builder config(File config) {
      this.config = config;
      return this;
    }

    /**
     * @param media the media folder (default is images)
     * @return {@link Builder}
     */
    public Builder media(String media) {
      this.media = media;
      return this;
    }

    /**
     * @param params the custom XSLT parameters
     * @return this builder
     */
    public Builder params(Map<String, String> params) {
      this.params = params;
      return this;
    }

    /**
     * @return the PSMLSplitter
     */
    public PSMLSplitter build() {
      if (this.logger != null) {
        return new PSMLSplitter(this, this.logger);
      } else {
        return new PSMLSplitter(this, LoggerFactory.getLogger(PSMLSplitter.class));
      }
    }
  }

}
