package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.*;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DocumentTreeHandlerTest {

  @Test
  public void testParseContent1() throws SAXException, IOException {
    DocumentTree tree = parse(1, "content1.psml");
    Part<?> p = h1("Test", "1", 1,
        h2("A", "2", 1,
            h3("A.1", "2", 2),
            h3("A.2", "2", 3)),
        h2("B", "2", 4,
            h3("B.1", "2", 5,
                h4("B.1.a", "2", 6),
                h4("B.1.b", "2", 7,
                    h5("B.1.b.x", "2", 8),
                    h5("B.1.b.y", "2", 9,
                        h6("B.1.b.y.i", "2", 10)),
                    h5("B.1.b.z", "2", 11)),
                h4("B.1.c", "2", 12,
                    h5("B.1.c.x", "2", 13))),
            h3("B.2", "2", 14),
            h3("B.3", "2", 15)),
        h2("C", "2", 16));
    DocumentTree expected = new DocumentTree(1, "Test", "", Collections.emptyList(), Collections.singletonList(p),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseContent1_normalizedAuto() throws SAXException, IOException {
    DocumentTree tree = parse(1, "content1.psml");
    List<Part<?>> parts = Arrays.asList(
        h2("A", "2", 1,
            h3("A.1", "2", 2),
            h3("A.2", "2", 3)),
        h2("B", "2", 4,
            h3("B.1", "2", 5,
                h4("B.1.a", "2", 6),
                h4("B.1.b", "2", 7,
                    h5("B.1.b.x", "2", 8),
                    h5("B.1.b.y", "2", 9,
                        h6("B.1.b.y.i", "2", 10)),
                    h5("B.1.b.z", "2", 11)),
                h4("B.1.c", "2", 12,
                    h5("B.1.c.x", "2", 13))),
            h3("B.2", "2", 14),
            h3("B.3", "2", 15)),
        h2("C", "2", 16));
    DocumentTree expected = new DocumentTree(1, "Test", "", Collections.emptyList(), parts,
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testParseContent2() throws SAXException, IOException {
    DocumentTree tree = parse(2, "content2.psml");
    List<Part<?>> parts = Arrays.asList(
        h1("Test #2", "1", 1),
        h1("A", "2", 1,
            h2("A.1", "2", 2,
                phantom(3,
                    h4("A.1.a.x", "2", 3)))),
        h1("B", "3", 1,
            phantom(2,
                h3("B.0.a", "3", 2),
                h3("B.0.b", "3", 4)),
            h2("B.1", "3", 6,
                h3("B.1.a", "3", 7)),
            h2("B.2", "4", 1)));
    List<Long> reverse = Arrays.asList(197490L);
    DocumentTree expected = new DocumentTree(2, "test_2.html", "", reverse, parts,
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
    // This tree cannot be normalised
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testParseContent3() throws SAXException, IOException {
    DocumentTree tree = parse(3, "content3.psml");
    Part<Heading> p1 =
      h1("Test 3", "1.1", 1,
        phantom(2,
          phantom(3,
            phantom(4,
              phantom(5,
                h6("_._._._.i", "1.2", 1)),
              h5("_._._.x", "1.2", 2)),
            h4("_._.a", "1.2", 3)),
          h3("_.0", "1.2", 4)),
        h2("A", "1.2", 5));
    List<Long> reverse = Arrays.asList(197490L);
    DocumentTree expected = new DocumentTree(4, "Test 3", "", reverse, Collections.singletonList(p1),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseContent3_normalizedAuto() throws SAXException, IOException {
    DocumentTree tree = parse(3, "content3.psml").normalize(TitleCollapse.auto);
    Part<Phantom> p1 =
        phantom(2,
          phantom(3,
            phantom(4,
              phantom(5,
                h6("_._._._.i", "1.2", 1)),
              h5("_._._.x", "1.2", 2)),
            h4("_._.a", "1.2", 3)),
          h3("_.0", "1.2", 4));
    Part<Heading> p2 = h2("A", "1.2", 5);
    List<Long> reverse = Arrays.asList(197490L);
    DocumentTree expected = new DocumentTree(4, "Test 3", "", reverse, Arrays.asList(p1, p2),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseContent4() throws SAXException, IOException {
    DocumentTree tree = parse(4, "content4.psml");
    Part<Phantom> p1 =
      phantom(1,
        phantom(2,
          h3("Test 4", "1", 1,
            h4("Another heading", "2", 1))));
    DocumentTree expected = new DocumentTree(4, "Test 4", "", Collections.emptyList(), Collections.singletonList(p1),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseContent4_normalizedAuto() throws SAXException, IOException {
    DocumentTree tree = parse(4, "content4.psml").normalize(TitleCollapse.auto);
    Part<Heading> p1 = h4("Another heading", "2", 1);
    DocumentTree expected = new DocumentTree(4, "Test 4", "", Collections.emptyList(), Collections.singletonList(p1),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseReferences1() throws SAXException, IOException {
    DocumentTree tree = parse(1, "references1.psml");
    Part<Heading> p = h1("A", "1", 1,
        ref(0, "B", 101),
        ref(0, "C", 102));
    List<Long> reverse = Arrays.asList(1L, 2L);
    DocumentTree expected = new DocumentTree(4, "A", "", reverse, Collections.singletonList(p),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseReferences1_normalizeAuto() throws SAXException, IOException {
    DocumentTree tree = parse(1, "references1.psml");
    List<Part<?>> parts = Arrays.asList(ref(0, "B", 101), ref(0, "C", 102));
    List<Long> reverse = Arrays.asList(1L, 2L);
    DocumentTree expected = new DocumentTree(4, 2, "A", "", reverse,
        DocumentTree.NO_FRAGMENT, false, DocumentTree.NO_PREFIX,
        parts, Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testParseReferences2() throws SAXException, IOException {
    DocumentTree tree = parse(1, "references2.psml");
    Part<Heading> p = h1("Test References #2", "1", 1,
        ref(0, "Alpha", 101),
        ref(0, "Bravo", 102,
            ref(1, "Bravo 1", 103),
            ref(1, "Bravo 2", 104)),
        ref(0, "Charlie", 105,
            ref(1, "Charlie 1", 106,
                ref(2, "Charlie 1A", 107),
                ref(2, "Charlie 1B", 108))));
    List<Long> reverse = Arrays.asList(1L, 2L);
    DocumentTree expected = new DocumentTree(4, "A", "", reverse, Collections.singletonList(p),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testParseMedia() throws SAXException, IOException {
    DocumentTree tree = parse(1, "media.psml");
    DocumentTree expected = new DocumentTree(5, "Clipboard01.jpg", "", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }


  @Test
  public void testParseTransclusions() throws SAXException, IOException {
    DocumentTree tree = parse(1, "transclusions.psml");
    List<Part<?>> p = Arrays.asList(
        h1("Assembly", "1", 1,
            ref(1, "Part A", "2", 186250L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
            h2("Part A", "2", 1,
                tend("2"),
                ref(2, "Sub-part 1", "2", 186251L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
                h3("Sub-part 1", "2", 3,
                    tend("2"),
                    ref(3, "Division a", "2", 186252L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
                    h4("Division a", "2", 5, tend("2"))))));
    tree.print(System.out);
    DocumentTree expected = new DocumentTree(5, "Assembly", "", Collections.emptyList(), p,
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

  @Test
  public void testParseTransclusions_normalizedAuto() throws SAXException, IOException {
    DocumentTree tree = parse(6, "transclusions.psml");
    List<Part<?>> p = Arrays.asList(
        ref(1, "Part A", "2", 186250L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
        h2("Part A", "2", 1,
            tend("2"),
            ref(2, "Sub-part 1", "2", 186251L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
            h3("Sub-part 1", "2", 3,
                tend("2"),
                ref(3, "Division a", "2", 186252L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "default"),
                h4("Division a", "2", 5, tend("2")))));
    Tests.print(tree);
    tree.print(System.out);
    DocumentTree expected = new DocumentTree(6, 2, "Assembly", "", Collections.emptyList(),
        DocumentTree.NO_FRAGMENT, false, DocumentTree.NO_PREFIX,
        p, Collections.emptyMap(), Collections.emptyMap());
    Tests.print(tree.normalize(TitleCollapse.auto));
    Tests.assertDocumentTreeEquals(expected, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testParseWACCC() throws SAXException, IOException {
    DocumentTree tree = parse(1, "waccc.psml");
    tree.normalize(TitleCollapse.auto);
    // TODO Write test
  }

  @Test
  public void testParseHub() throws SAXException, IOException {
    DocumentTree tree = parse(1, "hub.psml").normalize(TitleCollapse.auto);
    tree.normalize(TitleCollapse.auto);
    // TODO Write test
  }

  @Test
  public void testParseXrefLevel1() throws SAXException, IOException {
    DocumentTree tree = parse(1, "xref-level1.psml");
    Tests.print(tree);
    Part<Heading> p = h1("Test doc 1", "1", 1,
      phantom(2,
        phantom(3,
          ref(2, "Test doc 1", "content", 199329, Reference.DEFAULT_TYPE, "2")),
        ref(1, "Another heading 2", 199328),
        ref(1, "Test doc 1.pdf", "content", 6173, null, "default",
          ref(2, "Another heading 2a", 199326))),
      h2("Related", "default", 1,
          p(1, "A numbered para", "4", 2, true, "", ""),
          p(1, "A long long long long long long long lon...", "4", 3, true, "", ""),
          p(1, "A long long long long long long long long long long long block labeled numbered para", "4", 4, true, "", "block1")));
    List<Long> reverse = new ArrayList<>();
    DocumentTree expected = new DocumentTree(4, "Test doc 1", "", reverse, Collections.singletonList(p),
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
    tree = tree.normalize(TitleCollapse.auto);
    Tests.print(tree);
    List<Part<?>> parts = Arrays.asList(
        phantom(2,
          phantom(3,
            ref(2, "Test doc 1", "content", 199329, Reference.DEFAULT_TYPE, "2")),
          ref(1, "Another heading 2", 199328),
          ref(1, "Test doc 1.pdf", "content", 6173, null, "default",
            ref(2, "Another heading 2a", 199326))),
        h2("Related", "default", 1,
            p(1, "A numbered para", "4", 2, true, "", ""),
            p(1, "A long long long long long long long lon...", "4", 3, true, "", ""),
            p(1, "A long long long long long long long long long long long block labeled numbered para", "4", 4, true, "", "block1")));
    expected = new DocumentTree(4, "Test doc 1", "", reverse, parts,
        Collections.emptyMap(), Collections.emptyMap());
    Tests.assertDocumentTreeEquals(expected, tree);
  }

}
