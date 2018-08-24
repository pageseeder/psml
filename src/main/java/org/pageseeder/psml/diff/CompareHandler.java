/*
 * Copyright (c) 2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.pageseeder.psml.process.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Collects compare fragments in processed PSML.
 *
 * @author Philip Rutherford
 */
public final class CompareHandler extends DefaultHandler {

  /**
   * For writing XML
   */
  private Writer xml = null;

  /**
   * Current state.
   */
  private Stack<String> elements = new Stack<>();

  /**
   * Current fragment ID
   */
  private String fragmentId = null;

  /**
   * Map of URI ID to how many times it's content is included.
   */
  private Map<String, Integer> uriidCounts = new HashMap<>();

  /**
   * Map of fragment ID to fragment inside <compare> element.
   */
  private Map<String, String> compareFragments = new HashMap<>();

  /**
   * Return map of fragment ID to fragment inside <compare> element.
   */
  public Map<String, String> getCompareFragments() {
    return this.compareFragments;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

    if ("uri".equals(qName) && "documentinfo".equals(this.elements.peek())) {
      addURI(atts.getValue("id"));
    }
    if ("locator".equals(qName) && "blockxref".equals(this.elements.peek())) {
      String id = atts.getValue("fragment");
      if (id != null) {
        int i = id.indexOf('-');
        if (i != -1) {
          String uriid = id.substring(0, i);
          addURI(uriid);
        }
      }
    }
    if (isFragment(qName) && "content".equals(this.elements.peek())) {
      String id = atts.getValue("id");
      if (id != null) {
        int i = id.indexOf('-');
        if (i != -1) {
          String uriid = id.substring(0, i);
          Integer count = this.uriidCounts.get(uriid);
          this.fragmentId = (count != 1 ? count + "_" : "") + id;
          this.xml = new StringWriter();
        }
      }
    }

    // write start tag
    if (this.xml != null) {
      try {
        this.xml.write('<'+qName);
      } catch (IOException ex) {
        throw new SAXException("Failed to open element "+qName, ex);
      }
      // attributes
      for (int i = 0; i < atts.getLength(); i++) {
        String name = atts.getQName(i);
        String value = atts.getValue(i);
        try {
          this.xml.write(" "+name+"=\""+XMLUtils.escapeForAttribute(value)+"\"");
        } catch (IOException ex) {
          throw new SAXException("Failed to add attribute \""+atts.getQName(i)+"\" to element "+qName, ex);
        }
      }
      try {
        this.xml.write(">");
      } catch (IOException ex) {
        throw new SAXException("Failed to open element "+qName, ex);
      }
    }
    this.elements.push(qName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.elements.pop();
    try {
      if (this.xml != null) this.xml.write("</"+qName+">");
    } catch (IOException ex) {
      throw new SAXException("Failed to close element "+qName, ex);
    }
    if (isFragment(qName) && "content".equals(this.elements.peek()) && this.fragmentId != null) {
      this.compareFragments.put(this.fragmentId, this.xml.toString());
      this.xml = null;
      this.fragmentId = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      if (this.xml != null) this.xml.write(XMLUtils.escape(new String(ch, start, length)));
    } catch (IOException ex) {
      throw new SAXException("Failed to write text", ex);
    }
  }

  /**
   * Add URI to counts.
   *
   * @param uriid the URI ID
   */
  private void addURI(String uriid) {
    if (uriid == null) return;
    Integer count = this.uriidCounts.get(uriid);
    if (count == null) count = 0;
    this.uriidCounts.put(uriid, count++);
  }

  /**
   * Checks if element is a PSML fragment.
   *
   * @param qName  the element name
   *
   * @return <code>true</code> if element is a PSML fragment.
   */
  private boolean isFragment(String qName) {
    return "fragment".equals(qName) ||
      "media-fragment".equals(qName) ||
      "xref-fragment".equals(qName) ||
      "properties-fragment".equals(qName);
  }

}
