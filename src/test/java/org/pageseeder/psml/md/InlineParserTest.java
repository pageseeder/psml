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

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.util.List;

public final class InlineParserTest {


  @Test
  public void testUnescaping() {
    Assert.assertEquals("Escape ~ ` ! @ # $ % ^ & * ( ) _ - + = { [ } ] | \\ : ; \" ' < , > . ? / text",
        InlineParser.unescape("Escape ~ \\` \\! @ # $ % ^ & \\* ( ) \\_ - + = { \\[ } \\] | \\\\ : ; \" ' \\< , \\> . ? / text"));
  }

  @Test
  public void testEscaping() {
    Assert.assertEquals("*test*", toPSML("\\*test\\*"));
    Assert.assertEquals("**test**", toPSML("\\*\\*test\\*\\*"));
    Assert.assertEquals("_test_", toPSML("\\_test\\_"));
    Assert.assertEquals("__test__", toPSML("\\_\\_test\\_\\_"));
    Assert.assertEquals("`test`", toPSML("\\`test\\`"));
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toPSML("\\!\\[Alt text\\](/path/to/img.jpg)"));
    Assert.assertEquals("[test](http://example.net/)", toPSML("\\[test\\](http://example.net/)"));
    Assert.assertEquals("<link href=\"http://example.net/my_doc.html\">my_doc</link>",
            toPSML("[my\\_doc](http://example.net/my\\_doc.html)"));
    Assert.assertEquals("<link href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</link>",
            toPSML("[http://example.net/my\\_doc.html](http://example.net/my\\_doc.html)"));
    Assert.assertEquals("<link href=\"http://example.net/my_doc.html\">example.net/my_doc.html</link>",
            toPSML("<http://example.net/my\\_doc.html>"));
    Assert.assertEquals("<link href=\"http://example.net/my_doc.html\">http://example.net/my_doc.html</link>",
            toPSML("http://example.net/my\\_doc.html"));
    Assert.assertEquals("&lt;http://example.org&gt;", toPSML("\\<http://example.org\\>"));
  }

  @Test
  public void testDoubleEmphasis() {
    Assert.assertEquals("<bold>test</bold>", toPSML("**test**"));
    Assert.assertEquals("<bold>test</bold>", toPSML("__test__"));
    Assert.assertEquals("A <bold>test</bold>!", toPSML("A **test**!"));
    Assert.assertEquals("A <bold>test</bold>!", toPSML("A __test__!"));
    Assert.assertEquals("extra<bold>test</bold>", toPSML("extra**test**"));
    Assert.assertEquals("extra<bold>test</bold>", toPSML("extra__test__"));
    Assert.assertEquals("<bold>extra</bold>test", toPSML("**extra**test"));
    Assert.assertEquals("<bold>extra</bold>test", toPSML("__extra__test"));
    Assert.assertEquals("ex<bold>tra</bold>test", toPSML("ex**tra**test"));
    Assert.assertEquals("ex<bold>tra</bold>test", toPSML("ex__tra__test"));
    Assert.assertEquals("<bold/>", toPSML("****"));
    Assert.assertEquals("<bold/>", toPSML("____"));
  }

  @Test
  public void testSimpleEmphasis() {
    Assert.assertEquals("<italic>test</italic>", toPSML("*test*"));
    Assert.assertEquals("<italic>test</italic>", toPSML("_test_"));
    Assert.assertEquals("A <italic>test</italic>!", toPSML("A *test*!"));
    Assert.assertEquals("A <italic>test</italic>!", toPSML("A _test_!"));
    Assert.assertEquals("extra<italic>test</italic>", toPSML("extra*test*"));
    Assert.assertEquals("extra_test_", toPSML("extra_test_"));
    Assert.assertEquals("<italic>extra</italic>test", toPSML("*extra*test"));
    Assert.assertEquals("_extra_test", toPSML("_extra_test"));
    Assert.assertEquals("ex<italic>tra</italic>test", toPSML("ex*tra*test"));
    Assert.assertEquals("ex_tra_test", toPSML("ex_tra_test"));
    Assert.assertEquals("<italic/>", toPSML("**"));
    Assert.assertEquals("<italic/>", toPSML("__"));
  }

  @Test
  public void testCode() {
    Assert.assertEquals("<monospace>test</monospace>", toPSML("`test`"));
    Assert.assertEquals("A <monospace>test</monospace>!", toPSML("A `test`!"));
    Assert.assertEquals("extra<monospace>test</monospace>", toPSML("extra`test`"));
    Assert.assertEquals("<monospace>extra</monospace>test", toPSML("`extra`test"));
    Assert.assertEquals("ex<monospace>tra</monospace>test", toPSML("ex`tra`test"));
    Assert.assertEquals("<monospace/>", toPSML("``"));

    // From Markdown spec
    Assert.assertEquals("Use the <monospace>printf()</monospace> function.", toPSML("Use the `printf()` function."));
    Assert.assertEquals("Please don't use any <monospace>&lt;blink&gt;</monospace> tags.", toPSML("Please don't use any `<blink>` tags."));
    Assert.assertEquals("<monospace>&amp;#8212;</monospace> is the decimal-encoded equivalent of <monospace>&amp;mdash;</monospace>.", toPSML("`&#8212;` is the decimal-encoded equivalent of `&mdash;`."));
  }

  @Test
  public void testCodeEscape() {
    Assert.assertEquals("<monospace>test</monospace>", toPSML("`` test ``"));
    Assert.assertEquals("<monospace>`test`</monospace>", toPSML("`` `test` ``"));
    Assert.assertEquals("A <monospace>test</monospace>!", toPSML("A `` test ``!"));
    Assert.assertEquals("A <monospace>`test`</monospace>!", toPSML("A `` `test` ``!"));

    // From Markdown spec
    Assert.assertEquals("<monospace>There is a literal backtick (`) here.</monospace>", toPSML("``There is a literal backtick (`) here.``"));
    Assert.assertEquals("A single backtick in a code span: <monospace>`</monospace>", toPSML("A single backtick in a code span: `` ` ``"));
    Assert.assertEquals("A backtick-delimited string in a code span: <monospace>`foo`</monospace>", toPSML("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  public void testImage() {
    Assert.assertEquals("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", toPSML("![Alt text](/path/to/img.jpg)"));
  }

  @Test
  public void testRef() {
    Assert.assertEquals("<link href=\"http://example.net/\">test</link>", toPSML("[test](http://example.net/)"));
  }

  @Test
  public void testLink() {
    Assert.assertEquals("<link href=\"http://example.org\">example.org</link>", toPSML("<http://example.org>"));
    Assert.assertEquals("<link href=\"https://example.org\">example.org</link>", toPSML("<https://example.org>"));
    Assert.assertEquals("<link href=\"mailto:test@example.org\">test@example.org</link>", toPSML("<mailto:test@example.org>"));
  }

  @Test
  public void testAutolink() {
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>", toPSML("http://example.org"));
    Assert.assertEquals("<link href=\"https://example.org\">https://example.org</link>", toPSML("https://example.org"));
    // Included characters
    Assert.assertEquals("<link href=\"http://example.org/\">http://example.org/</link>", toPSML("http://example.org/"));
    Assert.assertEquals("<link href=\"https://example.org/\">https://example.org/</link>", toPSML("https://example.org/"));
    // Punctuation
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>,", toPSML("http://example.org,"));
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>.", toPSML("http://example.org."));
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>;", toPSML("http://example.org;"));
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>!", toPSML("http://example.org!"));
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>:", toPSML("http://example.org:"));
    Assert.assertEquals("<link href=\"http://example.org\">http://example.org</link>?", toPSML("http://example.org?"));
    // Brackets
    Assert.assertEquals("(<link href=\"http://example.org\">http://example.org</link>)", toPSML("(http://example.org)"));
    Assert.assertEquals("{<link href=\"http://example.org\">http://example.org</link>}", toPSML("{http://example.org}"));
    Assert.assertEquals("[<link href=\"http://example.org\">http://example.org</link>]", toPSML("[http://example.org]"));
    // With path
    Assert.assertEquals("<link href=\"http://example.org/home.html\">http://example.org/home.html</link>,", toPSML("http://example.org/home.html,"));
    Assert.assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>", toPSML("http://example.org/home"));
    Assert.assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>.", toPSML("http://example.org/home."));
    Assert.assertEquals("<link href=\"http://example.org/home\">http://example.org/home</link>,", toPSML("http://example.org/home,"));
    Assert.assertEquals("<link href=\"http://example.org/home/\">http://example.org/home/</link>", toPSML("http://example.org/home/"));
    // With parameters
    Assert.assertEquals("<link href=\"http://example.org?t\">http://example.org?t</link>", toPSML("http://example.org?t"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>", toPSML("http://example.org?t="));
    Assert.assertEquals("<link href=\"http://example.org?t=_\">http://example.org?t=_</link>", toPSML("http://example.org?t=_"));
    Assert.assertEquals("<link href=\"http://example.org?t=-\">http://example.org?t=-</link>", toPSML("http://example.org?t=-"));
    Assert.assertEquals("<link href=\"http://example.org?t==\">http://example.org?t==</link>", toPSML("http://example.org?t=="));
    Assert.assertEquals("<link href=\"http://example.org?t=/\">http://example.org?t=/</link>", toPSML("http://example.org?t=/"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>,", toPSML("http://example.org?t=,"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>.", toPSML("http://example.org?t=."));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>;", toPSML("http://example.org?t=;"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>:", toPSML("http://example.org?t=:"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>?", toPSML("http://example.org?t=?"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>!", toPSML("http://example.org?t=!"));
    Assert.assertEquals("<link href=\"http://example.org?t=\">http://example.org?t=</link>)", toPSML("http://example.org?t=)"));
  }

  /**
   * Returns the Markdown text as PSML using the inline parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding PSML as a string.
   *
   * @throws IOException If thrown by
   */
  private static String toPSML(String text) {
    try {
      InlineParser parser = new InlineParser();
      List<PSMLNode> nodes = parser.parse(text);
      XMLStringWriter xml = new XMLStringWriter(false);
      for (PSMLNode n : nodes) {
        n.toXML(xml);
      }
      xml.flush();
      return xml.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new RuntimeException(ex);
    }
  }

}
