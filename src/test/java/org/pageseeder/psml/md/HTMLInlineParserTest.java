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
import org.pageseeder.psml.html.HTMLNode;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.util.List;

public final class HTMLInlineParserTest {

  @Test
  public void testEscaping() {
    Assert.assertEquals("*test*", toHTML("\\*test\\*"));
    Assert.assertEquals("**test**", toHTML("\\*\\*test\\*\\*"));
    Assert.assertEquals("_test_", toHTML("\\_test\\_"));
    Assert.assertEquals("__test__", toHTML("\\_\\_test\\_\\_"));
    Assert.assertEquals("`test`", toHTML("\\`test\\`"));
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toHTML("\\!\\[Alt text\\](/path/to/img.jpg)"));
    Assert.assertEquals("[test](http://example.net/)", toHTML("\\[test\\](http://example.net/)"));
    Assert.assertEquals("<a href=\"http://example.net/my_doc.html\">my_doc</a>", toHTML("[my\\_doc](http://example.net/my\\_doc.html)"));
    Assert.assertEquals("&lt;http://example.org&gt;", toHTML("\\<http://example.org\\>"));
  }

  @Test
  public void testDoubleEmphasis() {
    Assert.assertEquals("<strong>test</strong>", toHTML("**test**"));
    Assert.assertEquals("<strong>test</strong>", toHTML("__test__"));
    Assert.assertEquals("A <strong>test</strong>!", toHTML("A **test**!"));
    Assert.assertEquals("A <strong>test</strong>!", toHTML("A __test__!"));
    Assert.assertEquals("extra<strong>test</strong>", toHTML("extra**test**"));
    Assert.assertEquals("extra<strong>test</strong>", toHTML("extra__test__"));
    Assert.assertEquals("<strong>extra</strong>test", toHTML("**extra**test"));
    Assert.assertEquals("<strong>extra</strong>test", toHTML("__extra__test"));
    Assert.assertEquals("ex<strong>tra</strong>test", toHTML("ex**tra**test"));
    Assert.assertEquals("ex<strong>tra</strong>test", toHTML("ex__tra__test"));
    Assert.assertEquals("<strong/>", toHTML("****"));
    Assert.assertEquals("<strong/>", toHTML("____"));
  }

  @Test
  public void testSimpleEmphasis() {
    Assert.assertEquals("<em>test</em>", toHTML("*test*"));
    Assert.assertEquals("<em>test</em>", toHTML("_test_"));
    Assert.assertEquals("A <em>test</em>!", toHTML("A *test*!"));
    Assert.assertEquals("A <em>test</em>!", toHTML("A _test_!"));
    Assert.assertEquals("extra<em>test</em>", toHTML("extra*test*"));
    Assert.assertEquals("extra_test_", toHTML("extra_test_"));
    Assert.assertEquals("<em>extra</em>test", toHTML("*extra*test"));
    Assert.assertEquals("_extra_test", toHTML("_extra_test"));
    Assert.assertEquals("ex<em>tra</em>test", toHTML("ex*tra*test"));
    Assert.assertEquals("ex_tra_test", toHTML("ex_tra_test"));
    Assert.assertEquals("<em/>", toHTML("**"));
    Assert.assertEquals("<em/>", toHTML("__"));
  }

  @Test
  public void testCode() {
    Assert.assertEquals("<code>test</code>", toHTML("`test`"));
    Assert.assertEquals("A <code>test</code>!", toHTML("A `test`!"));
    Assert.assertEquals("extra<code>test</code>", toHTML("extra`test`"));
    Assert.assertEquals("<code>extra</code>test", toHTML("`extra`test"));
    Assert.assertEquals("ex<code>tra</code>test", toHTML("ex`tra`test"));
    Assert.assertEquals("<code/>", toHTML("``"));

    // From Markdown spec
    Assert.assertEquals("Use the <code>printf()</code> function.", toHTML("Use the `printf()` function."));
    Assert.assertEquals("Please don't use any <code>&lt;blink&gt;</code> tags.", toHTML("Please don't use any `<blink>` tags."));
    Assert.assertEquals("<code>&amp;#8212;</code> is the decimal-encoded equivalent of <code>&amp;mdash;</code>.", toHTML("`&#8212;` is the decimal-encoded equivalent of `&mdash;`."));
  }

  @Test
  public void testCodeEscape() {
    Assert.assertEquals("<code>test</code>", toHTML("`` test ``"));
    Assert.assertEquals("<code>`test`</code>", toHTML("`` `test` ``"));
    Assert.assertEquals("A <code>test</code>!", toHTML("A `` test ``!"));
    Assert.assertEquals("A <code>`test`</code>!", toHTML("A `` `test` ``!"));

    // From Markdown spec
    Assert.assertEquals("<code>There is a literal backtick (`) here.</code>", toHTML("``There is a literal backtick (`) here.``"));
    Assert.assertEquals("A single backtick in a code span: <code>`</code>", toHTML("A single backtick in a code span: `` ` ``"));
    Assert.assertEquals("A backtick-delimited string in a code span: <code>`foo`</code>", toHTML("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  public void testImage() {
    Assert.assertEquals("<img alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", toHTML("![Alt text](/path/to/img.jpg)"));
  }

  @Test
  public void testRef() {
    Assert.assertEquals("<a href=\"http://example.net/\">test</a>", toHTML("[test](http://example.net/)"));
  }

  @Test
  public void testLink() {
    Assert.assertEquals("<a href=\"http://example.org\">example.org</a>", toHTML("<http://example.org>"));
    Assert.assertEquals("<a href=\"https://example.org\">example.org</a>", toHTML("<https://example.org>"));
    Assert.assertEquals("<a href=\"mailto:test@example.org\">test@example.org</a>", toHTML("<mailto:test@example.org>"));
  }

  @Test
  public void testAutolink() {
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>", toHTML("http://example.org"));
    Assert.assertEquals("<a href=\"https://example.org\">https://example.org</a>", toHTML("https://example.org"));
    // Included characters
    Assert.assertEquals("<a href=\"http://example.org/\">http://example.org/</a>", toHTML("http://example.org/"));
    Assert.assertEquals("<a href=\"https://example.org/\">https://example.org/</a>", toHTML("https://example.org/"));
    // Punctuation
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>,", toHTML("http://example.org,"));
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>.", toHTML("http://example.org."));
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>;", toHTML("http://example.org;"));
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>!", toHTML("http://example.org!"));
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>:", toHTML("http://example.org:"));
    Assert.assertEquals("<a href=\"http://example.org\">http://example.org</a>?", toHTML("http://example.org?"));
    // Brackets
    Assert.assertEquals("(<a href=\"http://example.org\">http://example.org</a>)", toHTML("(http://example.org)"));
    Assert.assertEquals("{<a href=\"http://example.org\">http://example.org</a>}", toHTML("{http://example.org}"));
    Assert.assertEquals("[<a href=\"http://example.org\">http://example.org</a>]", toHTML("[http://example.org]"));
    // With path
    Assert.assertEquals("<a href=\"http://example.org/home.html\">http://example.org/home.html</a>,", toHTML("http://example.org/home.html,"));
    Assert.assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>", toHTML("http://example.org/home"));
    Assert.assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>.", toHTML("http://example.org/home."));
    Assert.assertEquals("<a href=\"http://example.org/home\">http://example.org/home</a>,", toHTML("http://example.org/home,"));
    Assert.assertEquals("<a href=\"http://example.org/home/\">http://example.org/home/</a>", toHTML("http://example.org/home/"));
    // With parameters
    Assert.assertEquals("<a href=\"http://example.org?t\">http://example.org?t</a>", toHTML("http://example.org?t"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>", toHTML("http://example.org?t="));
    Assert.assertEquals("<a href=\"http://example.org?t=_\">http://example.org?t=_</a>", toHTML("http://example.org?t=_"));
    Assert.assertEquals("<a href=\"http://example.org?t=-\">http://example.org?t=-</a>", toHTML("http://example.org?t=-"));
    Assert.assertEquals("<a href=\"http://example.org?t==\">http://example.org?t==</a>", toHTML("http://example.org?t=="));
    Assert.assertEquals("<a href=\"http://example.org?t=/\">http://example.org?t=/</a>", toHTML("http://example.org?t=/"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>,", toHTML("http://example.org?t=,"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>.", toHTML("http://example.org?t=."));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>;", toHTML("http://example.org?t=;"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>:", toHTML("http://example.org?t=:"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>?", toHTML("http://example.org?t=?"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>!", toHTML("http://example.org?t=!"));
    Assert.assertEquals("<a href=\"http://example.org?t=\">http://example.org?t=</a>)", toHTML("http://example.org?t=)"));
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
      throw new RuntimeException(ex);
    }
  }

}
