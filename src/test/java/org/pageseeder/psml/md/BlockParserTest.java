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
import java.util.Arrays;
import java.util.List;

public class BlockParserTest {

  public BlockParserTest() {
  }

  @Test
  public void testEscaping() throws Exception {
    List<String> e1 = Arrays.asList("This is \\*escaped star\\*");
    List<String> e2 = Arrays.asList("This is \\[escaped brackets\\]");
    List<String> e3 = Arrays.asList(" \\- Red", " \\- Green", " \\- Blue");
    Assert.assertEquals("<para>This is *escaped star*</para>", toPSML(e1));
    Assert.assertEquals("<para>This is [escaped brackets]</para>", toPSML(e2));
    Assert.assertEquals("<para>- Red<br/>\n- Green<br/>\n- Blue</para>", toPSML(e3));
  }

  @Test
  public void testHeadingSetext() throws Exception {
    List<String> h1 = Arrays.asList("This is an H1", "=============");
    List<String> h2 = Arrays.asList("This is an H2", "-------------");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
  }

  @Test
  public void testHeadingATX() throws Exception {
    List<String> h1 = Arrays.asList("# This is an H1");
    List<String> h2 = Arrays.asList("## This is an H2");
    List<String> h3 = Arrays.asList("### This is an H3");
    List<String> h4 = Arrays.asList("#### This is an H4");
    List<String> h5 = Arrays.asList("##### This is an H5");
    List<String> h6 = Arrays.asList("###### This is an H6");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
    Assert.assertEquals("<heading level=\"3\">This is an H3</heading>", toPSML(h3));
    Assert.assertEquals("<heading level=\"4\">This is an H4</heading>", toPSML(h4));
    Assert.assertEquals("<heading level=\"5\">This is an H5</heading>", toPSML(h5));
    Assert.assertEquals("<heading level=\"6\">This is an H6</heading>", toPSML(h6));
  }

  @Test
  public void testHeadingATX2() throws Exception {
    List<String> h1 = Arrays.asList("# This is an H1 #");
    List<String> h2 = Arrays.asList("## This is an H2 ##");
    List<String> h3 = Arrays.asList("### This is an H3 ###");
    List<String> h4 = Arrays.asList("#### This is an H4 ####");
    List<String> h5 = Arrays.asList("##### This is an H5 #####");
    List<String> h6 = Arrays.asList("###### This is an H6 ######");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
    Assert.assertEquals("<heading level=\"3\">This is an H3</heading>", toPSML(h3));
    Assert.assertEquals("<heading level=\"4\">This is an H4</heading>", toPSML(h4));
    Assert.assertEquals("<heading level=\"5\">This is an H5</heading>", toPSML(h5));
    Assert.assertEquals("<heading level=\"6\">This is an H6</heading>", toPSML(h6));
  }

  @Test
  public void testBlockquotes() throws Exception {
    List<String> q1 = Arrays.asList("> Hello");
    Assert.assertEquals("<block label=\"quoted\"><para>Hello</para></block>", toPSML(q1));
  }

  @Test
  public void testParagraphs() throws Exception {
    List<String> p1 = Arrays.asList("Hi!");
    List<String> p2 = Arrays.asList("Hi!","","Welcome");
    List<String> p3 = Arrays.asList("Hi!","Welcome");
    List<String> p4 = Arrays.asList("Hi!","Welcome","","Test");
    List<String> p5 = Arrays.asList("Hi!","","Welcome","Test");
    List<String> p6 = Arrays.asList("Hi!","","Welcome","","Test");
    List<String> p7 = Arrays.asList("This line is longer than sixty-six characters and should continue without", "a line break");
    Assert.assertEquals("<para>Hi!</para>", toPSML(p1));
    Assert.assertEquals("<para>Hi!</para><para>Welcome</para>", toPSML(p2));
    Assert.assertEquals("<para>Hi!<br/>\nWelcome</para>", toPSML(p3));
    Assert.assertEquals("<para>Hi!<br/>\nWelcome</para><para>Test</para>", toPSML(p4));
    Assert.assertEquals("<para>Hi!</para><para>Welcome<br/>\nTest</para>", toPSML(p5));
    Assert.assertEquals("<para>Hi!</para><para>Welcome</para><para>Test</para>", toPSML(p6));
    Assert.assertEquals("<para>This line is longer than sixty-six characters and should continue without\na line break</para>", toPSML(p7));
  }


  @Test
  public void testUnorderedList() throws Exception {
    List<String> list1 = Arrays.asList(" * Red", " * Green", " * Blue");
    List<String> list2 = Arrays.asList(" + Red", " + Green", " + Blue");
    List<String> list3 = Arrays.asList(" - Red", " - Green", " - Blue");
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list1));
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list2));
    Assert.assertEquals("<list><item>Red</item><item>Green</item><item>Blue</item></list>", toPSML(list3));
  }

  @Test
  public void testOrderedList() throws Exception {
    List<String> list1 = Arrays.asList(" 1. Bird", " 2. McHale", " 3. Parish");
    List<String> list2 = Arrays.asList(" 1. Bird", " 1. McHale", " 1. Parish");
    List<String> list3 = Arrays.asList(" 7. Bird", " 8. McHale", " 9. Parish");
    List<String> list4 = Arrays.asList(" 3. Bird", " 1. McHale", " 8. Parish");
    Assert.assertEquals("<nlist><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list1));
    Assert.assertEquals("<nlist><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list2));
    Assert.assertEquals("<nlist start=\"7\"><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list3));
    Assert.assertEquals("<nlist start=\"3\"><item>Bird</item><item>McHale</item><item>Parish</item></nlist>", toPSML(list4));
  }

  @Test
  public void testOrderedListEscape() throws Exception {
    List<String> list1 = Arrays.asList(" 1986. What a great season.");
    List<String> list2 = Arrays.asList(" 1986\\. What a great season.");
    Assert.assertEquals("<nlist start=\"1986\"><item>What a great season.</item></nlist>", toPSML(list1));
    Assert.assertEquals("<para>1986\\. What a great season.</para>", toPSML(list2));
  }

  @Test
  public void testCodeBlock() throws Exception {
    List<String> pre = Arrays.asList("    This is a code block.");
    Assert.assertEquals("<preformat>This is a code block.</preformat>", toPSML(pre));
  }


  @Test
  public void testFencedCode() throws Exception {
    List<String> fencedCode1 = Arrays.asList("```", "function() { return 'Hello!';}", "```");
    List<String> fencedCode2 = Arrays.asList("```javascript", "function() { return 'Hello!';}", "```");
    List<String> fencedCode3 = Arrays.asList("```", "function() {", "  return 'Hello!';", "}", "```");
    List<String> fencedCode4 = Arrays.asList("```", "function() {", "  if (a", "  > b) then a = b;", "}", "```");
    List<String> fencedCode5 = Arrays.asList("```", "function() {","","return 'Hello!';}", "```");
    List<String> fencedCode6 = Arrays.asList("```", "<report>","  <errors>","    <error>", "```");
    Assert.assertEquals("<preformat>\nfunction() { return 'Hello!';}\n</preformat>", toPSML(fencedCode1));
    Assert.assertEquals("<preformat role=\"javascript\">\nfunction() { return 'Hello!';}\n</preformat>", toPSML(fencedCode2));
    Assert.assertEquals("<preformat>\nfunction() {\n  return 'Hello!';\n}\n</preformat>", toPSML(fencedCode3));
    Assert.assertEquals("<preformat>\nfunction() {\n  if (a\n  &gt; b) then a = b;\n}\n</preformat>", toPSML(fencedCode4));
    Assert.assertEquals("<preformat>\nfunction() {\n\nreturn 'Hello!';}\n</preformat>", toPSML(fencedCode5));
    Assert.assertEquals("<preformat>\n&lt;report&gt;\n  &lt;errors&gt;\n    &lt;error&gt;\n</preformat>", toPSML(fencedCode6));
  }

  @Test
  public void testUC1() throws Exception {
    List<String> mixed = Arrays.asList("`coconut`");
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
    state.push(Name.Fragment);
    Assert.assertTrue(state.isElement(Name.Fragment));
    state.commit();
    Assert.assertFalse(state.isElement(Name.Fragment));
    Assert.assertNull(state.current());
    // Empty fragment with paragraph
    state.push(Name.Fragment);
    state.push(Name.Para, "test");
    Assert.assertTrue(state.isElement(Name.Para));
    state.commit();
    Assert.assertFalse(state.isElement(Name.Para));
    Assert.assertTrue(state.isElement(Name.Fragment));
  }

  @Test
  public void testStateCurrent() {
    State state = new State();
    Assert.assertNull(state.current());
    state.push(Name.Fragment);
    Assert.assertEquals(Name.Fragment, state.current().getElement());
    Assert.assertNotEquals(Name.Para, state.current().getElement());
    state.push(Name.Para);
    Assert.assertNotEquals(Name.Fragment, state.current().getElement());
    Assert.assertEquals(Name.Para, state.current().getElement());
  }

  @Test
  public void testStateIsElement() {
    State state = new State();
    Assert.assertFalse(state.isElement(null));
    Assert.assertFalse(state.isElement(Name.Fragment));
    Assert.assertFalse(state.isElement(Name.Para));
    state.push(Name.Fragment);
    Assert.assertTrue(state.isElement(Name.Fragment));
    Assert.assertFalse(state.isElement(Name.Para));
    state.push(Name.Para);
    Assert.assertFalse(state.isElement(Name.Fragment));
    Assert.assertTrue(state.isElement(Name.Para));
  }

  @Test
  public void testStateIsDescendantOf() {
    State state = new State();
    Assert.assertFalse(state.isDescendantOf(Name.Para));
    state.push(Name.Fragment);
    Assert.assertFalse(state.isDescendantOf(Name.Para));
    state.push(Name.Para);
    Assert.assertTrue(state.isDescendantOf(Name.Para));
    state.push(Name.Bold);
    Assert.assertTrue(state.isDescendantOf(Name.Para));
    Assert.assertTrue(state.isDescendantOf(Name.Fragment));
  }

  /**
   * Returns the Markdown text as PSML using the block parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding PSML as a string.
   *
   * @throws IOException If thrown by
   */
  private static String toPSML(List<String> lines) {
    try {
      BlockParser parser = new BlockParser();
      List<PSMLElement> elements = parser.parse(lines);
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      for (PSMLElement e : elements) {
        e.toXML(xml);
      }
      xml.flush();
      return xml.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new RuntimeException(ex);
    }
  }

}
