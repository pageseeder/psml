/*
 * Copyright (c) 1999-2019 Allette systems pty. ltd.
 */
package org.pageseeder.psml.util;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;


/**
 * A utility class for common XSLT functions.
 *
 * @author Christophe Lauret
 */
public final class XSLT {

  /**
   * Maps XSLT templates to their URL as a string for easy retrieval.
   */
  private static final Map<String, Templates> CACHE = new Hashtable<>();

  /** Utility class. */
  private XSLT() {
  }

  /**
   * Returns the XSLT templates at the specified URL.
   *
   * <p>Templates are cached internally.
   *
   * @param url A URL to a template.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws XSLTException If XSLT templates could not be loaded from the specified URL.
   */
  public static Templates getTemplates(URL url) {
    if (url == null) return null;
    Templates templates = CACHE.get(url.toString());
    if (templates == null) {
      templates = toTemplates(url);
      CACHE.put(url.toString(), templates);
    }
    return templates;
  }

  /**
   * Return the XSLT templates from the given style.
   *
   * <p>This method will first try to load the resource using the class loader used for this class.
   *
   * <p>Use this class to load XSLT from the system.
   *
   * @param resource The path to a resource.
   *
   * @return the corresponding XSLT templates object;
   *         or <code>null</code> if the resource could not be found.
   *
   * @throws XSLTException If the loading fails.
   */
  public static Templates getTemplatesFromResource(String resource) {
    ClassLoader loader = XSLT.class.getClassLoader();
    URL url = loader.getResource(resource);
    if (url == null)
      throw new XSLTException("Unable to find templates at "+resource);
    return getTemplates(url);
  }

  /**
   * Utility function to transforms the specified XML source and returns the results as XML.
   *
   * Problems will be reported in the logs, the output will simply produce results as a comment.
   *
   * @param source     The Source XML data.
   * @param result     The Result XHTML data.
   * @param templates  The XSLT templates to use.
   * @param parameters Parameters to transmit to the transformer for use by the stylesheet (optional)
   *
   * @throws XSLTException For XSLT Transformation errors or XSLT configuration errors
   */
  public static void transform(File source, File result, Templates templates, Map<String, String> parameters) {
    try (InputStream in = new FileInputStream(source);
         OutputStream out = new FileOutputStream(result)) {
      // Prepare the input & output
      Source src = new StreamSource(new BufferedInputStream(in), source.toURI().toString());
      Result res = new StreamResult(new BufferedOutputStream(out));

      // Transform
      transform(src, res, templates, parameters);

    } catch (IOException ex) {
      throw new XSLTException(ex);
    }
  }

  /**
   * Utility function to transforms the specified XML source and returns the results as XML.
   *
   * Problems will be reported in the logs, the output will simply produce results as a comment.
   *
   * @param source     The Source XML data.
   * @param result     The Result data.
   * @param templates  The XSLT templates to use.
   * @param parameters Parameters to transmit to the transformer for use by the stylesheet (optional)
   *
   * @throws XSLTException For XSLT Transformation errors or XSLT configuration errors
   */
  public static void transform(Source source, Result result, Templates templates, Map<String, String> parameters) {
    try {
      // Create a transformer from the templates
      Transformer transformer = templates.newTransformer();

      // Transmit the properties to the transformer
      if (parameters != null) {

        for (Entry<String, String> e : parameters.entrySet()) {
          transformer.setParameter(e.getKey(), e.getValue());
        }
      }
      // Transform
      transformer.transform(source, result);

    } catch (TransformerException ex) {
      throw new XSLTException("Unable to transform ", ex);
    }
  }

  // private helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Return the XSLT templates from the given style.
   *
   * @param url A URL to a template.
   *
   * @return the corresponding XSLT templates object or <code>null</code> if the URL was <code>null</code>.
   *
   * @throws XSLTException If XSLT templates could not be loaded from the specified URL.
   */
  private static Templates toTemplates(URL url) {
    if (url == null) return null;
    // load the templates from the source URL
    Templates templates;
    try (InputStream in = url.openStream()) {
      Source source = new StreamSource(in);
      source.setSystemId(url.toString());
      TransformerFactory factory = TransformerFactory.newInstance();
      templates = factory.newTemplates(source);
    } catch (TransformerConfigurationException ex) {
      throw new XSLTException("Transformer exception while trying to load XSLT templates"+ url.toString(), ex);
    } catch (IOException ex) {
      throw new XSLTException("IO error while trying to load XSLT templates"+ url.toString(), ex);
    }
    return templates;
  }

}
