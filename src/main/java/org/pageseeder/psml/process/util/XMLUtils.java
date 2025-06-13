/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.ProcessException;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A utility class for XML data.
 *
 * @version 29 August 2002
 * @author  Christophe Lauret
 */
public final class XMLUtils {

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
   */
  public static String escape(String s) {
    // bypass null and empty strings
    if (s == null || s.isEmpty()) return s;
    // do not process valid strings.
    if (s.indexOf('&') == -1 && s.indexOf('<') == -1) return s;
    // process the rest
    StringBuilder valid = new StringBuilder(s);
    int shift = 0;
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '&' :
          valid.insert(i + shift + 1, "amp;");
          shift += 4;
          break;
        case '<' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&lt;");
          shift += 3;
          break;
        default :
      }
    }
    return valid.toString();
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
   */
  public static String escapeForAttribute(String s) {
    // bypass null and empty strings
    if (s == null || "".equals(s)) return s;
    // do not process valid strings.
    if (s.indexOf('&') == -1 && s.indexOf('<') == -1 && s.indexOf('"') == -1) return s;
    // process the rest
    StringBuffer valid = new StringBuffer(s);
    int shift = 0;
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '&' :
          valid.insert(i + shift + 1, "amp;");
          shift += 4;
          break;
        case '"' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&quot;");
          shift += 5;
          break;
        case '\'' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&apos;");
          shift += 5;
          break;
        case '<' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&lt;");
          shift += 3;
          break;
        default :
      }
    }
    return valid.toString();
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
  public static Transformer createTransformer(File xslt, @Nullable ErrorListener listener) throws ProcessException {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      if (listener != null) {
        factory.setErrorListener(listener);
      }
      return factory.newTemplates(new StreamSource(xslt)).newTransformer();
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
  public static void transform(File in, File out, Transformer t, URL schema,
      List<String> errors, List<String> warnings) throws ProcessException {
    try {
      FileInputStream fis = new FileInputStream(in);
      FileOutputStream fos = new FileOutputStream(out);
      try {
        // run transform
        Source source = new SAXSource(XMLReaderFactory.createXMLReader(), new InputSource(fis));
        source.setSystemId(in.toURI().toString());
        t.transform(source,  new StreamResult(fos));
      } finally {
        fis.close();
        fos.close();
      }
      // validate now if needed
      if (schema != null) {
        fis = new FileInputStream(out);
        try {
          XMLReader reader = XMLReaderFactory.createXMLReader();
          reader.setFeature("http://xml.org/sax/features/validation", true);
          reader.setFeature("http://apache.org/xml/features/validation/schema", true);
          reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
          XMLParserErrorHandler errorHandler = new XMLParserErrorHandler();
          reader.setErrorHandler(errorHandler);
          String path = schema.toString().replaceAll(" ", "%20");
          reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", path);
          reader.parse(new InputSource(fis));
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
        } catch (IOException ex) {
          throw new ProcessException("Failed to read/write XML", ex);
        } finally {
          try {
            fis.close();
          } catch (IOException ex) {
            throw new ProcessException("Failed to close XSLT output stream", ex);
          }
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
      List<String> errors, List<String> warnings) throws ProcessException {
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
      List<String> errors, List<String> warnings) throws ProcessException {

    try {
      parse(new InputSource(in), handler, errors, warnings);
    } finally {
      try {
        in.close();
      } catch (IOException ex) {
        throw new ProcessException("Failed to close PSML: " + ex.getMessage(), ex);
      }
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
      @Nullable List<String> errors, @Nullable List<String> warnings) throws ProcessException {
    try {
      // use the SAX parser factory to set features
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      // set not namespace aware
      factory.setNamespaceAware(false);
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
    } catch (SAXException ex) {
      throw new ProcessException(ex.getMessage(), ex);
    } catch (ParserConfigurationException ex) {
      throw new ProcessException(ex.getMessage(), ex);
    }
  }

}