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
import org.pageseeder.psml.PSML;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.toc.Tests;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MarkdownSerializerTest {

  @Test
  void testDoubleEmphasis() {
    assertEquals("__test__", toMarkdown("<bold>test</bold>"));
    assertEquals("A __test__!", toMarkdown("<wrapper>A <bold>test</bold>!</wrapper>"));
    assertEquals("A __test__!", toMarkdown("<wrapper>A <bold>test</bold>!</wrapper>"));
    assertEquals("extra__test__", toMarkdown("<wrapper>extra<bold>test</bold></wrapper>"));
    assertEquals("__extra__test", toMarkdown("<wrapper><bold>extra</bold>test</wrapper>"));
    assertEquals("ex__tra__test", toMarkdown("<wrapper>ex<bold>tra</bold>test</wrapper>"));
    assertEquals("____", toMarkdown("<bold/>"));
  }

  @Test
  void testSimpleEmphasis() {
    assertEquals("*test*", toMarkdown("<italic>test</italic>"));
    assertEquals("A *test*!", toMarkdown("<wrapper>A <italic>test</italic>!</wrapper>"));
    assertEquals("extra*test*", toMarkdown("<wrapper>extra<italic>test</italic></wrapper>"));
    assertEquals("*extra*test", toMarkdown("<wrapper><italic>extra</italic>test</wrapper>"));
    assertEquals("ex*tra*test", toMarkdown("<wrapper>ex<italic>tra</italic>test</wrapper>"));
    assertEquals("**", toMarkdown("<italic/>"));
  }

  @Test
  void testCode() {
    assertEquals("`test`", toMarkdown("<monospace>test</monospace>"));
    assertEquals("A `test`!", toMarkdown("<wrapper>A <monospace>test</monospace>!</wrapper>"));
    assertEquals("extra`test`", toMarkdown("<wrapper>extra<monospace>test</monospace></wrapper>"));
    assertEquals("`extra`test", toMarkdown("<wrapper><monospace>extra</monospace>test</wrapper>"));
    assertEquals("ex`tra`test", toMarkdown("<wrapper>ex<monospace>tra</monospace>test</wrapper>"));
    assertEquals("``", toMarkdown("<monospace/>"));

    // From Markdown spec
    assertEquals("Use the `printf()` function.", toMarkdown("<wrapper>Use the <monospace>printf()</monospace> function.</wrapper>"));
    assertEquals("Please don't use any `<blink>` tags.", toMarkdown("<wrapper>Please don't use any <monospace>&lt;blink&gt;</monospace> tags.</wrapper>"));
    assertEquals("`&#8212;` is the decimal-encoded equivalent of `&mdash;`.", toMarkdown("<wrapper><monospace>&amp;#8212;</monospace> is the decimal-encoded equivalent of <monospace>&amp;mdash;</monospace>.</wrapper>"));
  }

  @Test
  void testUnderline() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    assertEquals(MarkdownOutputOptions.UnderlineFormat.IGNORE, defaultOptions.underline());
    MarkdownOutputOptions ignoreOption = defaultOptions.underline(MarkdownOutputOptions.UnderlineFormat.IGNORE);
    assertEquals("test", toMarkdown("<underline>test</underline>", ignoreOption));
    assertEquals("A test!", toMarkdown("<wrapper>A <underline>test</underline>!</wrapper>", ignoreOption));
    assertEquals("", toMarkdown("<underline/>", ignoreOption));
    MarkdownOutputOptions htmlOption = defaultOptions.underline(MarkdownOutputOptions.UnderlineFormat.HTML);
    assertEquals("<u>test</u>", toMarkdown("<underline>test</underline>", htmlOption));
    assertEquals("A <u>test</u>!", toMarkdown("<wrapper>A <underline>test</underline>!</wrapper>", htmlOption));
    assertEquals("", toMarkdown("<underline/>", htmlOption));
  }

  @Test
  void testSuperScript() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    assertEquals(MarkdownOutputOptions.SuperSubFormat.IGNORE, defaultOptions.superSub());
    MarkdownOutputOptions ignoreOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.IGNORE);
    assertEquals("trademark", toMarkdown("<sup>trademark</sup>", ignoreOption));
    assertEquals("1st Ca2+", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", ignoreOption));
    assertEquals("", toMarkdown("<sup/>", ignoreOption));
    MarkdownOutputOptions extendedOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.CARET_TILDE);
    assertEquals("^trademark^", toMarkdown("<sup>trademark</sup>", extendedOption));
    assertEquals("1^st^ Ca^2+^", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", extendedOption));
    assertEquals("", toMarkdown("<sup/>", extendedOption));
    MarkdownOutputOptions unicodeOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.UNICODE);
    assertEquals("ᵗʳᵃᵈᵉᵐᵃʳᵏ", toMarkdown("<sup>trademark</sup>", unicodeOption));
    assertEquals("1ˢᵗ Ca²⁺", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", unicodeOption));
    assertEquals("", toMarkdown("<sup/>", unicodeOption));
    MarkdownOutputOptions htmlOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.HTML);
    assertEquals("<sup>trademark</sup>", toMarkdown("<sup>trademark</sup>", htmlOption));
    assertEquals("1<sup>st</sup> Ca<sup>2+</sup>", toMarkdown("<wrapper>1<sup>st</sup> Ca<sup>2+</sup></wrapper>", htmlOption));
    assertEquals("", toMarkdown("<sup/>", htmlOption));
  }

  @Test
  void testSubScript() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    assertEquals(MarkdownOutputOptions.SuperSubFormat.IGNORE, defaultOptions.superSub());
    MarkdownOutputOptions ignoreOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.IGNORE);
    assertEquals("2(a)", toMarkdown("<sub>2(a)</sub>", ignoreOption));
    assertEquals("H2O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", ignoreOption));
    assertEquals("", toMarkdown("<sub/>", ignoreOption));
    MarkdownOutputOptions extendedOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.CARET_TILDE);
    assertEquals("~2(a)~", toMarkdown("<sub>2(a)</sub>", extendedOption));
    assertEquals("H~2~O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", extendedOption));
    assertEquals("", toMarkdown("<sub/>", extendedOption));
    MarkdownOutputOptions unicodeOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.UNICODE);
    assertEquals("₂₍ₐ₎", toMarkdown("<sub>2(a)</sub>", unicodeOption));
    assertEquals("H₂O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", unicodeOption));
    assertEquals("", toMarkdown("<sub/>", unicodeOption));
    MarkdownOutputOptions htmlOption = defaultOptions.superSub(MarkdownOutputOptions.SuperSubFormat.HTML);
    assertEquals("<sub>2(a)</sub>", toMarkdown("<sub>2(a)</sub>", htmlOption));
    assertEquals("H<sub>2</sub>O!", toMarkdown("<wrapper>H<sub>2</sub>O!</wrapper>", htmlOption));
    assertEquals("", toMarkdown("<sub/>", htmlOption));
  }

  @Test
  void testCodeEscape() {
//    assertEquals("`` test ``", toMarkdown("<wrapper><monospace>test</monospace></wrapper>"));
//    assertEquals("`` `test` ``", toMarkdown("<wrapper><monospace>`test`</monospace></wrapper>"));
//    assertEquals("A `` test ``!", toMarkdown("<wrapper>A <monospace>test</monospace>!</wrapper>"));
//    assertEquals("A `` `test` ``!", toMarkdown("<wrapper>A <monospace>`test`</monospace>!</wrapper>"));

    // From Markdown spec
//    assertEquals("``There is a literal backtick (`) here.``", toMarkdown("<monospace>There is a literal backtick (`) here.</monospace>" ));
//    assertEquals("A single backtick in a code span: `` ` ``", toMarkdown("A single backtick in a code span: <monospace>`</monospace>"));
//    assertEquals("A backtick-delimited string in a code span: <monospace>`foo`</monospace>", toMarkdown("A backtick-delimited string in a code span: `` `foo` ``"));
  }

  @Test
  void testImage() {
    MarkdownOutputOptions defaultOptions = MarkdownOutputOptions.defaultOptions();
    assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions));
    assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.LOCAL)));
    assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.EXTERNAL)));
    assertEquals("<img src=\"/path/to/img.jpg\" alt=\"Alt text\" />", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.IMG_TAG)));
    assertEquals("", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>", defaultOptions.image(MarkdownOutputOptions.ImageFormat.NONE)));
  }

  @Test
  void testRef() {
    assertEquals("[test](http://example.net/)", toMarkdown("<link href=\"http://example.net/\">test</link>"));
  }

  @Test
  void testList() {
    String psml = "<list><item>Item 1</item><item>Item 2</item></list>";
    assertEquals("\n* Item 1\n* Item 2\n", toMarkdown(psml));
  }

  @Test
  void testListFiles() throws IOException {
    testFile("list1");
    testFile("list2");
    // Sub lists
    testFile("list3");
    testFile("list4");
    // List continuation
//    testFile("list5");
  }

  @Test
  void testTable1() throws IOException {
    testTableFormat("table1", MarkdownOutputOptions.TableFormat.COMPACT);
    testTableFormat("table1", MarkdownOutputOptions.TableFormat.PRETTY);
    testTableFormat("table1", MarkdownOutputOptions.TableFormat.NORMALIZED);
    testTableFormat("table1", MarkdownOutputOptions.TableFormat.HTML);
  }

//  @Test
  void testTable2() throws IOException {
    testTableFormat("table2", MarkdownOutputOptions.TableFormat.COMPACT);
    testTableFormat("table2", MarkdownOutputOptions.TableFormat.PRETTY);
    testTableFormat("table2", MarkdownOutputOptions.TableFormat.NORMALIZED);
    testTableFormat("table2", MarkdownOutputOptions.TableFormat.HTML);
  }

  private void testFile(String name) throws IOException {
    PSMLElement psml = getTestFile(name+".psml");
    String expected = getResultFile(name+".md");
    MarkdownSerializer serializer = new MarkdownSerializer();
    StringWriter out = new StringWriter();
    serializer.serialize(psml, out);
    assertEquals(expected.trim(), out.toString().trim());
  }

  private void testTableFormat(String name, MarkdownOutputOptions.TableFormat format) throws IOException {
    PSMLElement psml = getTestFile(name+".psml");
    String expected = getResultFile(name+"_"+format.name().toLowerCase()+".md");
    MarkdownSerializer serializer = new MarkdownSerializer();
    serializer.setOptions(MarkdownOutputOptions.defaultOptions().table(format));
    StringWriter out = new StringWriter();
    serializer.serialize(psml, out);
    assertEquals(expected.trim(), out.toString().trim());
  }

  @Test
  void testKitchenSink() throws IOException {
    PSMLElement psml = getTestFile("kitchen_sink.psml");
    MarkdownSerializer serializer = new MarkdownSerializer();
    StringWriter out = new StringWriter();
    serializer.serialize(psml, out);
    System.out.println(out);
  }

  @Test
  void testProperties() throws IOException {
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
      throw new UncheckedIOException("Unable to load test file '" + filename + "'", ex);
    }
  }

  public static String getResultFile(String filename) {
    try (Reader r = new InputStreamReader(Objects.requireNonNull(Tests.class.getResourceAsStream("/org/pageseeder/psml/md/out/" + filename)))) {
      StringBuilder sb = new StringBuilder();
      char[] buffer = new char[4096];
      int len;
      while ((len = r.read(buffer)) != -1) {
        sb.append(buffer, 0, len);
      }
      return sb.toString();
    } catch (IOException ex) {
      throw new UncheckedIOException("Unable to load test file '" + filename + "'", ex);
    }
  }


}
