/*
 * Copyright (c) 2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Adds <diff> elements to compare fragments in portable PSML.
 *
 * @author Philip Rutherford
 */
public final class DiffHandler extends DefaultHandler {

  /**
   * Logger for PageSeeder Diffing.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DiffHandler.class);

  /**
   * For writing XML
   */
  private final Writer xml;

  /**
   * Map of fragment ID to fragment inside <compare> element.
   */
  private final Map<String, String> compareFragments;

  /**
   * Differ to use for comparing fragments
   */
  private final PSMLDiffer differ;

  /**
   * Current state.
   */
  private final Deque<String> elements = new ArrayDeque<>();

  /**
   * Current fragment ID
   */
  private @Nullable String fragmentId = null;

  /**
   * Current fragment ID
   */
  private @Nullable Writer fragmentContent = null;

  /**
   * Constructor.
   *
   * @param out                for writing the result XML
   * @param comparefragments   map of fragment ID to current fragment with a corresponding <compare> element.
   * @param diff               differ to use for comparing fragments
   *
   */
  public DiffHandler(Writer out, Map<String, String> comparefragments, PSMLDiffer diff) {
    this.xml = out;
    this.compareFragments = comparefragments;
    this.differ = diff;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

    if (isFragment(qName) && "content".equals(this.elements.peek())) {
        this.fragmentId = atts.getValue("id");
        this.fragmentContent = new StringWriter();
    }

    // Write the start tag
    try {
      this.xml.write('<'+qName);
      if (this.fragmentContent != null) this.fragmentContent.write('<'+qName);
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    // Attributes
    for (int i = 0; i < atts.getLength(); i++) {
      String name = atts.getQName(i);
      String value = atts.getValue(i);
      try {
        this.xml.write(" "+name+"=\""+XMLUtils.escapeForAttribute(value)+"\"");
        if (this.fragmentContent != null) this.fragmentContent.write(" "+name+"=\""+XMLUtils.escapeForAttribute(value)+"\"");
      } catch (IOException ex) {
        throw new SAXException("Failed to add attribute \""+atts.getQName(i)+"\" to element "+qName, ex);
      }
    }
    try {
      this.xml.write(">");
      if (this.fragmentContent != null) this.fragmentContent.write(">");
    } catch (IOException ex) {
      throw new SAXException("Failed to open element "+qName, ex);
    }
    this.elements.push(qName);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.elements.pop();
    try {
      this.xml.write("</"+qName+">");
      if ("content".equals(qName) && this.fragmentId != null) {
        assert this.fragmentContent != null; // Set at the same time as this.fragmentId
        String current = this.compareFragments.get(this.fragmentId);
        if (current != null) {
          try {
            StringWriter diff = new StringWriter();
            this.differ.diff(new StringReader(current), new StringReader(this.fragmentContent.toString()), diff);
            String diffx = diff.toString();
            // remove XML declaration
            if (diffx.startsWith("<?")) {
              diffx = diffx.substring(diffx.indexOf('>')+1);
            }
            this.xml.write("\n<diff>");
            this.xml.write(diffx);
            this.xml.write("</diff>\n");
          } catch (org.pageseeder.diffx.DiffException ex) {
            LOGGER.error("Failed to diff content: {}", ex.getMessage());
          } catch (IOException ex) {
            throw new SAXException("Failed to write <diff> element: "+ex.getMessage(), ex);
          }
        }
        this.fragmentId = null;
        this.fragmentContent = null;
      }
      if (this.fragmentContent != null) this.fragmentContent.write("</"+qName+">");
    } catch (IOException ex) {
      throw new SAXException("Failed to close element "+qName, ex);
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      this.xml.write(XMLUtils.escape(new String(ch, start, length)));
      if (this.fragmentContent != null) this.fragmentContent.write(XMLUtils.escape(new String(ch, start, length)));
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
