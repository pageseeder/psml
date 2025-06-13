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

import java.io.*;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.PSML;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.toc.Tests;

public final class MarkdownSerializerTest {

  @Test
  public void testDoubleEmphasis() {
    Assert.assertEquals("__test__", toMarkdown("<bold>test</bold>"));
    Assert.assertEquals("A __test__!", toMarkdown("<wrapper>A <bold>test</bold>!</wrapper>"));
    Assert.assertEquals("A __test__!", toMarkdown("<wrapper>A <bold>test</bold>!</wrapper>"));
    Assert.assertEquals("extra__test__", toMarkdown( "<wrapper>extra<bold>test</bold></wrapper>"));
    Assert.assertEquals("__extra__test", toMarkdown( "<wrapper><bold>extra</bold>test</wrapper>"));
    Assert.assertEquals("ex__tra__test", toMarkdown( "<wrapper>ex<bold>tra</bold>test</wrapper>"));
    Assert.assertEquals("____", toMarkdown( "<bold/>"));
  }

  @Test
  public void testSimpleEmphasis() {
    Assert.assertEquals("*test*", toMarkdown( "<italic>test</italic>"));
    Assert.assertEquals("A *test*!", toMarkdown( "<wrapper>A <italic>test</italic>!</wrapper>"));
    Assert.assertEquals("extra*test*", toMarkdown( "<wrapper>extra<italic>test</italic></wrapper>"));
    Assert.assertEquals("*extra*test", toMarkdown( "<wrapper><italic>extra</italic>test</wrapper>"));
    Assert.assertEquals("ex*tra*test", toMarkdown( "<wrapper>ex<italic>tra</italic>test</wrapper>"));
    Assert.assertEquals("**", toMarkdown( "<italic/>"));
  }

  @Test
  public void testCode() {
    Assert.assertEquals("`test`", toMarkdown("<monospace>test</monospace>"));
    Assert.assertEquals("A `test`!", toMarkdown("<wrapper>A <monospace>test</monospace>!</wrapper>"));
    Assert.assertEquals("extra`test`", toMarkdown("<wrapper>extra<monospace>test</monospace></wrapper>"));
    Assert.assertEquals("`extra`test", toMarkdown("<wrapper><monospace>extra</monospace>test</wrapper>"));
    Assert.assertEquals("ex`tra`test", toMarkdown("<wrapper>ex<monospace>tra</monospace>test</wrapper>"));
    Assert.assertEquals("``", toMarkdown("<monospace/>"));

    // From Markdown spec
    Assert.assertEquals("Use the `printf()` function.", toMarkdown( "<wrapper>Use the <monospace>printf()</monospace> function.</wrapper>"));
    Assert.assertEquals("Please don't use any `<blink>` tags.", toMarkdown( "<wrapper>Please don't use any <monospace>&lt;blink&gt;</monospace> tags.</wrapper>"));
    Assert.assertEquals("`&#8212;` is the decimal-encoded equivalent of `&mdash;`.", toMarkdown( "<wrapper><monospace>&amp;#8212;</monospace> is the decimal-encoded equivalent of <monospace>&amp;mdash;</monospace>.</wrapper>"));
  }

  @Test
  public void testUnderline() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    Assert.assertEquals(MarkdownOutputOptions.UnderlineFormat.IGNORE, defaultOptions.underline());
    MarkdownOutputOptions ignoreOption = defaultOptions.underline(MarkdownOutputOptions.UnderlineFormat.IGNORE);
    Assert.assertEquals("test", toMarkdown("<underline>test</underline>", ignoreOption));
    Assert.assertEquals("A test!", toMarkdown("<wrapper>A <underline>test</underline>!</wrapper>", ignoreOption));
    Assert.assertEquals("", toMarkdown("<underline/>", ignoreOption));
    MarkdownOutputOptions htmlOption = defaultOptions.underline(MarkdownOutputOptions.UnderlineFormat.HTML);
    Assert.assertEquals("<u>test</u>", toMarkdown("<underline>test</underline>", htmlOption));
    Assert.assertEquals("A <u>test</u>!", toMarkdown("<wrapper>A <underline>test</underline>!</wrapper>", htmlOption));
    Assert.assertEquals("", toMarkdown("<underline/>", htmlOption));
  }

  @Test
  public void testSuperScript() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    Assert.assertEquals(MarkdownOutputOptions.SuperSubFormat.IGNORE, defaultOptions.superSub());
    MarkdownOutputOptions ignoreOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.IGNORE);
    Assert.assertEquals("trademark", toMarkdown("<sup>trademark</sup>", ignoreOption));
    Assert.assertEquals("1st Ca2+", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", ignoreOption));
    Assert.assertEquals("", toMarkdown("<sup/>", ignoreOption));
    MarkdownOutputOptions extendedOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.CARET_TILDE);
    Assert.assertEquals("^trademark^", toMarkdown("<sup>trademark</sup>", extendedOption));
    Assert.assertEquals("1^st^ Ca^2+^", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", extendedOption));
    Assert.assertEquals("", toMarkdown("<sup/>", extendedOption));
    MarkdownOutputOptions unicodeOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.UNICODE_EQUIVALENT);
    Assert.assertEquals("ᵗʳᵃᵈᵉᵐᵃʳᵏ", toMarkdown("<sup>trademark</sup>", unicodeOption));
    Assert.assertEquals("1ˢᵗ Ca²⁺", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", unicodeOption));
    Assert.assertEquals("", toMarkdown("<sup/>", unicodeOption));
    MarkdownOutputOptions htmlOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.HTML);
    Assert.assertEquals("<sup>trademark</sup>", toMarkdown("<sup>trademark</sup>", htmlOption));
    Assert.assertEquals("1<sup>st</sup> Ca<sup>2+</sup>", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", htmlOption));
    Assert.assertEquals("", toMarkdown("<sup/>", htmlOption));
  }

  @Test
  public void testSubScript() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    Assert.assertEquals(MarkdownOutputOptions.SuperSubFormat.IGNORE, defaultOptions.superSub());
    MarkdownOutputOptions ignoreOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.IGNORE);
    Assert.assertEquals("2(a)", toMarkdown("<sub>2(a)</sub>", ignoreOption));
    Assert.assertEquals("H2O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", ignoreOption));
    Assert.assertEquals("", toMarkdown("<sub/>", ignoreOption));
    MarkdownOutputOptions extendedOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.CARET_TILDE);
    Assert.assertEquals("~2(a)~", toMarkdown("<sub>2(a)</sub>", extendedOption));
    Assert.assertEquals("H~2~O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", extendedOption));
    Assert.assertEquals("", toMarkdown("<sub/>", extendedOption));
    MarkdownOutputOptions unicodeOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.UNICODE_EQUIVALENT);
    Assert.assertEquals("₂₍ₐ₎", toMarkdown("<sub>2(a)</sub>", unicodeOption));
    Assert.assertEquals("H₂O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", unicodeOption));
    Assert.assertEquals("", toMarkdown("<sub/>", unicodeOption));
    MarkdownOutputOptions htmlOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.HTML);
    Assert.assertEquals("<sub>2(a)</sub>", toMarkdown("<sub>2(a)</sub>", htmlOption));
    Assert.assertEquals("H<sub>2</sub>O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", htmlOption));
    Assert.assertEquals("", toMarkdown("<sub/>", htmlOption));
  }

  @Test
  public void testCodeEscape() {
//    Assert.assertEquals("`` test ``", toMarkdown("<wrapper><monospace>test</monospace></wrapper>"));
//    Assert.assertEquals("`` `test` ``", toMarkdown("<wrapper><monospace>`test`</monospace></wrapper>"));
//    Assert.assertEquals("A `` test ``!", toMarkdown("<wrapper>A <monospace>test</monospace>!</wrapper>"));
//    Assert.assertEquals("A `` `test` ``!", toMarkdown("<wrapper>A <monospace>`test`</monospace>!</wrapper>"));

    // From Markdown spec
//    Assert.assertEquals("``There is a literal backtick (`) here.``", toMarkdown("<monospace>There is a literal backtick (`) here.</monospace>" ));
//    Assert.assertEquals("A single backtick in a code span: `` ` ``", toMarkdown("A single backtick in a code span: <monospace>`</monospace>"));
//    Assert.assertEquals("A backtick-delimited string in a code span: <monospace>`foo`</monospace>", toMarkdown("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  public void testImage() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions));
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.LOCAL)));
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.EXTERNAL)));
    Assert.assertEquals("<img src=\"/path/to/img.jpg\" alt=\"Alt text\" />", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.IMG_TAG)));
    Assert.assertEquals("", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.NONE)));
  }

  @Test
  public void testRef() {
    Assert.assertEquals("[test](http://example.net/)", toMarkdown("<link href=\"http://example.net/\">test</link>"));
  }

  @Test
  public void testList() {
    String psml = "<list><item>Item 1</item><item>Item 2</item></list>";
    Assert.assertEquals(" * Item 1\n * Item 2\n", toMarkdown(psml));
  }

  @Test
  public void testKitchenSink() throws IOException {
    PSMLElement psml = getTestFile("kitchen_sink.psml");
    MarkdownSerializer serializer = new MarkdownSerializer();
    StringWriter out = new StringWriter();
    serializer.serialize(psml, out);
    System.out.println(out);
  }

  @Test
  public void testProperties() throws IOException {
    PSMLElement psml = getTestFile("clock_synchronisation.psml");
    MarkdownSerializer serializer = new MarkdownSerializer();
    StringWriter out = new StringWriter();
    serializer.serialize(psml, out);
    System.out.println(out);
  }

  private static String toMarkdown(String text) {
    return toMarkdown(text, MarkdownOutputOptions.defaultOptions());
  }

  /**
   * Returns the Markdown text as PSML using the inline parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding PSML as a string.
   */
  private static String toMarkdown(String text, MarkdownOutputOptions options) {
    try {
      PSMLElement element = PSML.load(new StringReader(text));
      MarkdownSerializer serializer = new MarkdownSerializer();
      serializer.setOptions(options);
      StringWriter out = new StringWriter();
      serializer.serialize(element, out);
      return out.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new RuntimeException(ex);
    }
  }

  public static PSMLElement getTestFile(String filename) {
    try (Reader r = new InputStreamReader(Objects.requireNonNull(Tests.class.getResourceAsStream("/org/pageseeder/psml/md/" + filename)))) {
      return PSML.load(r);
    } catch (IOException ex) {
      throw new UncheckedIOException("Unable to load test file '"+filename+"'", ex);
    }
  }

}
