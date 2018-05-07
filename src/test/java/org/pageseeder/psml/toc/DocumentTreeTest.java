package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;
import static org.pageseeder.psml.toc.Tests.phantom;
import static org.pageseeder.psml.toc.Tests.ref;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public final class DocumentTreeTest {

  @Test
  public void testEmpty() {
    DocumentTree tree = new DocumentTree.Builder(123).build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals(0, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(0, tree.listForwardReferences().size());
    Assert.assertEquals(0, tree.parts().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadID() {
    new DocumentTree.Builder(-1).build();
  }

  // Building
  // --------------------------------------------------------------------------

  @Test
  public void testMinimal() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(0, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(0, tree.listForwardReferences().size());
    Assert.assertEquals(0, tree.parts().size());
  }

  @Test
  public void testHeading() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .part(h1("A", "1", 1))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(1, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(0, tree.listForwardReferences().size());
    Assert.assertEquals(1, tree.parts().size());
  }

  @Test
  public void testHeadings() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .part(h1("A", "1", 1))
        .part(h1("B", "1", 1))
        .part(h1("C", "1", 1))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(1, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(0, tree.listForwardReferences().size());
    Assert.assertEquals(3, tree.parts().size());
  }

  @Test
  public void testReference() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .part(Tests.ref(1, "Test", 123L))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(0, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(1, tree.listForwardReferences().size());
    Assert.assertEquals(1, tree.parts().size());
  }

  @Test
  public void testReferences() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .part(ref(1, "Test #1", 100L))
        .part(ref(1, "Test #2", 101L))
        .part(ref(1, "Test #3", 102L))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(0, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(3, tree.listForwardReferences().size());
    Assert.assertEquals(3, tree.parts().size());
  }

  @Test
  public void testMix() {
    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .part(h1("A", "1", 1))
        .part(ref(1, "Test #1", 100L))
        .part(h1("B", "1", 1))
        .part(ref(1, "Test #2", 101L))
        .part(h1("C", "1", 1))
        .part(ref(1, "Test #3", 102L))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(1, tree.level());
    Assert.assertEquals(0, tree.listReverseReferences().size());
    Assert.assertEquals(3, tree.listForwardReferences().size());
    Assert.assertEquals(6, tree.parts().size());
  }

  @Test
  public void testBasic() {
    Part<Heading> p1 = h1("A", "1", 1, h2("x", "2", 2), h2("y", "2", 2));
    Part<Heading> p2 = h1("B", "3", 2);

    DocumentTree tree = new DocumentTree.Builder(123L)
        .title("Hello")
        .addReverseReference(7L)
        .addReverseReference(8L)
        .part(p1)
        .part(p2)
        .part(ref(1, "Test #3", 102L))
        .build();
    Assert.assertEquals(123, tree.id());
    Assert.assertEquals("Hello", tree.title());
    Assert.assertEquals(1, tree.level());
    Assert.assertEquals(1, tree.listForwardReferences().size());
    Assert.assertEquals(2, tree.listReverseReferences().size());
    Assert.assertEquals("7,8", tree.toReverseReferencesString(","));
    Assert.assertEquals(3, tree.parts().size());
  }

  // Level
  // --------------------------------------------------------------------------

  @Test
  public void testLevel_0() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").build();
    Assert.assertEquals(0, tree.level());
  }

  @Test
  public void testLevel_1() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(h1("A", "1", 1)).build();
    Assert.assertEquals(1, tree.level());
  }

  @Test
  public void testLevel_1R() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(ref(1, "A", 100L)).build();
    Assert.assertEquals(0, tree.level());
  }

  @Test
  public void testLevel_2() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(h2("A", "1", 1)).build();
    Assert.assertEquals(2, tree.level());
  }

  // Normalization
  // --------------------------------------------------------------------------

  @Test
  public void testNormalize_empty() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").build();
    Assert.assertSame(tree, tree.normalize(TitleCollapse.auto));
    Assert.assertSame(tree, tree.normalize(TitleCollapse.never));
    Assert.assertSame(tree, tree.normalize(TitleCollapse.always));
  }

  @Test
  public void testNormalize_Phantom1() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(phantom(1, h2("A", "1", 1))).build();
    DocumentTree exp = new DocumentTree.Builder(123L, "T").part(h2("A", "1", 1)).build();
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.auto));
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.never));
  }

  @Test
  public void testNormalize_Phantom2() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(phantom(1, phantom(2, h3("A", "1", 1)))).build();
    DocumentTree exp = new DocumentTree.Builder(123L, "T").part(h3("A", "1", 1)).build();
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.auto));
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.never));
  }

  @Test
  public void testNormalize_Phantom3() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(123L, "T").part(phantom(1, phantom(2, phantom(3, h4("A", "1", 1))))).build();
    DocumentTree exp = new DocumentTree.Builder(123L, "T").part(h4("A", "1", 1)).build();
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.never));
    Tests.assertDocumentTreeEquals(exp, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testSingleFragmentTree() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(1).title("Y")
      .part(
        h1("Y", "1", 1,
          h2("a", "1", 2,
              h3("x", "1", 3)),
          h2("b", "1", 4,
              phantom(3, "2",
                h4("c", "2", 1))),
          h2("d", "2", 1,
              phantom(3, "2",
                h4("e", "2", 2),
                h4("f", "3", 1)))))
      .build();
    DocumentTree tree2 = new DocumentTree.Builder(1).title("Y")
        .part(
            phantom(2, "1",
                phantom(3, "2",
                  h4("c", "2", 1))))
        .part(
            h2("d", "2", 1,
                phantom(3, "2",
                  h4("e", "2", 2))))
        .build();
    //Tests.print(tree.singleFragmentTree("2"));
    //Tests.print(tree2);
    Tests.assertDocumentTreeEquals(tree2, tree.singleFragmentTree("2"));
  }

  @Test
  public void testSingleFragmentTree2() throws IOException {
    DocumentTree tree = new DocumentTree.Builder(1).title("Y")
      .part(
          h1("a", "1", 2,
              h2("x", "1", 3)))
      .part(
          h1("b", "1", 4,
              phantom(2, "2",
                h3("c", "2", 1))))
      .part(
          h1("d", "2", 1,
              phantom(2, "2",
                h3("e", "2", 2),
                h3("f", "3", 1))))
      .build();
    DocumentTree tree2 = new DocumentTree.Builder(1).title("Y")
        .part(
            phantom(1, "1",
                phantom(2, "2",
                  h3("c", "2", 1))))
        .part(
            h1("d", "2", 1,
                phantom(2, "2",
                  h3("e", "2", 2))))
        .build();
    Tests.assertDocumentTreeEquals(tree2, tree.singleFragmentTree("2"));
  }

  @Test
  public void testNormalize_none() throws IOException {
    Part<Heading> p1 = h1("I", "1", 1,
                         h2("A", "1", 2),
                         h2("B", "1", 3));
    Part<Heading> p2 = h1("II", "1", 4);
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .part(p2)
        .build();
    Assert.assertSame(tree, tree.normalize(TitleCollapse.auto));
  }

  @Test
  public void testNormalize_1() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1);
    Part<Heading> p2 = h1("I", "2", 2, h2("A", "2", 3), h2("B", "2", 2));
    Part<Heading> p3 = h1("II", "2", 5, h2("A", "2", 6), h2("B", "2", 7));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .part(p2)
        .part(p3)
        .build();
    DocumentTree normalized = tree.normalize(TitleCollapse.auto);
    Assert.assertEquals(3, tree.parts().size());
    Assert.assertEquals(3, normalized.parts().size());
  }

  @Test
  public void testNormalize_2() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1,
                         h2("I", "2", 2,
                           h3("A", "2", 3),
                           h3("B", "2", 2)),
                         h2("II", "2", 5,
                           h3("A", "2", 6),
                           h3("B", "2", 7)));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .build();
    DocumentTree normalized = tree.normalize(TitleCollapse.auto);
    Assert.assertEquals(1, tree.parts().size());
    Assert.assertEquals(2, normalized.parts().size());
  }

  @Test
  public void testNormalize_3() throws IOException {
    Part<Phantom> p1 = phantom(1, phantom(2, h3("Hola", "1", 3)));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello!").part(p1).build();
    DocumentTree normalized = tree.normalize(TitleCollapse.auto);
    Assert.assertEquals(1, tree.parts().size());
    Assert.assertEquals(1, tree.parts().get(0).level());
    Assert.assertEquals(Element.NO_TITLE, tree.parts().get(0).title());
    Assert.assertEquals(1, normalized.parts().size());
    Assert.assertEquals(3, normalized.parts().get(0).level());
    Assert.assertEquals("Hola", normalized.parts().get(0).title());
  }

  @Test
  public void testNormalize_4() throws IOException {
    Part<Phantom> p1 = phantom(1, phantom(2, h3("Hello!", "1", 3)));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello!").part(p1).build();
    DocumentTree normalized = tree.normalize(TitleCollapse.auto);
    Assert.assertEquals(1, tree.parts().size());
    Assert.assertEquals(1, tree.parts().get(0).level());
    Assert.assertEquals(Element.NO_TITLE, tree.parts().get(0).title());
    Assert.assertEquals(0, normalized.parts().size());
  }

  // Forward references
  // --------------------------------------------------------------------------

  @Test
  public void testListForwardReferences_zero() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1);
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .build();
    Assert.assertEquals(0, tree.listForwardReferences().size());
  }

  @Test
  public void testListForwardReferences_one() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1);
    Part<Reference> p2 = ref(1, "Test #1", 100L);
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .part(p2)
        .build();
    Assert.assertEquals(Arrays.asList(100L), tree.listForwardReferences());
  }

  @Test
  public void testListForwardReferences_deep() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1, ref(2, "Test #1", 100L));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .build();
    Assert.assertEquals(Arrays.asList(100L), tree.listForwardReferences());
  }

  @Test
  public void testListForwardReferences_deeper() throws IOException {
    Part<Heading> p1 = h1("Hello", "1", 1, phantom(2, phantom(3, ref(4, "Test #1", 100L), ref(4, "Test #2", 101L))));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .build();
    Assert.assertEquals(Arrays.asList(100L, 101L), tree.listForwardReferences());
  }

  @Test
  public void testListForwardReferences_full() throws IOException {
    Part<Heading> p1 = Tests.h1("Hello", "1", 1);
    Part<Heading> p2 = Tests.h1("I", "2", 2,
                 Tests.ref(2, "2", 100),
                 Tests.ref(2, "2", 101));
    Part<Heading> p3 = Tests.h1("II", "2", 3,
                 Tests.h2("A", "2", 6,
                    Tests.ref(3, "2", 102,
                       Tests.ref(4, "2", 103))),
                 Tests.h2("B", "2", 7));
    Part<Reference> r4 = Tests.ref(1, "3", 104,
                 Tests.ref(2, "3", 105),
                    Tests.ref(3, "3", 106,
                 Tests.ref(2, "3", 107)));
    DocumentTree tree = new DocumentTree.Builder(123L, "Hello")
        .part(p1)
        .part(p2)
        .part(p3)
        .part(r4)
        .build();

    List<Long> exp = Arrays.asList(100L, 101L, 102L, 103L, 104L, 105L, 106L, 107L);
    Assert.assertEquals(exp, tree.listForwardReferences());
  }

}
