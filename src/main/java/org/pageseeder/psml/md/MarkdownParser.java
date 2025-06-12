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
package org.pageseeder.psml.md;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.spi.Parser;

/**
 * A parser for Markdown
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class MarkdownParser extends Parser {

  /**
   * The configuration instance used by the parser to customize its behavior.
   * This configuration determines aspects such as whether the output should
   * be a document or a fragment and the threshold for line breaks.
   */
  private Configuration config = new Configuration();

  @Override
  public String getMediatype() {
    return "text/markdown";
  }

  /**
   * Updates the configuration for the parser.
   *
   * <p>The provided configuration is used to customize
   * the behavior of the parser, such as determining if the output should be a document or a fragment,
   * and defining the line break threshold.
   *
   * @param config The configuration instance to be set, defining the behavior of the parser.
   */
  public void setConfig(Configuration config) {
    this.config = config;
  }

  /**
   * Retrieves the current configuration instance used by the parser.
   *
   * @return The configuration instance that defines the behavior of the parser.
   */
  public Configuration getConfig() {
    return this.config;
  }

  /**
   * Parses the content provided by a Reader into a PSMLElement structure.
   * The method processes the input lines to generate a structured representation
   * of the content, wrapped in an appropriate PSML element (e.g., document or fragment)
   * depending on the configuration.
   *
   * @param reader The reader providing the content to be parsed.
   * @return The root PSMLElement containing the parsed content, wrapped in an appropriate
   *         structure based on the configuration.
   * @throws IOException If an error occurs while reading from the provided reader.
   */
  @Override
  public PSMLElement parse(Reader reader) throws IOException {
    List<String> lines = toLines(reader);
    BlockParser parser = new BlockParser(config.toMarkdownInputOptions());
    List<PSMLElement> elements = parser.parse(lines);

    // Wrap the element based on the configuration
    PSMLElement wrapper;
    if (this.config.isFragment()) {
      wrapper = new PSMLElement(Name.FRAGMENT);
    } else {
      wrapper = new PSMLElement(Name.DOCUMENT);
      wrapper.setAttribute("level", "portable");
    }
    wrapper.addNodes(elements);

    return wrapper;
  }

  /**
   * Parses the contents provided by a reader into an HTML representation.
   * The method processes the input to produce a structured hierarchy of HTML elements.
   *
   * @param reader The reader providing the Markdown content to be parsed.
   * @return The root HTML element containing the parsed content, wrapped in an appropriate HTML tag
   *         (e.g., section or article) based on the configuration.
   * @throws IOException If an error occurs while reading from the provided reader.
   */
  public HTMLElement parseToHTML(Reader reader) throws IOException {
    List<String> lines = toLines(reader);
    HTMLBlockParser parser = new HTMLBlockParser();
    parser.setConfiguration(this.config);
    List<HTMLElement> elements = parser.parse(lines, this.config);

    // Wrap the element based on the configuration
    HTMLElement wrapper;
    if (this.config.isFragment()) {
      wrapper = new HTMLElement(HTMLElement.Name.SECTION);
    } else {
      wrapper = new HTMLElement(HTMLElement.Name.SECTION);
    }
    wrapper.addNodes(elements);

    return wrapper;
  }

  /**
   * Converts the content read from a Reader into a list of lines.
   * Each line corresponds to a single line of text read from the input source.
   *
   * @param reader The Reader instance providing the content to be processed.
   * @return A list of strings, where each string represents a single line of text extracted from the input.
   * @throws IOException If an error occurs while reading from the provided Reader.
   */
  private static List<String> toLines(Reader reader) throws IOException {
    List<String> lines = new ArrayList<>();
    BufferedReader r = new BufferedReader(reader);
    String line = r.readLine();
    while (line != null) {
      lines.add(line);
      line = r.readLine();
    }
    return lines;
  }

}
