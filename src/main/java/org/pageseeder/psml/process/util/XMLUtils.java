/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.xml.XML;
import org.pageseeder.psml.xml.XMLStrings;
import org.xml.sax.*;

/**
 * A utility class for XML data.
 *
 * @author  Christophe Lauret
 *
 * @since 0.9.0
 */
public final class XMLUtils {

  /**
   * Compiled schemas, keyed by URL. Schema instances are immutable and thread-safe.
   */
  private static final Map<String, Schema> SCHEMAS = new ConcurrentHashMap<>();

  /**
   * Prevent creation of instances.
   */
  private XMLUtils() {
  }

  /**
   * Replace characters which are invalid in element values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>these characters are:</p>
   * <ul>
   *  <li>{@code '&amp'} by the ampersand entity {@code "&amp;amp"}</li>
   *  <li>{@code '&lt;'} by the entity {@code "&amp;lt;"}</li>
   * </ul>
   *
   * <p>Empty strings or <code>null</code> return respectively "" and <code>null</code>.
   *
   * <p>Note: this function assumes that there are no entities in
   * the given String. If there are existing entities, then the
   * ampersand character will be escaped by the ampersand entity.
   *
   * @param  s The String to be parsed
   *
   * @return a valid string or empty if s is <code>null</code> or empty.
   *
   * @deprecated Use {@link XMLStrings#text(String)} instead.
   */
  @Deprecated(since = "1.7.0", forRemoval = true)
  public static @Nullable String escape(@Nullable String s) {
    return XMLStrings.nullableText(s);
  }

  /**
   * Replace characters which are invalid in attribute values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>these characters are:</p>
   * <ul>
   *  <li>{@code '&amp'} by the ampersand entity {@code "&amp;amp"}</li>
   *  <li>{@code '&lt;'} by the entity {@code "&amp;lt;"}</li>
   *  <li>{@code '"'} by the entity {@code "&amp;quot;"}</li>
   *  <li>{@code '''} by the entity {@code "&amp;apos;"}</li>
   * </ul>
   *
   * <p>Empty strings or <code>null</code> return respectively
   * "" and <code>null</code>.
   *
   * <p>Note: this function assumes that there are no entities in
   * the given String. If there are existing entities, then the
   * ampersand entity will replace the ampersand character.
   *
   * @param  s The String to be parsed
   *
   * @return a valid string or empty if s is <code>null</code> or empty.
   *
   * @deprecated Use {@link XMLStrings#attribute(String)} instead.
   */
  @Deprecated(since = "1.7.0", forRemoval = true)
  public static @Nullable String escapeForAttribute(@Nullable String s) {
    return XMLStrings.nullableAttribute(s);
  }

  /**
   * Create the templates for the PSML transform.
   *
   * @param xslt      the XSLT script
   * @param listener  an error listener (optional)
   *
   * @return the templates.
   *
   * @throws ProcessException if creating the transformer failed
   */
  public static Templates createTemplates(File xslt, @Nullable ErrorListener listener) throws ProcessException {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      if (listener != null) {
        factory.setErrorListener(listener);
      }
      return factory.newTemplates(new StreamSource(xslt));
    } catch (TransformerConfigurationException e) {
      throw new ProcessException("Failed to load XSLT stylesheet: " + e.getMessageAndLocation(), e);
    } catch (TransformerFactoryConfigurationError e) {
      throw new ProcessException("Failed to load XSLT stylesheet", e);
    }
  }

  /**
   * Transform the input to the output using the transformer.
   *
   * @param in  the XML input
   * @param out the XML output
   * @param t   the transformer
   *
   * @throws ProcessException if the transformation failed
   */
  public static void transform(File in, File out, Transformer t) throws ProcessException {
    transform(in, out, t, null, null, null);
  }

  /**
   * Transform the input to the output using the transformer.
   *
   * @param in        the XML input
   * @param out       the XML output
   * @param t         the transformer
   * @param schema    the schema to use to validate the output
   * @param errors    where validation errors are listed
   * @param warnings  where validation warnings are listed
   *
   * @throws ProcessException if the transformation failed
   */
  public static void transform(
      File in,
      File out,
      Transformer t,
      @Nullable URL schema,
      @Nullable List<String> errors,
      @Nullable List<String> warnings
  ) throws ProcessException {
    final Path inPath = in.toPath();
    final Path outPath = out.toPath();

    try {

      // Transform
      try (InputStream fis = Files.newInputStream(inPath);
           OutputStream fos = Files.newOutputStream(outPath)) {
        Source source = new SAXSource(newXMLReader(), new InputSource(fis));
        source.setSystemId(in.toURI().toString());
        t.transform(source, new StreamResult(fos));
      }

      // validate now if needed
      if (schema != null) {
        Schema compiled;
        try {
          compiled = getSchema(schema);
        } catch (SAXException ex) {
          throw new ProcessException("Failed to load schema " + schema + ": " + ex.getMessage(), ex);
        }
        try (InputStream fis = Files.newInputStream(outPath)) {
          Validator validator = compiled.newValidator();
          XMLParserErrorHandler errorHandler = new XMLParserErrorHandler();
          validator.setErrorHandler(errorHandler);
          validator.validate(new StreamSource(fis, out.toURI().toString()));
          if (errors != null)
            errors.addAll(errorHandler.getErrors());
          else if (errorHandler.hasErrors()) {
            StringBuilder all = new StringBuilder();
            for (String e : errorHandler.getErrors()) { all.append(e).append("\n"); }
            throw new ProcessException("Error when validating "+out.getAbsolutePath()+": "+all.toString());
          }
          if (warnings != null)
            warnings.addAll(errorHandler.getWarnings());
        } catch (SAXException ex) {
          throw new ProcessException("Error when validating XSLT output: " + ex.getMessage(), ex);
        }
      }
    } catch (SAXException ex) {
      throw new ProcessException("Failed to create XML reader for XSLT transformation", ex);
    } catch (TransformerConfigurationException ex) {
      throw new ProcessException("Failed to create XSLT transformer", ex);
    } catch (TransformerException ex) {
      throw new ProcessException("Failed to transform XML", ex);
    } catch (IOException ex) {
      throw new ProcessException("Failed to read/write XML", ex);
    }
  }

  /**
   * Returns the compiled schema for the URL, compiling and caching it on first use.
   */
  private static Schema getSchema(URL url) throws SAXException {
    String key = url.toExternalForm();
    Schema schema = SCHEMAS.get(key);
    if (schema == null) {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      try {
        factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
      } catch (SAXNotRecognizedException | SAXNotSupportedException ignore) {
        // this feature only checks the schema not the content, so carry on without it
      }
      schema = factory.newSchema(url);
      Schema existing = SCHEMAS.putIfAbsent(key, schema);
      if (existing != null) schema = existing;
    }
    return schema;
  }

  /**
   * Creates a new namespace-aware XML reader (same behaviour as XMLReaderFactory.createXMLReader()).
   */
  private static XMLReader newXMLReader() throws SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      return factory.newSAXParser().getXMLReader();
    } catch (ParserConfigurationException ex) {
      throw new SAXException("Unable to configure SAX parser", ex);
    }
  }
  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input
   * @param handler  the XML handler
   *
   * @throws ProcessException if the parsing failed
   */
  public static void parse(File in, ContentHandler handler) throws ProcessException {
    parse(in, handler, null, null);
  }

  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input
   * @param handler  the XML handler
   * @param errors   where errors are listed
   * @param warnings where warnings are listed
   *
   * @throws ProcessException if the parsing failed
   */
  public static void parse(File in, ContentHandler handler,
                           @Nullable List<String> errors,
                           @Nullable List<String> warnings) throws ProcessException {
    try {
      parse(new FileInputStream(in), handler, errors, warnings);
    } catch (FileNotFoundException ex) {
      throw new ProcessException("Invalid File "+in.getAbsolutePath(), ex);
    }
  }

  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input
   * @param handler  the XML handler
   *
   * @throws ProcessException if the parsing failed
   */
  public static void parse(InputStream in, ContentHandler handler) throws ProcessException {
    parse(in, handler, null, null);
  }

  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input (will be closed!)
   * @param handler  the XML handler
   * @param errors   where errors are listed
   * @param warnings where warnings are listed
   *
   * @throws ProcessException if the parsing failed
   */
  public static void parse(InputStream in, ContentHandler handler,
                           @Nullable List<String> errors,
                           @Nullable List<String> warnings) throws ProcessException {

    try (in) {
      parse(new InputSource(in), handler, errors, warnings);
    } catch (IOException ex) {
      throw new ProcessException("Failed to close PSML: " + ex.getMessage(), ex);
    }
}

  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input
   * @param handler  the XML handler
   * @param errors   where errors are listed (optional)
   * @param warnings where warnings are listed (optional)
   *
   * @throws ProcessException if the parsing failed
   */
  public static void parse(InputSource in, ContentHandler handler,
                           @Nullable List<String> errors,
                           @Nullable List<String> warnings) throws ProcessException {
    try {
      // use the SAX parser factory to set features
      SAXParserFactory factory = XML.newSAXParserFactory();
      // get reader
      XMLReader reader = factory.newSAXParser().getXMLReader();
      // set handlers
      reader.setContentHandler(handler);
      XMLParserErrorHandler errorHandler = new XMLParserErrorHandler();
      reader.setErrorHandler(errorHandler);
      // parse
      reader.parse(in);
      if (errors != null)
        errors.addAll(errorHandler.getErrors());
      else if (errorHandler.hasErrors()) {
        StringBuilder all = new StringBuilder();
        for (String e : errorHandler.getErrors()) { all.append(e).append("\n"); }
        throw new ProcessException(all.toString());
      }
      if (warnings != null)
        warnings.addAll(errorHandler.getWarnings());
    } catch (IOException ex) {
      throw new ProcessException("Failed to read/write PSML: " + ex.getMessage(), ex);
    } catch (SAXException | ParserConfigurationException ex) {
      throw new ProcessException(ex.getMessage(), ex);
    }
  }

}