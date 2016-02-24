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

import org.pageseeder.psml.model.PSMLElement.Name;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class loads
 *
 * @author clauret
 */
public final class Loader {

  /**
   * Lazily loaded SAX parser factory.
   */
  private SAXParserFactory factory = null;

  public Loader() {
  }

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
    Handler handler = new Handler();

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
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
    PSMLElement element = handler.result;
    return element;
  }

  /**
   * SAX Handler loading the model from SAX events.
   */
  public static final class Handler extends DefaultHandler {

    /**
     * The current context, before it is committed
     */
    private List<PSMLElement> context = new ArrayList<>(8);

    /**
     * Buffer for text nodes.
     */
    private StringBuilder text = new StringBuilder();

    /**
     * The current context, before it is committed
     */
    private PSMLElement result = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      // Commit any text
      commitText();
      // Push new element
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
    private PSMLElement current() {
      if (this.context.size() == 0) return null;
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
      if (current != null) {
        current.addNode(new PSMLText(this.text.toString()));
        this.text.setLength(0);
      }
    }

  }

}
