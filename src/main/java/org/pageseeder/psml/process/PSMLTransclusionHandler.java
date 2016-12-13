/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.pageseeder.psml.process.util.XMLUtils;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handle the resolving of the block XRefs with type="Transclude".
 *
 * @author Jean-Baptiste Reure
 * @version 5.0002
 *
 */
public final class PSMLTransclusionHandler extends DefaultHandler {

  /**
   * The href attribute
   */
  private static final String HREF_ATTRIBUTE = "href";

  /**
   * The logger object
   */
  private Logger logger = null;

  /**
   * The XML writer where XML content is stored.
   */
  private Writer xml = null;

  /**
   * If a non fatal error will stop the parsing.
   */
  private boolean failOnError = false;

  /**
   * If the images path should be rewritten to permalinks.
   */
  private boolean relativiseImagePaths = true;

  /**
   * Image cache where URI details for images are loaded from.
   */
  private String toc = null;

  /**
   * The relative path of the parent folder (used to compute relative paths).
   */
  private final String sourceRelativePath;

  /**
   * Number of URI/frag IDs in the each document sub-hierarchy <root n_uriid, <uriid[_fragid], [global count, local count]>
   */
  private Map<String, Map<String, Integer[]>> hierarchyUriFragIDs = new HashMap<String, Map<String, Integer[]>>();

  /**
   * Ancestor uriids of current node.
   */
  private Stack<String> ancestorUriIDs = new Stack<String>();
  
  /**
   * The multiple URI IDs already found (to keep uniqueness).
   */
  private Map<String, Integer> uriIDsAlreadyFound = new HashMap<String, Integer>();

  /**
   * List of TOCs.
   */
  private Map<String, String> subtocs = new HashMap<String, String>();

  /**
   * Current state.
   */
  private Stack<String> elements = new Stack<String>();

  /**
   * @param out            where the resulting XML should be written.
   * @param relativePath   the source's file relative path (used to compute relative paths).
   */
  public PSMLTransclusionHandler(Writer out, String relativePath) {
    this.xml = out;
    this.sourceRelativePath = relativePath;
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
   * @param tableofcontents The Table of Contents for the main document.
   */
  public void setMainTOC(String tableofcontents) {
    this.toc = tableofcontents;
  }

  /**
   * @param uriFragIDs  Map of number of URI/frag IDs in the each document sub-hierarchy
   */
  public void setHierarchyUriFragIDs(Map<String, Map<String, Integer[]>> uriFragIDs) {
    this.hierarchyUriFragIDs = uriFragIDs;
  }

  /**
   * @param relativise if the image paths should be relativised.
   */
  public void setRelativiseImagePaths(boolean relativise) {
    this.relativiseImagePaths = relativise;
  }

  /**
   * @param tocs The Table of Contents for the sub documents
   */
  public void setSubTOCs(Map<String, String> tocs) {
    this.subtocs = tocs;
  }

  // --------------------------------- Content Handler methods --------------------------------------------

  /**
   * {@inheritDoc}
   */
  public void startDocument() throws SAXException {
    // start to write something just in case there's an IO error
    try {
      this.xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    } catch (IOException ex) {
      throw new SAXException("Failed to write XML declaration ", ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    // check for xrefs or images
    boolean noNamespace = uri == null || uri.isEmpty();
    boolean isXRef     = noNamespace && ("blockxref".equals(qName) || "xref".equals(qName));
    boolean isReverseXRef = noNamespace && "reversexref".equals(qName);
    boolean isImage    = noNamespace && "image".equals(qName);
    boolean isTOC      = noNamespace && "toc".equals(qName);
    boolean isSection  = noNamespace && "section".equals(qName);
    boolean isDocument = noNamespace && "document".equals(qName);
//    boolean isLocator  = noNamespace && "locator".equals(qName);
    boolean isFrag     = noNamespace && ("fragment".equals(qName) ||
                                         "ext-fragment".equals(qName) ||
                                         "xref-fragment".equals(qName) ||
                                         "properties-fragment".equals(qName));
    if (isDocument && atts.getValue("id") == null)
      throw new SAXException("Document has no id attribute.");

    // if single transcluded fragment update uriids
    if ("document-fragment".equals(qName)) {
      String uriid = atts.getValue("uriid");
      Integer count = this.uriIDsAlreadyFound.get(uriid);
      if (count == null) count = 0;
      count++;
      this.uriIDsAlreadyFound.put(uriid, count);
      this.ancestorUriIDs.push(count + "_" + uriid);
      return;
    }
    // write start tag
    this.elements.push(qName);
    try {
      this.xml.write('<'+qName);
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // attributes
    if (!isTOC) {
      String xhref = atts.getValue("xhref");
      for (int i = 0; i < atts.getLength(); i++) {
        String name = atts.getQName(i);
        // check for image path rewrite
        String value;
        if ((isImage && HREF_ATTRIBUTE.equals(name)) ||
            (isXRef && "xhref".equals(name))) {
          continue;
        } else if ((isImage && "src".equals(name)) || (isXRef && HREF_ATTRIBUTE.equals(name) && xhref != null)) {
          try {
            value = handleImage(atts.getValue(i), isImage ? atts.getValue(HREF_ATTRIBUTE) : xhref);
          } catch (ProcessException ex) {
            // die or not?
            if (this.failOnError)
              throw new SAXException("Failed to rewrite src attribute "+atts.getValue(i)+": "+ex.getMessage(), ex);
            else if (this.logger != null)
              this.logger.warn("Failed to rewrite image src attribute "+atts.getValue(i)+": "+ex.getMessage());
            value = atts.getValue(i);
          }
        } else if ((isXRef || isReverseXRef) && "relpath".equals(name)) {
          continue;
        } else if (isReverseXRef && HREF_ATTRIBUTE.equals(name)) {
          // make it relative
          String relpath = atts.getValue("relpath");
          if (relpath == null) value = atts.getValue(i);
          else value = relativisePath(relpath, this.sourceRelativePath);
        } else if (isXRef && HREF_ATTRIBUTE.equals(name)) {
          try {
            value = handleXRef(atts);
          } catch (ProcessException e) {
            throw new SAXException(e.getMessage(), e);
          }
        } else if (isSection && "id".equals(name)) {
          String id = atts.getValue(i);
          // get uriid and make id unique
          int j = id.indexOf('-');
          if (j != -1) {
            String uriid = id.substring(0, j);
            Integer count = this.uriIDsAlreadyFound.get(uriid);
            value = count != 1 ? count + "_" + id : id;
          // shouldn't happen
          } else {
            value = id;
          }
        } else if (isDocument && "id".equals(name)) {
          String id = atts.getValue(i);
          Integer count = this.uriIDsAlreadyFound.get(id);
          if (count == null) count = 0;
          count++;
          value = count != 1 ? count + "_" + id : id;
          this.uriIDsAlreadyFound.put(id, count);
          this.ancestorUriIDs.push(count + "_" + id);
          // don't modify fragment attribute on locator element
//        } else if (isLocator && "fragment".equals(name)) {
//          String id = atts.getValue(i);
//          Integer nb = this.allFragIDs.get(id);
//          if (nb != null && nb > 1) {
//            Integer count = this.fragIDsAlreadyFound.get(id);
//            if (count == null) count = 0;
//            value = ++count + "_" + id;
//          } else {
//            value = atts.getValue(i);
//          }
        } else if (isFrag && "id".equals(name) && !this.elements.contains("compare")) {
          String id = atts.getValue(i);
          // get uriid and make id unique
          int j = id.indexOf('-');
          if (j != -1) {
            String uriid = id.substring(0, j);
            Integer count = this.uriIDsAlreadyFound.get(uriid);
            value = count != 1 ? count + "_" + id : id;
          // shouldn't happen
          } else {
            value = id;
          }
        } else {
          value = atts.getValue(i);
        }
        try {
          this.xml.write(" "+name+"=\""+XMLUtils.escapeForAttribute(value)+"\"");
        } catch (IOException ex) {
          throw new SAXException("Failed to add attribute \""+atts.getQName(i)+"\" to element "+qName, ex);
        }
      }
    }
    try {
      this.xml.write(">");
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // write toc if needed
    if (isTOC) {
      String uriid = atts.getValue("uriid");
      String thistoc = uriid == null ? this.toc : this.subtocs.get(uriid);
      if (thistoc != null) try {
        this.xml.write(thistoc);
      } catch (IOException ex) {
        throw new SAXException("Failed to write TOC contents", ex);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    // if transcluded document/fragment update uriids
    if ("document-fragment".equals(qName)) {
      this.ancestorUriIDs.pop();
      return;
    }
    if ("document".equals(qName)) this.ancestorUriIDs.pop();
    this.elements.pop();
    try {
      this.xml.write("</"+qName+">");
    } catch (IOException ex) {
      throw new SAXException("Failed to close element "+qName, ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      this.xml.write(XMLUtils.escape(new String(ch, start, length)));
    } catch (IOException ex) {
      throw new SAXException("Failed to write text", ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void endDocument() throws SAXException {
    // flush the thing
    try {
      this.xml.flush();
    } catch (IOException ex) {
      throw new SAXException("Failed to flush XML writer ", ex);
    }
  }

  /**
   * @param src  the current image path
   * @param href the href to the file
   *
   * @return the modified path
   *
   * @throws ProcessException If the metadata file is invlaid or couldn't be parsed
   */
  private String handleImage(String src, String href) throws ProcessException {
    // make sure we have to do something
    if (!this.relativiseImagePaths || href == null) return src;
    // build relative path
    return relativisePath(href, this.sourceRelativePath);
  }

  /**
   * @param atts the current xref attributes
   *
   * @return the modified href
   *
   * @throws ProcessException If it points to multiple locations in the current document
   */
  private String handleXRef(Attributes atts) throws ProcessException {
    String type = atts.getValue("type");
    String uriid = atts.getValue("uriid");
    String frag = atts.getValue("frag");
    if (frag == null)
      throw new ProcessException("XRef has no frag attribute.");       
    Integer global_count = 0;
    Integer local_count = 0;
    
    // if resolved and type is none try to find targets in ancestor sub-hierarchies
    if (uriid != null && "none".equals(type)) {
      List<String> ancestors = new ArrayList<String>(this.ancestorUriIDs);
      for (int i = ancestors.size() - 1; i >= 0; i--) {
        String id = ancestors.get(i);
        Map<String, Integer[]> sub_hierarchy = this.hierarchyUriFragIDs.get(id);
        Integer[] uri_counts = sub_hierarchy.get(uriid);
        if (uri_counts != null) {
          global_count = uri_counts[0];
          local_count = uri_counts[1];
          logger.debug("Hierarchy {} found ID {} globally {} times and locally {} times", id, uriid, uri_counts[0], uri_counts[1]);
        }
        // if link to fragment check transcluded fragments
        if (!"default".equals(frag)) {
          Integer[] frag_counts = sub_hierarchy.get(uriid + "-" + frag);
          if (frag_counts != null) {
            global_count = frag_counts[0];
            local_count = local_count + frag_counts[1];            
            logger.debug("Hierarchy {} found ID {}-{} globally {} times and locally {} times", id, uriid, frag, frag_counts[0], frag_counts[1]);
          }
        }
        // if target found then finished
        if (local_count >= 1) break;
      }
    }
    
    // generate correct target href
    if (local_count > 0) {
      // internal
      if (local_count > 1) {
        String message = "Internal link pointing to URI "+uriid+
            " fragment "+frag+" is ambiguous because this content appears in multiple locations (see Dev > References check for "+
            this.sourceRelativePath+").";
        if (this.failOnError) throw new ProcessException(message);
        else this.logger.error(message);
      }
      if ("default".equals(frag)) return "#" + (global_count != 1 ? global_count + "_" : "") + uriid;
      return "#" + (global_count != 1 ? global_count + "_" : "") + uriid + "-" + frag;
    } else {
      // external, make it relative
      String relpath = atts.getValue("relpath");
      if (relpath == null) return atts.getValue(HREF_ATTRIBUTE);
      return relativisePath(relpath, this.sourceRelativePath);
    }
  }

  /**
   * @param path             the path to make relative
   * @param currentLocation  the location of the current file (to make the path relative to)
   *
   * @return the relative path
   */
  private static String relativisePath(String path, String currentLocation) {
    // build relative path
    StringBuilder relative = new StringBuilder();
    String[] pathElements = path.split("/");
    String[] thisElements = currentLocation.isEmpty() ? new String[0] : currentLocation.split("/");
    for (int i = 0; i < pathElements.length; i++) {
      // strip common elements
      if (i < thisElements.length-1 && pathElements[i].equals(thisElements[i])) continue;
      // put the '..' (ignore last entry as it's relative to source folder, not source file)
      for (int j = i; j < thisElements.length-1; j++) { relative.append("/.."); }
      // append the rest
      for (int j = i; j < pathElements.length; j++) { relative.append('/'+pathElements[j]); }
      break;
    }
    return relative.length() == 0 ? "" : relative.substring(1);
  }

}
