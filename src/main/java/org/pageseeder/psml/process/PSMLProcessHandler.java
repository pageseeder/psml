/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import org.pageseeder.psml.md.BlockParser;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.process.XRefTranscluder.InfiniteLoopException;
import org.pageseeder.psml.process.XRefTranscluder.TooDeepException;
import org.pageseeder.psml.process.XRefTranscluder.XRefNotFoundException;
import org.pageseeder.psml.process.config.Images.ImageSrc;
import org.pageseeder.psml.process.config.Strip;
import org.pageseeder.psml.process.math.AsciiMathConverter;
import org.pageseeder.psml.process.math.TexConverter;
import org.pageseeder.psml.process.util.Files;
import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.psml.toc.DocumentTree;
import org.pageseeder.psml.toc.DocumentTreeHandler;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.psml.toc.PublicationTree;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Handle embedding and transcluding content and other processes.
 *
 * @author Jean-Baptiste Reure
 * @author Philip Rutherford
 */
public final class PSMLProcessHandler extends DefaultHandler {

  /**
   * Check depth message
   */
  private static final String CHECK_DEPTH = " (check export depth): ";
  /**
   * The logger object
   */
  private Logger logger = null;

  /**
   * The XML writer where XML content is stored.
   */
  private Writer xml;

  /**
   * The parent handler if current content is transcluded.
   */
  private PSMLProcessHandler parent;

  /**
   * Handle the transclusion of XRefs.
   */
  private final XRefTranscluder transcluder;

  /**
   * Details of elements/attributes to strip
   */
  private Strip strip = null;

  /**
   * If a non fatal error will stop the parsing.
   */
  private boolean failOnError = false;

  /**
   * Whether to change attribute level to "processed" and URL decode @href and @src
   */
  private boolean processed = true;

  /**
   * If markdown properties are converted to PSML
   */
  private boolean convertMarkdown = false;

  /**
   * If ascii content is converted to MathJax
   */
  private boolean convertAsciiMath = false;

  /**
   * If katex content is converted to MathJax
   */
  private boolean convertTex = false;

  /**
   * If an error should be logged when an image was not found.
   */
  private boolean logImageNotFound = false;

  /**
   * If an error should be logged when an xref reference was not found.
   */
  private boolean logXRefNotFound = false;

  /**
   * if alternate iamge xrefs are handled like images
   */
  private boolean alternateImageXRefs = false;

  /**
   * Whether to embed image metadata
   */
  private boolean embedImageMetadata = false;

  /**
   * Whether to resolve placeholder elements
   */
  private boolean placeholders = false;

  /**
   * How images src should be rewritten.
   */
  private ImageSrc imageSrc = ImageSrc.LOCATION;

  /**
   * Helper to compute numbering and TOC.
   */
  private NumberedTOCGenerator numberingAndTOC = null;

  /**
   * Config for publication.
   */
  private PublicationConfig publicationConfig = null;

  /**
   * Image cache where URI details for images are loaded from.
   */
  private ImageCache imageCache = null;

  /**
   * Site prefix, used to rewrite images paths to permalink
   * [siteprefix]/uri/[uriid].[extension].
   */
  private String sitePrefix = null;

  /**
   * The relative path of the parent folder (used to compute relative paths).
   */
  private String parentFolderRelativePath;

  /**
   * The source PSML file.
   */
  private final File sourceFile;

  /**
   * The folder where the PSML files are (to resolve relative paths).
   */
  private final File psmlRoot;

  /**
   * The foldet containing binary files (for images and xrefs to binary files).
   */
  private final File binaryRepository;

  /**
   * If the TOC should be generated
   */
  private boolean generateTOC = false;

  /**
   * A specific fragment to load, if it is not null only this fragment's content
   * will be loaded.
   */
  private String fragmentToLoad = null;

  /**
   * The URI ID of this document.
   */
  private String uriID = null;

  /**
   * The markdown or ascii content to convert.
   */
  private StringBuilder convertContent = null;

  /**
   * The convert flag.
   */
  private boolean convertingAsciimath = false;

  /**
   * Number of times this URI has appeared.
   */
  private Integer uriCount = null;

  /**
   * All URI IDs in the document (including transcluded docs).
   */
  private Map<String, Integer> allUriIDs = new HashMap<>();

  /**
   * Number of URI/frag IDs in the each document sub-hierarchy <root n_uriid,
   * <uriid[_fragid], [global count, local count, embed count]>
   */
  private Map<String, Map<String, Integer[]>> hierarchyUriFragIDs = new HashMap<>();

  /**
   * Publication metadata (for placeholders)
   */
  private Map<String,String> publicationMetadata = null;

  /**
   * Document metadata (for placeholders)
   */
  private Map<String,String> documentMetadata = new HashMap<>();

  /**
   * If parsing root or in hierarchy of all embed XRefs
   */
  private boolean inEmbedHierarchy = true;

  /**
   * If the XML declaration should be included.
   */
  private boolean includeXMLDeclaration = true;

  /**
   * Current state.
   */
  private Stack<String> elements = new Stack<>();

  /**
   * If the XML should be currently ignored.
   */
  private boolean ignore = false;

  /**
   * If the XML should be currently ignored because of a fragment loading.
   */
  private boolean inRequiredFragment = true;

  /**
   * If the parser is currently transcluding an XRef.
   */
  private boolean inTranscludedXRef = false;

  /**
   * If the parser is currently inside an alternate an XRef.
   */
  private boolean inAlternateXRef = false;

  /**
   * If the parser is currently in transcluded content.
   */
  private boolean inTranscludedContent = false;

  /**
   * If the current element is an XRef and it should be replaced by its
   * contents.
   */
  private boolean stripCurrentXRefElement = false;

  /**
   * The level to increase the headings by (for transclusions).
   */
  private int level = 0;

  /**
   * The current fragment being processed.
   */
  private String currentFragment = null;

  /**
   * The resolved content for the current placeholder
   */
  private String placeholderContent = null;

  /**
   * Values for pre-transcluded content handling (exported using process-publication=true)
   */
  private int preXrefLevel = 0;
  private String preFragment = null;
  private boolean inPreTranscluded = false;
  private String preUriID = null;
  private Integer preUriCount = null;
  private boolean preEmbedHierarchy = false;


  /**
   * @param out            where the resulting XML should be written.
   * @param file           the source file.
   * @param root           the root folder of the PSML files (used to compute relative
   *                       paths).
   * @param binariesFolder the folder containing binary files (to resolve xrefs to binary
   *                       files and images).
   */
  public PSMLProcessHandler(Writer out, PSMLProcessHandler parent, File file, File root,
                            File binariesFolder) {
    this.xml = out;
    this.parent = parent;
    this.sourceFile = file;
    this.psmlRoot = root;
    this.binaryRepository = binariesFolder;
    this.parentFolderRelativePath = Files.computeRelativePath(file.getParentFile(), root);
    this.transcluder = new XRefTranscluder(this);
    this.transcluder.addParentFile(file, "default");
  }

  /**
   * @param fragment the fragment to load
   */
  public void setFragment(String fragment) {
    this.fragmentToLoad = fragment;
  }

  /**
   * @param uriIDs the list of URI IDs
   */
  public void setAllUriIDs(Map<String, Integer> uriIDs) {
    this.allUriIDs = uriIDs;
  }

  /**
   * @return the list of URI IDs.
   */
  public Map<String, Integer> getAllUriIDs() {
    return this.allUriIDs;
  }

  /**
   * @param uriFragIDs Map of number of URI/frag IDs in the each document sub-hierarchy
   */
  public void setHierarchyUriFragIDs(Map<String, Map<String, Integer[]>> uriFragIDs) {
    this.hierarchyUriFragIDs = uriFragIDs;
  }

  /**
   * @return Map of number of URI/frag IDs in the each document sub-hierarchy
   */
  public Map<String, Map<String, Integer[]>> getHierarchyUriFragIDs() {
    return this.hierarchyUriFragIDs;
  }

  /**
   * @return Numbered TOC Generator
   */
  public NumberedTOCGenerator getNumberedTOCGenerator() {
    return this.numberingAndTOC;
  }

  /**
   * @return Publication config
   */
  public PublicationConfig getPublicationConfig() {
    return this.publicationConfig;
  }

  /**
   * @return Publication metadata
   */
  public Map<String,String> getPublicationMetadata() {
    return this.publicationMetadata;
  }

  /**
   *
   * @return Whether placeholders should be resolved.
   */
  public boolean resolvePlaceholders() {
    return this.placeholders;
  }

  /**
   * @param uriid the URI ID.
   */
  public void setURIID(String uriid) {
    this.uriID = uriid;
  }

  /**
   * @param count the Number of times this URI has appeared
   */
  public void setURICount(Integer count) {
    this.uriCount = count;
  }

  /**
   * @param log the logger facade.
   */
  public void setLogger(Logger log) {
    this.logger = log;
  }

  /**
   * @param failonerror if the first error should stop the parsing
   */
  public void setFailOnError(boolean failonerror) {
    this.failOnError = failonerror;
  }

  /**
   * @param processed the processed value to set
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  /**
   * @param convert if markdown properties are converted to PSML
   */
  public void setConvertMarkdown(boolean convert) {
    this.convertMarkdown = convert;
  }

  /**
   * @param embed if URL metadata is embedded in link elements
   */
  public void setEmbedLinkMetadata(boolean embed) {
    this.transcluder.setTranscludeLinks(embed);
  }

  /**
   * @param convert if ascii math content converted to MathJax
   */
  public void setConvertAsciiMath(boolean convert) {
    this.convertAsciiMath = convert;
  }

  /**
   * @param convert if katex content converted to MathJax
   */
  public void setConvertTex(boolean convert) {
    this.convertTex = convert;
  }

  /**
   * @param include whether or not to output the XML declaration
   */
  public void setIncludeXMLDeclaration(boolean include) {
    this.includeXMLDeclaration = include;
  }

  /**
   * @param embed if in hierarchy of all embed XRefs
   */
  public void setInEmbedHierarchy(boolean embed) {
    this.inEmbedHierarchy = embed;
  }

  /**
   * @param transclude if in transcluded content
   */
  public void setInTranscludedContent(boolean transclude) {
    this.inTranscludedContent = transclude;
  }

  /**
   * @param lvl the level to increase the headings by (for transclusions).
   */
  public void setLevel(int lvl) {
    this.level = lvl;
  }

  /**
   * @param xrefTypes           List of XRefs types to transclude
   * @param excludeXRefFragment If the xrefs in an xref-fragment are ignored.
   * @param onlyXRefFrament     If only the xrefs in an xref-fragment are included.
   * @param logxrefnotfound     If an error is logged when an XRef's target is not resolved
   */
  public void setXRefsHandling(List<String> xrefTypes, boolean excludeXRefFragment,
                               boolean onlyXRefFrament, boolean logxrefnotfound) {
    this.transcluder.addXRefsTypes(xrefTypes);
    this.transcluder.setXRefFragmentHandling(excludeXRefFragment, onlyXRefFrament);
    this.logXRefNotFound = logxrefnotfound;
  }

  /**
   * Add URI or frag ID to this uri and above in hierarchy
   *
   * @param uriid  the uri id
   * @param fragid the fragment id (may be null)
   * @param embed  whether hierarchy has all embed XRefs
   */
  public void addUriFragID(String uriid, String fragid, boolean embed) {
    addKeyUriFragID(this.uriCount + "_" + this.uriID, uriid, fragid, embed);
    // if not root add to parent
    if (this.parent != null && !this.inAlternateXRef)
      this.parent.addUriFragID(uriid, fragid, embed);
  }

  /**
   * Add URI or frag ID to this uri and above in hierarchy
   *
   * @param key    [this.uriCount]_[this.uriID]
   * @param uriid  the uri id
   * @param fragid the fragment id (may be null)
   * @param embed  whether hierarchy has all embed XRefs
   */
  public void addKeyUriFragID(String key, String uriid, String fragid, boolean embed) {
    // can't XRef to alternate content
    if (this.inAlternateXRef) return;
    Map<String, Integer[]> sub_hierarchy = this.hierarchyUriFragIDs.get(key);
    Integer global_count = this.allUriIDs.get(uriid);
    Integer[] counts = null;
    if (sub_hierarchy == null) {
      sub_hierarchy = new HashMap<>();
      this.hierarchyUriFragIDs.put(key, sub_hierarchy);
    } else
      counts = sub_hierarchy.get(uriid + (fragid == null ? "" : ("-" + fragid)));
    if (counts == null) {
      counts = new Integer[]{global_count, 1, embed ? 1 : 0};
      sub_hierarchy.put(uriid + (fragid == null ? "" : ("-" + fragid)), counts);
    } else {
      counts[1] = counts[1] + 1;
      if (embed) {
        // if first embed use this as the target
        if (counts[2] == 0) {
          counts[0] = global_count;
        }
        counts[2] = counts[2] + 1;
      }
    }
  }

  /**
   * @return the transcluder
   */
  public XRefTranscluder getTranscluder() {
    return this.transcluder;
  }

  /**
   * @param strp Details of elements/attributes to strip
   */
  public void setStrip(Strip strp) {
    this.strip = strp;
  }

  /**
   * @param config the publication config to set
   * @param root   the publication root file
   *
   * @throws ProcessException  if problem parsing root file
   */
  public void setPublicationConfig(PublicationConfig config, File root, boolean toc) throws ProcessException {
    // process transclussions
    XMLStringWriter out = new XMLStringWriter(NamespaceAware.No);
    TransclusionHandler thandler = new TransclusionHandler(out, "default", true, this);
    XMLUtils.parse(root, thandler);
    // load metadata now so it can be used in compare content
    this.publicationMetadata = thandler.getPublicationMetadata();
    // parse document tree
    DocumentTreeHandler tochandler = new DocumentTreeHandler();
    XMLUtils.parse(new InputSource(new StringReader(out.toString())), tochandler, null, null);
    DocumentTree tree = tochandler.get();
    if (tree != null) {
      tree = tree.normalize(config.getTocTitleCollapse());
      this.numberingAndTOC = new NumberedTOCGenerator(new PublicationTree(tree));
      this.publicationConfig = config;
      this.generateTOC = toc;
    }
  }

  /**
   * Set the image handling details. Note that if one of the flag is true, the
   * cache cannot be null and site prefix must be specified for permalinks.
   *
   * @param cache         where URI details for images are loaded from.
   * @param src           how image src should be rewritten
   * @param logNotFound   if the image not found should be logged as an error
   * @param siteprefix    site prefix, used to rewrite images paths to permalink
   * @param embedMetadata if images are transcluded (metadata embedded)
   */
  public void setImageHandling(ImageCache cache, ImageSrc src, boolean logNotFound,
                               String siteprefix, boolean embedMetadata) {
    // make sure the required bits are there
    if (src != ImageSrc.LOCATION && cache == null)
      throw new IllegalArgumentException("Required images metadata cache is missing");
    if (src == ImageSrc.PERMALINK && siteprefix == null)
      throw new IllegalArgumentException("Site prefix missing");
    // set flags
    this.imageSrc = src;
    this.logImageNotFound = logNotFound;
    this.imageCache = cache;
    this.sitePrefix = siteprefix;
    this.embedImageMetadata = embedMetadata;
    // set image transclusion
    this.transcluder.setTranscludeImages(embedMetadata);
  }

  /**
   * @param resolve  if placeholder elements are resolved
   */
  public void setPlaceholders(boolean resolve) {
    this.placeholders = resolve;
  }

  /**
   * @param metadata  the publication metadata
   */
  public void setPublicationMetadata(Map<String,String> metadata) {
    this.publicationMetadata = metadata;
  }

  /**
   * @param metadata  the document metadata
   */
  public void setDocumentMetadata(Map<String,String> metadata) {
    this.documentMetadata = metadata;
  }

  /**
   * @return the binaryRepository
   */
  public File getBinaryRepository() {
    return this.binaryRepository;
  }

  /**
   * @return the psmlRoot
   */
  public File getPSMLRoot() {
    return this.psmlRoot;
  }

  /**
   * @return the source file
   */
  public File getSourceFile() {
    return this.sourceFile;
  }


  /**
   * @return whether parser is inside an alternate XRef
   */
  public boolean inAlternateXRef() {
    return this.inAlternateXRef;
  }

  /**
   * @return the parentFolderRelativePath
   */
  public String getParentFolderRelativePath() {
    return this.parentFolderRelativePath;
  }

  /**
   * @return the logger
   */
  public Logger getLogger() {
    return this.logger;
  }

  // --------------------------------- Content Handler methods
  // --------------------------------------------

  @Override
  public void startDocument() throws SAXException {
    // start to write something just in case there's an IO error
    if (this.includeXMLDeclaration)
      try {
        this.xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      } catch (IOException ex) {
        throw new SAXException("Failed to write XML declaration ", ex);
      }
    // check for fragent to load
    if (this.fragmentToLoad != null) {
      this.inRequiredFragment = false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    boolean noNamespace = uri == null || uri.isEmpty();
    // load URI ID of root document
    if (this.uriID == null && noNamespace && "document".equals(qName)) {
      this.uriID = atts.getValue("id");
      this.uriCount = 1;
      this.allUriIDs.put(this.uriID, 1);
      addUriFragID(this.uriID, null, this.inEmbedHierarchy);
    }
    // if URL metadata document, set parent to root
    String dad = this.elements.isEmpty() ? null : this.elements.peek();
    if ("documentinfo".equals(dad) && "uri".equals(qName) &&
            "true".equals(atts.getValue("external"))) {
      this.parentFolderRelativePath = "";
    }
    // if pre-transcluded content update URI counts
    if (this.preXrefLevel == 1 && !this.inPreTranscluded) {
      this.inPreTranscluded = true;
      // update uri count
      Integer count = this.allUriIDs.get(this.uriID);
      if (count == null) count = 0;
      if (!this.inAlternateXRef) {
        count++;
        this.allUriIDs.put(this.uriID, count);
      }
      this.uriCount = count;

      if (!"default".equals(this.preFragment)) {
        addUriFragID(this.uriID, this.preFragment, false);
        addKeyUriFragID(this.preUriCount + "_" + this.preUriID, this.uriID, this.preFragment, false);
        write("<document-fragment uriid=\"" + XMLUtils.escapeForAttribute(this.uriID) + "\">");
      } else {
        addUriFragID(this.uriID, null, false);
        addKeyUriFragID(this.preUriCount + "_" + this.preUriID, this.uriID, null, false);
      }
    }
    // if fragment loading add temporary document element (stripped out later)
    if (this.fragmentToLoad != null && "document".equals(qName)) {
      write("<document-fragment uriid=\"" + XMLUtils.escapeForAttribute(this.uriID) + "\">");
    }
    // fragment loading?
    boolean isFragment = noNamespace && isFragment(qName);
    if (!this.inRequiredFragment) {
      if ((isFragment && this.fragmentToLoad != null
          && this.fragmentToLoad.equals(atts.getValue("id")) && !this.elements.contains("compare"))
          || (noNamespace && this.fragmentToLoad != null && "locator".equals(qName)
          && this.fragmentToLoad.equals(atts.getValue("fragment")))) {
        this.inRequiredFragment = true;
      } else {
        return;
      }
    }
    boolean isXRef = noNamespace && ("blockxref".equals(qName) || "xref".equals(qName));
    boolean isReverseXRef = noNamespace && "reversexref".equals(qName);
    boolean isImage = noNamespace && "image".equals(qName);
    boolean isLink = noNamespace && "link".equals(qName);
    boolean isMetadataProperty  = noNamespace && "property".equals(qName) && "properties".equals(this.elements.peek());
    // currently stripping?
    if (this.ignore)
      return;
    // element to strip?
    if (noNamespace && shouldStripElement(qName)) {
      this.ignore = true;
      return;
    }
    // check for xref to replace by its contents
    if (isXRef && this.strip != null) {
      if (shouldStripXRef(qName, atts)) {
        return;
      }
    }
    // convert ascii math inline labels
    if ((this.convertAsciiMath && noNamespace && "inline".equals(qName) && "asciimath".equals(atts.getValue("label"))) ||
        this.convertTex && noNamespace && "inline".equals(qName) && "tex".equals(atts.getValue("label"))) {
      this.convertContent = new StringBuilder();
      this.convertingAsciimath = "asciimath".equals(atts.getValue("label"));
      return;
    } else if ((this.convertAsciiMath && noNamespace && "media-fragment".equals(qName) && "text/asciimath".equals(atts.getValue("mediatype"))) ||
        this.convertTex && noNamespace && "media-fragment".equals(qName) && "application/x-tex".equals(atts.getValue("mediatype"))) {
      String id = (this.uriID == null || !this.transcluder.isTranscluding()) ?
          atts.getValue("id") : (this.uriID + "-" + atts.getValue("id"));
      write("<media-fragment id=\""+XMLUtils.escapeForAttribute(id)+"\" mediatype=\"application/mathml+xml\">");
      this.convertContent = new StringBuilder();
      this.convertingAsciimath = "text/asciimath".equals(atts.getValue("mediatype"));
      return;
    }

    // resolve placeholders
    if (this.placeholders) {
      // ignore diff elements inside placeholder when resolving
      if (this.placeholderContent != null) return;
      // if root document is a publication, start collecting metadata
      if ("publication".equals(qName) && this.parent == null) {
        this.publicationMetadata = new HashMap<>();
        this.documentMetadata = null;
        // if metadata property, collect metadata
      } else if (isMetadataProperty && !this.inTranscludedContent && atts.getValue("name") != null &&
          (atts.getValue("datatype") == null || "text".equals(atts.getValue("datatype")) ||
          "date".equals(atts.getValue("datatype")) || "datetime".equals(atts.getValue("datatype"))) &&
          (atts.getValue("count") == null || "1".equals(atts.getValue("count"))) &&
          atts.getValue("multiple") == null) {
        String value = atts.getValue("value") == null ? "" : atts.getValue("value");
        if (this.documentMetadata == null) {
          this.publicationMetadata.put(atts.getValue("name"), value);
        } else {
          this.documentMetadata.put(atts.getValue("name"), value);
        }
        // if placeholder, try to resolve
      } else if ("placeholder".equals(qName) && atts.getValue("name") != null) {
        String name = atts.getValue("name");
        if (this.publicationMetadata != null) {
          this.placeholderContent = this.publicationMetadata.get(name);
        } else if (this.documentMetadata != null) {
          this.placeholderContent = this.documentMetadata.get(name);
        }
      }
    }

    // store current fragment
    if (isFragment && !this.elements.contains("compare"))
      this.currentFragment = atts.getValue("id");
    // write start tag
    write('<' + qName);
    // level of heading if it is one
    int headingLevel = -1;
    // attributes
    String uriid = atts.getValue("uriid");
    for (int i = 0; i < atts.getLength(); i++) {
      String name = atts.getQName(i);
      boolean noAttNamespace = atts.getURI(i) == null || atts.getURI(i).isEmpty();
      // make sure it's not an attribute to strip
      if (noNamespace && noAttNamespace && shouldStripAttribute(qName, name))
        continue;
      // check for image path rewrite
      boolean rewriteImageSrc = noAttNamespace && "image".equals(qName) && noNamespace
          && "src".equals(name);
      boolean rewriteXRefHRef = noAttNamespace && "xref".equals(qName) && noNamespace
          && "href".equals(name) && this.alternateImageXRefs
          && "alternate".equals(atts.getValue("type")) && atts.getValue("mediatype") != null
          && atts.getValue("mediatype").startsWith("image/");
      if (rewriteImageSrc || rewriteXRefHRef) {
        try {
          handleImage(atts.getValue(i), uriid, rewriteXRefHRef);
        } catch (ProcessException ex) {
          // die or not?
          if (this.failOnError)
            throw new SAXException("Failed to rewrite src attribute " + atts.getValue(i) + ": " + ex.getMessage(), ex);
          else
            this.logger.warn("Failed to rewrite image src attribute " + atts.getValue(i) + ": "
                + ex.getMessage());
        }
      } else {
        String value;
        if ("fragment".equals(name) && noNamespace && "locator".equals(qName) && this.transcluder.isTranscluding()) {
          // modify value of locator fragment
          value = this.uriID == null ? atts.getValue(i) : (this.uriID + "-" + atts.getValue(i));
        } else if ("id".equals(name) && noNamespace && "section".equals(qName) && this.transcluder.isTranscluding()) {
          // modify value of section id
          value = this.uriID == null ? atts.getValue(i) : (this.uriID + "-" + atts.getValue(i));
        } else if ("id".equals(name) && isFragment && this.transcluder.isTranscluding()) {
          // modify value of fragment id
          value = this.uriID == null ? atts.getValue(i) : (this.uriID + "-" + atts.getValue(i));
        } else if (this.processed && "level".equals(name) && "document".equals(qName)) {
          // change document level to processed
          value = "processed";
        } else if (this.processed && "href".equals(name) && (isXRef || isReverseXRef)) {
          // decode href
          try {
            value = URLDecoder.decode(atts.getValue(i), "UTF-8");
          } catch (UnsupportedEncodingException e) {
            value = null;
          }
        } else if ("level".equals(name) && "heading".equals(qName)) {
          headingLevel = Integer.parseInt(atts.getValue(name));
          // increase level with our start value
          if (this.level > 0)
            headingLevel += this.level;
          value = String.valueOf(headingLevel);
        } else {
          value = atts.getValue(i);
        }
        write(" " + name + "=\"" + XMLUtils.escapeForAttribute(value) + '"');
      }
    }
    // change document level to processed
    if (this.uriID != null && "document".equals(qName) && atts.getValue("id") == null) {
      write(" id=\"" + XMLUtils.escapeForAttribute(this.uriID) + '"');
    }
    // add a full href path for xrefs, it will be stripped on second pass
    if ((isXRef || isReverseXRef) && !"true".equals(atts.getValue("external")) && !"true".equals(atts.getValue("unresolved"))) {
      String relpath = this.transcluder.findXRefRelativePath(atts.getValue("href"));
      if (relpath != null)
        write(" relpath=\"" + XMLUtils.escapeForAttribute(relpath) + "\"");
    }
    // unresolved placeholder
    if (this.placeholders && "placeholder".equals(qName) && this.placeholderContent == null) {
      write(" unresolved=\"true\"");
    }
    write(">");
    this.elements.push(qName);
    // handle markdown
    if (this.convertMarkdown && noNamespace && "markdown".equals(qName)) {
      this.convertContent = new StringBuilder();
    }
    // update pre-transclude level
    if (isXRef && this.preXrefLevel > 0) this.preXrefLevel++;
    // add transcluded content now
    if ((isXRef || isImage || isLink) && !this.elements.contains("compare")) {
      transcludeXRef(atts, isImage, isLink);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // placeholder content
    if (this.placeholderContent != null) {
      if ("placeholder".equals(qName)) {
        write(XMLUtils.escape(this.placeholderContent));
        this.placeholderContent = null;
      } else {
        return;
      }
    }
    // if fragment loading close temporary document element (stripped out later)
    if (this.fragmentToLoad != null && "document".equals(qName)) {
      write("</document-fragment>");
    }
    // fragment loading?
    if (!this.inRequiredFragment)
      return;
    // strip element?
    if (shouldStripElement(qName)) {
      this.ignore = false;
      return;
    }
    // currently stripping?
    if (this.ignore)
      return;
    // reset flags
    this.inTranscludedXRef = false;
    // replace xref by its contents?
    boolean isXRef = (uri == null || uri.isEmpty()) && ("blockxref".equals(qName) || "xref".equals(qName));
    // update pre-transclude values
    if (isXRef) {
      if (this.preXrefLevel == 1) {
        if (this.inPreTranscluded && !"default".equals(this.preFragment)) write("</document-fragment>");
        this.uriID = this.preUriID;
        this.uriCount = this.preUriCount;
        this.inEmbedHierarchy = this.preEmbedHierarchy;
        this.inTranscludedContent = false;
        this.preXrefLevel = 0;
        this.inPreTranscluded = false;
      } else if (this.preXrefLevel > 0) this.preXrefLevel--;
    }
    if (isXRef && this.stripCurrentXRefElement) {
      if ("blockxref".equals(qName))
        write("</para>");
      this.stripCurrentXRefElement = false;
      return;
    }
    // convert ascii?
    try {
      if ((this.convertAsciiMath || this.convertTex) && (uri == null || uri.isEmpty()) && "inline".equals(qName) && this.convertContent != null) {
        write("<xref frag=\"media\" type=\"math\" config=\"mathml\"><media-fragment id=\"media\" mediatype=\"application/mathml+xml\">");
        write(this.convertingAsciimath ? AsciiMathConverter.convert(this.convertContent.toString()) : TexConverter.convert(this.convertContent.toString()));
        write("</media-fragment></xref>");
        this.convertContent = null;
        return;
      } else if ((this.convertAsciiMath || this.convertTex) && (uri == null || uri.isEmpty()) && "media-fragment".equals(qName) && this.convertContent != null) {
        write(this.convertingAsciimath ? AsciiMathConverter.convert(this.convertContent.toString()) : TexConverter.convert(this.convertContent.toString()));
        write("</media-fragment>");
        this.convertContent = null;
        if (this.fragmentToLoad != null) {
          this.inRequiredFragment = false;
        }
        return;
      }
    } catch (IllegalArgumentException ex) {
      // Add filename for math conversion debugging
      throw new IllegalArgumentException("File " + this.sourceFile.getName() + ": " + ex.getMessage());
    }
    // handle markdown
    if (this.convertMarkdown && (uri == null || uri.isEmpty()) && "markdown".equals(qName) && this.convertContent != null) {
      // convert to PSML
      write(markdownToPSML(this.convertContent.toString()));
      this.convertContent = null;
    }
    // print close tag
    this.elements.pop();
    write("</" + qName + ">");
    // handle fragment ending (not the compare fragments!)
    if (isFragment(qName) && !this.elements.contains("compare")) {
      // reset current fragment
      this.currentFragment = null;
      // load a specific fragment?
      if (this.fragmentToLoad != null)
        this.inRequiredFragment = false;
    } else if ("locator".equals(qName) && this.fragmentToLoad != null) {
      this.inRequiredFragment = false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (this.ignore || !this.inRequiredFragment || this.inTranscludedXRef || this.placeholderContent != null) {
      return;
    }
    // markdown
    if ((this.convertAsciiMath || this.convertMarkdown || this.convertTex) && this.convertContent != null)
      this.convertContent.append(ch, start, length);
    else
      write(XMLUtils.escape(new String(ch, start, length)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endDocument() throws SAXException {
    // flush the thing
    try {
      this.xml.flush();
    } catch (IOException ex) {
      throw new SAXException("Failed to flush XML writer ", ex);
    }
  }

  // --------------------------------- Used by the transclusion handler
  // --------------------------------------------

  /**
   * Create a new handler using this one as parent.
   *
   * @param toParse    the file to transclude
   * @param uriid      the URI ID of the document to transclude
   * @param fragment   the fragment to transclude
   * @param lvl        the start level of numbering
   * @param fromImage  whether transclusion is from an image
   * @param embed      whether hierarchy has all embed XRefs
   * @param transclude whether the XRef was a transclude
   * @param alternate  whether the XRef was an alternate
   *
   * @return the handler
   */
  protected PSMLProcessHandler cloneForTransclusion(File toParse, String uriid, String fragment,
                                                    int lvl, boolean fromImage, boolean embed, boolean transclude, boolean alternate) {
    // update uri count
    Integer count = this.allUriIDs.get(uriid);
    if (count == null) count = 0;
    if (!alternate && !this.inAlternateXRef) {
      count++;
      this.allUriIDs.put(uriid, count);
    }

    // clone this handler
    PSMLProcessHandler handler = new PSMLProcessHandler(this.xml, this, toParse, this.psmlRoot,
        this.binaryRepository);
    // add existing level if no publication specified (for backward compatibility)
    handler.setLevel(lvl + (this.numberingAndTOC == null ? this.level : 0));
    handler.transcluder.addParentFile(this.sourceFile, this.currentFragment);
    handler.transcluder.addParentFile(this.sourceFile, "default");
    handler.setIncludeXMLDeclaration(false);
    handler.setImageHandling(this.imageCache, this.imageSrc, this.logImageNotFound, this.sitePrefix,
        this.embedImageMetadata);
    handler.setStrip(this.strip);
    handler.setLogger(this.logger);
    handler.setFailOnError(this.failOnError);
    handler.setProcessed(this.processed);
    handler.setXRefsHandling(this.transcluder.xrefsTranscludeTypes,
        this.transcluder.excludeXRefFragment, this.transcluder.onlyXRefFrament,
        this.logXRefNotFound);
    handler.alternateImageXRefs = fromImage;
    handler.inAlternateXRef = alternate;
    handler.setURIID(uriid);
    handler.setURICount(count);
    handler.generateTOC = this.generateTOC;
    handler.setAllUriIDs(this.allUriIDs);
    handler.setHierarchyUriFragIDs(this.hierarchyUriFragIDs);
    handler.setInEmbedHierarchy(embed);
    handler.setInTranscludedContent(transclude);
    handler.numberingAndTOC = this.numberingAndTOC;
    handler.publicationConfig = this.publicationConfig;
    handler.setConvertMarkdown(this.convertMarkdown);
    handler.setConvertAsciiMath(this.convertAsciiMath);
    handler.setConvertTex(this.convertTex);
    handler.setPlaceholders(this.placeholders);
    handler.setPublicationMetadata(this.publicationMetadata);
    if (transclude) {
      handler.setDocumentMetadata(this.documentMetadata);
    }
    // load only one fragment?
    if (fragment != null && !"default".equals(fragment)) {
      handler.setFragment(fragment);
      handler.addUriFragID(uriid, fragment, embed);
    } else {
      handler.addUriFragID(uriid, null, embed);
    }
    return handler;
  }

  /**
   * Write the contents of the file provided to the current writer.
   *
   * @param f the file to load the contents from
   * @throws SAXException if there was an error reading the file's contents
   */
  public void writeFileContents(File f) throws ProcessException {
    try (FileInputStream in = new FileInputStream(f)) {
      byte[] buffer = new byte[1024 * 4];
      int read;
      while ((read = in.read(buffer)) != -1) {
        String s = new String(buffer, 0, read, StandardCharsets.UTF_8);
        // check for BOM character
        if (s.charAt(0) == '\uFEFF') s = s.substring(1);
        // remove XML declaration if needed
        if ("<?xml ".equals(s.substring(0, 6))) s = s.substring(s.indexOf(">") + 1);
        this.xml.write(s);
      }
    } catch (IOException ex) {
      // die or not?
      if (this.failOnError)
        throw new ProcessException("Failed to write contents of file " + f.getName() + ": " + ex.getMessage(), ex);
      else
        this.logger.warn("Failed to write contents of file " + f.getName() + ": " + ex.getMessage());
    }
  }

  /**
   * Write some text to the correct writer.
   *
   * @param str the text to write
   * @throws SAXException If writing failed
   */
  public void write(String str) throws SAXException {
    try {
      this.xml.write(str);
    } catch (IOException e) {
      throw new SAXException("Failed to write XMl content to the writer", e);
    }
  }

  // --------------------------------- Private Helpers
  // --------------------------------------------

  /**
   * @param qName the name of the element
   * @param atts  the attributes of the element
   * @return <code>true</code> if the xref has been stripped (startElement
   * method should stop)
   * @throws SAXException if writing to the XML writer failed
   */
  private boolean shouldStripXRef(String qName, Attributes atts) throws SAXException {
    if (this.strip.stripAllXRefs()
        || (this.strip.stripUnresolvedXRefs() && "true".equals(atts.getValue("unresolved")))) {
      if ("blockxref".equals(qName))
        write("<para>");
      this.stripCurrentXRefElement = true;
      return true;
    }
    if (this.strip.stripNotFoundXRefs() && !"true".equals(atts.getValue("external")) && !"true".equals(atts.getValue("unresolved")) &&
        this.transcluder.isNotFoundXRef(atts.getValue("href"))) {
      // log it?
      if (this.logXRefNotFound) {
        String href = atts.getValue("href");
        this.logger.error(
            "XRef target not found in URI " + this.uriID + (href != null ? CHECK_DEPTH + href : ""));
      }
      if ("blockxref".equals(qName))
        write("<para>");
      this.stripCurrentXRefElement = true;
      return true;
    }
    return false;
  }

  /**
   * @param atts the attributes on the xref element
   * @throws SAXException if something went wrong
   */
  private void transcludeXRef(Attributes atts, boolean image, boolean link) throws SAXException {
    // ignore nested transclude
    String type = atts.getValue("type");
    if ("transclude".equals(type) && this.inTranscludedContent) return;
    String href = atts.getValue(image ? "src" : "href");
    try {
      // find out if the fragment we're in is an xref-fragment
      boolean isInXRefFragment = false;
      if (!image && !link) {
        for (int i = this.elements.size() - 1; i >= 0; i--) {
          String elem = this.elements.elementAt(i);
          if (isFragment(elem)) {
            isInXRefFragment = "xref-fragment".equals(elem);
            break;
          }
        }
      }
      // retrieve target document
      String uriid = atts.getValue("uriid");
      if (this.transcluder.transcludeXRef(atts, isInXRefFragment, image, link, this.inEmbedHierarchy, this.convertTex)) {
        // then ignore content of XRef
        this.inTranscludedXRef = !link;
      // else if pre-transcluded xref (ignoring generated media fragment without a uriid)
      } else if (this.preXrefLevel == 0 && !image && !link && uriid != null &&
          ("transclude".equals(type) || "math".equals(type))) {
        // set pre-transclude values in case content exported with process-publication=true
        this.preXrefLevel = 1;
        this.preFragment = atts.getValue("frag");
        this.preUriID = this.uriID;
        this.preUriCount = this.uriCount;
        this.preEmbedHierarchy = this.inEmbedHierarchy;
        this.uriID = uriid;
        this.inEmbedHierarchy = false;
        this.inTranscludedContent = true;
      }
    } catch (InfiniteLoopException ex) {
      File root_src = this.sourceFile;
      PSMLProcessHandler parent = this.parent;
      while (parent != null) {
        root_src = parent.sourceFile;
        parent = parent.parent;
      }
      String src, tgt;
      try {
        src = this.sourceFile.getCanonicalPath()
            .substring(this.psmlRoot.getCanonicalPath().length() + 1)
            .replace(File.separatorChar, '/');
        tgt = new File(this.sourceFile.getParent(), href).getCanonicalPath()
            .substring(this.psmlRoot.getCanonicalPath().length() + 1)
            .replace(File.separatorChar, '/');
      } catch (IOException e) {
        src = this.sourceFile.getName();
        tgt = href;
      }
      String error = "Reference loop detected when resolving xref" +
          (atts.getValue("urititle") == null ? "" : (" " + atts.getValue("urititle"))) +
          " from " + src + " (URIID " + this.uriID + ") to " + tgt +
          (atts.getValue("uriid") == null ? "" : (" (URIID " + atts.getValue("uriid")) + ").");
      if (this.failOnError) {
        throw new SAXException(error);
      } else {
        this.logger.error(error);
      }
    } catch (TooDeepException ex) {
      String src, tgt;
      try {
        src = this.sourceFile.getCanonicalPath()
            .substring(this.psmlRoot.getCanonicalPath().length() + 1)
            .replace(File.separatorChar, '/');
        tgt = new File(this.sourceFile.getParent(), href).getCanonicalPath()
            .substring(this.psmlRoot.getCanonicalPath().length() + 1)
            .replace(File.separatorChar, '/');
      } catch (IOException e) {
        src = this.sourceFile.getName();
        tgt = href;
      }
      throw new SAXException("Transclusion/embed depth is too big (max is "
          + XRefTranscluder.MAX_DEPTH + ") for XRef from " + src + " to " + tgt + ".");
    } catch (XRefNotFoundException ex) {
      if (this.logXRefNotFound && this.failOnError)
        throw new SAXException(
            "XRef target not found in URI " + this.uriID + (href != null ? CHECK_DEPTH + href : ""));
      else if (this.logXRefNotFound)
        this.logger.error(
            "XRef target not found in URI " + this.uriID + (href != null ? CHECK_DEPTH + href : ""));
      else
        this.logger
            .warn("XRef target not found in URI " + this.uriID + (href != null ? CHECK_DEPTH + href : ""));
    } catch (ProcessException ex) {
      if (this.failOnError)
        throw new SAXException("Failed to resolve XRef reference " + href + ": " + ex.getMessage(),
            ex);
      else
        this.logger.warn("Failed to resolve XRef reference " + href + ": " + ex.getMessage());
    }
  }

  /**
   * @param element the name of the element to check
   * @return true if this element is a fragment
   */
  public boolean isFragment(String element) {
    return "fragment".equals(element) || "media-fragment".equals(element)
        || "xref-fragment".equals(element) || "properties-fragment".equals(element);
  }

  /**
   * @param elemName name of current element
   * @return <code>true</code> if the element should be stripped,
   * <code>false</code> otherwise
   */
  private boolean shouldStripElement(String elemName) {
    if (this.strip == null)
      return false;
    // strip docinfo?
    if (this.strip.stripDocumentInfo() && "documentinfo".equals(elemName))
      return true;
    // strip fraginfo
    if (this.strip.stripFragmentInfo() && "fragmentinfo".equals(elemName))
      return true;
    // strip reversexrefs (check ancestors incase they have been stripped)
    String dad = this.elements.isEmpty() ? null : this.elements.pop();
    String granddad = this.elements.isEmpty() ? null : this.elements.peek();
    // put it back
    this.elements.push(dad);
    if ((this.strip.stripReverseXRefs() || this.strip.stripAllXRefs()) && "reversexrefs".equals(elemName) &&
        ("documentinfo".equals(dad) || "locator".equals(dad)))
      return true;
    // strip element in docinfo or fraginfo
    // check values
    if ("documentinfo".equals(granddad) && "uri".equals(dad)) {
      if (this.strip.stripDocumentInfoDescription() && "description".equals(elemName))
        return true;
      if (this.strip.stripDocumentInfoLabels() && "labels".equals(elemName))
        return true;
      if (this.strip.stripDocumentInfoTitle() && "displaytitle".equals(elemName))
        return true;
    } else if ("documentinfo".equals(dad)) {
      if (this.strip.stripDocumentInfoPublication() && "publication".equals(elemName))
        return true;
      if (this.strip.stripDocumentInfoVersions() && "versions".equals(elemName))
        return true;
    } else if ("locator".equals(dad)) {
      if (this.strip.stripFragmentInfoLabels() && "labels".equals(elemName))
        return true;
    }
    return false;
  }

  /**
   * @param elemName name of current element
   * @param attName  name of current attribute
   * @return <code>true</code> if the attribute should be stripped,
   * <code>false</code> otherwise
   */
  private boolean shouldStripAttribute(String elemName, String attName) {
    if (this.strip == null)
      return false;
    // strip docid or uriid in xrefs
    if (((this.strip.stripXRefsDocID() && "docid".equals(attName))
        || (this.strip.stripXRefsURIID() && "uriid".equals(attName)))
        && ("xref".equals(elemName) || "blockxref".equals(elemName)))
      return true;
    // strip uriid in images
    if (this.strip.stripImagesURIID() && "uriid".equals(attName)
        && "image".equals(elemName))
      return true;
    // strip docid or title in uri in docinfo
    String dad = this.elements.isEmpty() ? null : this.elements.peek();
    if ("documentinfo".equals(dad) && "uri".equals(elemName)) {
      if (this.strip.stripDocumentInfoDocID() && "docid".equals(attName))
        return true;
      if (this.strip.stripDocumentInfoTitle() && "title".equals(attName))
        return true;
    }
    return false;
  }

  /**
   * Write the image src attribute to the XML (and an href attribute if needed).
   *
   * @param src           the current image path
   * @param uriid         the image's URI ID
   * @param alternateXRef if this is not an image tag but and xref tag
   * @throws ProcessException If the metadata file is invlaid or couldn't be parsed
   * @throws SAXException     If writing the XML content failed
   */
  private void handleImage(String src, String uriid, boolean alternateXRef)
      throws ProcessException, SAXException {
    // decode src attribute
    this.logger.debug("Handling image " + src + " (" + uriid + ")");
    String finalSrc;
    try {
      finalSrc = URLDecoder.decode(src, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // should not happen as we're using UTF-8
      throw new ProcessException("Invalid encoding UTF-8", e);
    }
    this.logger.debug("Decoded src is " + finalSrc);
    // find image file
    String relativePath = cleanUpParentFolder() + '/' + finalSrc;
    this.logger.debug("Image file relative path is " + relativePath);
    File imageFile = new File(this.binaryRepository, relativePath);
    //this.logger.debug("Image file is " + imageFile.getAbsolutePath());
    // log image not found
    if ((!imageFile.exists() || !imageFile.isFile())) {
      if (this.logImageNotFound && this.failOnError)
        throw new SAXException(
            "Image not found in URI " + this.uriID + " with src " + src + " and URI ID " + uriid);
      else if (this.logImageNotFound)
        this.logger.error(
            "Image not found in URI " + this.uriID + " with src " + src + " and URI ID " + uriid);
      // don't warn as may be export with binarymetadataonly="true"
      // else
      // this.logger.warn("Image not found with src "+src+" and URI ID "+uriid);
    }
    if (uriid == null) {
      // unresolved image
      this.logger.warn("Unresolved image in URI " + this.uriID + " with src " + src);
    }

    // get canonical relative path
    relativePath = Files.computeRelativePath(imageFile, this.binaryRepository);
    if (relativePath == null) {
      this.logger
          .debug("Could not compute relative path for image src " + finalSrc + " (" + uriid + ")");
    } else {
      // if processing image paths
      if (this.imageSrc != ImageSrc.LOCATION) {
        String suffix = null;
        if (uriid != null) {
          suffix = this.imageCache.getImageNewPath(relativePath, this.imageSrc, uriid);
        } else if (this.imageCache != null) {
          suffix = this.imageCache.getImageNewPath(relativePath, this.imageSrc);
        }
        if (suffix != null)
          finalSrc = (this.imageSrc == ImageSrc.PERMALINK ? this.sitePrefix + "/uri/" : "")
              + suffix;
        this.logger.debug("Rewriting image src " + relativePath + " to " + finalSrc);
      } else {
        if (this.imageCache != null) this.imageCache.cacheImagePath(relativePath);
        // add an href att to rewrite the path later
        write(" " + (alternateXRef ? "xhref" : "href") + "=\""
            + XMLUtils.escapeForAttribute(relativePath) + "\"");
      }
    }
    if (!this.processed && this.imageSrc != ImageSrc.FILENAMEENCODE) {
      finalSrc = URLEncodeFilepath(finalSrc);
    }
    write(" " + (alternateXRef ? "href" : "src") + "=\"" + XMLUtils.escapeForAttribute(finalSrc) + "\"");
  }

  /**
   * Encode a file path as a valid URL
   *
   * @param filepath the path
   * @return the encoded path
   */
  public static String URLEncodeFilepath(String filepath) {
    StringBuilder path = new StringBuilder();
    String fp = filepath;
    try {
      while (fp.indexOf('/') != -1) {
        path.append(URLEncoder.encode(fp.substring(0, fp.indexOf('/')), "UTF-8")).append('/');
        fp = fp.substring(1 + fp.indexOf('/'));
      }
      // turn '+' to '%20'
      return path.append(URLEncoder.encode(fp, "UTF-8")).toString().replace("+", "%20");
    } catch (UnsupportedEncodingException ex) {
      // extremely unlikely...
      return null;
    }
  }

  /**
   * Convert markdown text to PSML content.
   *
   * @param markdown the markdown content
   * @return the PSML content
   */
  private String markdownToPSML(String markdown) {
    BlockParser parser = new BlockParser();
    XMLWriter result = new XMLStringWriter(NamespaceAware.No);
    try {
      List<PSMLElement> psml = parser.parse(Arrays.asList(markdown.split("\n")));
      for (PSMLElement elem : psml) {
        elem.toXML(result);
      }
    } catch (IOException ex) {
      this.logger.warn("Failed to convert markdown to PSML", ex);
    }
    return result.toString();
  }

  /**
   * Remove potential META-INF folder from parent folder
   *
   * @return the clean parent folder
   */
  public String cleanUpParentFolder() {
    return this.parentFolderRelativePath.replaceFirst("^META-INF", "");
  }
}
