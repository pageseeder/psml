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
package org.pageseeder.psml.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;

class HTMLParserTest {

  @Test
  void testMediatype() {
    assertEquals("application/xhtml+xml", new HTMLParser().getMediatype());
  }

  @Test
  void testDocumentStructure() {
    PSMLElement document = parse("<article>"
        + "<section id=\"title\"><section id=\"1\"><h1>Title</h1></section></section>"
        + "<section id=\"content\"><section id=\"2\"><p>A</p></section><section id=\"3\"><p>B</p></section></section>"
        + "</article>");
    assertEquals("<document level=\"portable\">"
        + "<section id=\"title\"><fragment id=\"1\"><heading level=\"1\">Title</heading></fragment></section>"
        + "<section id=\"content\"><fragment id=\"2\"><para>A</para></fragment><fragment id=\"3\"><para>B</para></fragment></section>"
        + "</document>", document.toString());
  }

  @Test
  void testDocumentLooseContent() {
    PSMLElement document = parse("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>T</title></head>"
        + "<body><h1>Title</h1>\n<p>Text</p><section><p>A</p><section id=\"x\"><p>B</p></section></section></body></html>");
    assertEquals(Name.DOCUMENT, document.getElement());
    List<PSMLElement> sections = document.getChildElements(Name.SECTION);
    assertEquals(2, sections.size());
    assertEquals("section-1", sections.get(0).getAttribute("id"));
    assertEquals("<fragment id=\"1\"><heading level=\"1\">Title</heading><para>Text</para></fragment>",
        sections.get(0).getFirstChildElement(Name.FRAGMENT).toString());
    List<PSMLElement> fragments = sections.get(1).getChildElements(Name.FRAGMENT);
    assertEquals(2, fragments.size());
    assertEquals("<fragment id=\"2\"><para>A</para></fragment>", fragments.get(0).toString());
    assertEquals("<fragment id=\"x\"><para>B</para></fragment>", fragments.get(1).toString());
  }

  @Test
  void testFragment() {
    assertEquals("<fragment id=\"f\"><para>A</para><para>B</para></fragment>",
        parse("<section id=\"f\"><p>A</p>\n  <p>B</p></section>").toString());
    assertEquals("<fragment id=\"1\"><para>A</para></fragment>", parse("<div><p>A</p></div>").toString());
  }

  @Test
  void testFragment_BlockRoot() {
    assertEquals("<fragment id=\"1\"><para>A</para></fragment>", parse("<p>A</p>").toString());
    assertEquals("<fragment id=\"1\"><list><item>A</item></list></fragment>", parse("<ul><li>A</li></ul>").toString());
  }

  @Test
  void testLooseInlineContentWrappedInPara() {
    assertEquals("<fragment id=\"1\"><para>Some <bold>text</bold></para><para>A</para><para>more</para></fragment>",
        parse("<section> Some <b>text</b> <p>A</p> more </section>").toString());
  }

  @Test
  void testHeadings() {
    assertEquals("<heading level=\"3\">Hello</heading>", block("<h3>Hello</h3>"));
    assertEquals("<heading level=\"6\">Hello</heading>", block("<h6>Hello</h6>"));
    PSMLElement heading = parseBlock("<h2><span class=\"prefix\">1.2</span> Hello</h2>");
    assertEquals("1.2", heading.getAttribute("prefix"));
    assertEquals("Hello", heading.getText());
  }

  @Test
  void testPara() {
    assertEquals("<para>A B C</para>", block("<p>\n  A\n  B   C\n</p>"));
    assertEquals("<para indent=\"2\">A</para>", block("<p class=\"indent-2\">A</p>"));
  }

  @Test
  void testInline() {
    assertEquals("<para><bold>a</bold><bold>b</bold><italic>c</italic><italic>d</italic><underline>e</underline>"
        + "<monospace>f</monospace><sub>g</sub><sup>h</sup><br/>i</para>",
        block("<p><strong>a</strong><b>b</b><em>c</em><i>d</i><u>e</u><code>f</code><sub>g</sub><sup>h</sup><br/>i</p>"));
    assertEquals("<para>A <bold>bold <italic>and italic</italic></bold> text</para>",
        block("<p>A <strong>bold <em>and italic</em></strong> text</p>"));
    assertEquals("<para>Transparent span</para>", block("<p><span>Transparent</span> <mark>span</mark></p>"));
    assertEquals("<para><inline label=\"term\">x</inline></para>", block("<p><span class=\"label-term\">x</span></p>"));
    PSMLElement diff = parseBlock("<p><ins>a</ins><del>b</del></p>");
    assertEquals(Name.DIFF_INS, diff.getChildElements().get(0).getElement());
    assertEquals(Name.DIFF_DEL, diff.getChildElements().get(1).getElement());
  }

  @Test
  void testLinks() {
    assertEquals("<para><link href=\"https://example.org\">Example</link></para>",
        block("<p><a href=\"https://example.org\">Example</a></p>"));
    assertEquals("<para><link href=\"#top\">Top</link></para>", block("<p><a href=\"#top\">Top</a></p>"));
    PSMLElement xref = parseBlock("<p><a href=\"other.psml#2\">Other</a></p>").getFirstChildElement(Name.XREF);
    assertNotNull(xref);
    assertEquals("other.psml", xref.getAttribute("href"));
    assertEquals("2", xref.getAttribute("frag"));
    assertEquals("Other", xref.getAttribute("title"));
    assertEquals("manual", xref.getAttribute("display"));
    assertEquals("Other", xref.getText());
    PSMLElement xref2 = parseBlock("<p><a href=\"other.psml\">Other</a></p>").getFirstChildElement(Name.XREF);
    assertNotNull(xref2);
    assertEquals("default", xref2.getAttribute("frag"));
    assertEquals("<para><anchor name=\"here\"/>Here</para>", block("<p><a id=\"here\"/>Here</p>"));
  }

  @Test
  void testImage() {
    PSMLElement image = parseBlock("<p><img src=\"a.png\" alt=\"A\" width=\"20\"/></p>").getFirstChildElement(Name.IMAGE);
    assertNotNull(image);
    assertEquals("a.png", image.getAttribute("src"));
    assertEquals("A", image.getAttribute("alt"));
    assertEquals("20", image.getAttribute("width"));
  }

  @Test
  void testLists() {
    assertEquals("<list><item>A</item><item>B</item></list>", block("<ul>\n <li>A</li>\n <li> B </li>\n</ul>"));
    assertEquals("<nlist start=\"3\"><item>A</item></nlist>", block("<ol start=\"3\"><li>A</li></ol>"));
    assertEquals("<nlist type=\"lowerroman\"><item>A</item></nlist>", block("<ol type=\"i\"><li>A</li></ol>"));
    assertEquals("<list><item>A<list><item>B</item></list></item></list>",
        block("<ul><li>A\n <ul><li>B</li></ul></li></ul>"));
    assertEquals("<list><item><para>A</para><para>B</para></item></list>", block("<ul><li><p>A</p> <p>B</p></li></ul>"));
  }

  @Test
  void testPreformat() {
    assertEquals("<preformat>a\n  b &lt; c</preformat>", block("<pre>a\n  b &lt; c</pre>"));
    assertEquals("<preformat role=\"lang-java\">int a;\n</preformat>", block("<pre><code class=\"lang-java\">int a;\n</code></pre>"));
    assertEquals("<preformat role=\"lang-js\">x</preformat>", block("<pre><code class=\"language-js\">x</code></pre>"));
  }

  @Test
  void testBlocks() {
    assertEquals("<block label=\"quoted\"><para>A</para><para>B</para></block>",
        block("<blockquote>A<p>B</p></blockquote>"));
    assertEquals("<block label=\"warning\"><para>A</para></block>", block("<div class=\"label-warning\"><p>A</p></div>"));
    assertEquals("<block label=\"note\"><para>A</para></block>", block("<div label=\"note\" class=\"label-note\"><p>A</p></div>"));
    assertEquals("<fragment id=\"1\"><para>A</para><para>B</para></fragment>",
        parse("<section><div><p>A</p><div><p>B</p></div></div><hr/></section>").toString());
  }

  @Test
  void testTable() {
    PSMLElement table = parseBlock("<table><caption>Cap</caption><colgroup><col/><col align=\"right\"/></colgroup>"
        + "<thead><tr><th>H1</th><th>H2</th></tr></thead>"
        + "<tbody><tr><th>A</th><td colspan=\"2\">B</td></tr></tbody>"
        + "<tfoot><tr><td>F</td></tr></tfoot></table>");
    assertEquals("<table><caption>Cap</caption><col/><col align=\"right\"/>"
        + "<row part=\"header\"><cell>H1</cell><cell>H2</cell></row>"
        + "<row><hcell>A</hcell><cell colspan=\"2\">B</cell></row>"
        + "<row part=\"footer\"><cell>F</cell></row></table>", table.toString());
  }

  @Test
  void testTable_HeaderRowWithoutThead() {
    assertEquals("<table><row part=\"header\"><cell>H</cell></row><row><cell>A</cell></row></table>",
        block("<table><tr><th>H</th></tr><tr><td>A</td></tr></table>"));
  }

  @Test
  void testPropertiesFragment() {
    PSMLElement fragment = parse("<section id=\"p\"><dl><dt>Author</dt><dd>Alice</dd><dt data-name=\"tags\">Tags</dt><dd>a</dd><dd>b</dd></dl></section>");
    assertEquals(Name.PROPERTIES_FRAGMENT, fragment.getElement());
    assertEquals("p", fragment.getAttribute("id"));
    List<PSMLElement> properties = fragment.getChildElements(Name.PROPERTY);
    assertEquals(2, properties.size());
    assertEquals("author", properties.get(0).getAttribute("name"));
    assertEquals("Author", properties.get(0).getAttribute("title"));
    assertEquals("Alice", properties.get(0).getAttribute("value"));
    assertEquals("tags", properties.get(1).getAttribute("name"));
    assertEquals("true", properties.get(1).getAttribute("multiple"));
    assertEquals(2, properties.get(1).getChildElements(Name.VALUE).size());
  }

  @Test
  void testDefinitionListInContent() {
    assertEquals("<fragment id=\"1\"><para>Intro</para><para><bold>Term</bold></para><para>Definition</para></fragment>",
        parse("<section><p>Intro</p><dl><dt>Term</dt><dd>Definition</dd></dl></section>").toString());
  }

  @Test
  void testIgnoredContent() {
    assertEquals("<fragment id=\"1\"><para>A B</para></fragment>",
        parse("<div xmlns=\"http://www.w3.org/1999/xhtml\"><p>A <script>alert(1)</script>"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\"><text>X</text></svg>B</p><style>p{}</style></div>").toString());
  }

  @Test
  void testMalformed() {
    assertThrows(IOException.class, () -> new HTMLParser().parse(new StringReader("<p>Unclosed")));
  }

  private static PSMLElement parse(String html) {
    try {
      return new HTMLParser().parse(new StringReader(html));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  /**
   * Parses the HTML block and returns the first PSML element in the resulting fragment.
   */
  private static PSMLElement parseBlock(String html) {
    PSMLElement fragment = parse("<section>" + html + "</section>");
    return fragment.getChildElements().get(0);
  }

  private static String block(String html) {
    return parseBlock(html).toString();
  }

}
