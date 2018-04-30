/*
 * Copyright (c) 1999-2016 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
   * Namespace mappings storage
   */
  private final Map<String, String> namespaces = new HashMap<>();

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
   * @param out        where the resulting XML should be written.
   * @param fragment   the fragment to include or "default" for whole document
   * @param transclude whether to transclude next level
   * @param parent     the parent handler.
   */
  public TransclusionHandler(XMLWriter out, String fragment, boolean transclude, PSMLProcessHandler parent) {
    this.xml = out;
    this.fragment = fragment;
    this.transclude = transclude;
    this.parentHandler = parent;
    if (!"default".equals(fragment)) {
      this.ignoreElements = true;
      this.ignoreText = true;
    }
  }

  @Override
  public final void startPrefixMapping(String prefix, String uri) throws SAXException {
    this.namespaces.put(prefix, uri);
  }

  @Override
  public final void endPrefixMapping(String prefix) throws SAXException {
    this.namespaces.remove(prefix);
  }

  @Override
  public final void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if ("compare".equals(qName)) {
      this.inCompare = true;
    }
    if (!this.inCompare && this.parentHandler.isFragment(qName) && this.fragment.equals(atts.getValue("id"))) {
      this.ignoreElements = false;
      this.ignoreText = false;
    }
    if (this.ignoreElements) return;
    try {
      this.xml.openElement(qName, true);
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // Put the prefix mapping was reported BEFORE the startElement was reported...
    if (!this.namespaces.isEmpty()) {
      for (Entry<String, String> e : this.namespaces.entrySet()) {
        boolean hasPrefix = e.getKey() != null && e.getKey().length() > 0;
        try {
          this.xml.attribute("xmlns"+(hasPrefix? ":"+ e.getKey() : e.getKey()), e.getValue());
        } catch (IOException ex) {
          throw new SAXException("Failed to output namespace", ex);
        }
      }
      this.namespaces.clear();
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
      if (!this.ignoreElements) this.xml.closeElement();
    } catch (IOException ex) {
      throw new SAXException("Failed to close element "+qName, ex);
    }
    if ("compare".equals(qName)) {
      this.inCompare = false;
    }
    if (this.parentHandler.isFragment(qName) && !"default".equals(this.fragment)) {
      this.ignoreElements = true;
    }
    this.ignoreText = this.ignoreElements;
  }

  @Override
  public final void characters(char[] ch, int start, int length) throws SAXException {
    if (this.ignoreText) return;
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
   */
  private boolean resolveTransclusion(String href, String fragment) throws TransclusionException {
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
    if (target == null || !target.exists() ||!target.isFile())
      throw new TransclusionException("XRef target not found for path: " + href);
    // parse target
    TransclusionHandler handler = new TransclusionHandler(this.xml, fragment, false, this.parentHandler);
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
