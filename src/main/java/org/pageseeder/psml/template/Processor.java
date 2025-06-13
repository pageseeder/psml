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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Processes a new document template to generate new document instances.
 *
 * <p>A new instance can be created as:
 * <pre>{@code
 * Processor p = new Processor();
 * File template = new File([path to template]);
 * File psml = new File([path to document to create]);
 * Map<String, String> values = new HashMap<String, String>();
 * values.put([name], [value]);
 *  ...
 * p.process(template, psml, values);
 * }</pre>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class Processor {

  /**
   * The handler for the specified encoding (lazily loaded).
   */
  private final Charset charset;

  /**
   * The fragment to process if any, otherwise document.
   */
  private @Nullable String fragment;

  /**
   * Whether to fail when an error is found.
   */
  private boolean failOnError = false;

  /**
   * Creates a new processor.
   */
  public Processor() {
    this.charset = StandardCharsets.US_ASCII;
  }

  /**
   * Creates a new processor to generate the PSML using the specified encoding.
   */
  public Processor(Charset charset) {
    if (!TemplateFactory.isSupported(charset)) throw new IllegalArgumentException("Unsupported encoding");
    this.charset = charset;
  }

  /**
   * @param failOnError the failOnError to set
   */
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   * @param fragment the fragment to set
   */
  public void setFragment(String fragment) {
    this.fragment = fragment;
  }

  /**
   * Processes the template and write out the corresponding PSML.
   *
   * <p>If the writer wraps and <code>OutputStream</code>, ensure that the encoding used matches the encoding
   * specified for this class.
   *
   * @param template The PSML template to process.
   * @param psml     The PSML document to generate.
   * @param values   The values to fill out the placeholders
   *
   * @throws IOException Should an I/O error occur while reading the XML.
   * @throws TemplateException Should an error occur while parsing the XML.
   */
  public void process(Reader template, Writer psml, Map<String, String> values) throws IOException, TemplateException {
    InputSource source = new InputSource(template);
    PrintWriter out = new PrintWriter(psml);
    process(source, out, values);
  }

  /**
   * Processes the template and write out the corresponding PSML.
   *
   * <p>Note this method will automatically select the correct encoding for the file output.
   *
   * @throws IOException Should an I/O error occur while reading the XML.
   * @throws SAXException Should an error occur while parsing the XML.
   */
  public void process(File template, File psml, Map<String, String> values) throws IOException, TemplateException {
    InputSource source = new InputSource(template.toURI().toASCIIString());
    PrintWriter out = new PrintWriter(psml, this.charset);
    process(source, out, values);
  }

  /**
   * Transcodes the specified XML and saves into a file.
   *
   * <p>Note this method will automatically select the correct encoding for the file output.
   *
   * @throws IOException Should an I/O error occur while reading the XML.
   * @throws TemplateException Should an error occur while processing the template.
   */
  public void process(Reader template, File psml, Map<String, String> values) throws IOException, TemplateException {
    InputSource source = new InputSource(template);
    PrintWriter out = new PrintWriter(psml, this.charset);
    process(source, out, values);
  }

  /**
   * Process the template and generate the PSML using the specified values.
   *
   * @throws IOException Should an I/O error occur while reading the XML.
   * @throws TemplateException Should an error occur while procesing the template.
   */
  public void process(InputSource template, PrintWriter psml, Map<String, String> values) throws IOException, TemplateException {
    TemplateFactory factory = new TemplateFactory(this.charset);
    factory.setFragment(this.fragment);
    Template t = factory.parse(template);
    if (t == null) throw new TemplateException("No matching template");
    t.process(psml, values, this.failOnError);
  }

  public static void main(String[] args) throws IOException, TemplateException {
    if (args.length > 0) {

      // Grab template
      File f = new File(args[0]);
      InputSource source = new InputSource(f.toURI().toASCIIString());
      PrintWriter psml = new PrintWriter(System.out);
      Processor p = new Processor(StandardCharsets.US_ASCII);

      // Generate parameter map
      Map<String, String> parameters = null;
      for (String a : args) {
        if (parameters == null) {
          parameters = new HashMap<>();
        } else {
          int equal = a.indexOf('=');
          if (a.equals("-failonerror")) {
            p.setFailOnError(true);
          } else if (a.startsWith("-fragment:")) {
            p.setFragment(a.substring(a.indexOf(':')+1));
          } else if (equal > 0) {
            parameters.put(a.substring(0, equal), a.substring(equal+1));
          }
        }
      }
      System.err.println(parameters);

      // Process
      p.process(source, psml, parameters);
    } else {
      System.err.println("Processor [template] [param1=value1] [param2=value2]...");
    }

  }

}