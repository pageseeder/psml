/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process;

import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Transcludes one level of <blockxref> with type="transclude" and href ending in ".psml".
 *
 * @author Philip Rutherford
 *
 */
public class TransclusionHandler extends DefaultHandler {

  /**
   * The parent handler.
   */
  private final PSMLProcessHandler parentHandler;

  /**
   * Fragment to include or "default" for whole document
   */
  private final String fragment;

  /**
   * Whether to transclude next level
   */
  private final boolean transclude;

  /**
   * The XML writer where XML content is stored
   */
  private final XMLWriter xml;

  /**
   * A flag to specifiy if characters should be outputed (for resolved transclusions)
   */
  private boolean ignoreText = false;

  /**
   * A flag to specifiy if elements should be outputed (for single fragment transclusion)
   */
  private boolean ignoreElements = false;

  /**
   * A flag to specifiy if inside a compare element.
   */
  private boolean inCompare = false;

  /**
   * A flag to specify if inside metadata properties element
   */
  private boolean inMetadata = false;

  /**
   * A flag to specify if this document is a publication root
   */
  private boolean publication = false;

  /**
   * The resolved content for the current placeholder
   */
  private String placeholderContent = null;

  /**
   * Document metadata (for placeholders)
   */
  private Map<String,String> documentMetadata = new HashMap<>();

  /**
   * @param out        where the resulting XML should be written.
   * @param fragment   the fragment to include or "default" for whole document
   * @param transclude whether to transclude next level
   * @param parent     the parent handler.
   */
  public TransclusionHandler(XMLWriter out, String fragment, boolean transclude,
      PSMLProcessHandler parent) {
    this.xml = out;
    this.fragment = fragment;
    this.transclude = transclude;
    this.parentHandler = parent;
    if (!"default".equals(fragment)) {
      this.ignoreElements = true;
      this.ignoreText = true;
    }
  }

  /**
   * @return Publication metadata
   */
  public Map<String,String> getPublicationMetadata() {
    return this.publication ? this.documentMetadata : null;
  }

  @Override
  public final void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if ("compare".equals(qName)) {
      this.inCompare = true;
    }
    if ("properties".equals(qName)) {
      this.inMetadata = true;
    }
    if ("publication".equals(qName)) {
      this.publication = true;
    }
    if (!this.inCompare && this.parentHandler.isFragment(qName) && this.fragment.equals(atts.getValue("id"))) {
      this.ignoreElements = false;
      this.ignoreText = false;
    }
    if (this.ignoreElements) return;

    // resolve placeholders
    if (this.parentHandler.resolvePlaceholders()) {
      // if metadata property, collect metadata
      if (this.inMetadata && "property".equals(qName) && this.transclude && atts.getValue("name") != null &&
          !"xref".equals(atts.getValue("datatype")) && !"markdown".equals(atts.getValue("datatype")) &&
          (atts.getValue("count") == null || "1".equals(atts.getValue("count"))) &&
           atts.getValue("multiple") == null) {
        String value = atts.getValue("value") == null ? "" : atts.getValue("value");
        this.documentMetadata.put(atts.getValue("name"), value);

      // if place holder, try to resolve
      } else if ("placeholder".equals(qName) && atts.getValue("name") != null) {
        String name = atts.getValue("name");
        if (this.parentHandler.getPublicationMetadata() != null) {
          this.placeholderContent = this.parentHandler.getPublicationMetadata().get(name);
        } else if (this.documentMetadata != null) {
          this.placeholderContent = this.documentMetadata.get(name);
        }
      }
    }

    try {
      this.xml.openElement(qName, true);
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // attributes
    for (int i = 0; i < atts.getLength(); i++) {
      String auri = atts.getURI(i);
      if (auri != null && auri.length() == 0) {
        auri = null;
      }
      try {
        this.xml.attribute(atts.getQName(i), atts.getValue(i));
      } catch (IOException ex) {
        throw new SAXException("Failed to add attribute \""+atts.getLocalName(i)+"\" to element "+qName, ex);
      }
    }
    // unresolved placeholder
    if (this.parentHandler.resolvePlaceholders() && "placeholder".equals(qName)) {
      try {
        if (this.placeholderContent == null) {
          this.xml.attribute("unresolved","true");
        } else {
          this.xml.attribute("resolved","true");
        }
      } catch (IOException ex) {
        throw new SAXException("Failed to add attribute \"unresolved\" to element "+qName, ex);
      }
    }
    // handle transclusions now
    if ("blockxref".equalsIgnoreCase(qName) && "transclude".equalsIgnoreCase(atts.getValue("type"))) {
      try {
        // retrieve target document
        this.ignoreText = resolveTransclusion(atts.getValue("href"), atts.getValue("frag"));
      } catch (TransclusionException ex) {
        this.parentHandler.getLogger().error(ex.getMessage(), ex);
        // handle it normally then
        this.ignoreText = false;
      }
    }
  }

  @Override
  public final void endElement(String uri, String localName, String qName) throws SAXException {
    try {
      // placeholder content
      if (this.placeholderContent != null && "placeholder".equals(qName)) {
          this.xml.writeText(this.placeholderContent);
          this.placeholderContent = null;
      }
      if (!this.ignoreElements) this.xml.closeElement();
    } catch (IOException ex) {
      throw new SAXException("Failed to close element "+qName, ex);
    }
    if ("compare".equals(qName)) {
      this.inCompare = false;
    }
    if ("properties".equals(qName)) {
      this.inMetadata = false;
    }
    if (this.parentHandler.isFragment(qName) && !"default".equals(this.fragment)) {
      this.ignoreElements = true;
    }
    this.ignoreText = this.ignoreElements;
  }

  @Override
  public final void characters(char[] ch, int start, int length) throws SAXException {
    if (this.ignoreText || this.placeholderContent != null) return;
    try {
      this.xml.writeText(ch, start, length);
    } catch (IOException ex) {
      throw new SAXException("Failed to write text", ex);
    }
  }

  // --------------------------------- Private Helpers --------------------------------------------
  /**
   * Resolve a transclusion by loading the content of the URI specified
   * by the ID. If the fragment is not null and not "default",
   * only the content of that fragment is loaded.
   *
   * @param href      The href of the document to load
   * @param fragment  The fragment to load
   *
   * @return whether content was transcluded
   *
   * @throws TransclusionException if the target is invalid or could not be read
   * @throws SAXException          if the target is not found
   */
  private boolean resolveTransclusion(String href, String fragment)
          throws TransclusionException, SAXException {
    if (href == null || !href.endsWith(".psml") || !this.transclude) return false;
    try {
      href = URLDecoder.decode(href, "utf-8");
    } catch (UnsupportedEncodingException ex) {
      this.parentHandler.getLogger().error(ex.getMessage(), ex);
    }
    String dadPath = this.parentHandler.getParentFolderRelativePath();
    // find target file
    File target = new File(this.parentHandler.getPSMLRoot(), dadPath + '/' + href);
    // make sure it's valid
    if (target == null || !target.exists() || !target.isFile()) {
      PSMLProcessHandler.handleError(
              "XRef transclusion not found for path: " + dadPath + '/' + href,
              this.parentHandler.getFailOnError(), this.parentHandler.getLogger(),
              this.parentHandler.getErrorXRefNotFound(), this.parentHandler.getWarnXRefNotFound());
      return false;
    }
    // parse target
    TransclusionHandler handler = new TransclusionHandler(this.xml, fragment, false, this.parentHandler);
    handler.documentMetadata = this.documentMetadata;
    try {
      XMLUtils.parse(target, handler);
    } catch (ProcessException ex) {
      throw new TransclusionException("Error parsing XRef target for path: " + href, ex);
    }
    return true;
  }

  /**
   * For internal errors.
   */
  private static class TransclusionException extends Exception {

    /** As required by Serializable */
    private static final long serialVersionUID = 7733046469035213494L;

    /**
     * Create a new exception.
     * @param msg the error message.
     */
    public TransclusionException(String msg) {
      super(msg);
    }
    /**
     * Create a new exception.
     * @param msg   the error message.
     * @param cause the cause of the eroor
     */
    public TransclusionException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
