package org.pageseeder.psml.md;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.pageseeder.psml.md.MarkdownOutputOptions.DesignatorStyle;
import org.pageseeder.psml.md.MarkdownOutputOptions.DiffFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.ImageFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.SuperSubFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.XrefFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.PropertiesFormat;
import org.pageseeder.psml.md.MarkdownOutputOptions.TableFormat;
import org.pageseeder.psml.PSML;
import org.pageseeder.psml.diff.PSMLDiffer;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLText;
import org.pageseeder.psml.util.ListDiagnosticCollector;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the Markdown serializer with {@link DiffFormat#INS_DEL}.
 *
 * <p>Fixtures: {@code src/test/data/mddiff/generated/<name>.psml} (see {@link MarkdownDiffFixtures})
 * with the expected output in {@code src/test/data/mddiff/expected/<name>.md}.
 *
 * <p>Spec acceptance tests ({@code .plans/psml-markdown-diff.md}):
 * <ol>
 *   <li>Unchanged PSML, same output in both modes: {@link #testAcceptance1_ExistingInputsSameInBothModes()},
 *       {@link #testNoDiff_SameInBothModes()}, {@link #testTable_NoDiffSameInBothModes()},
 *       {@link #testCodeAndInline_NoDiffSameInBothModes()}</li>
 *   <li>Default options preserve existing output: {@code MarkdownSerializerTest} (unchanged),
 *       {@link #testIgnore_DiffMarkupNotEmitted()}</li>
 *   <li>Inline insertion, deletion, replacement: fixture {@code para-inline}</li>
 *   <li>Whole paragraph insertion/deletion: fixtures {@code para-insert}, {@code para-delete}</li>
 *   <li>Threshold paragraph replacement: fixture {@code para-replace}</li>
 *   <li>Headings, lists, nested formatting, multi-block fragments: fixtures {@code heading-*}, {@code list-item},
 *       {@link #testNestedFormatting()}, {@link #testMultiBlockFragment()}, {@link #testDeletedAttributes()}</li>
 *   <li>Namespaces and literal markers: {@link #testAlternatePrefixes()}, {@link #testNotDiff_UnqualifiedAndLiteralMarkers()},
 *       {@link #testSentinelsRemoved()}</li>
 *   <li>Attribute-backed properties: fixtures {@code property-value}, {@code property-insert}, {@code property-delete},
 *       {@link #testProperties_ValuePairs()}, {@link #testProperties_ValueAddedOrRemoved()}</li>
 *   <li>Multi-value and markup properties: fixture {@code property-multivalue}, {@link #testProperties_MarkupValue()},
 *       {@link #testProperties_InsertedFragment()}</li>
 *   <li>Tables: fixtures {@code table-*}, {@code testTable_*}</li>
 *   <li>Images and cross-references: fixture {@code image}, {@link #testImage_ExcludedCreatesNoMarkers()}, {@code testXref_*}</li>
 *   <li>Code-like content and sup/sub: fixtures {@code preformat}, {@code monospace}, {@code property-markdown},
 *       {@code supsub}, {@link #testSupSub_Formats()}, {@link #testMediaFragment()}</li>
 *   <li>Roots: {@link #testDiffRoot()}, {@link #testMultiBlockFragment()}, {@link #testDocumentIgnoresCompare()}</li>
 *   <li>Unicode: {@link #testUnicodePreserved()}</li>
 *   <li>Direction through {@code PSMLDiffer}: {@link #testAcceptance15_LiveDiffer()}</li>
 *   <li>Diagnostics: {@code testDiagnostics_*}</li>
 * </ol>
 */
class MarkdownSerializerDiffTest {

  private static final Path EXPECTED = Paths.get("src/test/data/mddiff/expected");

  private static final MarkdownOutputOptions INS_DEL = MarkdownOutputOptions.defaultOptions().diff(DiffFormat.INS_DEL);

  private static final String NS = " xmlns:diff='https://www.pageseeder.org/diffx'"
      + " xmlns:ins='https://www.pageseeder.org/diffx/insert'"
      + " xmlns:del='https://www.pageseeder.org/diffx/delete'";

  private static PSMLElement load(String xml) throws IOException {
    return PSML.load(new StringReader(xml));
  }

  private static String toMarkdown(PSMLElement element, MarkdownOutputOptions options) throws IOException {
    MarkdownSerializer serializer = new MarkdownSerializer();
    serializer.setOptions(options);
    StringBuilder out = new StringBuilder();
    serializer.serialize(element, out);
    return out.toString();
  }

  private static String toMarkdown(String xml) throws IOException {
    return toMarkdown(load(xml), INS_DEL).trim();
  }

  @TestFactory
  Stream<DynamicTest> generatedFixtures() throws IOException {
    List<DynamicTest> tests = new ArrayList<>();
    try (Stream<Path> files = Files.list(EXPECTED)) {
      files.sorted().forEach(expected -> {
        String name = expected.getFileName().toString().replace(".md", "");
        tests.add(DynamicTest.dynamicTest(name, () -> {
          Path source = MarkdownDiffFixtures.GENERATED.resolve(name + ".psml");
          String md = toMarkdown(load(Files.readString(source)), INS_DEL);
          assertEquals(Files.readString(expected).trim(), md.trim());
        }));
      });
    }
    return tests.stream();
  }

  @Test
  void testIgnore_DiffMarkupNotEmitted() throws IOException {
    String xml = "<para" + NS + ">Payment within<diff:del> 30</diff:del><diff:ins> 14</diff:ins> days.</para>";
    String md = toMarkdown(load(xml), MarkdownOutputOptions.defaultOptions());
    assertEquals("Payment within 30 14 days.", md.trim());
  }

  @Test
  void testNoDiff_SameInBothModes() throws IOException {
    String xml = "<fragment id='1'><heading level='2'>Title</heading><para>Some <bold>bold</bold> and <italic>italic</italic>.</para>"
        + "<list><item>One</item><item>Two</item></list><preformat>code</preformat></fragment>";
    assertEquals(toMarkdown(load(xml), MarkdownOutputOptions.defaultOptions()), toMarkdown(load(xml), INS_DEL));
  }

  @Test
  void testAlternatePrefixes() throws IOException {
    String current = "<para" + NS + " diff:insert='true'><diff:ins>New</diff:ins> text</para>";
    String legacy = "<para xmlns:dfx='http://www.topologi.com/2005/Diff-X' dfx:insert='true'><dfx:ins>New</dfx:ins> text</para>";
    String custom = "<para xmlns:x='https://www.pageseeder.org/diffx' x:insert='true'><x:ins>New</x:ins> text</para>";
    assertEquals("<ins>New text</ins>", toMarkdown(current));
    assertEquals("<ins>New text</ins>", toMarkdown(legacy));
    assertEquals("<ins>New text</ins>", toMarkdown(custom));
  }

  @Test
  void testNotDiff_UnqualifiedAndLiteralMarkers() throws IOException {
    String xml = "<para xmlns:x='urn:other'><ins>a</ins> <x:del>b</x:del> &lt;ins&gt;c&lt;/INS&gt; &lt;insert&gt;</para>";
    assertEquals("a b &lt;ins>c&lt;/INS> <insert>", toMarkdown(xml));
    // Legacy output is not escaped
    assertEquals("a b <ins>c</INS> <insert>", toMarkdown(load(xml), MarkdownOutputOptions.defaultOptions()).trim());
  }

  @Test
  void testSentinelsRemoved() throws IOException {
    ListDiagnosticCollector collector = new ListDiagnosticCollector();
    MarkdownSerializer serializer = new MarkdownSerializer();
    serializer.setOptions(INS_DEL);
    StringBuilder out = new StringBuilder();
    serializer.serialize(load("<para>abc</para>"), out, collector);
    assertEquals("abc", out.toString().trim());
    assertEquals(1, collector.getWarnings().size());
  }

  @Test
  void testTextMarkerInsideDeletedElement() throws IOException {
    String xml = "<para" + NS + " diff:delete='true'><diff:del>Old</diff:del> <diff:ins>new</diff:ins></para>";
    assertEquals("<del>Old </del><ins>new</ins>", toMarkdown(xml));
  }

  @Test
  void testNestedFormatting() throws IOException {
    String xml = "<para" + NS + " diff:insert='true'><diff:ins>Some </diff:ins><bold><diff:ins>bold</diff:ins></bold><diff:ins> text</diff:ins></para>";
    assertEquals("<ins>Some __bold__ text</ins>", toMarkdown(xml));
    String inline = "<para" + NS + ">Some <italic><diff:del>old</diff:del><diff:ins>new</diff:ins></italic> text</para>";
    assertEquals("Some *<del>old</del><ins>new</ins>* text", toMarkdown(inline));
  }

  @Test
  void testMultiBlockFragment() throws IOException {
    String xml = "<fragment" + NS + " id='1' diff:insert='true'><heading level='1'><diff:ins>Title</diff:ins></heading>"
        + "<para><diff:ins>Text</diff:ins></para><list><item><diff:ins>Item</diff:ins></item></list></fragment>";
    assertEquals("# <ins>Title</ins>\n\n<ins>Text</ins>\n\n- <ins>Item</ins>", toMarkdown(xml));
  }

  @Test
  void testDeletedAttributes() throws IOException {
    String xml = "<fragment" + NS + " id='1'><heading diff:delete='true' del:level='3' del:prefix='1.2'><diff:del>Old</diff:del></heading>"
        + "<para diff:delete='true' del:prefix='(a)'><diff:del>Gone</diff:del></para></fragment>";
    assertEquals("### 1.2 <del>Old</del>\n\n(a) <del>Gone</del>", toMarkdown(xml));
  }

  @Test
  void testInsertedInlineElement() throws IOException {
    String xml = "<para" + NS + ">See <xref diff:insert='true' href='a.psml' ins:href='true'><diff:ins>Doc</diff:ins></xref>.</para>";
    assertEquals("See **<ins>Doc</ins>**.", toMarkdown(xml));
  }

  @Test
  void testDiffRoot() throws IOException {
    String xml = "<diff><fragment" + NS + " id='1'><para>A<diff:ins> B</diff:ins></para></fragment></diff>";
    assertEquals("A<ins> B</ins>", toMarkdown(xml));
  }

  @Test
  void testDocumentIgnoresCompare() throws IOException {
    String xml = "<document level='portable'><section id='s'><fragment id='1'><para>Current text</para></fragment></section>"
        + "<compare><compareto version='1'/><diff><fragment" + NS + " id='1'><para>Current<diff:ins> text</diff:ins></para></fragment></diff></compare></document>";
    assertEquals("Current text", toMarkdown(xml));
  }

  private static String fixture(String name, MarkdownOutputOptions options) throws IOException {
    Path source = MarkdownDiffFixtures.GENERATED.resolve(name + ".psml");
    return toMarkdown(load(Files.readString(source)), options).trim();
  }

  // Phase 4: properties

  @Test
  void testProperties_ValuePairs() throws IOException {
    MarkdownOutputOptions pairs = INS_DEL.properties(PropertiesFormat.VALUE_PAIRS);
    assertEquals("Effective date: <del>1 July 2026</del><ins>1 August 2026</ins>", fixture("property-value", pairs));
    assertEquals("A: alpha\n<ins>B</ins>: <ins>beta</ins>", fixture("property-insert", pairs));
    assertEquals("A: alpha\n<del>B</del>: <del>beta</del>", fixture("property-delete", pairs));
    assertEquals("Tags: [red, <del>green</del><ins>blue</ins>]", fixture("property-multivalue", pairs));
    assertEquals("Notes: <del>Some *old* text</del><ins>Some *new* text</ins>", fixture("property-markdown", pairs));
  }

  @Test
  void testProperties_ValueAddedOrRemoved() throws IOException {
    String xml = "<properties-fragment" + NS + " id='1'>"
        + "<property name='a' title='A' value='new' ins:value='true'/>"
        + "<property name='b' title='B' del:value='old'/></properties-fragment>";
    assertEquals("A: <ins>new</ins>\nB: <del>old</del>", toMarkdown(load(xml), INS_DEL.properties(PropertiesFormat.VALUE_PAIRS)).trim());
  }

  @Test
  void testProperties_InsertedFragment() throws IOException {
    String xml = "<properties-fragment" + NS + " id='1' diff:insert='true'>"
        + "<property diff:insert='true' name='a' ins:name='true' title='A' ins:title='true' value='x' ins:value='true'/>"
        + "<property name='tags' title='Tags' multiple='true'><value><diff:ins>t</diff:ins></value></property></properties-fragment>";
    assertEquals("| Name | Value |\n|---|---|\n| <ins>A</ins> | <ins>x</ins> |\n| <ins>Tags</ins> | [<ins>t</ins>] |", toMarkdown(xml));
  }

  @Test
  void testProperties_MarkupValue() throws IOException {
    String xml = "<properties-fragment" + NS + " id='1'><property name='a' title='A'>"
        + "<para>Some<diff:del> old</diff:del><diff:ins> new</diff:ins> text</para></property></properties-fragment>";
    // The para line breaks become <br> as in the legacy output
    assertEquals("A: <br>Some<del> old</del><ins> new</ins> text<br>", toMarkdown(load(xml), INS_DEL.properties(PropertiesFormat.VALUE_PAIRS)).trim());
  }

  @Test
  void testProperties_MetadataNotIncluded() throws IOException {
    String xml = "<document" + NS + "><metadata><properties><property name='a' title='A' del:value='1' value='2' ins:value='true'/></properties></metadata>"
        + "<section id='s'><fragment id='1'><para>Text</para></fragment></section></document>";
    assertEquals("Text", toMarkdown(load(xml), INS_DEL.metadata(false)).trim());
    assertTrue(toMarkdown(load(xml), INS_DEL).contains("A: <del>1</del><ins>2</ins>"));
  }

  // Phase 5: tables

  @Test
  void testTable_Formats() throws IOException {
    String row = "| <ins>c</ins> | <ins>d</ins> |";
    assertTrue(fixture("table-row", INS_DEL.table(TableFormat.COMPACT)).endsWith("|---|---|\n| a | b |\n" + row));
    assertTrue(fixture("table-row", INS_DEL.table(TableFormat.NORMALIZED)).endsWith("|---|---|\n| a | b |\n" + row));
    String html = fixture("table-row", INS_DEL.table(TableFormat.HTML));
    assertTrue(html.contains("<tr>\n    <td><ins>c</ins></td>\n    <td><ins>d</ins></td>\n  </tr>"), html);
  }

  @Test
  void testTable_CaptionHtml() throws IOException {
    String md = fixture("table-caption", INS_DEL.table(TableFormat.HTML).designator(DesignatorStyle.DOUBLE_ASTERISK));
    assertTrue(md.startsWith("**Table 1**: <del>Old</del><ins>New</ins> caption\n"), md);
    assertTrue(md.contains("<caption>Table 1: <del>Old</del><ins>New</ins> caption</caption>"), md);
  }

  @Test
  void testTable_HtmlEscaping() throws IOException {
    String xml = "<table" + NS + "><row><cell>a &lt;ins&gt; &amp; <diff:ins>b</diff:ins></cell></row></table>";
    assertTrue(toMarkdown(load(xml), INS_DEL.table(TableFormat.HTML)).contains("<td>a &lt;ins&gt; &amp; <ins>b</ins></td>"));
  }

  @Test
  void testTable_SpansAndPipes() throws IOException {
    String xml = "<table" + NS + "><row><cell colspan='2'><diff:del>a|b</diff:del><diff:ins>c</diff:ins></cell></row>"
        + "<row><cell>x</cell><cell>y</cell></row></table>";
    String md = toMarkdown(load(xml), INS_DEL.table(TableFormat.NORMALIZED)).trim();
    assertEquals("| <del>a\\|b</del><ins>c</ins> | <del>a\\|b</del><ins>c</ins> |\n|---|---|\n| x | y |", md);
  }

  @Test
  void testTable_DeletedRowAndCell() throws IOException {
    String xml = "<table" + NS + "><row part='header'><cell>H1</cell><cell>H2</cell></row>"
        + "<row diff:delete='true' del:part='footer'><cell diff:delete='true'><diff:del>x</diff:del></cell><cell diff:delete='true'><diff:del>y</diff:del></cell></row>"
        + "<row><cell diff:delete='true' del:colspan='2'><diff:del>wide</diff:del></cell></row></table>";
    String md = toMarkdown(load(xml), INS_DEL.table(TableFormat.COMPACT)).trim();
    assertEquals("| H1 | H2 |\n|---|---|\n| *<del>x</del>* | *<del>y</del>* |\n| <del>wide</del> |  |", md);
  }

  @Test
  void testTable_PrettyWidths() throws IOException {
    String xml = "<table" + NS + "><row><cell>Header</cell></row><row><cell><diff:ins>abc</diff:ins></cell></row></table>";
    assertEquals("| Header         |\n|----------------|\n| <ins>abc</ins> |", toMarkdown(xml));
  }

  @Test
  void testTable_NoDiffSameInBothModes() throws IOException {
    String xml = "<table><col align='right'/><col/><row part='header'><cell>H1</cell><cell>H2</cell></row>"
        + "<row><cell colspan='2'>wide</cell></row><row part='footer'><cell>f <bold>1</bold></cell><cell>f2</cell></row></table>";
    for (TableFormat format : TableFormat.values()) {
      MarkdownOutputOptions options = MarkdownOutputOptions.defaultOptions().table(format);
      assertEquals(toMarkdown(load(xml), options), toMarkdown(load(xml), options.diff(DiffFormat.INS_DEL)), format.name());
    }
  }

  // Phase 6: other content

  @Test
  void testXref_Formats() throws IOException {
    String xml = "<para" + NS + ">See <xref href='b.psml' del:href='a.psml' ins:href='true'>Doc<diff:del> A</diff:del><diff:ins> B</diff:ins></xref>.</para>";
    assertEquals("See **Doc<del> A</del><ins> B</ins>**.", toMarkdown(xml));
    assertEquals("See [Doc<del> A</del><ins> B</ins>](b.psml).", toMarkdown(load(xml), INS_DEL.xref(XrefFormat.LOCAL_LINK)).trim());
    assertEquals("See Doc<del> A</del><ins> B</ins>.", toMarkdown(load(xml), INS_DEL.xref(XrefFormat.TEXT)).trim());
  }

  @Test
  void testXref_Deleted() throws IOException {
    String xml = "<para" + NS + ">See <xref diff:delete='true' del:href='a.psml'><diff:del>Doc</diff:del></xref>.</para>";
    assertEquals("See **<del>Doc</del>**.", toMarkdown(xml));
    assertEquals("See [<del>Doc</del>](a.psml).", toMarkdown(load(xml), INS_DEL.xref(XrefFormat.LOCAL_LINK)).trim());
  }

  @Test
  void testXref_TextMarkerWins() throws IOException {
    String xml = "<para" + NS + "><xref diff:delete='true' del:href='a.psml'><diff:ins>content.</diff:ins></xref></para>";
    assertEquals("**<ins>content.</ins>**", toMarkdown(xml));
  }

  @Test
  void testXref_InInsertedPara() throws IOException {
    String xml = "<para" + NS + " diff:insert='true'><diff:ins>See </diff:ins><xref diff:insert='true' href='a.psml' ins:href='true'><diff:ins>Doc</diff:ins></xref><diff:ins>.</diff:ins></para>";
    assertEquals("<ins>See **Doc**.</ins>", toMarkdown(xml));
  }

  @Test
  void testLink() throws IOException {
    String xml = "<para" + NS + "><link href='https://x.test'><diff:del>old</diff:del><diff:ins>new</diff:ins></link></para>";
    assertEquals("[<del>old</del><ins>new</ins>](https://x.test)", toMarkdown(xml));
  }

  @Test
  void testPlaceholder_ModelApi() throws IOException {
    // Built with the model API: the loader does not currently resolve <placeholder> (Name.PLACEHOLDER is "name")
    PSMLElement para = new PSMLElement(Name.PARA).addNodes(
        new PSMLElement(Name.PLACEHOLDER).setAttribute("diff:insert", "true").setAttribute("name", "client").setAttribute("ins:name", "true"),
        new PSMLText(" "),
        new PSMLElement(Name.PLACEHOLDER).setAttribute("name", "p").addNode(new PSMLElement(Name.DIFF_INS).setText("Text")),
        new PSMLText(" "),
        new PSMLElement(Name.PLACEHOLDER).setAttribute("diff:delete", "true").setAttribute("del:name", "gone"));
    assertEquals("[[<ins>client</ins>]] [[<ins>Text</ins>]] [[<del>gone</del>]]", toMarkdown(para, INS_DEL).trim());
    assertEquals("[[client]] [[Text]] [[]]", toMarkdown(para, MarkdownOutputOptions.defaultOptions()).trim());
  }

  @Test
  void testImage_ExcludedCreatesNoMarkers() throws IOException {
    String xml = "<para" + NS + ">See <image diff:insert='true' src='a.png' ins:src='true' alt='A' ins:alt='true'/> here.</para>";
    assertEquals("See <ins>![A](a.png)</ins> here.", toMarkdown(xml));
    assertEquals("See  here.", toMarkdown(load(xml), INS_DEL.image(ImageFormat.NONE)).trim());
    assertEquals("See  here.", toMarkdown(load(xml), INS_DEL.image(ImageFormat.DATA_URI)).trim());
  }

  @Test
  void testPreformat_WhollyInsertedOrDeleted() throws IOException {
    String inserted = "<fragment" + NS + " id='1'><preformat diff:insert='true' role='lang-js'><diff:ins>let x;</diff:ins></preformat></fragment>";
    assertEquals("<ins>\n```js\nlet x;\n```\n</ins>", toMarkdown(inserted));
    String deleted = "<fragment" + NS + " id='1'><preformat diff:delete='true' del:role='lang-js'><diff:del>let x;</diff:del></preformat></fragment>";
    assertEquals("<del>\n```js\nlet x;\n```\n</del>", toMarkdown(deleted));
  }

  @Test
  void testMediaFragment() throws IOException {
    String xml = "<media-fragment" + NS + " id='1' mediatype='text/x-python'>print(<diff:del>1</diff:del><diff:ins>2</diff:ins>)</media-fragment>";
    assertEquals("<del>\n```py\nprint(1)\n```\n</del>\n<ins>\n```py\nprint(2)\n```\n</ins>", toMarkdown(xml));
  }

  @Test
  void testMonospace_Backticks() throws IOException {
    String xml = "<para" + NS + "><monospace>a`<diff:ins>b</diff:ins></monospace> <monospace diff:insert='true'><diff:ins>c</diff:ins></monospace></para>";
    assertEquals("<del>`a\\``</del><ins>`a\\`b`</ins> <ins>`c`</ins>", toMarkdown(xml));
  }

  @Test
  void testSupSub_Formats() throws IOException {
    assertEquals("E = mc<del>²</del><ins>³</ins> and H<del>₂</del><ins>₃</ins>O", fixture("supsub", INS_DEL.superSub(SuperSubFormat.UNICODE)));
    assertEquals("E = mc<sup><del>2</del><ins>3</ins></sup> and H<sub><del>2</del><ins>3</ins></sub>O", fixture("supsub", INS_DEL.superSub(SuperSubFormat.HTML)));
    assertEquals("E = mc^<del>2</del><ins>3</ins>^ and H~<del>2</del><ins>3</ins>~O", fixture("supsub", INS_DEL.superSub(SuperSubFormat.CARET_TILDE)));
    String inserted = "<para" + NS + ">x<sup diff:insert='true'><diff:ins>2</diff:ins></sup></para>";
    assertEquals("x<ins>²</ins>", toMarkdown(load(inserted), INS_DEL.superSub(SuperSubFormat.UNICODE)).trim());
  }

  @Test
  void testUnicodePreserved() throws IOException {
    String xml = "<para" + NS + ">It\u2019s <diff:del>\"old\"</diff:del><diff:ins>\u201Cnew\u201D</diff:ins> caf\u00E9</para>";
    assertEquals("It\u2019s <del>\"old\"</del><ins>\u201Cnew\u201D</ins> caf\u00E9", toMarkdown(xml));
  }

  @Test
  void testCodeAndInline_NoDiffSameInBothModes() throws IOException {
    String xml = "<fragment id='1'><para>Use <monospace>a`b</monospace>, <xref href='a.psml'>A <bold>doc</bold></xref>, "
        + "<link href='https://x.test'>link</link>, <placeholder name='p'/> and x<sup>2</sup>.</para>"
        + "<preformat role='lang-java'>\nint x;\n\n</preformat></fragment>";
    for (SuperSubFormat format : SuperSubFormat.values()) {
      MarkdownOutputOptions options = MarkdownOutputOptions.defaultOptions().superSub(format);
      assertEquals(toMarkdown(load(xml), options), toMarkdown(load(xml), options.diff(DiffFormat.INS_DEL)), format.name());
    }
    String media = "<media-fragment id='1' mediatype='text/x-java'>class A {}</media-fragment>";
    assertEquals(toMarkdown(load(media), MarkdownOutputOptions.defaultOptions()), toMarkdown(load(media), INS_DEL));
  }

  // Phase 7: diagnostics

  private static List<String> warnings(String xml, MarkdownOutputOptions options, StringBuilder out) throws IOException {
    ListDiagnosticCollector collector = new ListDiagnosticCollector();
    MarkdownSerializer serializer = new MarkdownSerializer();
    serializer.setOptions(options);
    serializer.serialize(load(xml), out, collector);
    return collector.getWarnings();
  }

  private static boolean isDiffWarning(String warning) {
    return warning.contains("Diff-X") || warning.contains("inserted and deleted") || warning.contains("diff:");
  }

  @Test
  void testDiagnostics_Conflict() throws IOException {
    StringBuilder out = new StringBuilder();
    List<String> warnings = warnings("<para" + NS + " diff:insert='true' diff:delete='true'><diff:del>a</diff:del><diff:ins>b</diff:ins> c</para>", INS_DEL, out);
    assertEquals("<del>a</del><ins>b</ins> c", out.toString().trim());
    assertEquals(1, warnings.size(), warnings.toString());
    assertTrue(warnings.get(0).contains("<para> is marked as both inserted and deleted"), warnings.get(0));
  }

  @Test
  void testDiagnostics_InvalidValue() throws IOException {
    StringBuilder out = new StringBuilder();
    List<String> warnings = warnings("<para" + NS + " diff:insert='yes'>x</para>", INS_DEL, out);
    assertEquals("x", out.toString().trim());
    assertEquals(1, warnings.size(), warnings.toString());
    assertTrue(warnings.get(0).contains("invalid value for diff:insert ('yes')"), warnings.get(0));
  }

  @Test
  void testDiagnostics_UnknownElementAndAttribute() throws IOException {
    StringBuilder out = new StringBuilder();
    List<String> warnings = warnings("<para" + NS + " diff:other='1'>a<diff:foo>b</diff:foo></para>", INS_DEL, out);
    assertEquals("ab", out.toString().trim());
    assertEquals(2, warnings.size(), warnings.toString());
    // The inherited operation still applies to the content of an unknown element
    StringBuilder inserted = new StringBuilder();
    warnings("<para" + NS + " diff:insert='true'><diff:foo>b</diff:foo></para>", INS_DEL, inserted);
    assertEquals("<ins>b</ins>", inserted.toString().trim());
  }

  @Test
  void testDiagnostics_TableCellReportedOnce() throws IOException {
    String xml = "<table" + NS + "><row><cell diff:insert='true' diff:delete='true'><diff:ins>x</diff:ins></cell></row></table>";
    for (TableFormat format : TableFormat.values()) {
      StringBuilder out = new StringBuilder();
      List<String> warnings = warnings(xml, INS_DEL.table(format), out);
      assertEquals(1, warnings.stream().filter(MarkdownSerializerDiffTest::isDiffWarning).count(), format + ": " + warnings);
    }
  }

  @Test
  void testDiagnostics_IgnoreModeSilent() throws IOException {
    List<String> warnings = warnings("<para" + NS + " diff:insert='true' diff:delete='true' diff:x='1'><diff:foo>a</diff:foo></para>",
        MarkdownOutputOptions.defaultOptions(), new StringBuilder());
    assertTrue(warnings.isEmpty(), warnings.toString());
  }

  @Test
  void testDiagnostics_ValidMarkupSilent() throws IOException {
    MarkdownOutputOptions[] variants = {
        INS_DEL, INS_DEL.properties(PropertiesFormat.VALUE_PAIRS), INS_DEL.table(TableFormat.HTML),
        INS_DEL.table(TableFormat.NORMALIZED), INS_DEL.superSub(SuperSubFormat.UNICODE)
    };
    for (String name : MarkdownDiffFixtures.names()) {
      String xml = Files.readString(MarkdownDiffFixtures.GENERATED.resolve(name + ".psml"));
      for (MarkdownOutputOptions options : variants) {
        List<String> warnings = warnings(xml, options, new StringBuilder());
        assertTrue(warnings.stream().noneMatch(MarkdownSerializerDiffTest::isDiffWarning), name + ": " + warnings);
      }
    }
  }

  // Acceptance tests 1 and 15

  @Test
  void testAcceptance1_ExistingInputsSameInBothModes() throws IOException {
    List<Path> inputs = new ArrayList<>();
    try (Stream<Path> files = Files.list(Paths.get("src/test/resources/org/pageseeder/psml/md"))) {
      files.filter(f -> f.toString().endsWith(".psml")).sorted().forEach(inputs::add);
    }
    try (Stream<Path> files = Files.list(MarkdownDiffFixtures.PAIRS)) {
      files.sorted().forEach(inputs::add);
    }
    List<MarkdownOutputOptions> variants = new ArrayList<>();
    for (TableFormat format : TableFormat.values()) {
      variants.add(MarkdownOutputOptions.defaultOptions().table(format));
    }
    variants.add(MarkdownOutputOptions.defaultOptions().properties(PropertiesFormat.VALUE_PAIRS).superSub(SuperSubFormat.UNICODE));
    assertFalse(inputs.isEmpty());
    for (Path input : inputs) {
      String xml = Files.readString(input);
      for (MarkdownOutputOptions options : variants) {
        assertEquals(toMarkdown(load(xml), options), toMarkdown(load(xml), options.diff(DiffFormat.INS_DEL)), input + " " + options);
      }
    }
  }

  @Test
  void testAcceptance15_LiveDiffer() throws Exception {
    String oldXml = "<fragment id='1'><para>Payment within 30 days.</para><property name='d' title='Due' value='old'/></fragment>";
    String newXml = "<fragment id='1'><para>Payment within 14 days.</para><property name='d' title='Due' value='new'/></fragment>";
    PSMLDiffer differ = new PSMLDiffer(4_000_000);
    // The differ reverses its public arguments internally: pass the current (new) version first, like DiffHandler
    java.io.StringWriter diff = new java.io.StringWriter();
    differ.diff(new StringReader(newXml), new StringReader(oldXml), diff);
    String md = toMarkdown(load(diff.toString()), INS_DEL.properties(PropertiesFormat.VALUE_PAIRS)).trim();
    assertEquals("Payment within<del> 30</del><ins> 14</ins> days.\nDue: <del>old</del><ins>new</ins>", md);
    // Passing the arguments the other way round swaps the operations
    java.io.StringWriter reversed = new java.io.StringWriter();
    differ.diff(new StringReader(oldXml), new StringReader(newXml), reversed);
    String mdReversed = toMarkdown(load(reversed.toString()), INS_DEL.properties(PropertiesFormat.VALUE_PAIRS)).trim();
    assertEquals("Payment within<del> 14</del><ins> 30</ins> days.\nDue: <del>new</del><ins>old</ins>", mdReversed);
  }

}
