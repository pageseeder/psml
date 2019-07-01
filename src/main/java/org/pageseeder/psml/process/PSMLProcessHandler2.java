/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.psml.toc.DocumentTree;
import org.pageseeder.psml.toc.FragmentNumbering;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Handle inserting TOC and other processes.
 *
 * @author Jean-Baptiste Reure
 * @author Philip Rutherford
 *
 */
public final class PSMLProcessHandler2 extends DefaultHandler {

  private enum DiffType {
    INSERT,
    DELETE,
    CHANGE
  }

  private enum DiffElement {
    INS,
    DEL
  }

  /**
   * Specifies a heading/para location within a publication.
   *
   */
  private final class Location {

    /**
     * Current URI ID
     */
    private long uriid;

    /**
     * Current URI position (instance number in publication)
     */
    private int position;

    /**
     * Current fragment
     */
    private String fragment = null;

    /**
     * Index (instance number) of heading/para in fragment
     */
    private int index = 0;

    /**
     * The number of nested blockxrefs
     * (effectively number of nested transclusions as embeds have a new location object)
     */
    int blockxrefs = 0;

    /**
     * Constructor
     */
    private Location(long uriid, int position) {
      this.uriid = uriid;
      this.position = position;
    }

  }

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
   *  Whether to raise an error when an XRef target is ambiguous.
   */
  private boolean errorOnAmbiguous = false;


  /**
   * Whether to change attribute level to "processed" and URL decode @href and @src
   */
  private boolean processed = true;

  /**
   * Process XRefs.
   */
  private boolean processXRefs = true;

  /**
   * If the images path should be rewritten to permalinks.
   */
  private boolean relativiseImagePaths = true;

  /**
   * The relative path of the parent folder (used to compute relative paths).
   */
  private final String sourceRelativePath;

  /**
   * If the TOC should be generated
   */
  private boolean generateTOC = false;

  /**
   * If the TOC has been written yet
   */
  private boolean tocWritten = false;

  /**
   * Whether the last blockxref was a transclude.
   */
  boolean lastXRefTransclude = false;

  /**
   * Number of URI/frag IDs in the each document sub-hierarchy <root n_uriid,
   * <uriid[_fragid], [global count, local count, embed count]>
   */
  private Map<String, Map<String, Integer[]>> hierarchyUriFragIDs = new HashMap<>();

  /**
   * Config for publication.
   */
  private PublicationConfig publicationConfig = null;

  /**
   * Helper to compute numbering and TOC.
   */
  private NumberedTOCGenerator numberingAndTOC = null;

  /**
   * Ancestor uriids of current node.
   */
  private Stack<String> ancestorUriIDs = new Stack<>();

  /**
   * The multiple URI IDs already found (to keep uniqueness).
   */
  private Map<String, Integer> uriIDsAlreadyFound = new HashMap<>();

  /**
   * Current state.
   */
  private Stack<String> elements = new Stack<>();

  /**
   * Ancestor locations
   */
  private Stack<Location> locations = new Stack<>();

  /**
   * Unadjusted level of previous heading
   */
  private int previousheadingLevel = 0;

  /**
   * The resolved title when inside an <xref display="template" title="[{prefix}|{heading}|{parentnumber}]">,
   * otherwise <code>null</code>.
   */
  private String resolvedXRefTemplate = null;

  /**
   * The position of the target URI in the publication for the current XRef.
   */
  private int xrefTargetPosition = 1;

  /**
   * The type of diffx change when inside an xref element.
   */
  private DiffType xrefElementChange = null;

  /**
   * The element when inside a diffx element.
   */
  private DiffElement insideDiffElement = null;

  /**
   * The number of nested XRefs under an alternate XRef (including itself)
   */
  private int alternateXRefs = 0;

  /**
   * @param out            where the resulting XML should be written.
   * @param relativePath   the source's file relative path (used to compute relative paths).
   */
  public PSMLProcessHandler2(Writer out, String relativePath) {
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
   * @param error whether to raise an error when an XRef target is ambiguous.
   */
  public void setErrorOnAmbiguous(boolean error) {
    this.errorOnAmbiguous = error;
  }

  /**
   * @param uriFragIDs  Map of number of URI/frag IDs in the each document sub-hierarchy
   */
  public void setHierarchyUriFragIDs(Map<String, Map<String, Integer[]>> uriFragIDs) {
    this.hierarchyUriFragIDs = uriFragIDs;
  }

  /**
   * @param config     Publication config
   * @param generator  Numbered TOC Generator
   * @param toc        Whether to generate TOC
   */
  public void setPublicationConfig(PublicationConfig config, NumberedTOCGenerator generator, boolean toc) {
    this.publicationConfig = config;
    this.numberingAndTOC = generator;
    this.generateTOC = toc;
  }

  /**
   * @param processed the processed value to set (id <code>true</code> URL decode @href and @src)
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  /**
   * @param process if XRefs targets should be processed (i.e. make fragment IDs unique)
   */
  public void setProcessXRefs(boolean process) {
    this.processXRefs = process;
  }

  /**
   * @param relativise if the image paths should be relativised.
   */
  public void setRelativiseImagePaths(boolean relativise) {
    this.relativiseImagePaths = relativise;
  }

  // --------------------------------- Content Handler methods --------------------------------------------

  @Override
  public void startDocument() throws SAXException {
    // start to write something just in case there's an IO error
    try {
      this.xml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    } catch (IOException ex) {
      throw new SAXException("Failed to write XML declaration ", ex);
    }
  }

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
                                         "media-fragment".equals(qName) ||
                                         "xref-fragment".equals(qName) ||
                                         "properties-fragment".equals(qName));
    boolean isHeading  = noNamespace && "heading".equals(qName);
    boolean isPara  = noNamespace && "para".equals(qName);
    if (isDocument && atts.getValue("id") == null)
      throw new SAXException("Document has no id attribute.");

    this.insideDiffElement = "dfx:ins".equals(qName) ? DiffElement.INS :
      ("dfx:del".equals(qName) ? DiffElement.DEL : null);
    // only diff elements are handled for XRef template (not alternate content)
    if (this.insideDiffElement == null) {
      this.resolvedXRefTemplate = null;
    }

    // update alternate XRef counter
    if (isXRef) {
      if (this.alternateXRefs > 0) {
        this.alternateXRefs++;
      } else if ("alternate".equals(atts.getValue("type"))) {
        this.alternateXRefs = 1;
      }
    }

    // if single transcluded fragment update uriids (document-fragment added temporarily by first process)
    if ("document-fragment".equals(qName)) {
      if (this.alternateXRefs == 0) {
        String uriid = atts.getValue("uriid");
        Integer count = this.uriIDsAlreadyFound.get(uriid);
        if (count == null) count = 0;
        count++;
        this.uriIDsAlreadyFound.put(uriid, count);
        this.ancestorUriIDs.push(count + "_" + uriid);
        if (!this.lastXRefTransclude) {
          this.locations.push(new Location(Long.parseLong(uriid), count));
        }
      }
      return;
    }

    // write start tag
    this.elements.push(qName);
    try {
      this.xml.write('<'+qName);
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // set heading/para prefix
    Location location = this.locations.isEmpty() ? null : this.locations.peek();
    if ((isHeading || isPara) && !this.elements.contains("compare") && this.numberingAndTOC != null && this.alternateXRefs == 0) {
      if (isHeading) {
        this.previousheadingLevel = Integer.parseInt(atts.getValue("level"));
      }
      location.index++;
      String prefix = atts.getValue("prefix");
      if ("true".equals(atts.getValue("numbered"))) {
        Prefix pref = this.numberingAndTOC.fragmentNumbering().getTranscludedPrefix(
            location.uriid, location.position, location.fragment, location.index);
        if (pref != null) {
          prefix = pref.value;
        }
      }
      if (prefix != null) {
        try {
          this.xml.write(" prefix=\""+XMLUtils.escapeForAttribute(prefix)+"\"");
        } catch (IOException ex) {
          throw new SAXException("Failed to add id attribute: " + ex.getMessage(), ex);
        }
      }
    }
    // attributes
    String xhref = atts.getValue("xhref");
    for (int i = 0; i < atts.getLength(); i++) {
      String name = atts.getQName(i);
      // check for image path rewrite
      String value;
      if ((isImage && HREF_ATTRIBUTE.equals(name)) ||
          (isXRef && "xhref".equals(name))) {
        continue;
      // if image or alternate xref to image
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
      } else if (isReverseXRef && HREF_ATTRIBUTE.equals(name) && this.processXRefs) {
        // make it relative
        String relpath = atts.getValue("relpath");
        if (relpath == null) value = atts.getValue(i);
        else value = URLEncodeIfNotProcessed(relativisePath(relpath, this.sourceRelativePath));
      } else if (isXRef && HREF_ATTRIBUTE.equals(name) && this.processXRefs) {
        try {
          value = handleXRef(atts);
        } catch (ProcessException e) {
          throw new SAXException(e.getMessage(), e);
        }
      } else if (isSection && "id".equals(name) && this.alternateXRefs == 0 && this.processXRefs) {
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
      } else if (isDocument && "id".equals(name) && this.alternateXRefs == 0) {
        String id = atts.getValue(i);
        Integer count = this.uriIDsAlreadyFound.get(id);
        if (count == null) count = 0;
        count++;
        if (this.processXRefs) value = count != 1 ? count + "_" + id : id;
        else value = id;
        this.uriIDsAlreadyFound.put(id, count);
        this.ancestorUriIDs.push(count + "_" + id);
        if (!this.lastXRefTransclude) {
          this.locations.push(new Location(Long.parseLong(id), count));
        }
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
      } else if (!this.elements.contains("compare")) {
        if (isFrag && "id".equals(name) && this.alternateXRefs == 0) {
          String id = atts.getValue(i);
          // get uriid and make id unique
          int j = id.indexOf('-');
          if (j != -1) {
            // if not inside a transclusion, update fragment ID
            if (this.locations.peek().blockxrefs == 0) {
              location.fragment = id.substring(j + 1);
              location.index = 0;
            }
            String uriid = id.substring(0, j);
            Integer count = this.uriIDsAlreadyFound.get(uriid);
            if (this.processXRefs) value = count != 1 ? count + "_" + id : id;
            else value = id;
          // shouldn't happen
          } else {
            // if not inside a transclusion, update fragment ID
            if (this.locations.peek().blockxrefs == 0) {
              location.fragment = id;
              location.index = 0;
            }
            value = id;
          }
        } else if ((isHeading || isPara)  && "prefix".equals(name)
            && !this.elements.contains("compare") && this.numberingAndTOC != null) {
          // prefix already added
          continue;
        } else if (isHeading && "level".equals(name) && this.numberingAndTOC != null && this.alternateXRefs == 0 &&
            this.publicationConfig.getHeadingLevelAdjust() == PublicationConfig.LevelAdjust.CONTENT) {
          int headingLevel = Integer.parseInt(atts.getValue(name));
          Prefix pref = this.numberingAndTOC.fragmentNumbering().getPrefix(location.uriid, location.position);
          int base = pref == null ? 0 : pref.level;
          headingLevel += base - 1;
          value = String.valueOf(headingLevel > 0 ? headingLevel : 1);
        } else if (isPara && "indent".equals(name) && this.numberingAndTOC != null && this.alternateXRefs == 0 &&
            this.publicationConfig.getParaLevelAdjust() == PublicationConfig.LevelAdjust.CONTENT) {
          int paraLevel = Integer.parseInt(atts.getValue(name));
          Prefix pref = this.numberingAndTOC.fragmentNumbering().getPrefix(location.uriid, location.position);
          int base = pref == null ? 0 : pref.level;
          paraLevel += base - 1;
          if (this.publicationConfig.getParaLevelRelativeTo() == PublicationConfig.LevelRelativeTo.HEADING) {
            paraLevel += this.previousheadingLevel;
          }
          value = String.valueOf(paraLevel > 0 ? paraLevel : 1);
        } else {
          value = atts.getValue(i);
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
    // write toc ids if needed
    if (isHeading && this.generateTOC && !this.elements.contains("compare") && this.alternateXRefs == 0) {
      try {
        this.xml.write(" id=\""+XMLUtils.escapeForAttribute(
            location.uriid + "-" + location.position + "-" + location.fragment + "-" + location.index)+"\"");
      } catch (IOException ex) {
        throw new SAXException("Failed to add id attribute: " + ex.getMessage(), ex);
      }
    }
    try {
      this.xml.write(">");
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // write toc if needed
    if (isTOC && this.generateTOC && !this.tocWritten) {
      XMLWriter xmlwriter = new XMLWriterImpl(this.xml);
      try {
        this.numberingAndTOC.toXML(xmlwriter);
        xmlwriter.flush();
      } catch (IOException ex) {
        throw new SAXException("Unable to write TOC: " + ex.getMessage(), ex);
      }
      this.tocWritten = true;
    }
    // handle blockxref
    if ("blockxref".equals(qName) && !this.elements.contains("compare")) {
      this.locations.peek().blockxrefs++;
      this.lastXRefTransclude = "transclude".equals(atts.getValue("type"));
    // else handle xref template
    } else if ("xref".equals(qName)
        && ("template".equals(atts.getValue("display")) || "template".equals(atts.getValue("del:display")))
        && this.numberingAndTOC != null) {
      // only title with the right tokens
      String title = atts.getValue("title") != null ? atts.getValue("title") : atts.getValue("del:title");
      if (title != null && title.matches(".*?((\\{prefix})|(\\{heading})|(\\{parentnumber})).*?")) {

        this.xrefElementChange = "true".equals(atts.getValue("dfx:insert")) ? DiffType.INSERT :
                                 ("true".equals(atts.getValue("dfx:delete")) ? DiffType.DELETE :
                                 (("true".equals(atts.getValue("ins:frag")) ||
                                  "true".equals(atts.getValue("ins:uriid")))  ? DiffType.CHANGE : null));

        // get xref's traget details
        String targetfrag = atts.getValue("frag") != null ? atts.getValue("frag") : atts.getValue("del:frag");
        String targeturi = atts.getValue("uriid") != null ? atts.getValue("uriid") : atts.getValue("del:uriid");
        if (targeturi != null) {
          long targeturiid = Long.parseLong(targeturi);

          // compute adjusted values
          FragmentNumbering.Prefix prefix = this.numberingAndTOC.fragmentNumbering().getPrefix(
              targeturiid, this.xrefTargetPosition, targetfrag, 1);
          String newPrefix       = prefix == null ? null : prefix.value;
          DocumentTree tree      = this.numberingAndTOC.publicationTree().tree(targeturiid);
          String newHeading      = tree == null ? null : tree.fragmentheadings().get(targetfrag);
          String newParentNumber = prefix == null ? null : prefix.parentNumber;

          // set content and title attribute
          this.resolvedXRefTemplate = title.replaceAll("\\{prefix}",       newPrefix       == null ? "?" : newPrefix)
                                         .replaceAll("\\{heading}",      newHeading      == null ? "?" : newHeading)
                                         .replaceAll("\\{parentnumber}", newParentNumber == null ? "" : newParentNumber);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("blockxref".equals(qName) && !this.elements.contains("compare")) {
      this.locations.peek().blockxrefs--;
      this.lastXRefTransclude = false;
    // if writing xref template ensure it's inside the correct diffx element.
    } else if (this.resolvedXRefTemplate != null &&
        !(this.xrefElementChange == DiffType.INSERT && this.insideDiffElement == DiffElement.DEL) &&
        !(this.xrefElementChange == DiffType.DELETE && this.insideDiffElement == DiffElement.INS)) {
      try {
        // if xref has changed but no diffx elements then add some to flag the change
        if (this.xrefElementChange == DiffType.CHANGE && this.insideDiffElement == null) {
          this.xml.write("<dfx:ins>");
          this.xml.write(XMLUtils.escape(this.resolvedXRefTemplate));
          this.xml.write("</dfx:ins>");
          // it's difficult to find the previous template value so just output x.
          this.xml.write("<dfx:del>x</dfx:del>");
        } else {
          this.xml.write(XMLUtils.escape(this.resolvedXRefTemplate));
        }
      } catch (IOException ex) {
        throw new SAXException("Failed to write text", ex);
      } finally {
        this.resolvedXRefTemplate = null;
      }
    }
    // update alternate XRef counter
    if (("xref".equals(qName) || "blockxref".equals(qName)) && this.alternateXRefs > 0) {
      this.alternateXRefs--;
    }
    if ("xref".equals(qName)) this.xrefElementChange = null;
    if ("document-fragment".equals(qName)) {
      if (this.alternateXRefs == 0) {
        this.ancestorUriIDs.pop();
        // if not in a transclusion
        if (this.locations.peek().blockxrefs == 0) {
          this.locations.pop();
          this.previousheadingLevel = 0;
        }
      }
      return;
    }
    if ("document".equals(qName) && this.alternateXRefs == 0) {
      this.ancestorUriIDs.pop();
      // if not in a transclusion
      if (this.locations.peek().blockxrefs == 0) {
        this.locations.pop();
        this.previousheadingLevel = 0;
      }
    }
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
    // if XRef template already output ignore other diffx element content of the same type
    if (this.resolvedXRefTemplate == null &&
        ((this.xrefElementChange == DiffType.INSERT && this.insideDiffElement == DiffElement.INS) ||
          (this.xrefElementChange == DiffType.DELETE && this.insideDiffElement == DiffElement.DEL))) return;
    try {
      if (this.resolvedXRefTemplate == null) {
        this.xml.write(XMLUtils.escape(new String(ch, start, length)));
      }
    } catch (IOException ex) {
      throw new SAXException("Failed to write text", ex);
    }
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

  /**
   * @param src  the current image path
   * @param href the href to the file
   *
   * @return the modified path
   *
   * @throws ProcessException If the metadata file is invlaid or couldn't be parsed
   */
  private String handleImage(String src, String href) throws ProcessException {
    this.logger.debug("Image temp href={}", href);
    // make sure we have to do something
    if (!this.relativiseImagePaths || href == null) return src;
    // build relative path
    String rel = URLEncodeIfNotProcessed(relativisePath(href, this.sourceRelativePath));
    this.logger.debug("Image relative path={}", rel);
    return rel;
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
    Integer embed_count = 0;

    // if resolved and type is none try to find targets in ancestor sub-hierarchies
    if (uriid != null && "none".equals(type)) {
      List<String> ancestors = new ArrayList<>(this.ancestorUriIDs);
      for (int i = ancestors.size() - 1; i >= 0; i--) {
        String id = ancestors.get(i);
        Map<String, Integer[]> sub_hierarchy = this.hierarchyUriFragIDs.get(id);
        Integer[] uri_counts = sub_hierarchy.get(uriid);
        if (uri_counts != null) {
          global_count = uri_counts[0];
          local_count = uri_counts[1];
          embed_count = uri_counts[2];
          this.logger.debug("Hierarchy {} found ID {} globally {}, locally {} and embedded {} times",
              id, uriid, uri_counts[0], uri_counts[1], uri_counts[2]);
        }
        // if link to fragment check transcluded fragments
        if (!"default".equals(frag)) {
          Integer[] frag_counts = sub_hierarchy.get(uriid + "-" + frag);
          if (frag_counts != null) {
            global_count = frag_counts[0];
            local_count = local_count + frag_counts[1];
            embed_count = embed_count + frag_counts[2];
            this.logger.debug("Hierarchy {} found ID {}-{} globally {} times, locally {} times",
                id, uriid, frag, frag_counts[0], frag_counts[1], frag_counts[2]);
          }
        }
        // if embedded target or single transcluded target found then finished
        if (embed_count > 0 || local_count == 1) break;
      }
    }

    // generate correct target href
    if (local_count > 0) {
      // if more than 1 embedded target or only multiple transcluded targets generate error
      if (embed_count > 1 || (embed_count == 0 && local_count > 1)) {
        String message = "Internal link pointing to URI "+uriid+
            " fragment "+frag+" is ambiguous because this content appears in multiple locations (see Dev > References check for "+
            this.sourceRelativePath+").";
        if (this.errorOnAmbiguous) {
          if (this.failOnError) throw new ProcessException(message);
          else this.logger.error(message);
        } else {
          this.logger.warn(message);
        }
      }
      this.xrefTargetPosition = global_count;
      if ("default".equals(frag)) return "#" + (global_count != 1 ? global_count + "_" : "") + uriid;
      return "#" + (global_count != 1 ? global_count + "_" : "") + uriid + "-" + frag;
    } else {
      // external, make it relative
      String relpath = atts.getValue("relpath");
      if (relpath == null) return atts.getValue(HREF_ATTRIBUTE);
      return URLEncodeIfNotProcessed(relativisePath(relpath, this.sourceRelativePath));
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

  /**
   * URL encode path if level is portable (i.e. not processed)
   *
   * @param path  the path
   *
   * @return the path encoded if required
   */
  private String URLEncodeIfNotProcessed(String path) {
    if (!this.processed) return PSMLProcessHandler.URLEncodeFilepath(path);
    else return path;
  }
}
