/*
 * Copyright 2016 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.model;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loader class for parsing XML inputs using SAX and converting them into PSMLElement models.
 *
 * <p>This class provides functionality for transforming XML content into a structured representation
 * while supporting lazy initialization of necessary components like the SAXParserFactory.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class Loader {

  /**
   * Lazily loaded SAX parser factory.
   */
  private @Nullable SAXParserFactory factory = null;

  /**
   * Indicates whether whitespace should be preserve even in contexts where
   * it can be safely ignored, such as between tables cells and list items
   */
  private boolean preserveWhitespace = false;

  public Loader() {
  }

  public Loader(boolean ignoreWhitespace) {
    this.preserveWhitespace = ignoreWhitespace;
  }

  /**
   * Configures whether whitespace should be preserve even in contexts where
   * it can be safely ignored, such as between tables cells and list items
   *
   * @param preserveWhitespace If true, ignorable whitespace will be preserved; otherwise, it will be ignored.
   */
  public void setPreserveWhitespace(boolean preserveWhitespace) {
    this.preserveWhitespace = preserveWhitespace;
  }

  /**
   * Indicates whether the loader is configured to preserve whitespace even in
   * contexts where it can be safely ignored, such as between table cells and
   * list items.
   *
   * @return true if ignorable whitespace will be preserved; false if it will be ignored.
   */
  public boolean isPreserveWhitespace() {
    return this.preserveWhitespace;
  }

  /**
   * Parses the given reader input and returns the corresponding PSMLElement.
   *
   * @param reader the reader supplying the input data to parse.
   * @return the resulting PSMLElement derived from the input data.
   * @throws IOException if an I/O error occurs during parsing.
   */
  public PSMLElement parse(Reader reader) throws IOException {
    InputSource source = new InputSource(reader);
    return parse(source);
  }

  /**
   * Parses the specified input source.
   *
   * @param source The source to parse
   *
   * @return The corresponding model
   *
   * @throws IOException Should an I/O error occur.
   */
  public PSMLElement parse(InputSource source) throws IOException {
    Handler handler = new Handler(this.preserveWhitespace);

    // Initialize the factory if needed
    if (this.factory == null) {
      SAXParserFactory f = SAXParserFactory.newInstance();
      f.setNamespaceAware(true);
      f.setValidating(false);
      this.factory = f;
    }

    // Run parser
    try {
      SAXParser parser = this.factory.newSAXParser();
      parser.parse(source, handler);
    } catch (SAXException | ParserConfigurationException ex) {
      throw new IOException(ex);
    }
    return handler.result;
  }

  /**
   * SAX Handler loading the model from SAX events.
   */
  public static final class Handler extends DefaultHandler {

    private final boolean preserveWhitespace;

    public Handler(boolean preserveWhitespace) {
      this.preserveWhitespace = preserveWhitespace;
    }

    /**
     * The current context, before it is committed
     */
    private final List<PSMLElement> context = new ArrayList<>(8);

    /**
     * Buffer for text nodes.
     */
    private final StringBuilder text = new StringBuilder();

    /**
     * The current context, before it is committed
     */
    private PSMLElement result = new PSMLElement(Name.UNKNOWN);

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      // Commit any text
      commitText();
      // Push a new element
      PSMLElement element = new PSMLElement(Name.forElement(qName));
      for (int i=0; i < attributes.getLength(); i++) {
        String name = attributes.getQName(i);
        String value = attributes.getValue(i);
        element.setAttribute(name, value);
      }
      push(element);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      // Commit any text
      commitText();
      // Pop element and attach to parent
      PSMLElement element = pop();
      PSMLElement parent = current();
      if (parent != null) {
        parent.addNode(element);
      } else {
        this.result = element;
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      this.text.append(ch, start, length);
    }

    /**
     * @return The current element in the stack.
     */
    private @Nullable PSMLElement current() {
      if (this.context.isEmpty()) return null;
      return this.context.get(this.context.size()-1);
    }

    /**
     * Remove element at the top of the stack.
     *
     * @return The element that was at the top of the stack
     */
    private PSMLElement pop() {
      return this.context.remove(this.context.size()-1);
    }

    /**
     * Add element at the top of the stack.
     *
     * @param element The element to add
     */
    private void push(PSMLElement element) {
      this.context.add(element);
    }

    private void commitText() {
      PSMLElement current = current();
      if (current != null && this.text.length() > 0) {
        if (this.preserveWhitespace || !isIgnorableSpace(this.text, current)) {
          current.addNode(new PSMLText(this.text.toString()));
        }
        this.text.setLength(0);
      }
    }

    /**
     * Determines if the given text represents ignorable whitespace in the context of the specified element.
     *
     * @param text The character sequence to evaluate.
     * @param element The PSMLElement used to determine contextual significance of the whitespace.
     * @return {@code true} if the text consists entirely of whitespace and the specified element is of a type
     *         where the whitespace can be considered ignorable; {@code false} otherwise.
     */
    private static boolean isIgnorableSpace(CharSequence text, PSMLElement element) {
      return isWhiteSpace(text) &&
          element.isAnyElement(
              Name.DOCUMENT, Name.DOCUMENTINFO, Name.SECTION,
              Name.FRAGMENT, Name.XREF_FRAGMENT, Name.PROPERTIES_FRAGMENT,
              Name.TABLE, Name.ROW, Name.LIST, Name.NLIST
          );
    }

    /**
     * Determines if the provided character sequence consists entirely of whitespace characters.
     *
     * @param text The character sequence to check.
     * @return {@code true} if all characters in the sequence are whitespace, or if the sequence is empty;
     *         {@code false} otherwise.
     */
    private static boolean isWhiteSpace(CharSequence text) {
      for (int i = 0; i < text.length(); i++) {
        if (!Character.isWhitespace(text.charAt(i))) {
          return false;
        }
      }
      return true;
    }
  }

}
