package org.pageseeder.psml.md;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.pageseeder.psml.md.MarkdownOutputOptions.DiffFormat;
import org.pageseeder.psml.model.Loader;
import org.pageseeder.psml.model.PSMLElement;
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
 */
class MarkdownSerializerDiffTest {

  private static final Path EXPECTED = Paths.get("src/test/data/mddiff/expected");

  private static final MarkdownOutputOptions INS_DEL = MarkdownOutputOptions.defaultOptions().diff(DiffFormat.INS_DEL);

  private static final String NS = " xmlns:diff='https://www.pageseeder.org/diffx'"
      + " xmlns:ins='https://www.pageseeder.org/diffx/insert'"
      + " xmlns:del='https://www.pageseeder.org/diffx/delete'";

  private static PSMLElement load(String xml) throws IOException {
    return new Loader().parse(new StringReader(xml));
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
    assertEquals("See <ins>**Doc**</ins>.", toMarkdown(xml));
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

}
