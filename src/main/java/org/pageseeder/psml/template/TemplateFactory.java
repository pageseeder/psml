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
package org.pageseeder.psml.template;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.xml.XML;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A factory for templates
 *
 * <p>A new instance can be created as:
 * <pre>{@code
 * TemplateFactory factory = new TemplateFactory();
 * File file = new File([path to template]);
 * Template template = factory.parse(file);
 * }</pre>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class TemplateFactory {

  /**
   * Find placeholders in a variable: "{$variable-name}"
   */
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\$[^\\}]+\\}");

  /**
   * The charset below are supported target charset.
   *
   * <p>Note every Java platform must support the charset below.
   */
  private static final Charset[] SUPPORTED = new Charset[]{
      StandardCharsets.US_ASCII,
      StandardCharsets.UTF_8
  };

  /**
   * The character set used to encode the template's content and output.
   */
  private final Charset charset;

  /**
   * The parser to use (lazily loaded).
   */
  private @Nullable SAXParser parser;

  /**
   * <code>null</code> for a document otherwise it is the type of fragment.
   */
  private @Nullable String fragment;

  /**
   * Creates a new processor.
   */
  public TemplateFactory() {
    this.charset = StandardCharsets.UTF_8;
  }

  /**
   * Creates a new processor to generate the PSML using the specified encoding.
   *
   * @param charset The charset to use
   *
   * @throws IllegalArgumentException if the charset is not supported
   */
  public TemplateFactory(Charset charset) {
    if (!isSupported(charset)) throw new IllegalArgumentException("Unsupported encoding");
    this.charset = charset;
  }

  /**
   * @param fragment the fragment to set
   */
  public void setFragment(String fragment) {
    this.fragment = fragment;
  }

  /**
   * Parses the given template file and returns a processed {@link Template} instance.
   *
   * @param template the file representing the template to be parsed.
   * @return the parsed {@link Template} object.
   *
   * @throws IOException if an IO error occurs while reading the template file.
   * @throws TemplateException if an error occurs while processing the template.
   */
  public Template parse(File template) throws IOException, TemplateException {
    InputSource source = new InputSource(template.toURI().toASCIIString());
    return parse(source);
  }

  /**
   * Parses the given template reader and returns a processed {@link Template} instance.
   *
   * @param template the reader representing the template to be parsed.
   * @return the parsed {@link Template} object.
   *
   * @throws IOException if an IO error occurs while reading the template.
   * @throws TemplateException if an error occurs while processing the template.
   */
  public Template parse(Reader template) throws IOException, TemplateException {
    InputSource source = new InputSource(template);
    return parse(source);
  }

  /**
   * Parses the given template input source and returns a processed {@link Template} instance.
   *
   * @param template the input source representing the template to be parsed.
   * @return the parsed {@link Template} object.
   *
   * @throws IOException if an IO error occurs while reading the template.
   * @throws TemplateException if an error occurs while processing the template.
   */
  public Template parse(InputSource template) throws IOException, TemplateException {
    Handler handler = new Handler(this.charset, this.fragment);
    try {
      SAXParser parser = getParser();
      parser.parse(template, handler);
      return handler.getTemplate();
    } catch (SAXException ex) {
      throw new TemplateException(ex);
    }
  }

  // Static helpers
  // ==============================================================================================

  /**
   * Indicates what encoding are supported for output.
   *
   * @param cs the charset to use for the target XML output.
   * @return <code>true</code> is that charset is supported;
   *         <code>false</code> otherwise.
   */
  public static boolean isSupported(Charset cs) {
    for (Charset supported : SUPPORTED) {
      if (supported.equals(cs)) return true;
    }
    return false;
  }

  /**
   * @return a namespace aware, non validating parser.
   */
  private SAXParser getParser() {
    if (this.parser == null) {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      try {
        this.parser = factory.newSAXParser();
      } catch (ParserConfigurationException ex) {
        throw new UnsupportedOperationException(ex);
      } catch (SAXException ex) {
        throw new UnsupportedOperationException(ex);
      }
    }
    return this.parser;
  }

  /**
   * A namespace aware handler to process to generate a Template instance from a PSML template SAX stream.
   *
   * <p>The Template instance can be retrieved after a parse with the {@link #getTemplate()} method.
   *
   * @author Christophe Lauret
   * @version 26 June 2013
   */
  public class Handler extends DefaultHandler {

    // Class attributes
    // ==============================================================================================

    /**
     * Whether to include the XML declaration when generating the PSML.
     */
    private boolean includeXMLDeclaration = false;

    /**
     * Encodes the XML text and attribute values when needed.
     */
    private final Charset _charset;

    /**
     * Encodes the XML text and attribute values when needed.
     */
    private final XML.Encoder _encoder;

    /**
     * The current template builder
     */
    private final TemplateBuilder<? extends Template> _builder;

    /**
     * Holds PSML data to be written out.
     */
    private final StringBuilder buffer = new StringBuilder();

    /**
     * The builder for fragments (may be <code>null</code>).
     */
    private TFragment.Builder _fragment;

    /**
     * Set to true when an element has been left unclosed.
     */
    private boolean unclosed = false;

    /**
     * We use this to determine whether to output white spaces depth of PSML elements.
     */
    private int depth = 0;

    /**
     * When set to <code>true</code> we ignore any text data.
     */
    private boolean ignore = false;

    /**
     * The stack of parent elements.
     */
    private Deque<NSElement> parents = new ArrayDeque<>();

    /**
     * We use this to determine whether to output white spaces.
     */
    private NSElement previous = null;

    // Constructors and attributes
    // ==============================================================================================

    /**
     * Creates a new handler.
     *
     * @param charset  The charset for the XML output
     * @param fragment The fragment type
     */
    public Handler(@Nullable Charset charset, @Nullable String fragment) {
      if (charset == null) {
        charset = StandardCharsets.UTF_8;
      }
      if (!isSupported(charset)) throw new IllegalArgumentException("Only supports ASCII and UTF-8");
      this._charset = charset;
      if (fragment != null) {
        this._builder = new FragmentTemplate.Builder(charset, fragment);
      } else {
        this._builder = new DocumentTemplate.Builder(charset);
      }
      this._encoder = XML.getEncoder(charset);
    }

    /**
     * @param yes <code>true</code> to include the XML declaration.
     */
    public void includeXMLDeclaration(boolean yes) {
      this.includeXMLDeclaration = yes;
    }

    /**
     * @return the Template instance after a parse.
     */
    public Template getTemplate() {
      return this._builder.build();
    }

    @Override
    public void startDocument() throws SAXException {
      if (this.includeXMLDeclaration) {
        this.buffer.append("<?xml version=\"1.0\" encoding=\""+this._charset.name()+"\"?>");
      }
    }

    @Override
    public void endDocument() throws SAXException {
      checkPushData();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      checkUnclosed();
      if (Constants.NS_URI.equals(uri)) {
        startTemplateElement(localName, attributes);

      } else if (localName.endsWith("fragment") && this._fragment != null) {
        this._fragment.setKind(localName);
        String mediatype = attributes.getValue("mediatype");
        if (mediatype != null) {
          this._fragment.setMediatype(mediatype);
        }
        this.ignore = false;
      } else {
        startPSMLElement(qName, attributes);
      }
      // Maintain state
      NSElement element = new NSElement(uri, localName);
      this.previous = element;
      this.parents.push(element);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      this.parents.pop();
      if (Constants.NS_URI.equals(uri)) {
        endTemplateElement(localName);
      } else if (localName.endsWith("fragment") && this._fragment != null) {
        this.ignore = true;
      } else {
        endPSMLElement(qName);
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      checkUnclosed();
      NSElement element = this.previous;
      if (this.ignore) return;
      if (this.depth <= 1 && element != null && element.isTemplateElement("param")) {
        String characters = new String(ch, start, length);
        characters = characters.trim();
        this._encoder.text(characters.toCharArray(), 0, characters.length(), this.buffer);
      } else {
        this._encoder.text(ch, start, length, this.buffer);
      }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
      checkUnclosed();
      this.buffer.append('<').append('?').append(target).append(' ').append(data).append('?').append('>');
    }


    /**
     * Handle the template elements
     *
     * @param localName  The local name of the element.
     * @param attributes Attributes attached to the element
     */
    private void startTemplateElement(String localName, Attributes attributes) throws SAXException {
      // Don't output XML from the template namespace.
      if ("param".equals(localName)) {
        String name  = attributes.getValue("name");
        String value = attributes.getValue("default");
        String type  = attributes.getValue("type");
        if (this._fragment != null) {
          this._fragment.addParameter(name, value, ParameterType.forName(type));
        } else {
          this._builder.addParameter(name, value, ParameterType.forName(type));
        }

      } else if ("value".equals(localName)) {
        checkPushData();
        String name  = attributes.getValue("name");
        if (name == null) {
          this._builder.pushError("a value without a name is pointless");
        } else {
          if (this._fragment != null) {
            this._fragment.pushValue(name, false);
          } else {
            this._builder.pushValue(name, false);
          }
        }

      } else if ("fragment".equals(localName)) {
        checkPushData();
        String type = attributes.getValue("type");
        if (type == null) {
          this._builder.pushError("type is required for fragment templates");
        } else {
          this._fragment = new TFragment.Builder(type);
        }
        this.ignore = true;

      } else if ("fragment-ref".equals(localName)) {
        checkPushData();
        String id  = attributes.getValue("id");
        String type = attributes.getValue("type");
        this._builder.pushFragmentRef(id, type);

      } else if ("description".equals(localName)) {
        // Nothing to do

      } else {
        System.err.println("Found unknown template element: "+localName);
      }
    }

    /**
     * Handle the closing of template elements
     *
     * @param localName  The local name of the element.
     */
    private void endTemplateElement(String localName) throws SAXException {
      // End of a fragment definition
      if ("fragment".equals(localName) && this._fragment != null) {
        checkPushData();
        TFragment fragment = this._fragment.build();
        this._builder.addFragment(fragment);
        this._fragment = null;
        this.ignore = false; // just in case this wasn't reset
      }
    }

    /**
     * Start a PSML element.
     *
     * @param qName      the qualified name of the element.
     * @param attributes attributes attached to the element.
     */
    private void startPSMLElement(String qName, Attributes attributes) throws SAXException {
      // Copy the output verbatim
      StringBuilder psml = this.buffer;
      this.depth++;
      psml.append('<').append(qName);
      int length = attributes.getLength();
      for (int i=0; i < length; i++) {
        // ingnore template NS attributes
        if (Constants.NS_URI.equals(attributes.getURI(i))) continue;
        String name = attributes.getQName(i);
        String value = attributes.getValue(i);
        psml.append(' ').append(name).append("=\"");
        // Check attribute value
        Matcher m = PLACEHOLDER.matcher(value);
        int from = 0;
        while (m.find()) {
          // Check for any data for each match
          if (m.start() != from) {
            String data = value.substring(from, m.start());
            this._encoder.attribute(data, psml);
          }
          checkPushData();
          // Get the placeholder
          String placeholder = value.substring(m.start()+2, m.end()-1);
          if (this._fragment != null) {
            this._fragment.pushValue(placeholder, true);
          } else {
            this._builder.pushValue(placeholder, true);
          }
          from = m.end();
        }

        // Check the tail
        if (from != value.length()) {
          String data = value.substring(from, value.length());
          this._encoder.attribute(data, psml);
        }

        psml.append('"');
      }
      this.unclosed = true;
    }

    /**
     *
     * @param qName the qualified name of the element.
     */
    private void endPSMLElement(String qName) throws SAXException {
      this.depth--;
      if (this.unclosed) {
        this.buffer.append('/').append('>');
        this.unclosed = false;
      } else {
        this.buffer.append("</").append(qName).append('>');
      }
    }

    /**
     * Ensures that the element's open tag ends with a '>'
     */
    private void checkUnclosed() {
      if (this.unclosed) {
        this.buffer.append('>');
        this.unclosed = false;
      }
    }

    /**
     * Checks whether some data needs to be pushed out to the template.
     */
    private void checkPushData() {
      if (this.buffer.length() > 0) {
        if (this._fragment != null) {
          this._fragment.pushData(this.buffer.toString());
        } else {
          this._builder.pushData(this.buffer.toString());
        }
        this.buffer.setLength(0);
      }
    }

  }

  /**
   * Just to keep track of elements
   */
  private static class NSElement {

    final String uri;
    final String name;

    /**
     *
     */
    public NSElement(String uri, String name) {
      this.uri = uri;
      this.name = name;
    }

    public boolean isTemplateElement(String name) {
      return Constants.NS_URI.equals(this.uri) && this.name.equals(name);
    }

    @Override
    public String toString() {
      return "{"+this.uri+"}"+this.name;
    }
  }
}