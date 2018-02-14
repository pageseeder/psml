package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;
import static org.pageseeder.psml.toc.Tests.h5;
import static org.pageseeder.psml.toc.Tests.parse;
import static org.pageseeder.psml.toc.Tests.phantom;
import static org.pageseeder.psml.toc.Tests.ref;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.NumberingConfig;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.toc.DocumentTree.Builder;
import org.xml.sax.SAXException;

public final class PublicationTreeTest {

  @Test
  public void testEmpty() {
    DocumentTree tree = new DocumentTree.Builder(1).build();
    PublicationTree publication = new PublicationTree(tree);
    Assert.assertEquals(1, publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.tree(1));
    Tests.assertDocumentTreeEquals(tree, publication.root());
    assertValidPublication(publication);
  }

  @Test
  public void testSimpleReference() throws SAXException {
    DocumentTree tree = new DocumentTree.Builder(1).part(ref(1, "A", 100L)).part(ref(1, "A", 100L)).build();
    PublicationTree publication = new PublicationTree(tree);
    Assert.assertEquals(1, publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.tree(1));
    Tests.assertDocumentTreeEquals(tree, publication.root());
    assertValidPublication(publication);
//    Tests.print(publication);
//    Tests.print(tree);
  }

  @Test
  public void testSimpleReferenceWithTitle() throws SAXException {
    DocumentTree tree = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 0)).part(ref(1, "A", 100L)).part(ref(1, "A", 100L)).build();
    tree = tree.normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(tree);
    Assert.assertEquals(tree.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.tree(1));
    Tests.assertDocumentTreeEquals(tree, publication.root());
    assertValidPublication(publication);
//    Tests.print(publication);
//    Tests.print(tree);
  }

  @Test
  public void testTwoLevels() throws SAXException {
    DocumentTree root = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 0)).part(ref(1, "A", 100L)).part(ref(1, "A", 101L)).build();
    DocumentTree tree = new DocumentTree.Builder(100).title("T").part(h1("T", "1", 0)).part(ref(1, "X", 102L)).part(ref(1, "Y", 103L)).build();
    PublicationTree publication = new PublicationTree(tree);
    publication = publication.root(root);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.tree(100));
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
//    Tests.print(publication);
//    Tests.print(tree);
  }


  @Test
  public void testThreeLevels() throws SAXException {
    DocumentTree root = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 0)).part(ref(1, "A", 100L)).part(ref(1, "A", 101L)).build();
    DocumentTree inter = new DocumentTree.Builder(100).title("A").part(h1("A", "1", 0)).part(ref(1, "X", 1000L)).part(ref(1, "Y", 1001L)).build();
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y").part(h1("a", "1", 0)).part(h1("b", "1", 1, h2("x", "1", 2))).part(h1("c", "1", 3)).build();
    PublicationTree publication = new PublicationTree(tree);
    publication = publication.root(inter);
    publication = publication.root(root);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    Tests.print(publication);
    Tests.print(tree);
  }

  @Test
  public void testAutoNumbering() throws SAXException, ProcessException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 0,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build();
    //Tests.print(root);
    root = root.normalize(TitleCollapse.auto);
    //Tests.print(root);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 0, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build();
    //Tests.print(inter);
    inter = inter.normalize(TitleCollapse.auto);
    //Tests.print(inter);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 0, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 0, true, "x.x",
            h2("a", "2", 0, true, "x.x.x"),
            h2("b", "2", 1, true, "", h3("x", "3", 2, true, "")),
            h2("c", "4", 3, false, ""),
            h2("d", "4", 4, true, "", h3("xc", "5", 5, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 0, true, "x.x",
            h2("a", "2", 0, true, "x.x.x"),
            h2("b", "2", 1, true, "", h3("x", "3", 2, true, "")),
            h2("c", "4", 3, false, ""),
            phantom(3, h4("d", "4", 4, true, "", h5("xc", "5", 5, false, "x.x.x.x")))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree2, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    NumberingConfig number = Tests.parseNumbering("numbering-config.xml");
    Tests.print(publication, -1, number);
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, number);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingPerformance() throws SAXException, ProcessException {
    Builder builder = new DocumentTree.Builder(1).title("T");
    for(int i = 0; i < 100000; i++) {
      builder = builder.part(ref(2, "A", 1000L + i));
    }
    DocumentTree root = builder.build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    for(int i = 0; i < 100000; i++) {
      DocumentTree tree = new DocumentTree.Builder(1000 + i).title("X")
          .part(h1("X", "0", 0, true, "",
              h2("a", "1", 1, true, ""),
              h2("b", "2", 2, true, ""),
              h2("c", "3", 3, true, ""),
              h2("d", "4", 4, true, ""),
              h2("e", "5", 5, true, ""),
              h2("f", "6", 6, true, ""),
              h2("g", "7", 7, true, ""),
              h2("h", "8", 8, true, ""),
              h2("h", "9", 9, true, "")))
          .addReverseReference(1L).build().normalize(TitleCollapse.auto);
      publication = publication.add(tree);
    }
    // Generate fragment numbering
    NumberingConfig number = Tests.parseNumbering("numbering-config.xml");
    //Tests.print(publication, -1, number);
    long start = System.currentTimeMillis();
    FragmentNumbering numbering = new FragmentNumbering(publication, number);
    long end = System.currentTimeMillis();
    Map<String,String> prefixes = numbering.getAllPrefixes();
    //String result = prefixes.entrySet()
    //    .stream().sorted(Map.Entry.comparingByKey())
    //    .map(entry -> entry.getKey() + " - " + entry.getValue())
    //    .collect(Collectors.joining("\n"));
    //System.out.println(result);
    System.out.println("Number of prefixes: " + prefixes.size());
    System.out.println("Generation time: " + (end - start));
  }

  @Test
  public void testParseWACCC() throws SAXException {
    DocumentTree tree = Tests.parse(1, "waccc.psml").normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(tree);
    assertValidPublication(publication);
  }

  @Test
  public void testParseHub() throws SAXException {
    DocumentTree tree = Tests.parse(1, "hub.psml").normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(tree);
    assertValidPublication(publication);
    Tests.print(publication);
  }

  @Test
  public void testParseXrefLevel1() throws SAXException, IOException {
    DocumentTree tree = parse(1, "xref-level1.psml");
    Tests.print(tree);
    tree = tree.normalize(TitleCollapse.auto);
    Tests.print(tree);
    PublicationTree publication = new PublicationTree(tree);
    Tests.print(publication);
  }

  private static void assertValidPublication(PublicationTree publication) {
    try {
      Assert.assertThat(Tests.toDOMSource(publication), Tests.validates("publication-tree.xsd"));
    } catch (AssertionError ex) {
      Tests.print(publication);
      throw ex;
    }
  }


}
