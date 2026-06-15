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
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class InlineParserTest {

  @Test
  void testUnescaping() {
    assertEquals("Escape ~ ` ! @ # $ % ^ & * ( ) _ - + = { [ } ] | \\ : ; \" ' < , > . ? / text",
        InlineParser.unescape("Escape ~ \\` \\! @ # $ % ^ & \\* ( ) \\_ - + = { \\[ } \\] | \\\\ : ; \" ' \\< , \\> . ? / text"));
  }

  @Test
  void testEscaping() {
    assertEquals("*test*", toPSML("\\*test\\*"));
    assertEquals("**test**", toPSML("\\*\\*test\\*\\*"));
    assertEquals("_test_", toPSML("\\_test\\_"));
    assertEquals("__test__", toPSML("\\_\\_test\\_\\_"));
    assertEquals("`test`", toPSML("\\`test\\`"));
    assertEquals("![Alt text](/path/to/img.jpg)", toPSML("\\!\\[Alt text\\](/path/to/img.jpg)"));
    assertEquals("[test](http://example.net/)", toPSML("\\[test\\](http://example.net/)"));
    assertEquals("<link href=\"http://example.net/my_doc.html\">my_doc</link>",
        toPSML("[my\\_doc](http://example.net/my\\_doc.html)"));
    assertEquals("<link href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</link>",
        toPSML("[http://example.net/my\\_doc.html](http://example.net/my\\_doc.html)"));
    assertEquals("<link href=\"http://example.net/my_doc.html\">example.net/my_doc.html</link>",
        toPSML("<http://example.net/my\\_doc.html>"));
    assertEquals("<link href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</link>",
        toPSML("http://example.net/my\\_doc.html"));
    assertEquals("&lt;http://example.org&gt;", toPSML("\\<http://example.org\\>"));
  }

  @Test
  void testDoubleEmphasis() {
    assertEquals("<bold>test</bold>", toPSML("**test**"));
    assertEquals("<bold>test</bold>", toPSML("__test__"));
    assertEquals("A <bold>test</bold>!", toPSML("A **test**!"));
    assertEquals("A <bold>test</bold>!", toPSML("A __test__!"));
    assertEquals("extra<bold>test</bold>", toPSML("extra**test**"));
    assertEquals("extra<bold>test</bold>", toPSML("extra__test__"));
    assertEquals("<bold>extra</bold>test", toPSML("**extra**test"));
    assertEquals("<bold>extra</bold>test", toPSML("__extra__test"));
    assertEquals("ex<bold>tra</bold>test", toPSML("ex**tra**test"));
    assertEquals("ex<bold>tra</bold>test", toPSML("ex__tra__test"));
    assertEquals("<bold/>", toPSML("****"));
    assertEquals("<bold/>", toPSML("____"));
  }

  @Test
  void testSimpleEmphasis() {
    assertEquals("<italic>test</italic>", toPSML("*test*"));
    assertEquals("<italic>test</italic>", toPSML("_test_"));
    assertEquals("A <italic>test</italic>!", toPSML("A *test*!"));
    assertEquals("A <italic>test</italic>!", toPSML("A _test_!"));
    assertEquals("extra<italic>test</italic>", toPSML("extra*test*"));
    assertEquals("extra_test_", toPSML("extra_test_"));
    assertEquals("<italic>extra</italic>test", toPSML("*extra*test"));
    assertEquals("_extra_test", toPSML("_extra_test"));
    assertEquals("ex<italic>tra</italic>test", toPSML("ex*tra*test"));
    assertEquals("ex_tra_test", toPSML("ex_tra_test"));
    assertEquals("<italic/>", toPSML("**"));
    assertEquals("<italic/>", toPSML("__"));
  }

  @Test
  void testCode() {
    assertEquals("<monospace>test</monospace>", toPSML("`test`"));
    assertEquals("A <monospace>test</monospace>!", toPSML("A `test`!"));
    assertEquals("extra<monospace>test</monospace>", toPSML("extra`test`"));
    assertEquals("<monospace>extra</monospace>test", toPSML("`extra`test"));
    assertEquals("ex<monospace>tra</monospace>test", toPSML("ex`tra`test"));
    assertEquals("<monospace/>", toPSML("``"));

    // From Markdown spec
    assertEquals("Use the <monospace>printf()</monospace> function.", toPSML("Use the `printf()` function."));
    assertEquals("Please don't use any <monospace>&lt;blink&gt;</monospace> tags.", toPSML("Please don't use any `<blink>` tags."));
    assertEquals("<monospace>&amp;#8212;</monospace> is the decimal-encoded equivalent of <monospace>&amp;mdash;</monospace>.", toPSML("`&#8212;` is the decimal-encoded equivalent of `&mdash;`."));
  }

  @Test
  void testCodeEscape() {
    assertEquals("<monospace>test</monospace>", toPSML("`` test ``"));
    assertEquals("<monospace>`test`</monospace>", toPSML("`` `test` ``"));
    assertEquals("A <monospace>test</monospace>!", toPSML("A `` test ``!"));
    assertEquals("A <monospace>`test`</monospace>!", toPSML("A `` `test` ``!"));

    // From Markdown spec
    assertEquals("<monospace>There is a literal backtick (`) here.</monospace>", toPSML("``There is a literal backtick (`) here.``"));
    assertEquals("A single backtick in a code span: <monospace>`</monospace>", toPSML("A single backtick in a code span: `` ` ``"));
    assertEquals("A backtick-delimited string in a code span: <monospace>`foo`</monospace>", toPSML("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  void testPlainText() {
    assertEquals("", toPSML(""));
    assertEquals("Hello world", toPSML("Hello world"));
    assertEquals("Hello, world!", toPSML("Hello, world!"));
    assertEquals("1 + 1 = 2", toPSML("1 + 1 = 2"));
  }

  @Test
  void testMixedInline() {
    assertEquals("<bold>bold</bold> and <italic>italic</italic>", toPSML("**bold** and *italic*"));
    assertEquals("<monospace>code</monospace> and <bold>bold</bold>", toPSML("`code` and **bold**"));
    assertEquals("Text <italic>italic</italic> more <monospace>code</monospace> end", toPSML("Text *italic* more `code` end"));
    assertEquals("<bold>bold</bold> <italic>italic</italic> <monospace>code</monospace>", toPSML("**bold** *italic* `code`"));
  }

  @Test
  void testImage() {
    assertEquals("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", toPSML("![Alt text](/path/to/img.jpg)"));
    assertEquals("<image alt=\"\" src=\"/img.jpg\"/>", toPSML("![](/img.jpg)"));
    assertEquals("before <image alt=\"alt\" src=\"/img.jpg\"/> after", toPSML("before ![alt](/img.jpg) after"));
  }

  @Test
  void testImageExternal() {
    // External images (http/https src) are rendered as links
    assertEquals("<link href=\"http://example.com/img.png\">Alt text</link>", toPSML("![Alt text](http://example.com/img.png)"));
    assertEquals("<link href=\"https://example.com/img.png\">Alt text</link>", toPSML("![Alt text](https://example.com/img.png)"));
    assertEquals("<link href=\"http://example.com/img.png\"/>", toPSML("![](http://example.com/img.png)"));
  }

  @Test
  void testRef() {
    assertEquals("<link href=\"http://example.net/\">test</link>", toPSML("[test](http://example.net/)"));
    assertEquals("<link href=\"http://example.net/\"/>", toPSML("[](http://example.net/)"));
    assertEquals("before <link href=\"http://example.net/\">test</link> after", toPSML("before [test](http://example.net/) after"));
  }

  @Test
  void testRefXRef() {
    // Non-HTTP refs produce XREF elements pointing to internal documents
    assertEquals("<xref frag=\"default\" href=\"page.psml\" reverselink=\"true\" title=\"title\" display=\"manual\">title</xref>",
        toPSML("[title](page.psml)"));
    assertEquals("<xref frag=\"section1\" href=\"page.psml\" reverselink=\"true\" title=\"title\" display=\"manual\">title</xref>",
        toPSML("[title](page.psml#section1)"));
  }

  @Test
  void testRefWithInlineFormatting() {
    // Inline formatting inside link titles is parsed recursively
    assertEquals("<link href=\"http://example.org\"><monospace>printf</monospace></link>", toPSML("[`printf`](http://example.org)"));
    assertEquals("<link href=\"http://example.org\"><italic>italic</italic></link>", toPSML("[*italic*](http://example.org)"));
    assertEquals("<link href=\"http://example.org\"><bold>bold</bold></link>", toPSML("[**bold**](http://example.org)"));
    assertEquals("<link href=\"http://example.org\"><bold>bold</bold></link>", toPSML("[__bold__](http://example.org)"));
  }

  @Test
  void testLink() {
    assertEquals("<link href=\"http://example.org\">example.org</link>", toPSML("<http://example.org>"));
    assertEquals("<link href=\"https://example.org\">example.org</link>", toPSML("<https://example.org>"));
    assertEquals("<link href=\"mailto:test@example.org\">test@example.org</link>", toPSML("<mailto:test@example.org>"));
  }

  @Test
  void testAutolink() {
    assertEquals("<link href=\"http://example.org\">http://example.org</link>", toPSML("http://example.org"));
    assertEquals("<link href=\"https://example.org\">https://example.org</link>", toPSML("https://example.org"));
    // Included characters
    assertEquals("<link href=\"http://example.org/\">http://example.org/</link>", toPSML("http://example.org/"));
    assertEquals("<link href=\"https://example.org/\">https://example.org/</link>", toPSML("https://example.org/"));
    // Punctuation
    assertEquals("<link href=\"http://example.org\">http://example.org</link>,", toPSML("http://example.org,"));
    assertEquals("<link href=\"http://example.org\">http://example.org</link>.", toPSML("http://example.org."));
    assertEquals("<link href=\"http://example.org\">http://example.org</link>;", toPSML("http://example.org;"));
    assertEquals("<link href=\"http://example.org\">http://example.org</link>!", toPSML("http://example.org!"));
    assertEquals("<link href=\"http://example.org\">http://example.org</link>:", toPSML("http://example.org:"));
    assertEquals("<link href=\"http://example.org\">http://example.org</link>?", toPSML("http://example.org?"));
    // Brackets
    assertEquals("(<link href=\"http://example.org\">http://example.org</link>)", toPSML("(http://example.org)"));
    assertEquals("{<link href=\"http://example.org\">http://example.org</link>}", toPSML("{http://example.org}"));
    assertEquals("[<link href=\"http://example.org\">http://example.org</link>]", toPSML("[http://example.org]"));
    // With path
    assertEquals("<link href=\"http://example.org/home.html\">http://example.org/home.html</link>,", toPSML("http://example.org/home.html,"));
    assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>", toPSML("http://example.org/home"));
    assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>.", toPSML("http://example.org/home."));
    assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>,", toPSML("http://example.org/home,"));
    assertEquals("<link href=\"http://example.org/home/\">http://example.org/home/</link>", toPSML("http://example.org/home/"));
    // With parameters
    assertEquals("<link href=\"http://example.org?t\">http://example.org?t</link>", toPSML("http://example.org?t"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>", toPSML("http://example.org?t="));
    assertEquals("<link href=\"http://example.org?t=_\">http://example.org?t=_</link>", toPSML("http://example.org?t=_"));
    assertEquals("<link href=\"http://example.org?t=-\">http://example.org?t=-</link>", toPSML("http://example.org?t=-"));
    assertEquals("<link href=\"http://example.org?t==\">http://example.org?t==</link>", toPSML("http://example.org?t=="));
    assertEquals("<link href=\"http://example.org?t=/\">http://example.org?t=/</link>", toPSML("http://example.org?t=/"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>,", toPSML("http://example.org?t=,"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>.", toPSML("http://example.org?t=."));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>;", toPSML("http://example.org?t=;"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>:", toPSML("http://example.org?t=:"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>?", toPSML("http://example.org?t=?"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>!", toPSML("http://example.org?t=!"));
    assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>)", toPSML("http://example.org?t=)"));
  }

  /**
   * Returns the Markdown text as PSML using the inline parser.
   *
   * @param text The text to parse
   * @return The corresponding PSML as a string.
   */
  private static String toPSML(String text) {
    try {
      InlineParser parser = new InlineParser();
      List<PSMLNode> nodes = parser.parse(text);
      XMLStringWriter xml = new XMLStringWriter(XML.NamespaceAware.No);
      for (PSMLNode n : nodes) {
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
