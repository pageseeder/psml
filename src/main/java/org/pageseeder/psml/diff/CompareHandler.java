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
 * Collects compare fragments in portable PSML.
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
   * Map of fragment ID to current fragment with a corresponding <compare> element.
   */
  private Map<String, String> compareFragments = new HashMap<>();

  /**
   * @return map of fragment ID to current fragment with a corresponding <compare> element.
   */
  public Map<String, String> getCompareFragments() {
    return this.compareFragments;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

    if (isFragment(qName)) {
      if ("content".equals(this.elements.peek())) {
            this.compareFragments.put(atts.getValue("id"), "");
      } else if (this.compareFragments.get(atts.getValue("id")) != null) {
        this.fragmentId = atts.getValue("id");
        this.xml = new StringWriter();
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
    if (isFragment(qName) && !"content".equals(this.elements.peek()) && this.fragmentId != null) {
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
