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
import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.md.HTMLBlockParser.State;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HTMLBlockParserTest {

  public HTMLBlockParserTest() {
  }

  @Test
  public void testEscaping() throws Exception {
    List<String> e1 = Arrays.asList("This is \\*escaped star\\*");
    List<String> e2 = Arrays.asList("This is \\[escaped brackets\\]");
    List<String> e3 = Arrays.asList(" \\- Red", " \\- Green", " \\- Blue");
    Assert.assertEquals("<p>This is *escaped star*</p>", toHTML(e1));
    Assert.assertEquals("<p>This is [escaped brackets]</p>", toHTML(e2));
    Assert.assertEquals("<p>- Red<br/>\n- Green<br/>\n- Blue</p>", toHTML(e3));
  }

  @Test
  public void testHeadingSetext() throws Exception {
    List<String> h1 = Arrays.asList("This is an H1", "=============");
    List<String> h2 = Arrays.asList("This is an H2", "-------------");
    Assert.assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    Assert.assertEquals("<h2>This is an H2</h2>", toHTML(h2));
  }

  @Test
  public void testHeadingATX() throws Exception {
    List<String> h1 = Arrays.asList("# This is an H1");
    List<String> h2 = Arrays.asList("## This is an H2");
    List<String> h3 = Arrays.asList("### This is an H3");
    List<String> h4 = Arrays.asList("#### This is an H4");
    List<String> h5 = Arrays.asList("##### This is an H5");
    List<String> h6 = Arrays.asList("###### This is an H6");
    Assert.assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    Assert.assertEquals("<h2>This is an H2</h2>", toHTML(h2));
    Assert.assertEquals("<h3>This is an H3</h3>", toHTML(h3));
    Assert.assertEquals("<h4>This is an H4</h4>", toHTML(h4));
    Assert.assertEquals("<h5>This is an H5</h5>", toHTML(h5));
    Assert.assertEquals("<h6>This is an H6</h6>", toHTML(h6));
  }

  @Test
  public void testHeadingATX2() throws Exception {
    List<String> h1 = Arrays.asList("# This is an H1 #");
    List<String> h2 = Arrays.asList("## This is an H2 ##");
    List<String> h3 = Arrays.asList("### This is an H3 ###");
    List<String> h4 = Arrays.asList("#### This is an H4 ####");
    List<String> h5 = Arrays.asList("##### This is an H5 #####");
    List<String> h6 = Arrays.asList("###### This is an H6 ######");
    Assert.assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    Assert.assertEquals("<h2>This is an H2</h2>", toHTML(h2));
    Assert.assertEquals("<h3>This is an H3</h3>", toHTML(h3));
    Assert.assertEquals("<h4>This is an H4</h4>", toHTML(h4));
    Assert.assertEquals("<h5>This is an H5</h5>", toHTML(h5));
    Assert.assertEquals("<h6>This is an H6</h6>", toHTML(h6));
  }

  @Test
  public void testBlockquotes() throws Exception {
    List<String> q1 = Arrays.asList("> Hello");
    Assert.assertEquals("<blockquote><p>Hello</p></blockquote>", toHTML(q1));
  }

  @Test
  public void testUnorderedList() throws Exception {
    List<String> list1 = Arrays.asList(" * Red", " * Green", " * Blue");
    List<String> list2 = Arrays.asList(" + Red", " + Green", " + Blue");
    List<String> list3 = Arrays.asList(" - Red", " - Green", " - Blue");
    Assert.assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list1));
    Assert.assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list2));
    Assert.assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list3));
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
    Assert.assertEquals("<p>Hi!</p>", toHTML(p1));
    Assert.assertEquals("<p>Hi!</p><p>Welcome</p>", toHTML(p2));
    Assert.assertEquals("<p>Hi!<br/>\nWelcome</p>", toHTML(p3));
    Assert.assertEquals("<p>Hi!<br/>\nWelcome</p><p>Test</p>", toHTML(p4));
    Assert.assertEquals("<p>Hi!</p><p>Welcome<br/>\nTest</p>", toHTML(p5));
    Assert.assertEquals("<p>Hi!</p><p>Welcome</p><p>Test</p>", toHTML(p6));
    Assert.assertEquals("<p>This line is longer than sixty-six characters and should continue without\na line break</p>", toHTML(p7));
  }

  @Test
  public void testOrderedList() throws Exception {
    List<String> list1 = Arrays.asList(" 1. Bird", " 2. McHale", " 3. Parish");
    List<String> list2 = Arrays.asList(" 1. Bird", " 1. McHale", " 1. Parish");
    List<String> list3 = Arrays.asList(" 7. Bird", " 8. McHale", " 9. Parish");
    List<String> list4 = Arrays.asList(" 3. Bird", " 1. McHale", " 8. Parish");
    Assert.assertEquals("<ol><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list1));
    Assert.assertEquals("<ol><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list2));
    Assert.assertEquals("<ol start=\"7\"><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list3));
    Assert.assertEquals("<ol start=\"3\"><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list4));
  }

  @Test
  public void testOrderedListEscape() throws Exception {
    List<String> list1 = Arrays.asList(" 1986. What a great season.");
    List<String> list2 = Arrays.asList(" 1986\\. What a great season.");
    Assert.assertEquals("<ol start=\"1986\"><li>What a great season.</li></ol>", toHTML(list1));
    Assert.assertEquals("<p>1986. What a great season.</p>", toHTML(list2));
  }

  @Test
  public void testCodeBlock() throws Exception {
    List<String> pre = Arrays.asList("    This is a code block.");
    Assert.assertEquals("<pre>This is a code block.</pre>", toHTML(pre));
  }


  @Test
  public void testFencedCode() throws Exception {
    List<String> fencedCode1 = Arrays.asList("```", "function() { return 'Hello!';}", "```");
    List<String> fencedCode2 = Arrays.asList("```javascript", "function() { return 'Hello!';}", "```");
    List<String> fencedCode3 = Arrays.asList("```", "function() {", "  return 'Hello!';", "}", "```");
    List<String> fencedCode4 = Arrays.asList("```", "function() {", "  if (a", "  > b) then a = b;", "}", "```");
    List<String> fencedCode5 = Arrays.asList("```", "function() {","","return 'Hello!';}", "```");
    List<String> fencedCode6 = Arrays.asList("```", "<report>","  <errors>","    <error>", "```");
    Assert.assertEquals("<pre>\nfunction() { return 'Hello!';}\n</pre>", toHTML(fencedCode1));
    Assert.assertEquals("<pre><code class=\"javascript\">\nfunction() { return 'Hello!';}\n</code></pre>", toHTML(fencedCode2));
    Assert.assertEquals("<pre>\nfunction() {\n  return 'Hello!';\n}\n</pre>", toHTML(fencedCode3));
    Assert.assertEquals("<pre>\nfunction() {\n  if (a\n  &gt; b) then a = b;\n}\n</pre>", toHTML(fencedCode4));
    Assert.assertEquals("<pre>\nfunction() {\n\nreturn 'Hello!';}\n</pre>", toHTML(fencedCode5));
    Assert.assertEquals("<pre>\n&lt;report&gt;\n  &lt;errors&gt;\n    &lt;error&gt;\n</pre>", toHTML(fencedCode6));
  }

  @Test
  public void testUC1() throws Exception {
    List<String> mixed = Arrays.asList("`coconut`");
    Assert.assertEquals("<p><code>coconut</code></p>", toHTML(mixed));
  }



  // Testing the parser state
  // --------------------------------------------------------------------------------------------

  @Test
  public void testStateCommit() {
    State state = new State();
    state.commit();
    Assert.assertNull(state.current());
    // Empty fragment
    state.push(Name.SECTION);
    Assert.assertTrue(state.isElement(Name.SECTION));
    state.commit();
    Assert.assertFalse(state.isElement(Name.SECTION));
    Assert.assertNull(state.current());
    // Empty fragment with paragraph
    state.push(Name.SECTION);
    state.push(Name.P, "test");
    Assert.assertTrue(state.isElement(Name.P));
    state.commit();
    Assert.assertFalse(state.isElement(Name.P));
    Assert.assertTrue(state.isElement(Name.SECTION));
  }

  @Test
  public void testStateCurrent() {
    State state = new State();
    Assert.assertNull(state.current());
    state.push(Name.SECTION);
    Assert.assertEquals(Name.SECTION, state.current().getElement());
    Assert.assertNotEquals(Name.P, state.current().getElement());
    state.push(Name.P);
    Assert.assertNotEquals(Name.SECTION, state.current().getElement());
    Assert.assertEquals(Name.P, state.current().getElement());
  }

  @Test
  public void testStateIsElement() {
    State state = new State();
    Assert.assertFalse(state.isElement(null));
    Assert.assertFalse(state.isElement(Name.SECTION));
    Assert.assertFalse(state.isElement(Name.P));
    state.push(Name.SECTION);
    Assert.assertTrue(state.isElement(Name.SECTION));
    Assert.assertFalse(state.isElement(Name.P));
    state.push(Name.P);
    Assert.assertFalse(state.isElement(Name.SECTION));
    Assert.assertTrue(state.isElement(Name.P));
  }

  @Test
  public void testStateIsDescendantOf() {
    State state = new State();
    Assert.assertFalse(state.isDescendantOf(Name.P));
    state.push(Name.SECTION);
    Assert.assertFalse(state.isDescendantOf(Name.P));
    state.push(Name.P);
    Assert.assertTrue(state.isDescendantOf(Name.P));
    state.push(Name.STRONG);
    Assert.assertTrue(state.isDescendantOf(Name.P));
    Assert.assertTrue(state.isDescendantOf(Name.SECTION));
  }

  /**
   * Returns the Markdown text as HTML using the block parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding HTML as a string.
   *
   * @throws IOException If thrown by
   */
  private static String toHTML(List<String> lines) {
    try {
      HTMLBlockParser parser = new HTMLBlockParser();
      List<HTMLElement> elements = parser.parse(lines);
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      for (HTMLElement e : elements) {
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
