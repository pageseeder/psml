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

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MarkdownParserTest {

  private static final String SOURCE_FOLDER = "src/test/data/md";

  @Test
  void testParseFragment_Headings() {
    PSMLElement document = parseMarkdown("headings.md", false);
    assertNotNull(document);
    assertEquals(PSMLElement.Name.DOCUMENT, document.getElement());
    System.out.println(toXML(document));
  }

  @Test
  void testParseFragment_Metadata() {
    PSMLElement document = parseMarkdown("metadata.md", false);
    assertNotNull(document);
    assertEquals(PSMLElement.Name.DOCUMENT, document.getElement());
    System.out.println(toXML(document));
  }

  @Test
  void testParseFragment_Table() {
    PSMLElement fragment = parseMarkdown("table.md", true);
    assertNotNull(fragment);
    assertEquals(PSMLElement.Name.FRAGMENT, fragment.getElement());
    System.out.println(toXML(fragment));
  }

  @Test
  void testProcess() throws Exception {
    File md = new File(SOURCE_FOLDER, "test.md");
    File psml = new File(SOURCE_FOLDER, "test.psml");
    FileInputStream in = new FileInputStream(md);
    Reader r = new InputStreamReader(in);
    MarkdownParser parser = new MarkdownParser();
    parser.getConfig().setFragmentMode(false);
    PSMLElement document = parser.parse(r);

    XMLWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    document.toXML(xml);
    xml.flush();
    String result = xml.toString();
    System.out.println(result);

    // load expected
    String expected = Files.readString(psml.toPath());
    expected = expected.replaceAll("\r", "");
    assertEquals(expected, result);
  }

  @Test
  void testProcess2() throws Exception {
    File md = new File(SOURCE_FOLDER, "test2.md");
    File psml = new File(SOURCE_FOLDER, "test2.psml");
    FileInputStream in = new FileInputStream(md);
    Reader r = new InputStreamReader(in);
    MarkdownParser parser = new MarkdownParser();
    parser.getConfig().setFragmentMode(false);
    PSMLElement document = parser.parse(r);

    XMLWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    document.toXML(xml);
    xml.flush();
    String result = xml.toString();
    System.out.println(result);

    // load expected
    String expected = Files.readString(psml.toPath());
    expected = expected.replaceAll("\r", "");
    assertEquals(expected, result);
  }

  private static PSMLElement parseMarkdown(String filename, boolean isFragment) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(Tests.class.getResourceAsStream("/org/pageseeder/psml/md/" + filename)))) {
      MarkdownParser parser = new MarkdownParser();
      parser.getConfig().setFragmentMode(isFragment);
      return parser.parse(reader);
    } catch (IOException ex) {
      throw new UncheckedIOException("Unable to load test file '" + filename + "'", ex);
    }
  }

  private static String toXML(PSMLElement element) {
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    try {
      element.toXML(xml);
      xml.flush();
      return xml.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
