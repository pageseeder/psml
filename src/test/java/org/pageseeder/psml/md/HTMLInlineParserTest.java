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
import org.pageseeder.psml.html.HTMLNode;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for InlineParser functionality, ensuring accurate parsing of Markdown text
 * into the corresponding PSML format.
 */
final class HTMLInlineParserTest {

  @Test
  void testEscaping() {
    assertEquals("*test*", toHTML("\\*test\\*"));
    assertEquals("**test**", toHTML("\\*\\*test\\*\\*"));
    assertEquals("_test_", toHTML("\\_test\\_"));
    assertEquals("__test__", toHTML("\\_\\_test\\_\\_"));
    assertEquals("`test`", toHTML("\\`test\\`"));
    assertEquals("![Alt text](/path/to/img.jpg)", toHTML("\\!\\[Alt text\\](/path/to/img.jpg)"));
    assertEquals("[test](http://example.net/)", toHTML("\\[test\\](http://example.net/)"));
    assertEquals("<a href=\"http://example.net/my_doc.html\">my_doc</a>",
        toHTML("[my\\_doc](http://example.net/my\\_doc.html)"));
    assertEquals("<a href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</a>",
        toHTML("[http://example.net/my\\_doc.html](http://example.net/my\\_doc.html)"));
    assertEquals("<a href=\"http://example.net/my_doc.html\">example.net/my_doc.html</a>",
        toHTML("<http://example.net/my\\_doc.html>"));
    assertEquals("<a href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</a>",
        toHTML("http://example.net/my\\_doc.html"));
    assertEquals("&lt;http://example.org&gt;", toHTML("\\<http://example.org\\>"));
  }

  @Test
  void testDoubleEmphasis() {
    assertEquals("<strong>test</strong>", toHTML("**test**"));
    assertEquals("<strong>test</strong>", toHTML("__test__"));
    assertEquals("A <strong>test</strong>!", toHTML("A **test**!"));
    assertEquals("A <strong>test</strong>!", toHTML("A __test__!"));
    assertEquals("extra<strong>test</strong>", toHTML("extra**test**"));
    assertEquals("extra<strong>test</strong>", toHTML("extra__test__"));
    assertEquals("<strong>extra</strong>test", toHTML("**extra**test"));
    assertEquals("<strong>extra</strong>test", toHTML("__extra__test"));
    assertEquals("ex<strong>tra</strong>test", toHTML("ex**tra**test"));
    assertEquals("ex<strong>tra</strong>test", toHTML("ex__tra__test"));
    assertEquals("<strong/>", toHTML("****"));
    assertEquals("<strong/>", toHTML("____"));
  }

  @Test
  void testSimpleEmphasis() {
    assertEquals("<em>test</em>", toHTML("*test*"));
    assertEquals("<em>test</em>", toHTML("_test_"));
    assertEquals("A <em>test</em>!", toHTML("A *test*!"));
    assertEquals("A <em>test</em>!", toHTML("A _test_!"));
    assertEquals("extra<em>test</em>", toHTML("extra*test*"));
    assertEquals("extra_test_", toHTML("extra_test_"));
    assertEquals("<em>extra</em>test", toHTML("*extra*test"));
    assertEquals("_extra_test", toHTML("_extra_test"));
    assertEquals("ex<em>tra</em>test", toHTML("ex*tra*test"));
    assertEquals("ex_tra_test", toHTML("ex_tra_test"));
    assertEquals("<em/>", toHTML("**"));
    assertEquals("<em/>", toHTML("__"));
  }

  @Test
  void testCode() {
    assertEquals("<code>test</code>", toHTML("`test`"));
    assertEquals("A <code>test</code>!", toHTML("A `test`!"));
    assertEquals("extra<code>test</code>", toHTML("extra`test`"));
    assertEquals("<code>extra</code>test", toHTML("`extra`test"));
    assertEquals("ex<code>tra</code>test", toHTML("ex`tra`test"));
    assertEquals("<code/>", toHTML("``"));

    // From Markdown spec
    assertEquals("Use the <code>printf()</code> function.", toHTML("Use the `printf()` function."));
    assertEquals("Please don't use any <code>&lt;blink&gt;</code> tags.", toHTML("Please don't use any `<blink>` tags."));
    assertEquals("<code>&amp;#8212;</code> is the decimal-encoded equivalent of <code>&amp;mdash;</code>.", toHTML("`&#8212;` is the decimal-encoded equivalent of `&mdash;`."));
  }

  @Test
  void testCodeEscape() {
    assertEquals("<code>test</code>", toHTML("`` test ``"));
    assertEquals("<code>`test`</code>", toHTML("`` `test` ``"));
    assertEquals("A <code>test</code>!", toHTML("A `` test ``!"));
    assertEquals("A <code>`test`</code>!", toHTML("A `` `test` ``!"));

    // From Markdown spec
    assertEquals("<code>There is a literal backtick (`) here.</code>", toHTML("``There is a literal backtick (`) here.``"));
    assertEquals("A single backtick in a code span: <code>`</code>", toHTML("A single backtick in a code span: `` ` ``"));
    assertEquals("A backtick-delimited string in a code span: <code>`foo`</code>", toHTML("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  void testImage() {
    assertEquals("<img alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", toHTML("![Alt text](/path/to/img.jpg)"));
  }

  @Test
  void testRef() {
    assertEquals("<a href=\"http://example.net/\">test</a>", toHTML("[test](http://example.net/)"));
  }

  @Test
  void testLink() {
    assertEquals("<a href=\"http://example.org\">example.org</a>", toHTML("<http://example.org>"));
    assertEquals("<a href=\"https://example.org\">example.org</a>", toHTML("<https://example.org>"));
    assertEquals("<a href=\"mailto:test@example.org\">test@example.org</a>", toHTML("<mailto:test@example.org>"));
  }

  @Test
  void testAutolink() {
    assertEquals("<a href=\"http://example.org\">http://example.org</a>", toHTML("http://example.org"));
    assertEquals("<a href=\"https://example.org\">https://example.org</a>", toHTML("https://example.org"));
    // Included characters
    assertEquals("<a href=\"http://example.org/\">http://example.org/</a>", toHTML("http://example.org/"));
    assertEquals("<a href=\"https://example.org/\">https://example.org/</a>", toHTML("https://example.org/"));
    // Punctuation
    assertEquals("<a href=\"http://example.org\">http://example.org</a>,", toHTML("http://example.org,"));
    assertEquals("<a href=\"http://example.org\">http://example.org</a>.", toHTML("http://example.org."));
    assertEquals("<a href=\"http://example.org\">http://example.org</a>;", toHTML("http://example.org;"));
    assertEquals("<a href=\"http://example.org\">http://example.org</a>!", toHTML("http://example.org!"));
    assertEquals("<a href=\"http://example.org\">http://example.org</a>:", toHTML("http://example.org:"));
    assertEquals("<a href=\"http://example.org\">http://example.org</a>?", toHTML("http://example.org?"));
    // Brackets
    assertEquals("(<a href=\"http://example.org\">http://example.org</a>)", toHTML("(http://example.org)"));
    assertEquals("{<a href=\"http://example.org\">http://example.org</a>}", toHTML("{http://example.org}"));
    assertEquals("[<a href=\"http://example.org\">http://example.org</a>]", toHTML("[http://example.org]"));
    // With path
    assertEquals("<a href=\"http://example.org/home.html\">http://example.org/home.html</a>,", toHTML("http://example.org/home.html,"));
    assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>", toHTML("http://example.org/home"));
    assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>.", toHTML("http://example.org/home."));
    assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>,", toHTML("http://example.org/home,"));
    assertEquals("<a href=\"http://example.org/home/\">http://example.org/home/</a>", toHTML("http://example.org/home/"));
    // With parameters
    assertEquals("<a href=\"http://example.org?t\">http://example.org?t</a>", toHTML("http://example.org?t"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>", toHTML("http://example.org?t="));
    assertEquals("<a href=\"http://example.org?t=_\">http://example.org?t=_</a>", toHTML("http://example.org?t=_"));
    assertEquals("<a href=\"http://example.org?t=-\">http://example.org?t=-</a>", toHTML("http://example.org?t=-"));
    assertEquals("<a href=\"http://example.org?t==\">http://example.org?t==</a>", toHTML("http://example.org?t=="));
    assertEquals("<a href=\"http://example.org?t=/\">http://example.org?t=/</a>", toHTML("http://example.org?t=/"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>,", toHTML("http://example.org?t=,"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>.", toHTML("http://example.org?t=."));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>;", toHTML("http://example.org?t=;"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>:", toHTML("http://example.org?t=:"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>?", toHTML("http://example.org?t=?"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>!", toHTML("http://example.org?t=!"));
    assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>)", toHTML("http://example.org?t=)"));
  }

  /**
   * Returns the Markdown text as PSML using the inline parser.
   *
   * @param text The text to parse
   * @return The corresponding PSML as a string.
   */
  private static String toHTML(String text) {
    try {
      HTMLInlineParser parser = new HTMLInlineParser();
      List<HTMLNode> nodes = parser.parse(text);
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      for (HTMLNode n : nodes) {
        n.toXML(xml);
      }
      xml.flush();
      return xml.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new UncheckedIOException(ex);
    }
  }

}
