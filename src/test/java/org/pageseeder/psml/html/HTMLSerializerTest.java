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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.model.Loader;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;

class HTMLSerializerTest {

  private static final String MD_RESOURCES = "src/test/resources/org/pageseeder/psml/md";

  @Test
  void testDocument() {
    assertEquals("<article><section id=\"content\"><section id=\"1\"><p>A</p></section></section></article>",
        html("<document level=\"portable\"><documentinfo><uri><displaytitle>T</displaytitle></uri></documentinfo>"
            + "<section id=\"content\"><fragment id=\"1\"><para>A</para></fragment></section></document>"));
  }

  @Test
  void testHeadingsAndParas() {
    assertEquals("<h2>Hello</h2>", html("<heading level=\"2\">Hello</heading>"));
    assertEquals("<h6>Hello</h6>", html("<heading level=\"9\">Hello</heading>"));
    assertEquals("<h1><span class=\"prefix\">1.</span> Hello</h1>", html("<heading level=\"1\" prefix=\"1.\">Hello</heading>"));
    assertEquals("<p class=\"indent-1\">A</p>", html("<para indent=\"1\">A</para>"));
  }

  @Test
  void testInline() {
    assertEquals("<p><strong>a</strong><em>b</em><u>c</u><code>d</code><sub>e</sub><sup>f</sup><br/>"
        + "<span class=\"label-x\">g</span><ins>h</ins><del>i</del></p>",
        html("<para><bold>a</bold><italic>b</italic><underline>c</underline><monospace>d</monospace><sub>e</sub>"
            + "<sup>f</sup><br/><inline label=\"x\">g</inline><diff:ins>h</diff:ins><diff:del>i</diff:del></para>"));
  }

  @Test
  void testLinks() {
    assertEquals("<a href=\"https://example.org\">Example</a>", html("<link href=\"https://example.org\">Example</link>"));
    assertEquals("<a href=\"doc.psml#2\">Doc</a>", html("<xref href=\"doc.psml\" frag=\"2\">Doc</xref>"));
    assertEquals("<a href=\"doc.psml\">Title</a>", html("<xref href=\"doc.psml\" frag=\"default\" title=\"Title\"/>"));
    assertEquals("<a id=\"here\"/>", html("<anchor name=\"here\"/>"));
  }

  @Test
  void testBlocks() {
    assertEquals("<blockquote><p>A</p></blockquote>", html("<block label=\"quoted\"><para>A</para></block>"));
    assertEquals("<div class=\"label-warning\"><p>A</p></div>", html("<block label=\"warning\"><para>A</para></block>"));
    assertEquals("<pre>a &lt; b</pre>", html("<preformat>a &lt; b</preformat>"));
    assertEquals("<pre><code class=\"lang-java\">int a;</code></pre>", html("<preformat role=\"lang-java\">int a;</preformat>"));
  }

  @Test
  void testLists() {
    assertEquals("<ul><li>A</li><li>B</li></ul>", html("<list><item>A</item><item>B</item></list>"));
    assertEquals("<ol start=\"2\"><li>A</li></ol>", html("<nlist start=\"2\"><item>A</item></nlist>"));
    assertEquals("<ol type=\"A\"><li>A</li></ol>", html("<nlist type=\"upperalpha\"><item>A</item></nlist>"));
  }

  @Test
  void testTable() {
    assertEquals("<table><caption>Cap</caption><colgroup><col/><col align=\"right\"/></colgroup>"
        + "<thead><tr><th>H</th><th>I</th></tr></thead>"
        + "<tbody><tr><th>A</th><td colspan=\"2\">B</td></tr></tbody></table>",
        html("<table><caption>Cap</caption><col/><col align=\"right\"/>"
            + "<row part=\"header\"><cell>H</cell><cell>I</cell></row>"
            + "<row><hcell>A</hcell><cell colspan=\"2\">B</cell></row></table>"));
  }

  @Test
  void testImageAndPlaceholder() {
    assertEquals("<img src=\"a.png\" alt=\"\"/>", html("<image src=\"a.png\"/>"));
    // Built with the model API: the loader does not currently resolve <placeholder> (Name.PLACEHOLDER is "name")
    PSMLElement placeholder = new PSMLElement(Name.PLACEHOLDER).setAttribute("name", "client");
    assertEquals("<var data-name=\"client\">client</var>", serialize(placeholder));
  }

  @Test
  void testToHTML_Wrapper() {
    HTMLElement html = new HTMLSerializer().toHTML(load("<documentinfo/>"));
    assertEquals(HTMLElement.Name.DIV, html.getElement());
    assertTrue(html.isEmpty());
  }

  @Test
  void testRoundTrip_KitchenSink() throws IOException {
    PSMLElement psml;
    try (Reader r = Files.newBufferedReader(Path.of(MD_RESOURCES, "kitchen_sink.psml"))) {
      psml = new Loader().parse(r);
    }
    String html1 = serialize(psml);
    PSMLElement psml2 = new HTMLParser().parse(new StringReader(html1));
    // Structure is preserved
    assertEquals(Name.DOCUMENT, psml2.getElement());
    assertEquals(psml.getChildElements(Name.SECTION).size(), psml2.getChildElements(Name.SECTION).size());
    for (int i = 0; i < psml.getChildElements(Name.SECTION).size(); i++) {
      PSMLElement s1 = psml.getChildElements(Name.SECTION).get(i);
      PSMLElement s2 = psml2.getChildElements(Name.SECTION).get(i);
      assertEquals(s1.getAttribute("id"), s2.getAttribute("id"));
      assertEquals(s1.getChildElements().size(), s2.getChildElements().size());
      for (int j = 0; j < s1.getChildElements().size(); j++) {
        assertEquals(s1.getChildElements().get(j).getElement(), s2.getChildElements().get(j).getElement());
        assertEquals(s1.getChildElements().get(j).getAttribute("id"), s2.getChildElements().get(j).getAttribute("id"));
      }
    }
    // Once normalized, the HTML is stable
    String html2 = serialize(psml2);
    String html3 = serialize(new HTMLParser().parse(new StringReader(html2)));
    assertEquals(html2, html3);
  }

  private static String serialize(PSMLElement psml) {
    StringWriter out = new StringWriter();
    try {
      new HTMLSerializer().serialize(psml, out);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    return out.toString();
  }

  private static PSMLElement load(String psml) {
    String xml = psml.contains("diff:")
        ? psml.replaceFirst("^<(\\w+)", "<$1 xmlns:diff=\"https://www.pageseeder.org/diffx\"")
        : psml;
    try {
      return new Loader().parse(new StringReader(xml));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private static String html(String psml) {
    return serialize(load(psml));
  }

}
