/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.config.*;
import org.pageseeder.psml.process.config.Images.ImageSrc;
import org.pageseeder.psml.process.math.AsciiMathConverter;
import org.pageseeder.psml.process.util.Files;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;
import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.psml.process.util.XSLTTransformer;
import org.pageseeder.psml.toc.FragmentNumbering;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.psml.toc.XRefLoopException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Perform the process task.
 *
 * @see <a href="https://dev.pageseeder.com/guide/publishing/ant_api/tasks/task_process.html">Task process</a>
 *
 * @author Jean-Baptiste Reure
 */
public final class Process {

  /**
   * UTF-8 charset.
   */
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  /**
   * The size of the byte buffer used to copy files.
   */
  private static final int BUFFER_SIZE = 12 * 1024;

  /**
   * The logger.
   */
  private @Nullable Logger logger = null;

  /**
   * The folder containing the documents to process.
   */
  private @Nullable File src = null;

  /**
   * Where to export it.
   */
  private @Nullable File dest = null;

  /**
   * A list of document filters.
   */
  private boolean generateToc = false;

  /**
   * If only the first error stops the process.
   */
  private boolean failOnError = true;

  /**
   * Whether to change the attribute level to "processed".
   */
  private boolean processed = true;

  /**
   * If source documents are preserved or not
   */
  private boolean preserveSrc = false;

  /**
   * If URL metadata is embedded in link elements
   */
  private boolean embedLinkMetadata = false;

  /**
   * If markdown properties are converted to PSML
   */
  private boolean convertMarkdown = false;

  /**
   * If ascii math is converted to mathjax
   */
  private boolean convertAsciiMath = false;

  /**
   * If katex is converted to mathjax
   */
  private boolean convertTex = false;

  /**
   * If placeholder elements are resolved
   */
  private boolean placeholders = false;

  /**
   * If we should process the XML content.
   */
  private boolean processXML = false;

  /**
   * The manifest creator.
   */
  private @Nullable ManifestCreator manifestCreator = null;

  /**
   * The pretransform details
   */
  private @Nullable XSLTTransformer preTransform = null;

  /**
   * How the xrefs are processed
   */
  private @Nullable XRefsTransclude xrefs = null;

  /**
   * Defines elements to strip
   */
  private @Nullable Strip strip = null;

  /**
   * Defines images to process
   */
  private @Nullable Images imageHandling = null;

  /**
   * Defines the images to process
   */
  private @Nullable IncludesExcludesMatcher imageMatcher = null;

  /**
   * Defines the numbering
   */
  private @Nullable PublicationConfig publicationConfig = null;

  /**
   * Defines the numbering
   */
  private @Nullable String publicationRoot = null;

  /**
   * The posttransform details.
   */
  private @Nullable XSLTTransformer postTransform = null;

  /**
   * The error handling details
   */
  private @Nullable ErrorHandling error = null;

  /**
   * The warning handling details
   */
  private @Nullable WarningHandling warning = null;

  /**
   * @param fail the failOnError to set
   */
  public void setFailOnError(boolean fail) {
    this.failOnError = fail;
  }

  /**
   * @param processed the processed value to set
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
    this.processXML = true;
  }

  /**
   * @param preserve If source documents should be preserved
   */
  public void setPreserveSrc(boolean preserve) {
    this.preserveSrc = preserve;
  }

  /**
   * @param convert If markdown properties are converted to PSML
   */
  public void setConvertMarkdown(boolean convert) {
    this.convertMarkdown = convert;
    this.processXML = true;
  }

  /**
   * @param embed If URL metadata is embedded in link elements
   */
  public void setEmbedLinkMetadata(boolean embed) {
    this.embedLinkMetadata = embed;
    this.processXML = true;
  }

  /**
   * @param convert If ascii math content is converted to Mathjax
   */
  public void setConvertAsciiMath(boolean convert) {
    this.convertAsciiMath = convert;
    this.processXML = true;
  }

  /**
   * @param convert If ascii math content is converted to Mathjax
   */
  public void setConvertTex(boolean convert) {
    this.convertTex = convert;
    this.processXML = true;
  }

  /**
   * @param resolve If placeholder elements are resolved
   */
  public void setPlaceholders(boolean resolve) {
    this.placeholders = resolve;
    this.processXML = true;
  }

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
   * @param manifestDoc the manifest document details
   */
  public void setManifestDoc(@Nullable ManifestDocument manifestDoc) {
    if (manifestDoc != null)
      this.manifestCreator = new ManifestCreator(manifestDoc);
  }

  /**
   * @param transform the transform details
   */
  public void setPreTransform(@Nullable XSLTTransformation transform) {
    if (transform != null)
      this.preTransform = new XSLTTransformer(transform);
  }

  /**
   * @param transform the transform details
   */
  public void setPostTransform(@Nullable XSLTTransformation transform) {
    if (transform != null)
      this.postTransform = new XSLTTransformer(transform);
  }

  /**
   * @param cfg   the publication config
   * @param root  the root file path
   * @param toc   whether to generate TOC
   */
  public void setPublicationConfig(@Nullable PublicationConfig cfg, @Nullable String root, boolean toc) {
    if (cfg == null || root == null)
      throw new IllegalArgumentException("Publication config and root cannot be null");
    this.publicationConfig = cfg;
    this.publicationRoot = root;
    this.generateToc = toc;
    this.processXML = true;
  }

  /**
   * @param matcher the imageMatcher to set
   */
  public void setImageMatcher(IncludesExcludesMatcher matcher) {
    this.imageMatcher = matcher;
  }

  /**
   * @param err defines the error handling.
   */
  public void setError(@Nullable ErrorHandling err) {
    if (err == null) return;
    this.error = err;
    if (this.error.getImageNotFound() || this.error.getXrefNotFound() || this.error.getXrefAmbiguous())
      this.processXML = true;
  }

  /**
   * @param warn defines the error handling.
   */
  public void setWarning(@Nullable WarningHandling warn) {
    if (warn == null) return;
    this.warning = warn;
  }

  /**
   * @param xr the xrefs processing details
   */
  public void setXrefs(@Nullable XRefsTransclude xr) {
    if (xr == null) return;
    this.xrefs = xr;
    this.processXML = true;
  }

  /**
   * @param stripDetails defines the elements to strip
   */
  public void setStrip(@Nullable Strip stripDetails) {
    if (stripDetails == null) return;
    this.strip = stripDetails;
    this.processXML = true;
  }

  /**
   * @param img defines how the images paths are re-written.
   */
  public void setImages(@Nullable Images img) {
    if (img == null) return;
    this.imageHandling = img;
    this.processXML = true;
  }


  /**
   * Processes the source files from the specified source directory and performs the following steps:
   * - Validates input parameters including source and destination directories.
   * - Collects PSML and other files from the source directory for processing.
   * - Optionally creates a manifest document if a manifest creator is specified.
   * - Executes a pre-transform operation if configured.
   * - Processes PSML files, handling their content such as cross-references, images, metadata stripping, and numbering.
   * - Executes a post-transform operation if configured.
   * - Moves processed PSML files and other non-PSML files to the destination directory.
   * - Handles specialized image-handling logic if configured.
   * - Deletes temporary files and folders used during processing.
   * - Optionally removes source files after processing is complete.
   * - Cleans up any changes made in case of a processing exception.
   *
   * <p>This method also uses an image cache to optimize image lookup and processing.
   *
   * @throws ProcessException if any errors occur during processing, particularly if input validation fails,
   *                          temporary folders cannot be removed, or source files cannot be deleted.
   */
  public void process() throws ProcessException {

    // parameters validation
    // src
    if (this.src == null)
      throw new ProcessException("Src must be specified");
    if (!this.src.exists() || !this.src.isDirectory())
      throw new ProcessException("Invalid src location");
    // dest
    if (this.dest == null)
      throw new ProcessException("Dest must be specified");
    if (!this.dest.exists() || !this.dest.isDirectory())
      throw new ProcessException("Invalid destination folder");

    // make sure there's a logger
    if (this.logger == null) this.logger = LoggerFactory.getLogger(Process.class);

    // collect files
    this.logger.debug("Collecting PSML files from {}", this.src.getAbsolutePath());
    Map<String, File> psml = new HashMap<>();
    Map<String, File> rest = new HashMap<>();
    collectAll(this.src, psml, null, rest);

    String processID = "P"+System.currentTimeMillis();
    // start processing
    this.logger.info("Found {} PSML file(s) and {} non PSML file(s)", psml.size(), rest.size());

    // create manifest first
    File manifestFile = null;
    if (this.manifestCreator != null) {
      this.logger.info("Creating manifest document");
      this.manifestCreator.setLog(this.logger);
      manifestFile = this.manifestCreator.createManifest(psml, this.src);
      // add it to the list of PSML source
      if (manifestFile != null) {
        psml.put(Files.computeRelativePath(manifestFile, this.src), manifestFile);
      }
    }

    String tempFolder = System.getProperty("java.io.tmpdir");
    List<File> tempFoldersToDelete = new ArrayList<>();
    File[] ffiles = this.dest.listFiles();
    List<File> originalFilesInDestination = ffiles == null ? new ArrayList<>() : Arrays.asList(ffiles);
    try {
      // run pre transform
      File currentSource = this.src;
      if (this.preTransform != null) {
        this.logger.info("Running Pre Transform with {}", this.preTransform.getXSLT());
        this.preTransform.setLog(this.logger);
        this.preTransform.setPreserveSrc(this.preserveSrc);
        this.preTransform.setFailOnError(this.failOnError);
        boolean useOutputToTempDir = this.processXML || this.postTransform != null;
        File output;
        if (useOutputToTempDir) {
          output = new File(tempFolder, "pretransform-"+processID);
          this.logger.debug("Creating temp output folder {}", output.getAbsolutePath());
          output.mkdirs();
          tempFoldersToDelete.add(output);
        } else {
          output = this.dest;
        }
        this.preTransform.transform(psml, output);
        // reload psml source if needed
        if (useOutputToTempDir) {
          psml.clear();
          collectPSML(output, psml);
          currentSource = output;
        }
      }

      // build an image cache so that their details are not copmuted multiple times
      ImageCache imageCache = new ImageCache(new File(this.src, "META-INF"));
      boolean parseMetadata = this.strip != null;
      // process PSML
      if (this.processXML) {
        this.logger.info("Processing content PSML (XRefs, images, strip, numbering)");
        boolean useOutputToTempDir = this.postTransform != null;
        File output;
        if (useOutputToTempDir) {
          output = new File(tempFolder, "process-"+processID);
          this.logger.debug("Creating temp output folder {}", output.getAbsolutePath());
          output.mkdirs();
          tempFoldersToDelete.add(output);
        } else {
          output = this.dest;
        }
        process(psml, currentSource, output, this.src, imageCache);
        // reload psml source if needed
        if (useOutputToTempDir) {
          psml.clear();
          collectPSML(output, psml);
        }
      }

      // run post transform
      if (this.postTransform != null) {
        this.logger.info("Running Post Transform with {}", this.postTransform.getXSLT());
        this.postTransform.setLog(this.logger);
        this.postTransform.setPreserveSrc(this.preserveSrc);
        this.postTransform.setFailOnError(this.failOnError);
        this.postTransform.transform(psml, this.dest);
      } else if (this.preTransform == null && !this.processXML) {
        // move PSML files manually
        this.logger.info("Moving {} PSML content file(s)", psml.size());
        for (Map.Entry<String, File> fileEntry : psml.entrySet()) {
          moveFile(fileEntry.getValue(), new File(this.dest, fileEntry.getKey()));
        }
      }

      // move other files, including images to maybe a new location
      boolean moveImages = this.imageHandling != null && this.imageHandling.getLocation() != null;
      this.logger.info("Moving {} non PSML file(s)", rest.size());
      if (moveImages) this.logger.info("Moving images to {}", this.imageHandling.getLocation());
      for (Map.Entry<String, File> fileEntry : rest.entrySet()) {
        String relPath = fileEntry.getKey();
        // Strip manifest?
        if ("META-INF/manifest.xml".equals(relPath) && this.strip != null && this.strip.stripManifest()) continue;
        File other = fileEntry.getValue();
        File target;
        if (moveImages && imageCache.isCached(relPath)) {
          String newPath = this.imageHandling.getSrc() == ImageSrc.LOCATION ? relPath : imageCache.getImageNewPath(relPath);
          target = new File(this.imageHandling.getLocation(), newPath);
        } else {
          target = new File(this.dest, relPath);
        }
        moveFile(other, target);
      }

      // remove temp folders
      if (!tempFoldersToDelete.isEmpty()) {
        this.logger.debug("Removing {} temp folder(s)", tempFoldersToDelete.size());
        for (File folder : tempFoldersToDelete) {
          if (!deleteDirectory(folder, true))
            this.logger.warn("Failed to remove temp folder {}", folder.getAbsolutePath());
        }
      }

      // remove the source psml documents
      if (!this.preserveSrc) {
        this.logger.debug("Removing source document(s)");
        if (!deleteDirectory(this.src, false))
          throw new ProcessException("Failed to delete source files");
      } else if (manifestFile != null) {
        // remove manifest file that was created in the source folder
        if (!manifestFile.delete())
          throw new ProcessException("Failed to delete manifest file "+manifestFile.getAbsolutePath());
      }

    } catch (ProcessException ex) {
      // revert created files
      // first manifest
      if (manifestFile != null && manifestFile.exists() && !manifestFile.delete())
        this.logger.warn("Failed to delete manifest file {}", manifestFile.getAbsolutePath());
      // then compare current destination files with original ones
      if (ffiles != null) for (File f : ffiles) {
        if (!originalFilesInDestination.contains(f)) {
          if (f.isFile()) {
            if (!f.delete())
              this.logger.warn("Failed to delete created file {}", f.getName());
          } else if (!deleteDirectory(f, true))
            this.logger.warn("Failed to delete created folder {}", f.getName());
        }
      }
      throw ex;
    }
  }

  /**
   * Process the XRefs using the XML parser.
   *
   * @param psmlFiles     the list of files to process
   * @param source        where the source PSML files are located
   * @param destination   where to save the output files
   * @param binaries      where the binary files are located
   * @param cache         the image metadata cache
   *
   * @throws ProcessException if anything goes wrong
   */
  public void process(Map<String, File> psmlFiles, File source, File destination, File binaries,
                      ImageCache cache) throws ProcessException {
    // make sure we've got something to do
    if (!this.processXML) return;
    AsciiMathConverter.reset();
    IncludesExcludesMatcher xrefsMatcher = this.xrefs == null ? null : this.xrefs.buildMatcher();
    for (Map.Entry<String, File> fileEntry : psmlFiles.entrySet()) {
      String relPath = fileEntry.getKey();
      // log
      this.logger.debug("Processing file {}", relPath);
      // create temp output file
      FileOutputStream fos;
      File tempOutput;
      try {
        tempOutput = File.createTempFile("temp", ".psml");
        fos = new FileOutputStream(tempOutput);
      } catch (IOException e) {
        this.logger.error("Failed to create temp output file: {}", e.getMessage(), e);
        throw new ProcessException("Failed to create temp output file: "+e.getMessage(), e);
      }
      File psml = fileEntry.getValue();
      // create handler
      PSMLProcessHandler handler1 = new PSMLProcessHandler(new OutputStreamWriter(fos, UTF8), null, psml, source, binaries);
      // set error handling details
      handler1.setLogger(this.logger);
      handler1.setFailOnError(this.failOnError);
      handler1.setProcessed(this.processed);
      handler1.setConvertMarkdown(this.convertMarkdown);
      handler1.setConvertAsciiMath(this.convertAsciiMath);
      handler1.setConvertTex(this.convertTex);
      handler1.setPlaceholders(this.placeholders);
      // add xrefs handling details
      List<String> xrefsTypes = null;
      boolean excludeXRefFrag = false;
      boolean onlyXRefFrag = false;

      // make sure the path matches
      if (this.xrefs != null && this.xrefs.getTypes() != null && (xrefsMatcher == null ||
          !xrefsMatcher.hasPatterns() ||
          xrefsMatcher.matches(relPath))) {
        xrefsTypes = Arrays.asList(this.xrefs.getTypes().toLowerCase().split(","));
        excludeXRefFrag = this.xrefs.excludeXRefsInXRefFragment();
        onlyXRefFrag = this.xrefs.onlyXRefsInXRefFragment();
        if (!this.xrefs.getLevels()) {
          this.logger.error("XRef levels option is no longer supported, use publication config instead.");
        }
      }
      handler1.setXRefsHandling(xrefsTypes, excludeXRefFrag, onlyXRefFrag,
              this.error != null && this.error.getXrefNotFound(),
              this.warning == null || this.warning.getXrefNotFound());
      handler1.setEmbedLinkMetadata(this.embedLinkMetadata);
      // add images paths processing details
      boolean embedMetadata = false;
      ImageCache thecache = null;
      ImageSrc imageSrc = ImageSrc.LOCATION;
      String siteprefix = null;
      if (this.imageHandling != null && (this.imageMatcher == null ||
          !this.imageMatcher.hasPatterns() ||
          this.imageMatcher.matches(relPath))) {
        // set proper values
        thecache            = cache;
        imageSrc            = this.imageHandling.getSrc();
        siteprefix          = this.imageHandling.getSitePrefix();
        embedMetadata       = this.imageHandling.isMetadataEmbedded();
      }
      handler1.setImageHandling(thecache, imageSrc,
              this.error != null && this.error.getImageNotFound(),
              this.warning == null || this.warning.getImageNotFound(),
              siteprefix, embedMetadata);
      // add publication config
      try {
        if (this.publicationConfig != null && this.publicationRoot.equals(relPath)) {
          handler1.setPublicationConfig(this.publicationConfig, psml, this.generateToc);
        }
        // add elements stripping details
        handler1.setStrip(this.strip);
        // parse XML input
        XMLUtils.parse(psml, handler1);
      } catch (ProcessException e) {
        if (this.failOnError) throw e;
        else this.logger.error(e.getMessage());
      } catch (Throwable e) {
        throw new ProcessException("Failed to process " + relPath + ": " + e.getMessage(), e);
      } finally {
        try {
          fos.close();
        } catch (IOException ex) {
          throw new ProcessException("Failed to close output stream: "+ex.getMessage(), ex);
        }
      }
      // removed as isDebugEnabled may not be reliable
//      if (this.logger.isDebugEnabled()) {
//        Map<String, Map<String, Integer[]>> ids = handler1.getHierarchyUriFragIDs();
//        Set<String> keys = ids.keySet();
//        for (String key : keys) {
//          this.logger.info("Hierarchy {}", key);
//          Map<String, Integer[]> sub = ids.get(key);
//          Set<String> keys2 = sub.keySet();
//          for (String key2 : keys2) {
//            Integer[] counts = sub.get(key2);
//            this.logger.info("  Found ID {} globally {}, locally {} and embedded {} times", key2, counts[0], counts[1], counts[2]);
//          }
//        }
//      }
      // ok second pass now
      this.logger.debug("Second pass file {}", relPath);
      try {
        File output = new File(destination, relPath);
        // just in case
        output.getParentFile().mkdirs();
        if (!output.exists() && !output.createNewFile())
          throw new ProcessException("Failed to create output file "+output.getAbsolutePath());
        fos = new FileOutputStream(output);
      } catch (IOException e) {
        this.logger.error("Failed to create output file: "+e.getMessage(), e);
        throw new ProcessException("Failed to create output file: "+e.getMessage(), e);
      }
      // create parser
      PSMLProcessHandler2 handler2 = new PSMLProcessHandler2(new OutputStreamWriter(fos, UTF8), relPath);
      handler2.setLogger(this.logger);
      handler2.setFailOnError(this.failOnError);
      handler2.setErrorOnAmbiguous(this.error != null && this.error.getXrefAmbiguous());
      handler2.setWarnOnAmbiguous(this.warning == null || this.warning.getXrefAmbiguous());
      handler2.setHierarchyUriFragIDs(handler1.getHierarchyUriFragIDs());
      handler2.setRelativiseImagePaths(imageSrc == ImageSrc.LOCATION);
      handler2.setProcessed(this.processed);
      handler2.setProcessXRefs(xrefsTypes != null);
      try {
        // generate numbering
        NumberedTOCGenerator numberingAndTOC = handler1.getNumberedTOCGenerator();
        if (numberingAndTOC != null) {
          numberingAndTOC.updatePublication();
          numberingAndTOC.setFragmentNumbering(
              new FragmentNumbering(numberingAndTOC.publicationTree(), this.publicationConfig));
          handler2.setPublicationConfig(this.publicationConfig, numberingAndTOC, this.generateToc);
          //Map<String,Prefix> prefixes = numberingAndTOC.fragmentNumbering().getAllPrefixes();
          //String result = prefixes.entrySet()
          //    .stream().sorted(Map.Entry.comparingByKey())
          //    .map(entry -> entry.getKey() + " - " + entry.getValue())
          //    .collect(Collectors.joining("\n"));
          //System.out.println(result);
        }
        // parse XML input
        XMLUtils.parse(tempOutput, handler2);
      } catch (XRefLoopException e) {
        throw new ProcessException(e.getMessage(), e);
      } catch (ProcessException e) {
        if (this.failOnError) throw e;
        else this.logger.error(e.getMessage());
      } finally {
        // try to remove temp file
        tempOutput.delete();
        // close stream
        try {
          fos.close();
        } catch (IOException ex) {
          throw new ProcessException("Failed to close output stream: "+ex.getMessage(), ex);
        }
      }
    }
    // log
    this.logger.debug("Complete");
  }

  /*
   * ===========================================================================
   * File util classes
   * ===========================================================================
   */
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
        try (FileInputStream fis = new FileInputStream(from); FileOutputStream fos = new FileOutputStream(to)) {
          int read;
          byte[] buffer = new byte[BUFFER_SIZE];
          while ((read = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
          }
        }
      } catch (IOException ex) {
        throw new ProcessException("Failed to copy file "+from.getAbsolutePath()+" to "+to.getAbsolutePath(), ex);
      }
    } else {
      // move file
      try {
        java.nio.file.Files.move(from.toPath(), to.toPath());
      } catch (IOException ex) {
        throw new ProcessException("Failed to move file "+from.getAbsolutePath()+" to "+to.getAbsolutePath(), ex);
      }
    }
  }

  /**
   * Collect all the files from the folder provided and its sub-folders.
   *
   * @param file      a file/folder to collect
   * @param psml      the list of PSML files already collected.
   */
  private void collectPSML(File file, Map<String, File> psml) {
    collectFiles(file, file, psml, null, null, true);
  }

  /**
   * Collect all the files from the folder provided and its sub-folders.
   *
   * @param file      a file/folder to collect
   * @param psml      the list of PSML files already collected.
   * @param metadata  the list of metadata PSML files already collected.
   * @param others    the list of non PSML files already collected.
   */
  private void collectAll(File file, Map<String, File> psml, Map<String, File> metadata, Map<String, File> others) {
    collectFiles(file, file, psml, metadata, others, true);
  }

  /**
   * Collect all the files from the folder provided and its sub-folders.
   *
   * @param file        a file/folder to collect
   * @param root        path of root folder, used to compute the relative path
   * @param psml        the list of PSML files already collected.
   * @param metadata    the list of metadata PSML files already collected.
   * @param others      the list of non PSML files already collected.
   * @param isRoot      if the current file is the root folder
   */
  private void collectFiles(File file, File root, Map<String, File> psml,
                            @Nullable Map<String, File> metadata,
                            @Nullable Map<String, File> others, boolean isRoot) {
    if (file.isDirectory()) {
      File[] all = file.listFiles();
      if (all != null) for (File f : all) {
        if (isRoot && "META-INF".equals(f.getName()) && metadata != null) {
          collectFiles(f, root, metadata, metadata, others, false);
        } else {
          collectFiles(f, root, psml, metadata, others, false);
        }
      }
    } else if (file.isFile() && file.getName().toLowerCase().endsWith(".psml")) {
      psml.put(Files.computeRelativePath(file, root), file);
    } else if (others != null) {
      others.put(Files.computeRelativePath(file, root), file);
    }
  }

  /**
   * Remove a directory and all its sub-directories.
   *
   * @param dir        the root directory
   * @param deleteSelf if the root directory should also be deleted.
   *
   * @return true if all were removed
   */
  private boolean deleteDirectory(File dir, boolean deleteSelf) {
    for (File child : dir.listFiles()) {
      if (child.isFile()) {
        if (!child.delete()) return false;
      } else {
        if (!deleteDirectory(child, true)) return false;
      }
    }
    if (!deleteSelf) return true;
    return dir.delete();
  }

}
