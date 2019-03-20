/*
 *  Copyright (c) 2019 Allette Systems pty. ltd.
 */
package org.pageseeder.psml.split;

import org.pageseeder.psml.util.XSLT;

import javax.xml.transform.Templates;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Split a PSML document into multiple documents.
 *
 * @author Philip Rutherford
 */
public final class PSMLSplitter {

  /**
   * The builder
   */
  private final Builder _builder;

  /**
   * A writer to store the log
   */
  private final Writer _log;

  private PSMLSplitter(Builder producer) {
    this(producer, new StringWriter());
  }

  private PSMLSplitter(Builder producer, Writer log) {
    if (producer.source() == null) { throw new NullPointerException("source is null"); }
    if (producer.destination() == null) { throw new NullPointerException("destination is null"); }
    if (producer.config() == null) { throw new NullPointerException("config is null"); }
    this._builder = producer;
    this._log = log;
  }

  /**
   * @return the log in string
   */
  public String getLog() {
    if (this._log != null) {
      try {
        this._log.flush();
        this._log.close();
      } catch (IOException e) {
        return "";
      }
      return this._log.toString();
    } else {
      return "";
    }
  }

  /**
   * Split the psml.
   *
   * @throws IOException
   */
  public void process() throws IOException {

    // Find destination folder and filename
    File source = this._builder.source();
    File destination;
    String name;
    if (this._builder.destination().isFile()) {
      destination = this._builder.destination().getParentFile();
      name = this._builder.destination().getName();
    } else {
      destination = this._builder.destination();
      name = source.getName();
    }

    // Ensure that output folder exists
    if (!destination.exists()) {
      destination.mkdirs();
    }

    // Parse templates
    Templates pre = XSLT.getTemplatesFromResource("org/pageseeder/psml/split/pre-split.xsl");
    Templates split = XSLT.getTemplatesFromResource("org/pageseeder/psml/split/split.xsl");
    String outuri = destination.toURI().toString();

    // Move the media files
    log("Moving media files");
    String mediaFolderName = this._builder.media() == null ? "images" : this._builder.media();
    File mediaFolder = new File(source.getParentFile(), mediaFolderName);
    if (mediaFolder.exists()) {
      mediaFolder.renameTo(new File(destination, mediaFolderName));
    }

    // Initiate parameters
    Map<String, String> parameters = new HashMap<>();
    parameters.put("_outputfolder", outuri);
    parameters.put("_outputfilename", name);
    parameters.put("_mediafoldername", mediaFolderName);
    parameters.put("_configfileurl", this._builder.config().toURI().toString());

    // Add custom parameters
    parameters.putAll(this._builder.params());

    // Map fragments to new locations
    log("Preprocessing PSML (this may take several minutes)");
    File pre_split = new File(this._builder.working(), "pre-split.xml");
    XSLT.transform(source, pre_split, pre, parameters);

    // Split files
    log("Splitting PSML");
    XSLT.transform(pre_split, new File(destination, name), split, parameters);

  }

  // Helpers
  // ----------------------------------------------------------------------------------------------

  private void log(String log) throws IOException {
    this._log.append(log).append("\n");
  }

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
     *  A writer to store the log
     */
    private Writer log;

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
     * @param log the Writer to store the log
     * @return {@link Builder}
     */
    public Builder log(Writer log) {
      this.log = log;
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
     * @return
     */
    public Builder params(Map<String, String> params) {
      this.params = params;
      return this;
    }

    /**
     * @return the PSMLSplitter
     */
    public PSMLSplitter build() {
      if (this.log != null) {
        return new PSMLSplitter(this, this.log);
      } else {
        return new PSMLSplitter(this);
      }
    }
  }

}
