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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.PSML;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLNode;

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
    Assert.assertEquals("![Alt text](/path/to/img.jpg)", toMarkdown("<image alt=\"Alt text\" src=\"/path/to/img.jpg\"/>"));
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

  /**
   * Returns the Markdown text as PSML using the inline parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding PSML as a string.
   */
  private static String toMarkdown(String text) {
    try {
      PSMLElement element = PSML.load(new StringReader(text));
      MarkdownSerializer serializer = new MarkdownSerializer();
      StringWriter out = new StringWriter();
      serializer.serialize(element, out);
      return out.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new RuntimeException(ex);
    }
  }

}
