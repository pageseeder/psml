/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.pageseeder.xmlwriter.esc.XMLEscapeUTF8;

/**
 * @author Jean-Baptiste Reure
 * @version 24/10/2012
 *
 */
public final class NumberingAndTOCGenerator {

  /**
   * The numbering config.
   */
  private NumberingConfig numberConfig = null;

  /**
   * If the TOC should be generated
   */
  private boolean createTOC = false;

  /**
   * The TOC generated.
   */
  private StringBuilder toc = null;

  /**
   * The parent toc if current content is transcluded.
   */
  private NumberingAndTOCGenerator parent = null;

  /**
   * List of TOCs computed for sub-docs
   */
  private Map<String, String> subTOCs = new HashMap<String, String>();

  /**
   * List of current heading levels, used for numbering.
   */
  private Stack<Integer> headingLevels = new Stack<Integer>();

  /**
   * List of current para levels, used for numbering.
   */
  private Stack<Integer> paraLevels = new Stack<Integer>();
  /**
   * The TOC ID counter.
   */
  private TOCCounter counter;

  /**
   * if the text should be recorded in the toc.
   */
  private boolean currentTOCrefOpened = false;

  /**
   * If this is a fragment being transcluded (count heading but don't create
   * TOC, otherwise the TOC computed for this fragment might overwrite an
   * existing TOC fir the entire document)
   */
  private boolean transcludingFragment = false;

  /**
   * an empty constructor (no parent).
   */
  public NumberingAndTOCGenerator() {
    this(null, false);
  }

  /**
   * @param dad
   *          a parent toc
   * @param transcludingFrag
   *          if we're currently transcluding a fragment.
   */
  public NumberingAndTOCGenerator(NumberingAndTOCGenerator dad, boolean transcludingFrag) {
    if (dad != null) {
      this.counter = dad.counter;
      this.numberConfig = dad.numberConfig;
      this.parent = dad;
      this.headingLevels = dad.headingLevels;
      this.paraLevels = dad.paraLevels;
    } else {
      this.counter = new TOCCounter();
    }
    this.transcludingFragment = transcludingFrag;
  }

  /**
   * @param cfg
   *          the numbering config, if null, no numbering is generated
   */
  public void setNumberingConfig(NumberingConfig cfg) {
    this.numberConfig = cfg;
  }

  /**
   * @param generateTOC
   *          whether or not to generate the TOC
   */
  public void setGenerateToc(boolean generateTOC) {
    this.createTOC = generateTOC;
    if (this.createTOC)
      this.toc = new StringBuilder();
  }

  /**
   * @return the createTOC
   */
  public boolean willCreateTOC() {
    return this.createTOC || (this.parent != null && this.parent.willCreateTOC());
  }

  /**
   * @return the table of contents if computed, <code>null</code> otherwise.
   */
  public String getTOC() {
    if (!this.createTOC)
      return null;
    return toc.toString();
  }

  /**
   * Find the TOCs computed for a given document.
   *
   * @return the TOCs.
   */
  public Map<String, String> getSubTOCs() {
    return this.subTOCs;
  }

  /**
   * Add subTOC to root
   * 
   * @param uriid
   *          the uri id for the subTOC
   * @param toc
   *          the subTOC
   */
  public void putSubTOC(String uriid, StringBuilder toc) {
    // if not root add to parent
    if (this.parent != null)
      this.parent.putSubTOC(uriid, toc);
    // add to root
    else
      this.subTOCs.put(uriid, toc.toString());
  }

  /**
   * Add the label attribute for generated numbering.
   *
   * @param level
   *          the level of the heading
   * @param numbered
   *          the value of the numbered attribute (nothing happens if not
   *          "true")
   * @param manualPrefix
   *          the manual prefix of the heading, to add to the toc (ignored for
   *          numbered headings)
   * @param xml
   *          the XML writer where the attributes are written to (can be null)
   *
   * @throws IOException
   *           if the writing the attributes to the XML failed
   * @throws NumberFormatException
   *           if the elementName does not follow the "heading[number]" pattern
   */
  public void generateHeadingNumbering(int level, boolean numbered, String manualPrefix, Writer xml)
      throws IOException, NumberFormatException {
    String canonical = null;
    String label = null;
    char endQuote = '"';
    if (numbered && this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.headingLevels, level);
      // compute canonical label
      canonical = canonicalLabel(this.headingLevels);
      // compute numbered label
      label = this.numberConfig.getHeadingLabel(canonical);
      // add label attribute to XML
      if (xml != null)
        xml.append(" prefix=\"").append(label).append(endQuote);
    }
    // create TOC entry
    this.counter.increase();
    StringBuilder levelString = new StringBuilder();
    levelString.append("<tocref level=\"").append(level).append(endQuote);
    levelString.append(" idref=\"toc-").append(this.counter.value()).append(endQuote);
    if (numbered && this.numberConfig != null) {
      levelString.append(" canonical=\"")
          .append(XMLEscapeUTF8.UTF8_ESCAPE.toAttributeValue(canonical)).append(endQuote);
      levelString.append(" prefix=\"").append(XMLEscapeUTF8.UTF8_ESCAPE.toAttributeValue(label))
          .append(endQuote);
    } else if (manualPrefix != null) {
      levelString.append(" prefix=\"")
          .append(XMLEscapeUTF8.UTF8_ESCAPE.toAttributeValue(manualPrefix)).append(endQuote);
    }
    levelString.append('>');
    // ok add it to current toc and parent
    addTOCLevel(levelString.toString(), level);
    if (willCreateTOC()) {
      // add id attribute to XML
      if (xml != null)
        xml.append(" id=\"toc-").append(this.counter.value()).append(endQuote);
    }
  }

  /**
   * Add the TOC level to the current TOC and the parent.
   *
   * @param levelString
   *          the level as an XML start tag.
   * @param level
   *          the level as an int.
   */
  private void addTOCLevel(String levelString, int level) {
    if (this.createTOC) {
      this.toc.append(levelString.toString());
      this.currentTOCrefOpened = true;
    }
    if (this.parent != null) {
      this.parent.addTOCLevel(levelString, level);
    }
  }

  /**
   * New text from the XML source.
   *
   * @param ch
   *          the characters array
   * @param start
   *          the start offset
   * @param length
   *          the length of text
   */
  public void characters(char[] ch, int start, int length) {
    // same for the parents
    if (this.parent != null) {
      this.parent.characters(ch, start, length);
    }
    if (!this.createTOC || !this.currentTOCrefOpened)
      return;
    // escape XML characters
    this.toc.append(XMLEscapeUTF8.UTF8_ESCAPE.toElementText(ch, start, length));
  }

  /**
   * There was an end tag event in the XML source.
   */
  public void endElement() {
    // same for the parents
    if (this.parent != null)
      this.parent.endElement();
    if (!this.createTOC)
      return;
    if (this.currentTOCrefOpened) {
      this.toc.append("</tocref>\n");
      this.currentTOCrefOpened = false;
    }
  }

  /**
   * When the document is completed.
   *
   * @param uriid
   *          the URIID of the document that just completed, the TOC will be
   *          stored against that ID.
   */
  public void endDocument(String uriid) {
    if (!this.createTOC || uriid == null || this.toc == null || this.transcludingFragment)
      return;
    // save subTOC
    putSubTOC(uriid, this.toc);
  }

  /**
   * Add the label attribute for generated numbering.
   *
   * @param indent
   *          the value of the indent attribute
   * @param xml
   *          the XML writer where the attributes are written to (can be null)
   *
   * @throws IOException
   *           if the writing the attributes to the XML failed
   * @throws NumberFormatException
   *           if the indent is not an int
   */
  public void generateParaNumbering(String indent, Writer xml)
      throws IOException, NumberFormatException {
    if (this.numberConfig != null) {
      addNewLevel(this.paraLevels, Integer.parseInt(indent));
      String label = this.numberConfig.getParaLabel(canonicalLabel(this.paraLevels),
          canonicalLabel(this.headingLevels));
      // add it to XML
      xml.append(" prefix=\"").append(label).append("\"");
    }
  }

  /**
   * @param levels
   *          list of current levels
   * @param level
   *          the level to add to the list
   */
  private static void addNewLevel(Stack<Integer> levels, int level) {
    if (levels.size() == level) {
      levels.push(levels.pop() + 1);
    } else if (levels.size() + 1 == level) {
      levels.push(1);
    } else if (levels.size() > level) {
      levels.pop();
      addNewLevel(levels, level);
    } else if (levels.size() < level) {
      levels.push(0);
      addNewLevel(levels, level);
    }
  }

  /**
   * @param levels
   *          list of current levels
   *
   * @return the canonical level according to the list of levels provided
   */
  private static String canonicalLabel(Stack<Integer> levels) {
    StringBuilder label = new StringBuilder();
    for (Integer level : levels) {
      label.append(level).append('.');
    }
    return label.toString();
  }

  /**
   * A counter shared by all the embedded objects so that TOC IDs are the same
   * for all transcluded documents.
   *
   * @author Jean-Baptiste Reure
   * @version 02/11/2012
   *
   */
  private static class TOCCounter {
    /** The value of the counter */
    private int counter = 0;

    /** Increase the counter */
    protected void increase() {
      this.counter++;
    }

    /** @return the value as a String */
    protected String value() {
      return String.valueOf(this.counter);
    }
  }
}
