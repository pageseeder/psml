package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.parse;
import static org.pageseeder.psml.toc.Tests.ref;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public final class PublicationTreeTest {

  @Test
  public void testEmpty() {
    DocumentTree tree = new DocumentTree.Builder(1).build();
    PublicationTree publication = new PublicationTree(tree);
    Assert.assertEquals(1, publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
    Tests.assertDocumentTreeEquals(tree, publication.root());
    assertValidPublication(publication);
  }

  @Test
  public void testSimpleReference() throws SAXException {
    DocumentTree tree = new DocumentTree.Builder(1).part(ref(1, "A", 100L)).part(ref(1, "A", 100L)).build();
    PublicationTree publication = new PublicationTree(tree);
    Assert.assertEquals(1, publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
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
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
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
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
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
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    Tests.print(publication);
    Tests.print(tree);
  }

  @Test
  public void testThreeLevelsTopDown() throws SAXException {
    DocumentTree root = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 0)).part(ref(1, "A", 100L)).part(ref(1, "A", 101L)).build();
    DocumentTree inter = new DocumentTree.Builder(100).title("A").part(h1("A", "1", 0)).part(ref(1, "X", 1000L)).part(ref(1, "Y", 1001L)).build();
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y").part(h1("a", "1", 0)).part(h1("b", "1", 1, h2("x", "1", 2))).part(h1("c", "1", 3)).build();
    PublicationTree publication = new PublicationTree(root);
    publication = publication.leaf(inter);
    publication = publication.leaf(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree, publication.leaf());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    Tests.print(publication);
    Tests.print(tree);
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
  }

  @Test
  public void testParseXrefLevel1() throws SAXException, IOException {
    DocumentTree tree = parse(1, "xref-level1.psml").normalize(TitleCollapse.auto);
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
