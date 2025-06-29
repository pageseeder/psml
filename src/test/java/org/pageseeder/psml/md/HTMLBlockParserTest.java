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
import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.md.HTMLBlockParser.State;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BlockParser functionality, ensuring accurate parsing of Markdown text
 * into the corresponding PSML format.
 */
class HTMLBlockParserTest {

  @Test
  void testEscaping() {
    List<String> e1 = List.of("This is \\*escaped star\\*");
    List<String> e2 = List.of("This is \\[escaped brackets\\]");
    List<String> e3 = List.of(" \\- Red", " \\- Green", " \\- Blue");
    assertEquals("<p>This is *escaped star*</p>", toHTML(e1));
    assertEquals("<p>This is [escaped brackets]</p>", toHTML(e2));
    assertEquals("<p>- Red<br/>\n- Green<br/>\n- Blue</p>", toHTML(e3));
  }

  @Test
  void testHeadingSetext() {
    List<String> h1 = List.of("This is an H1", "=============");
    List<String> h2 = List.of("This is an H2", "-------------");
    assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    assertEquals("<h2>This is an H2</h2>", toHTML(h2));
  }

  @Test
  void testHeadingATX() {
    List<String> h1 = List.of("# This is an H1");
    List<String> h2 = List.of("## This is an H2");
    List<String> h3 = List.of("### This is an H3");
    List<String> h4 = List.of("#### This is an H4");
    List<String> h5 = List.of("##### This is an H5");
    List<String> h6 = List.of("###### This is an H6");
    assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    assertEquals("<h2>This is an H2</h2>", toHTML(h2));
    assertEquals("<h3>This is an H3</h3>", toHTML(h3));
    assertEquals("<h4>This is an H4</h4>", toHTML(h4));
    assertEquals("<h5>This is an H5</h5>", toHTML(h5));
    assertEquals("<h6>This is an H6</h6>", toHTML(h6));
  }

  @Test
  void testHeadingATX2() {
    List<String> h1 = List.of("# This is an H1 #");
    List<String> h2 = List.of("## This is an H2 ##");
    List<String> h3 = List.of("### This is an H3 ###");
    List<String> h4 = List.of("#### This is an H4 ####");
    List<String> h5 = List.of("##### This is an H5 #####");
    List<String> h6 = List.of("###### This is an H6 ######");
    assertEquals("<h1>This is an H1</h1>", toHTML(h1));
    assertEquals("<h2>This is an H2</h2>", toHTML(h2));
    assertEquals("<h3>This is an H3</h3>", toHTML(h3));
    assertEquals("<h4>This is an H4</h4>", toHTML(h4));
    assertEquals("<h5>This is an H5</h5>", toHTML(h5));
    assertEquals("<h6>This is an H6</h6>", toHTML(h6));
  }

  @Test
  void testBlockquotes() {
    List<String> q1 = List.of("> Hello");
    assertEquals("<blockquote><p>Hello</p></blockquote>", toHTML(q1));
  }

  @Test
  void testUnorderedList() {
    List<String> list1 = List.of(" * Red", " * Green", " * Blue");
    List<String> list2 = List.of(" + Red", " + Green", " + Blue");
    List<String> list3 = List.of(" - Red", " - Green", " - Blue");
    assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list1));
    assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list2));
    assertEquals("<ul><li>Red</li><li>Green</li><li>Blue</li></ul>", toHTML(list3));
  }

  @Test
  void testParagraphs() {
    List<String> p1 = List.of("Hi!");
    List<String> p2 = List.of("Hi!", "", "Welcome");
    List<String> p3 = List.of("Hi!", "Welcome");
    List<String> p4 = List.of("Hi!", "Welcome", "", "Test");
    List<String> p5 = List.of("Hi!", "", "Welcome", "Test");
    List<String> p6 = List.of("Hi!", "", "Welcome", "", "Test");
    List<String> p7 = List.of("This line is longer than sixty-six characters and should continue without", "a line break");
    assertEquals("<p>Hi!</p>", toHTML(p1));
    assertEquals("<p>Hi!</p><p>Welcome</p>", toHTML(p2));
    assertEquals("<p>Hi!<br/>\nWelcome</p>", toHTML(p3));
    assertEquals("<p>Hi!<br/>\nWelcome</p><p>Test</p>", toHTML(p4));
    assertEquals("<p>Hi!</p><p>Welcome<br/>\nTest</p>", toHTML(p5));
    assertEquals("<p>Hi!</p><p>Welcome</p><p>Test</p>", toHTML(p6));
    assertEquals("<p>This line is longer than sixty-six characters and should continue without\na line break</p>", toHTML(p7));
  }

  @Test
  void testOrderedList() {
    List<String> list1 = List.of(" 1. Bird", " 2. McHale", " 3. Parish");
    List<String> list2 = List.of(" 1. Bird", " 1. McHale", " 1. Parish");
    List<String> list3 = List.of(" 7. Bird", " 8. McHale", " 9. Parish");
    List<String> list4 = List.of(" 3. Bird", " 1. McHale", " 8. Parish");
    assertEquals("<ol><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list1));
    assertEquals("<ol><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list2));
    assertEquals("<ol start=\"7\"><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list3));
    assertEquals("<ol start=\"3\"><li>Bird</li><li>McHale</li><li>Parish</li></ol>", toHTML(list4));
  }

  @Test
  void testOrderedListEscape() {
    List<String> list1 = List.of(" 1986. What a great season.");
    List<String> list2 = List.of(" 1986\\. What a great season.");
    assertEquals("<ol start=\"1986\"><li>What a great season.</li></ol>", toHTML(list1));
    assertEquals("<p>1986. What a great season.</p>", toHTML(list2));
  }

  @Test
  void testCodeBlock() {
    List<String> pre = List.of("    This is a code block.");
    assertEquals("<pre>This is a code block.</pre>", toHTML(pre));
  }


  @Test
  void testFencedCode() {
    List<String> fencedCode1 = List.of("```", "function() { return 'Hello!';}", "```");
    List<String> fencedCode2 = List.of("```javascript", "function() { return 'Hello!';}", "```");
    List<String> fencedCode3 = List.of("```", "function() {", "  return 'Hello!';", "}", "```");
    List<String> fencedCode4 = List.of("```", "function() {", "  if (a", "  > b) then a = b;", "}", "```");
    List<String> fencedCode5 = List.of("```", "function() {", "", "return 'Hello!';}", "```");
    List<String> fencedCode6 = List.of("```", "<report>", "  <errors>", "    <error>", "```");
    assertEquals("<pre>\nfunction() { return 'Hello!';}\n</pre>", toHTML(fencedCode1));
    assertEquals("<pre><code class=\"lang-javascript\">\nfunction() { return 'Hello!';}\n</code></pre>", toHTML(fencedCode2));
    assertEquals("<pre>\nfunction() {\n  return 'Hello!';\n}\n</pre>", toHTML(fencedCode3));
    assertEquals("<pre>\nfunction() {\n  if (a\n  &gt; b) then a = b;\n}\n</pre>", toHTML(fencedCode4));
    assertEquals("<pre>\nfunction() {\n\nreturn 'Hello!';}\n</pre>", toHTML(fencedCode5));
    assertEquals("<pre>\n&lt;report&gt;\n  &lt;errors&gt;\n    &lt;error&gt;\n</pre>", toHTML(fencedCode6));
  }

  //  @Test
  void testFencedBlock() {
    List<String> fencedBlock1 = List.of("~~~warning", "A warning!", "~~~");
    List<String> fencedBlock2 = List.of("~~~", "Anonymous block", "~~~");
    List<String> fencedBlock3 = List.of("~~~empty", "~~~");
    List<String> fencedBlock4 = List.of("~~~warning", "A double", "", "warning", "~~~");
    List<String> fencedBlock5 = List.of("~~~note", " - apple", " - pear", "", "~~~");
    List<String> fencedBlock6 = List.of("~~~wrap", "~~~warning", "The warning", "~~~", "~~~");
    assertEquals("<div class=\"label-warning\" label=\"warning\"><p>A warning!</p></div>", toHTML(fencedBlock1));
    assertEquals("<div><p>Anonymous block</p></div>", toHTML(fencedBlock2));
    assertEquals("<div class=\"label-empty\" label=\"empty\"/>", toHTML(fencedBlock3));
    assertEquals("<div class=\"label-warning\" label=\"warning\"><p>A double</p><p>warning</p></div>", toHTML(fencedBlock4));
    assertEquals("<div class=\"label-note\" label=\"note\"><ul><li>apple</li><li>pear</li></ul></div>", toHTML(fencedBlock5));
    assertEquals("<div class=\"label-wrap\" label=\"wrap\"><div class=\"label-warning\"><para>The warning</para></div></div>", toHTML(fencedBlock6));
  }

  @Test
  void testTable() {
    List<String> table1 = List.of(
        "| TH 1 | TH 2 | TH 3 |",
        "|------|------|------|",
        "| R1C1 | R1C2 | R1C3 |",
        "| R2C1 | R2C2 | R2C3 |");
    String expected = "<table>" +
        "<col/><col/><col/>" +
        "<tr><th>TH 1</th><th>TH 2</th><th>TH 3</th></tr>" +
        "<tr><td>R1C1</td><td>R1C2</td><td>R1C3</td></tr>" +
        "<tr><td>R2C1</td><td>R2C2</td><td>R2C3</td></tr>" +
        "</table>";
    assertEquals(expected, toHTML(table1));
  }

  @Test
  void testUC1() {
    List<String> mixed = List.of("`coconut`");
    assertEquals("<p><code>coconut</code></p>", toHTML(mixed));
  }


  // Testing the parser state
  // --------------------------------------------------------------------------------------------

  @Test
  void testStateCommit() {
    State state = new State();
    state.commit();
    assertNull(state.current());
    // Empty fragment
    state.push(Name.SECTION);
    assertTrue(state.isElement(Name.SECTION));
    state.commit();
    assertFalse(state.isElement(Name.SECTION));
    assertNull(state.current());
    // Empty fragment with paragraph
    state.push(Name.SECTION);
    state.push(Name.P, "test");
    assertTrue(state.isElement(Name.P));
    state.commit();
    assertFalse(state.isElement(Name.P));
    assertTrue(state.isElement(Name.SECTION));
  }

  @Test
  void testStateCurrent() {
    State state = new State();
    assertNull(state.current());
    state.push(Name.SECTION);
    assertEquals(Name.SECTION, state.current().getElement());
    assertNotEquals(Name.P, state.current().getElement());
    state.push(Name.P);
    assertNotEquals(Name.SECTION, state.current().getElement());
    assertEquals(Name.P, state.current().getElement());
  }

  @Test
  void testStateIsElement() {
    State state = new State();
    assertFalse(state.isElement(null));
    assertFalse(state.isElement(Name.SECTION));
    assertFalse(state.isElement(Name.P));
    state.push(Name.SECTION);
    assertTrue(state.isElement(Name.SECTION));
    assertFalse(state.isElement(Name.P));
    state.push(Name.P);
    assertFalse(state.isElement(Name.SECTION));
    assertTrue(state.isElement(Name.P));
  }

  @Test
  void testStateIsDescendantOf() {
    State state = new State();
    assertFalse(state.isDescendantOf(Name.P));
    state.push(Name.SECTION);
    assertFalse(state.isDescendantOf(Name.P));
    state.push(Name.P);
    assertTrue(state.isDescendantOf(Name.P));
    state.push(Name.STRONG);
    assertTrue(state.isDescendantOf(Name.P));
    assertTrue(state.isDescendantOf(Name.SECTION));
  }

  /**
   * Returns the Markdown text as HTML using the block parser.
   *
   * @param lines The text to parse
   * @return The corresponding HTML as a string.
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
