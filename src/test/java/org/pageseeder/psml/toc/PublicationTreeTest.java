package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;
import static org.pageseeder.psml.toc.Tests.h5;
import static org.pageseeder.psml.toc.Tests.p;
import static org.pageseeder.psml.toc.Tests.parse;
import static org.pageseeder.psml.toc.Tests.phantom;
import static org.pageseeder.psml.toc.Tests.ref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.toc.DocumentTree.Builder;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
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
    DocumentTree root = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 1)).part(ref(1, "A", 100L)).part(ref(1, "A", 101L)).build();
    DocumentTree tree = new DocumentTree.Builder(100).title("T").part(h1("T", "1", 1)).part(ref(1, "X", 102L)).part(ref(1, "Y", 103L)).build();
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
    DocumentTree root = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 1)).part(ref(1, "A", 100L)).part(ref(1, "A", 101L)).build();
    DocumentTree inter = new DocumentTree.Builder(100).title("A").part(h1("A", "1", 1)).part(ref(1, "X", 1000L)).part(ref(1, "Y", 1001L)).build();
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y").part(h1("a", "1", 1)).part(h1("b", "1", 1, h2("x", "1", 2))).part(h1("c", "1", 3)).build();
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
  public void testAutoNumbering() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build();
    //Tests.print(root);
    root = root.normalize(TitleCollapse.auto);
    //Tests.print(root);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build();
    //Tests.print(inter);
    inter = inter.normalize(TitleCollapse.auto);
    //Tests.print(inter);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2"),
              ref(1, "BZ", 1002L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .part(h1("X2", "6", 1,
            ref(1, "Z2", 1003L)))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            phantom(3, h4("d", "4", 2, true, "", h5("xc", "5", 1, false, "x.x.x.x")))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree3 = new DocumentTree.Builder(1002).title("Z")
        .part(h1("Z", "1", 1, true, "x.x"))
        .addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree4 = new DocumentTree.Builder(1003).title("Z2")
        .part(h1("Z2", "1", 1, true, "x.x"))
        .addReverseReference(1000L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    publication = publication.add(tree3);
    publication = publication.add(tree4);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree2, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    Tests.print(publication, 1000, 2, numbering, true);
    Tests.print(publication, 1002, -1, numbering, true);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingUntranscluded() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", "2", 100L),
            ref(3, "B", "2", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", "2", 1000L),
            ref(2, "Y", "2", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(phantom(1,
              ref(0, "BX", "1", 1000L),
              ref(0, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ", "2", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ2", "2", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, Reference.DEFAULT_FRAGMENT),
              h2("c", "2", 1, true, "x.x.x")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", ref(3, "Z", "2", 1002)),
            h2("c", "4", 1, false, ""),
            phantom(3, h4("d", "4", 2, true, "", h5("xc", "5", 1, false, "x.x.x.x")))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree3 = new DocumentTree.Builder(1002).title("Z")
        .part(h1("Z", "1", 1, true, "x.x"))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    publication = publication.add(tree3);
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    Tests.print(publication, 101, 1, numbering, true);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    String tresult = numbering.getAllTranscludedPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("Transcluded:\n" + tresult);
  }

  @Test
  public void testAutoNumberingXRefsRelative() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(0, "A", 100L),
            ref(0, "B", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(0, "X", 1000L),
            ref(0, "Y", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            phantom(3, h4("d", "4", 2, true, "", h5("xc", "5", 1, false, "x.x.x.x")))))
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
    PublicationConfig config = Tests.parseConfig("publication-config-xrefs-relative.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    Tests.print(publication, 1000, 2, numbering, true);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingBlank() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "X", 1000L),
            ref(3, "Y", 1001L),
            ref(3, "Z", "2", 101L, Reference.DEFAULT_TYPE, "2")))).build().normalize(TitleCollapse.auto);
    // not referenced
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build();
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config-blank.xml");
    // Generate fragment numbering
    List<Long> unusedIds = new ArrayList<>();
    FragmentNumbering numbering = new FragmentNumbering(publication, config, unusedIds);
    Tests.print(publication, -1, -1, numbering, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    System.out.println("unusedIds: " + unusedIds);
  }

  @Test
  public void testAutoNumberingBlockLabels() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "X", 1000L),
            ref(3, "Y", 1001L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "",
            p(1, "1a", 1, true, ""),
            p(1, "1b", 1, true, "", "table-caption"),
            p(1, "1c", 1, true, ""),
            p(1, "1d", 1, true, "", "table-caption"),
            p(1, "1e", 1, true, "", "figure-caption"),
            p(1, "1f", 1, true, "", "table-caption")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "",
            p(1, "1a", 1, true, ""),
            p(1, "1b", 1, true, "", "table-caption"),
            p(1, "1c", 1, true, ""),
            p(1, "1d", 1, true, "", "table-caption"),
            p(1, "1e", 1, true, "", "figure-caption"),
            p(1, "1f", 1, true, "", "table-caption")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config-block-labels.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<>());
    Tests.print(publication, -1, -1, numbering, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingSkippedLevels() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("X", "1", 1, true, "",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "",
                phantom(3, h4("x", "3", 1, true, ""))),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "",
                phantom(3, h4("xc", "5", 1, true, "x.x.x.x"))),
            ref(3, "X", 1000L))).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "",
                phantom(3, h4("x", "3", 1, true, ""))),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "",
                phantom(3, h4("xc", "5", 1, true, "x.x.x.x")))))
        .addReverseReference(1L).labels("autonumber1").build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config-skipped-levels.xml");
    // Generate fragment numbering
    List<Long> unusedIds = new ArrayList<>();
    FragmentNumbering numbering = new FragmentNumbering(publication, config, unusedIds);
    Tests.print(publication, -1, -1, numbering, false);
  }

  @Test
  public void testAutoNumberingLabels() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1, true, "",
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).labels("autonumber2,autonumber1").build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).labels("autonumber1").build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            phantom(3, h4("d", "4", 2, true, "", h5("xc", "5", 1, false, "x.x.x.x")))))
        .addReverseReference(100L).addReverseReference(101L).labels("autonumber2").build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree2, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    //assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingParas() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build();
    //Tests.print(root);
    root = root.normalize(TitleCollapse.auto);
    //Tests.print(root);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build();
    //Tests.print(inter);
    inter = inter.normalize(TitleCollapse.auto);
    //Tests.print(inter);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            p(1, "1a", 1, true, ""),
            p(2, "1b", 1, true, ""),
            p(1, "1c", 1, false, "x"),
            h2("a", "2", 1, true, "x.x.x"),
            p(0, "3", 1, false, ""),
            h2("b", "3", 2, true, "",
                p(1, "3a", 1, true, ""),
                p(2, "3a", 2, true, ""),
                p(1, "3b", 1, true, "x.x")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
                p(1, "1a", 1, true, ""),
                p(3, "1b", 1, true, ""),
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "",
                h3("x", "3", 1, true, "")), // will not be numbered when adjusted to level 5
            h2("c", "4", 1, false, "")))
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
    PublicationConfig config = Tests.parseConfig("publication-config-paras.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    tree.print(System.out);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingParasRelative() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build();
    //Tests.print(root);
    root = root.normalize(TitleCollapse.auto);
    //Tests.print(root);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build();
    //Tests.print(inter);
    inter = inter.normalize(TitleCollapse.auto);
    //Tests.print(inter);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1000L),
              ref(1, "BY", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            p(1, "1a", 1, true, ""),
            p(2, "1b", 1, true, ""),
            p(1, "1c", 1, false, "x"),
            h2("a", "2", 1, true, "x.x.x"),
            p(0, "3", 1, false, ""),
            h2("b", "3", 2, true, "",
                p(3, "3a", 1, true, ""),
                p(4, "3a", 2, true, ""),
                p(2, "3b", 1, true, "x.x")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
                p(1, "1a", 1, true, ""),
                p(3, "1b", 1, true, ""),
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "",
                h3("x", "3", 1, true, "")), // will not be numbered when adjusted to level 5
            h2("c", "4", 1, false, "")))
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
    PublicationConfig config = Tests.parseConfig("publication-config-paras-relative.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    tree.print(System.out);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
  }

  @Test
  public void testAutoNumberingPerformance() throws SAXException, IOException {
    Builder builder = new DocumentTree.Builder(1).title("T");
    for(int i = 0; i < 500; i++) {
      builder = builder.part(ref(2, "A", 1000L + i));
    }
    DocumentTree root = builder.build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    for(int i = 0; i < 500; i++) {
      DocumentTree tree = new DocumentTree.Builder(1000 + i).title("X")
          .part(h1("X", "0", 1, true, "",
              h2("a", "1", 1, true, ""),
              h2("b", "2", 1, true, ""),
              h2("c", "3", 1, true, ""),
              h2("d", "4", 1, true, ""),
              h2("e", "5", 1, true, ""),
              h2("f", "6", 1, true, ""),
              h2("g", "7", 1, true, ""),
              h2("h", "8", 1, true, ""),
              h2("i", "9", 1, true, ""),
              p(1, "9a", 1, true, ""),
              p(2, "9b", 1, true, ""),
              p(3, "9c", 1, true, ""),
              p(4, "9d", 1, true, ""),
              p(5, "9e", 1, true, ""),
              p(6, "9f", 1, true, ""),
              p(1, "9g", 1, true, ""),
              p(2, "9h", 1, true, ""),
              p(3, "9i", 1, true, ""),
              p(4, "9j", 1, true, "")))
          .addReverseReference(1L).build().normalize(TitleCollapse.auto);
      publication = publication.add(tree);
    }
    // Generate fragment numbering
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    //Tests.print(publication, -1, number);
    long start = System.currentTimeMillis();
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    long end = System.currentTimeMillis();
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    System.out.println("Number of prefixes: " + prefixes.size());
    System.out.println("Generation time: " + (end - start));
    start = System.currentTimeMillis();
    Tests.print(publication, 1499, -1, numbering, true);
    end = System.currentTimeMillis();
    System.out.println("Print time: " + (end - start));
  }

  @Test(expected = IllegalStateException.class)
  public void testLoopDetection() throws SAXException, ProcessException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L))))
        .addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "B", 101L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1L)))
        .addReverseReference(100L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    Tests.print(publication, -1, -1, null, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testLoopDetectionAutonumber() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L))))
        .addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "B", 101L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(1, "BX", 1L)))
        .addReverseReference(100L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    // Generate fragment numbering
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    new FragmentNumbering(publication, config, new ArrayList<Long>());
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
    System.out.println(tree.listReverseReferences());
    String headings = tree.fragmentheadings().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("HEADINGS\n" + headings);
    String levels = tree.fragmentlevels().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("LEVELS\n" + levels);
    PublicationTree publication = new PublicationTree(tree);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>());
    Tests.print(publication, -1, -1, numbering, true);
    tree.print(System.out);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
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
