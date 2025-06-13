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
import org.pageseeder.psml.md.BlockParser.State;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class BlockParserTest {

  @Test
  public void testEscaping() {
    List<String> e1 = List.of("This is \\*escaped star\\*");
    List<String> e2 = List.of("This is \\[escaped brackets\\]");
    List<String> e3 = List.of(" \\- Red", " \\- Green", " \\- Blue");
    Assert.assertEquals("<para>This is *escaped star*</para>", toPSML(e1));
    Assert.assertEquals("<para>This is [escaped brackets]</para>", toPSML(e2));
    Assert.assertEquals("<para>- Red<br/>\n- Green<br/>\n- Blue</para>", toPSML(e3));
  }

  @Test
  public void testHeadingSetext() {
    List<String> h1 = List.of("This is an H1", "=============");
    List<String> h2 = List.of("This is an H2", "-------------");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
  }

  @Test
  public void testHeadingATX()  {
    List<String> h1 = List.of("# This is an H1");
    List<String> h2 = List.of("## This is an H2");
    List<String> h3 = List.of("### This is an H3");
    List<String> h4 = List.of("#### This is an H4");
    List<String> h5 = List.of("##### This is an H5");
    List<String> h6 = List.of("###### This is an H6");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
    Assert.assertEquals("<heading level=\"3\">This is an H3</heading>", toPSML(h3));
    Assert.assertEquals("<heading level=\"4\">This is an H4</heading>", toPSML(h4));
    Assert.assertEquals("<heading level=\"5\">This is an H5</heading>", toPSML(h5));
    Assert.assertEquals("<heading level=\"6\">This is an H6</heading>", toPSML(h6));
  }

  @Test
  public void testHeadingATX2() {
    List<String> h1 = List.of("# This is an H1 #");
    List<String> h2 = List.of("## This is an H2 ##");
    List<String> h3 = List.of("### This is an H3 ###");
    List<String> h4 = List.of("#### This is an H4 ####");
    List<String> h5 = List.of("##### This is an H5 #####");
    List<String> h6 = List.of("###### This is an H6 ######");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
    Assert.assertEquals("<heading level=\"3\">This is an H3</heading>", toPSML(h3));
    Assert.assertEquals("<heading level=\"4\">This is an H4</heading>", toPSML(h4));
    Assert.assertEquals("<heading level=\"5\">This is an H5</heading>", toPSML(h5));
    Assert.assertEquals("<heading level=\"6\">This is an H6</heading>", toPSML(h6));
  }

  @Test
  public void testBlockquotes() {
    List<String> q1 = List.of("> Hello");
    Assert.assertEquals("<block label=\"quoted\"><para>Hello</para></block>", toPSML(q1));
  }

  @Test
  public void testParagraphs() {
    List<String> p1 = List.of("Hi!");
    List<String> p2 = List.of("Hi!","","Welcome");
    List<String> p3 = List.of("Hi!","Welcome");
    List<String> p4 = List.of("Hi!","Welcome","","Test");
    List<String> p5 = List.of("Hi!","","Welcome","Test");
    List<String> p6 = List.of("Hi!","","Welcome","","Test");
    List<String> p7 = List.of("This line is longer than sixty-six characters and should continue without", "a line break");
    Assert.assertEquals("<para>Hi!</para>", toPSML(p1));
    Assert.assertEquals("<para>Hi!</para><para>Welcome</para>", toPSML(p2));
    Assert.assertEquals("<para>Hi!<br/>\nWelcome</para>", toPSML(p3));
    Assert.assertEquals("<para>Hi!<br/>\nWelcome</para><para>Test</para>", toPSML(p4));
    Assert.assertEquals("<para>Hi!</para><para>Welcome<br/>\nTest</para>", toPSML(p5));
    Assert.assertEquals("<para>Hi!</para><para>Welcome</para><para>Test</para>", toPSML(p6));
    Assert.assertEquals("<para>This line is longer than sixty-six characters and should continue without\na line break</para>", toPSML(p7));
  }

  @Test
  public void testUnorderedList() {
    List<String> list1 = List.of(" * Red", " * Green", " * Blue");
    List<String> list2 = List.of(" + Red", " + Green", " + Blue");
    List<String> list3 = List.of(" - Red", " - Green", " - Blue");
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list1));
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list2));
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list3));
  }

  @Test
  public void testOrderedList() {
    List<String> list1 = List.of(" 1. Bird", " 2. McHale", " 3. Parish");
    List<String> list2 = List.of(" 1. Bird", " 1. McHale", " 1. Parish");
    List<String> list3 = List.of(" 7. Bird", " 8. McHale", " 9. Parish");
    List<String> list4 = List.of(" 3. Bird", " 1. McHale", " 8. Parish");
    Assert.assertEquals("<nlist><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list1));
    Assert.assertEquals("<nlist><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list2));
    Assert.assertEquals("<nlist start=\"7\"><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list3));
    Assert.assertEquals("<nlist start=\"3\"><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list4));
  }

  // Not supported yet
  public void tesNestedLists() {
    List<String> nested = List.of(
        "- Fruits:",
        "    - Apple",
        "    - Banana",
        "",
        "- Vegetables:",
        "    - Carrot"
    );
    Assert.assertEquals("", toPSML(nested));
  }

  @Test
  public void testOrderedListEscape() {
    List<String> list1 = List.of(" 1986. What a great season.");
    List<String> list2 = List.of(" 1986\\. What a great season.");
    Assert.assertEquals("<nlist start=\"1986\"><item>What a great season.</item></nlist>", toPSML(list1));
    Assert.assertEquals("<para>1986. What a great season.</para>", toPSML(list2));
  }

  @Test
  public void testCodeBlock() {
    List<String> pre = List.of("    This is a code block.");
    Assert.assertEquals("<preformat>This is a code block.</preformat>", toPSML(pre));
  }

  @Test
  public void testFencedCode() {
    List<String> fencedCode1 = List.of("```", "function() { return 'Hello!';}", "```");
    List<String> fencedCode2 = List.of("```javascript", "function() { return 'Hello!';}", "```");
    List<String> fencedCode3 = List.of("```", "function() {", "  return 'Hello!';", "}", "```");
    List<String> fencedCode4 = List.of("```", "function() {", "  if (a", "  > b) then a = b;", "}", "```");
    List<String> fencedCode5 = List.of("```", "function() {","","return 'Hello!';}", "```");
    List<String> fencedCode6 = List.of("```", "<report>","  <errors>","    <error>", "```");
    Assert.assertEquals("<preformat>\nfunction() { return 'Hello!';}\n</preformat>", toPSML(fencedCode1));
    Assert.assertEquals("<preformat role=\"lang-javascript\">\nfunction() { return 'Hello!';}\n</preformat>", toPSML(fencedCode2));
    Assert.assertEquals("<preformat>\nfunction() {\n  return 'Hello!';\n}\n</preformat>", toPSML(fencedCode3));
    Assert.assertEquals("<preformat>\nfunction() {\n  if (a\n  &gt; b) then a = b;\n}\n</preformat>", toPSML(fencedCode4));
    Assert.assertEquals("<preformat>\nfunction() {\n\nreturn 'Hello!';}\n</preformat>", toPSML(fencedCode5));
    Assert.assertEquals("<preformat>\n&lt;report&gt;\n  &lt;errors&gt;\n    &lt;error&gt;\n</preformat>", toPSML(fencedCode6));
  }

  @Test
  public void testFencedBlock() {
    List<String> fencedBlock1 = List.of("~~~warning", "A warning!", "~~~");
    List<String> fencedBlock2 = List.of("~~~", "Anonymous block", "~~~");
    List<String> fencedBlock3 = List.of("~~~empty",  "~~~");
    List<String> fencedBlock4 = List.of("~~~warning", "A double", "", "warning", "~~~");
    List<String> fencedBlock5 = List.of("~~~note", " - apple", " - pear", "", "~~~");
    List<String> fencedBlock6 = List.of("~~~wrap", "~~~warning", "The warning", "~~~", "~~~");
    Assert.assertEquals("<block label=\"warning\"><para>A warning!</para></block>", toPSML(fencedBlock1));
    Assert.assertEquals("<block><para>Anonymous block</para></block>", toPSML(fencedBlock2));
    Assert.assertEquals("<block label=\"empty\"/>", toPSML(fencedBlock3));
    Assert.assertEquals("<block label=\"warning\"><para>A double</para><para>warning</para></block>", toPSML(fencedBlock4));
    Assert.assertEquals("<block label=\"note\"><list><item>apple</item><item>pear</item></list></block>", toPSML(fencedBlock5));
    Assert.assertEquals("<block label=\"wrap\"><block label=\"warning\"><para>The warning</para></block></block>", toPSML(fencedBlock6));
  }

  @Test
  public void testUC1() {
    List<String> mixed = List.of("`coconut`");
    Assert.assertEquals("<para><monospace>coconut</monospace></para>", toPSML(mixed));
  }

  // Testing the parser state
  // --------------------------------------------------------------------------------------------

  @Test
  public void testStateCommit() {
    State state = new State();
    state.commit();
    Assert.assertNull(state.current());
    // Empty fragment
    state.push(Name.FRAGMENT);
    Assert.assertTrue(state.isElement(Name.FRAGMENT));
    state.commit();
    Assert.assertFalse(state.isElement(Name.FRAGMENT));
    Assert.assertNull(state.current());
    // Empty fragment with paragraph
    state.push(Name.FRAGMENT);
    state.push(Name.PARA, "test");
    Assert.assertTrue(state.isElement(Name.PARA));
    state.commit();
    Assert.assertFalse(state.isElement(Name.PARA));
    Assert.assertTrue(state.isElement(Name.FRAGMENT));
  }

  @Test
  public void testStateCurrent() {
    State state = new State();
    Assert.assertNull(state.current());
    state.push(Name.FRAGMENT);
    Assert.assertEquals(Name.FRAGMENT, state.current().getElement());
    Assert.assertNotEquals(Name.PARA, state.current().getElement());
    state.push(Name.PARA);
    Assert.assertNotEquals(Name.FRAGMENT, state.current().getElement());
    Assert.assertEquals(Name.PARA, state.current().getElement());
  }

  @Test
  public void testStateIsElement() {
    State state = new State();
    Assert.assertFalse(state.isElement(null));
    Assert.assertFalse(state.isElement(Name.FRAGMENT));
    Assert.assertFalse(state.isElement(Name.PARA));
    state.push(Name.FRAGMENT);
    Assert.assertTrue(state.isElement(Name.FRAGMENT));
    Assert.assertFalse(state.isElement(Name.PARA));
    state.push(Name.PARA);
    Assert.assertFalse(state.isElement(Name.FRAGMENT));
    Assert.assertTrue(state.isElement(Name.PARA));
  }

  @Test
  public void testStateIsDescendantOf() {
    State state = new State();
    Assert.assertFalse(state.isDescendantOf(Name.PARA));
    state.push(Name.FRAGMENT);
    Assert.assertFalse(state.isDescendantOf(Name.PARA));
    state.push(Name.PARA);
    Assert.assertTrue(state.isDescendantOf(Name.PARA));
    state.push(Name.BOLD);
    Assert.assertTrue(state.isDescendantOf(Name.PARA));
    Assert.assertTrue(state.isDescendantOf(Name.FRAGMENT));
  }

  /**
   * Returns the Markdown text as PSML using the block parser.
   *
   * @param lines The lines of text to parse
   *
   * @return The corresponding PSML as a string.
   */
  private static String toPSML(List<String> lines) {
    return toPSML(lines, MarkdownInputOptions.defaultFragmentOptions());
  }

  /**
   * Returns the Markdown text as PSML using the block parser.
   *
   * @param lines The lines of text to parse
   *
   * @return The corresponding PSML as a string.
   */
  private static String toPSML(List<String> lines, MarkdownInputOptions options) {
    try {
      BlockParser parser = new BlockParser(options);
      List<PSMLElement> elements = parser.parse(lines);
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      for (PSMLElement e : elements) {
        e.toXML(xml);
      }
      xml.flush();
      return xml.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new UncheckedIOException(ex);
    }
  }

}
