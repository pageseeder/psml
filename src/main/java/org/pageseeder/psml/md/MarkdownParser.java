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

import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.spi.Parser;

/**
 * A parser for Markdown
 *
 * @author Christophe Lauret
 */
public class MarkdownParser extends Parser {

  public MarkdownParser() {
  }

  @Override
  public String getMediatype() {
    return "text/markdown";
  }

  @Override
  public PSMLElement parse(Reader reader) throws IOException {
    List<String> lines = toLines(reader);
    Configuration config = new Configuration();
    BlockParser parser = new BlockParser();
    List<PSMLElement> elements = parser.parse(lines, config);

    PSMLElement document = new PSMLElement(Name.Document);
    document.addNodes(elements);

    return document;
  }



  /**
   *
   * @param reader The reader
   *
   * @return The list of line in the content
   *
   * @throws IOException If thrown while reading the contents
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
