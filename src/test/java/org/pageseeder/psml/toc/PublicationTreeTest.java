package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.*;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.toc.DocumentTree.Builder;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    DocumentTree tree = new DocumentTree.Builder(1).title("T").part(h1("T", "1", 1)).part(ref(1, "A", 100L)).part(ref(1, "A", 100L)).build();
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
  public void testAutoNumbering() throws SAXException, IOException, XRefLoopException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(1, "A", 100L),
            ref(1, "B", 101L)))).build();
    //Tests.print(root);
    root = root.normalize(TitleCollapse.auto);
    //Tests.print(root);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(0, "X", 1000L),
            ref(0, "Y", 1001L)))
        .addReverseReference(1L).build();
    //Tests.print(inter);
    inter = inter.normalize(TitleCollapse.auto);
    //Tests.print(inter);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("BA", "1", 1, true, "",
              ref(0, "BX", 1000L),
              ref(0, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ", 1002L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", h3("xc", "5", 1, false, "x.x.x.x"))))
        .part(h1("X2", "6", 1,
            ref(0, "Z2", 1003L)))
        .addReverseReference(100L).addReverseReference(101L).build();
    //Tests.print(tree);
    tree = tree.normalize(TitleCollapse.auto);
    //Tests.print(tree);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x"),
            h2("b", "2", 2, true, "", h3("x", "3", 1, true, "")),
            h2("c", "4", 1, false, ""),
            phantom(3, h4("d", "4", 2, true, "", h5("xc", "5", 1, false, "x.x.x.x")))))
        .addReverseReference(100L).addReverseReference(101L).build();
    //Tests.print(tree2);
    tree2 = tree2.normalize(TitleCollapse.auto);
    //Tests.print(tree2);
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    Tests.print(publication, 1000, 2, numbering, null, true);
    Tests.print(publication, 1002, -1, numbering, null, true);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"100-1-1-1",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"100-1-default",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.1.",4,"0.1.0.1.");
    assertHasPrefix(prefixes,"1000-1-2-1","1.1.","(a)",5,"0.1.0.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-2","1.1.","(b)",5,"0.1.0.1.2.");
    assertHasPrefix(prefixes,"1000-1-3-1","1.1.(b)","(i)",6,"0.1.0.1.2.1.");
    assertHasPrefix(prefixes,"1000-1-4-2","1.1.","(c)",5,"0.1.0.1.3.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"",4,null);
    assertHasPrefix(prefixes,"1000-2-1-1","1.2.","(a)",5,"0.1.2.0.1.");
    assertHasPrefix(prefixes,"1000-2-2-1","1.2.(a)","(i)",6,"0.1.2.0.1.1.");
    assertHasPrefix(prefixes,"1000-2-2-2","1.2.(a)","(ii)",6,"0.1.2.0.1.2.");
    assertHasPrefix(prefixes,"1000-2-3-1","1.2.(a)(ii)","(A)",7,"0.1.2.0.1.2.1.");
    assertHasPrefix(prefixes,"1000-2-4-2","1.2.(a)","(iii)",6,"0.1.2.0.1.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",7,null);
    assertHasPrefix(prefixes,"1000-2-default",null,"",5,null);
    assertHasPrefix(prefixes,"1001-1-1-1",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"1.1.1.",4,"0.1.1.1.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"1.1.2.",4,"0.1.1.2.");
    assertHasPrefix(prefixes,"1001-1-3-1","1.1.2.","(a)",5,"0.1.1.2.1.");
    assertHasPrefix(prefixes,"1001-1-4-2","1.1.2.","(b)",5,"0.1.1.2.2.");
    assertHasPrefix(prefixes,"1001-1-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1001-1-default",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1001-2-2-1","1.2.","(b)",5,"0.1.2.0.2.");
    assertHasPrefix(prefixes,"1001-2-2-2","1.2.","(c)",5,"0.1.2.0.3.");
    assertHasPrefix(prefixes,"1001-2-default",null,"",4,null);
    assertHasPrefix(prefixes,"1002-1-1-1",null,"1.2.1.",4,"0.1.2.1.");
    assertHasPrefix(prefixes,"1002-1-default",null,"1.2.1.",4,"0.1.2.1.");
    assertHasPrefix(prefixes,"1003-1-1-1","1.1.","(d)",5,"0.1.0.1.4.");
    assertHasPrefix(prefixes,"1003-1-default","1.1.","(d)",5,"0.1.0.1.4.");
    assertHasPrefix(prefixes,"1003-2-1-1","1.2.(a)","(iv)",6,"0.1.2.0.1.4.");
    assertHasPrefix(prefixes,"1003-2-default","1.2.(a)","(iv)",6,"0.1.2.0.1.4.");
    assertHasPrefix(prefixes,"101-1-1-1",null,"1.2.",3,"0.1.2.");
    assertHasPrefix(prefixes,"101-1-default",null,"",3,null);
    Assert.assertEquals(35, prefixes.size());
  }

  @Test
  public void testAutoNumberingTranscluded() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(1, "A", "2", 100L),
            ref(1, "B", "2", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(0, "X", "2", 1000L),
            ref(0, "Y", "2", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(h1("B2", "1", 1,
              ref(0, "BX", "1", 1000L),
              ref(0, "BY", "2", 1001L, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ", "2", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "2"),
              h2("a", "2", 1, true, "x.x.x"),
              h2("b", "2", 2, true, "", ref(0, "Z", "2", 1002), tend("2")),
              ref(0, "BZ2", "2", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, Reference.DEFAULT_FRAGMENT)))
        .part(h1("Y", "2", "1", 3, true, "x.x",
              h2("a", "2", "2", 4, true, "x.x.x"),
              h2("b", "2", "2", 5, true, "", ref(3, "Z", "2", 1002)),
              h2("c", "2", "4", 6, false, ""),
                phantom(3, h4("d", "2", "4", 7, true, "", h5("xc", "2", "5", 8, false, "x.x.x.x", tend("2")))),
              h2("c2", "2", 9, true, "x.x.x")))
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
            h2("b", "2", 2, true, "", ref(0, "Z", "2", 1002)),
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
    Map<Long,List<Long>> transclusions = new HashMap<>();
    FragmentNumbering numbering = new FragmentNumbering(publication, config, new ArrayList<Long>(), transclusions);
    Tests.print(publication, -1, -1, numbering, null, true);
    Tests.print(publication, 101, 1, numbering, null, true);
    String result = numbering.getAllPrefixes().entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("Prefixes:\n" + result);
    Map<String,Prefix> prefixes = numbering.getAllTranscludedPrefixes();
    String tpresult = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("Transcluded prefixes:\n" + tpresult);
    assertHasPrefix(prefixes,"100-1-1-1",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-1",null,"1.1.1.",4,"0.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-2",null,"1.1.2.",4,"0.1.1.2.");
    assertHasPrefix(prefixes,"1000-1-3-1","1.1.2.","(a)",5,"0.1.1.2.1.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"1.1.3.",4,"0.1.1.3.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",5,null);
    assertHasPrefix(prefixes,"1000-2-1-1",null,"1.2.3.",4,"0.1.2.3.");
    assertHasPrefix(prefixes,"1000-2-2-1","1.2.3.","(a)",5,"0.1.2.3.1.");
    assertHasPrefix(prefixes,"1000-2-2-2","1.2.3.","(b)",5,"0.1.2.3.2.");
    assertHasPrefix(prefixes,"1000-2-3-1","1.2.3.(b)","(i)",6,"0.1.2.3.2.1.");
    assertHasPrefix(prefixes,"1000-2-4-2","1.2.3.","(c)",5,"0.1.2.3.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1001-1-1-1",null,"1.2.",3,"0.1.2.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"1.2.1.",4,"0.1.2.1.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"1.2.2.",4,"0.1.2.2.");
    assertHasPrefix(prefixes,"1001-1-4-2","1.2.2.","(b)",5,"0.1.2.2.2.");
    assertHasPrefix(prefixes,"1001-1-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1001-2-2-1","1.2.3.","(d)",5,"0.1.2.3.4.");
    assertHasPrefix(prefixes,"1001-2-2-2","1.2.3.","(e)",5,"0.1.2.3.5.");
    assertHasPrefix(prefixes,"1002-1-1-1","1.2.2.","(a)",5,"0.1.2.2.1.");
    assertHasPrefix(prefixes,"1002-2-1-1","1.2.3.(e)","(i)",6,"0.1.2.3.5.1.");
    assertHasPrefix(prefixes,"1002-3-1-1","1.2.5.","(a)",5,"0.1.2.5.1.");
    assertHasPrefix(prefixes,"1002-4-1-1","1.3.2.","(a)",5,"0.1.3.2.1.");
    assertHasPrefix(prefixes,"101-1-2-1",null,"1.2.4.",4,"0.1.2.4.");
    assertHasPrefix(prefixes,"101-1-2-2",null,"1.2.5.",4,"0.1.2.5.");
    assertHasPrefix(prefixes,"101-1-2-3",null,"1.3.",3,"0.1.3.");
    assertHasPrefix(prefixes,"101-1-2-4",null,"1.3.1.",4,"0.1.3.1.");
    assertHasPrefix(prefixes,"101-1-2-5",null,"1.3.2.",4,"0.1.3.2.");
    assertHasPrefix(prefixes,"101-1-2-7","1.3.2.","(b)",5,"0.1.3.2.2.");
    assertHasPrefix(prefixes,"101-1-2-8",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"101-1-2-9",null,"1.3.3.",4,"0.1.3.3.");
    Assert.assertEquals(32, prefixes.size());
    String tresult = transclusions.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("Transclusions:\n" + tresult);
    List<Long> t = transclusions.get(1001L);
    Assert.assertNotNull(t);
    Assert.assertEquals(2, t.size());
    Assert.assertEquals(-1, t.get(0).longValue());
    Assert.assertEquals(101, t.get(1).longValue());
    Assert.assertEquals(1, transclusions.size());
  }

  @Test
  public void testAutoNumberingXRefsRelative() throws SAXException, IOException, XRefLoopException {
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    Tests.print(publication, 1000, 2, numbering, null, true);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"100-1-1-1",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"100-1-default",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"2.",1,"2.");
    assertHasPrefix(prefixes,"1000-1-2-1",null,"2.1.",2,"2.1.");
    assertHasPrefix(prefixes,"1000-1-2-2",null,"2.2.",2,"2.2.");
    assertHasPrefix(prefixes,"1000-1-3-1",null,"2.2.1.",3,"2.2.1.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"2.3.",2,"2.3.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",3,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"2.",1,"2.");
    assertHasPrefix(prefixes,"1000-2-1-1",null,"3.4.",2,"3.4.");
    assertHasPrefix(prefixes,"1000-2-2-1",null,"3.4.1.",3,"3.4.1.");
    assertHasPrefix(prefixes,"1000-2-2-2",null,"3.4.2.",3,"3.4.2.");
    assertHasPrefix(prefixes,"1000-2-3-1",null,"3.4.2.1.",4,"3.4.2.1.");
    assertHasPrefix(prefixes,"1000-2-4-2",null,"3.4.3.",3,"3.4.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",4,null);
    assertHasPrefix(prefixes,"1000-2-default",null,"3.4.",2,"3.4.");
    assertHasPrefix(prefixes,"1001-1-1-1",null,"3.",1,"3.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"3.1.",2,"3.1.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"3.2.",2,"3.2.");
    assertHasPrefix(prefixes,"1001-1-3-1",null,"3.2.1.",3,"3.2.1.");
    assertHasPrefix(prefixes,"1001-1-4-2",null,"3.2.2.",3,"3.2.2.");
    assertHasPrefix(prefixes,"1001-1-5-1",null,"x.x.x.x",4,null);
    assertHasPrefix(prefixes,"1001-1-default",null,"3.",1,"3.");
    assertHasPrefix(prefixes,"1001-2-2-1",null,"3.4.4.",3,"3.4.4.");
    assertHasPrefix(prefixes,"1001-2-2-2",null,"3.4.5.",3,"3.4.5.");
    assertHasPrefix(prefixes,"1001-2-default",null,"",2,null);
    assertHasPrefix(prefixes,"101-1-1-1",null,"3.3.",2,"3.3.");
    assertHasPrefix(prefixes,"101-1-default",null,"",2,null);
    Assert.assertEquals(29, prefixes.size());
  }

  @Test
  public void testAutoNumberingBlank() throws SAXException, IOException, XRefLoopException {
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config, unusedIds, new HashMap<Long,List<Long>>());
    Tests.print(publication, -1, -1, numbering, null, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"1000-1-2-1",null,"x.x.x",3,null);
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",4,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"",2,null);
    Assert.assertEquals(4, prefixes.size());
    System.out.println("unusedIds: " + unusedIds);
    Assert.assertEquals(1, unusedIds.size());
    Assert.assertEquals(100, unusedIds.get(0).longValue());
  }

  @Test
  public void testAutoNumberingBlockLabels() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
              ref(0, "X", 1000L),
              ref(0, "Y", 1001L))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "",
            p(2, "1a", 1, true, ""),
            p(2, "1b", 1, true, "", "table-caption"),
            p(2, "1c", 1, true, ""),
            p(2, "1d", 1, true, "", "table-caption"),
            p(2, "1e", 1, true, "", "figure-caption"),
            p(2, "1f", 1, true, "", "table-caption"),
            p(1, "1g", 1, true, "", "table-caption"),
            p(3, "1h", 1, true, "", "table-caption")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "",
            p(2, "1a", 1, true, ""),
            p(2, "1b", 1, true, "", "table-caption"),
            p(2, "1c", 1, true, ""),
            p(2, "1d", 1, true, "", "table-caption"),
            p(2, "1e", 1, true, "", "figure-caption"),
            p(2, "1f", 1, true, "", "table-caption")))
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, config, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"1000-1-1a-1",null,"1.1.1.",3,"1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1b-1",null,"Table 1-1",2,"1.1.");
    assertHasPrefix(prefixes,"1000-1-1c-1",null,"1.1.2.",3,"1.1.2.");
    assertHasPrefix(prefixes,"1000-1-1d-1",null,"Table 1-2",2,"1.2.");
    assertHasPrefix(prefixes,"1000-1-1e-1",null,"Fig 1-A",2,"1.1.");
    assertHasPrefix(prefixes,"1000-1-1f-1",null,"Table 1-3",2,"1.3.");
    assertHasPrefix(prefixes,"1000-1-1g-1",null,"Table 1-4",2,"1.4.");
    assertHasPrefix(prefixes,"1000-1-1h-1",null,"Table 1-5",2,"1.5.");
    assertHasPrefix(prefixes,"1000-1-default",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"1001-1-1-1",null,"2.",1,"2.");
    assertHasPrefix(prefixes,"1001-1-1a-1",null,"2.1.1.",3,"2.1.1.");
    assertHasPrefix(prefixes,"1001-1-1b-1",null,"Table 2-1",2,"2.1.");
    assertHasPrefix(prefixes,"1001-1-1c-1",null,"2.1.2.",3,"2.1.2.");
    assertHasPrefix(prefixes,"1001-1-1d-1",null,"Table 2-2",2,"2.2.");
    assertHasPrefix(prefixes,"1001-1-1e-1",null,"Fig 2-A",2,"2.1.");
    assertHasPrefix(prefixes,"1001-1-1f-1",null,"Table 2-3",2,"2.3.");
    assertHasPrefix(prefixes,"1001-1-default",null,"2.",1,"2.");
    Assert.assertEquals(19, prefixes.size());
  }

  @Test
  public void testAutoNumberingBlankFormat() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
              ref(0, "X", 1000L),
              ref(0, "Y", 1001L))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "",
              h2("A", "1", 2, true, "",
                h3("B", "1", 3, true, "",
                  p(1, "1a", 1, true, ""),
                  p(2, "1b", 1, true, "")))))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree tree = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "",
              h2("A", "1", 2, true, "",
                h3("B", "1", 3, true, "",
                  p(1, "1a", 1, true, ""),
                  p(2, "1b", 1, true, "")))))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config-blank-format.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"1000-1-1-1","","1.",2,"1.1.");
    assertHasPrefix(prefixes,"1000-1-1-2","","1.1.",3,"1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1a-1","1.1.","(a)",4,"1.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1b-1","1.1.","(a)(i)",5,"1.1.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-default","","",1,"1.");
    assertHasPrefix(prefixes,"1001-1-1-1","","1.",2,"2.1.");
    assertHasPrefix(prefixes,"1001-1-1-2","","1.1.",3,"2.1.1.");
    assertHasPrefix(prefixes,"1001-1-1a-1","1.1.","(a)",4,"2.1.1.1.");
    assertHasPrefix(prefixes,"1001-1-1b-1","1.1.","(a)(i)",5,"2.1.1.1.1.");
    assertHasPrefix(prefixes,"1001-1-default","","",1,"2.");
    Assert.assertEquals(11, prefixes.size());
  }

  @Test
  public void testAutoNumberingSkippedLevels() throws SAXException, IOException, XRefLoopException {
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config, unusedIds, new HashMap<Long,List<Long>>());
    Tests.print(publication, -1, -1, numbering, null, false);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-1-1",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"1-1-2-1",null,"1.1.",2,"1.1.");
    assertHasPrefix(prefixes,"1-1-2-2",null,"1.2.",2,"1.2.");
    assertHasPrefix(prefixes,"1-1-3-1",null,"1.2.1.1.",4,"1.2.1.1.");
    assertHasPrefix(prefixes,"1-1-4-2",null,"1.3.",2,"1.3.");
    assertHasPrefix(prefixes,"1-1-5-1",null,"1.3.1.1.",4,"1.3.1.1.");
    assertHasPrefix(prefixes,"1-1-default",null,"",1,null);
    assertHasPrefix(prefixes,"1000-1-1-1",null,"0.1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-2-1",null,"0.1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-2",null,"0.1.2.",3,"0.1.2.");
    assertHasPrefix(prefixes,"1000-1-3-1","0.1.2.0.","(a)",5,"0.1.2.0.1.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"0.1.3.",3,"0.1.3.");
    assertHasPrefix(prefixes,"1000-1-5-1","0.1.3.0.","(a)",5,"0.1.3.0.1.");
    assertHasPrefix(prefixes,"1000-1-default",null,"0.1.",2,"0.1.");
    Assert.assertEquals(14, prefixes.size());
  }

  @Test
  public void testAutoNumberingLabels() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1, true, "",
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L),
            ref(2, "Z", 1002L)))
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
    DocumentTree tree3 = new DocumentTree.Builder(1002).title("Z")
        .part(h1("Z", "1", 1, true, "x.x",
            h2("a", "2", 1, true, "x.x.x")))
        .addReverseReference(100L).addReverseReference(101L).labels("autonumber1").build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    publication = publication.add(tree3);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree2, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    assertValidPublication(publication, numbering, config);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"100-1-1-1",null,"a.",2,"0.1.");
    assertHasPrefix(prefixes,"100-1-default",null,"a.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"a.a.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-1",null,"a.a.a.",4,"0.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-2-2",null,"a.a.b.",4,"0.1.1.2.");
    assertHasPrefix(prefixes,"1000-1-3-1","a.a.b.","(a)",5,"0.1.1.2.1.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"a.a.c.",4,"0.1.1.3.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",5,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"a.a.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-2-1-1",null,"a.",4,"0.0.0.1.");
    assertHasPrefix(prefixes,"1000-2-2-1","a.","(a)",5,"0.0.0.1.1.");
    assertHasPrefix(prefixes,"1000-2-2-2","a.","(b)",5,"0.0.0.1.2.");
    assertHasPrefix(prefixes,"1000-2-3-1","a.(b)","(i)",6,"0.0.0.1.2.1.");
    assertHasPrefix(prefixes,"1000-2-4-2","a.","(c)",5,"0.0.0.1.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1000-2-default",null,"a.",4,"0.0.0.1.");
    assertHasPrefix(prefixes,"1001-1-1-1",null,"A.",3,"0.0.1.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"A.A.",4,"0.0.1.1.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"A.B.",4,"0.0.1.2.");
    assertHasPrefix(prefixes,"1001-1-3-1","A.B.","(a)",5,"0.0.1.2.1.");
    assertHasPrefix(prefixes,"1001-1-4-2","A.B.","(b)",5,"0.0.1.2.2.");
    assertHasPrefix(prefixes,"1001-1-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1001-1-default",null,"A.",3,"0.0.1.");
    assertHasPrefix(prefixes,"1001-2-2-1","","(a)",5,"0.0.0.0.1.");
    assertHasPrefix(prefixes,"1001-2-2-2","","(b)",5,"0.0.0.0.2.");
    assertHasPrefix(prefixes,"1001-2-default",null,"",4,null);
    assertHasPrefix(prefixes,"1002-1-1-1",null,"a.b.",3,"0.1.2.");
    assertHasPrefix(prefixes,"1002-1-2-1",null,"a.b.a.",4,"0.1.2.1.");
    assertHasPrefix(prefixes,"1002-1-default",null,"a.b.",3,"0.1.2.");
    assertHasPrefix(prefixes,"101-1-1-1",null,"1.",3,"0.0.1.");
    assertHasPrefix(prefixes,"101-1-default",null,"",3,null);
    Assert.assertEquals(32, prefixes.size());
  }

  @Test
  public void testAutoNumberingParas() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L),
            ref(3, "C", 102L)))).build();
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
    DocumentTree inter3 = new DocumentTree.Builder(102).title("C")
        .part(phantom(1,
              p(1, "1", 1, true, ""),
              h2("C", "1", 2, true, ""),
              h2("CA", "1", 3, true, "")))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    System.out.println("Tree 102 level: " + inter3.level());
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            p(1, "1a", 1, true, ""),
            p(2, "1b", 1, true, ""),
            p(1, "1c", 1, false, "x"),
            h2("a", "2", 1, true, "x.x.x"), // will not be numbered when adjusted to level 5 or 6
            p(0, "3", 1, false, ""),
            h2("b", "3", 2, true, "", // will not be numbered when adjusted to level 5 or 6
                p(1, "3a", 1, true, ""),
                p(2, "3a", 2, true, ""),
                p(1, "3b", 1, true, "x.x")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "", // will not be numbered when adjusted to level 5 or 6
                h3("xc", "5", 1, false, "x.x.x.x"))))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    DocumentTree tree2 = new DocumentTree.Builder(1001).title("Y")
        .part(h1("Y", "1", 1, true, "x.x",
                p(1, "1a", 1, true, ""),
                p(3, "1b", 1, true, ""),
            h2("a", "2", 1, true, "x.x.x"), // will not be numbered when adjusted to level 5 or 6
            h2("b", "2", 2, true, "", // will not be numbered when adjusted to level 5 or 6
                h3("x", "3", 1, true, "")), // will not be numbered twice when adjusted to level 5 or 6
            h2("c", "4", 1, false, "")))
        .addReverseReference(100L).addReverseReference(101L).build().normalize(TitleCollapse.auto);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(inter);
    publication = publication.add(inter2);
    publication = publication.add(inter3);
    publication = publication.add(tree);
    publication = publication.add(tree2);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(tree2, publication.tree(1001));
    Tests.assertDocumentTreeEquals(root, publication.root());
    assertValidPublication(publication);
    PublicationConfig config = Tests.parseConfig("publication-config-paras.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    tree.print(System.out);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"100-1-1-1",null,"1.1.",2,"1.1.");
    assertHasPrefix(prefixes,"100-1-default",null,"1.1.",2,"1.1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.1.1.",3,"1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1a-1",null,"1.1.1.1.",4,"1.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1b-1","1.1.1.1.","(a)",5,"1.1.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-1c-1",null,"x",4,null);
    assertHasPrefix(prefixes,"1000-1-2-1",null,"1.1.1.2.",4,"1.1.1.2.");
    assertHasPrefix(prefixes,"1000-1-3-2",null,"1.1.1.3.",4,"1.1.1.3.");
    assertHasPrefix(prefixes,"1000-1-3a-1","1.1.1.3.","(a)",5,"1.1.1.3.1.");
    assertHasPrefix(prefixes,"1000-1-3a-2","1.1.1.3.(a)","(i)",6,"1.1.1.3.1.1.");
    assertHasPrefix(prefixes,"1000-1-3b-1","1.1.1.3.","(b)",5,"1.1.1.3.2.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"1.1.1.4.",4,"1.1.1.4.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",5,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"1.1.1.",3,"1.1.1.");
    assertHasPrefix(prefixes,"1000-2-1-1",null,"1.1.3.1.",4,"1.1.3.1.");
    assertHasPrefix(prefixes,"1000-2-1a-1","1.1.3.1.","(a)",5,"1.1.3.1.1.");
    assertHasPrefix(prefixes,"1000-2-1b-1","1.1.3.1.(a)","(i)",6,"1.1.3.1.1.1.");
    assertHasPrefix(prefixes,"1000-2-1c-1",null,"x",5,null);
    assertHasPrefix(prefixes,"1000-2-3a-1","1.1.3.1.(a)","(ii)",6,"1.1.3.1.1.2.");
    assertHasPrefix(prefixes,"1000-2-3a-2","1.1.3.1.(a)(ii)","(A)",7,"1.1.3.1.1.2.1.");
    assertHasPrefix(prefixes,"1000-2-3b-1","1.1.3.1.(a)","(iii)",6,"1.1.3.1.1.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1000-2-default",null,"1.1.3.1.",4,"1.1.3.1.");
    assertHasPrefix(prefixes,"1001-1-1-1",null,"1.1.2.",3,"1.1.2.");
    assertHasPrefix(prefixes,"1001-1-1a-1",null,"1.1.2.1.",4,"1.1.2.1.");
    assertHasPrefix(prefixes,"1001-1-1b-1","1.1.2.1.(a)","(i)",6,"1.1.2.1.1.1.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"1.1.2.2.",4,"1.1.2.2.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"1.1.2.3.",4,"1.1.2.3.");
    assertHasPrefix(prefixes,"1001-1-default",null,"1.1.2.",3,"1.1.2.");
    assertHasPrefix(prefixes,"1001-2-1-1",null,"1.1.3.2.",4,"1.1.3.2.");
    assertHasPrefix(prefixes,"1001-2-1a-1","1.1.3.2.","(a)",5,"1.1.3.2.1.");
    assertHasPrefix(prefixes,"1001-2-1b-1","1.1.3.2.(a)(i)","(A)",7,"1.1.3.2.1.1.1.");
    assertHasPrefix(prefixes,"1001-2-default",null,"1.1.3.2.",4,"1.1.3.2.");
    assertHasPrefix(prefixes,"101-1-1-1",null,"1.1.3.",3,"1.1.3.");
    assertHasPrefix(prefixes,"101-1-default",null,"",3,null);
    assertHasPrefix(prefixes,"102-1-1-1",null,"1.1.4.",3,"1.1.4.");
    assertHasPrefix(prefixes,"102-1-1-2",null,"1.1.5.",3,"1.1.5.");
    assertHasPrefix(prefixes,"102-1-1-3",null,"1.1.6.",3,"1.1.6.");
    assertHasPrefix(prefixes,"102-1-default",null,"",2,null);
    Assert.assertEquals(40, prefixes.size());
  }

  @Test
  public void testAutoNumberingParasFixed() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 1000L,
              phantom(4,
                ref(5, "B", 1000L)))))).build();
    root = root.normalize(TitleCollapse.always);
    Tests.print(root);
    DocumentTree tree = new DocumentTree.Builder(1000).title("X")
        .part(h1("X", "1", 1, true, "x.x",
            p(1, "1a", 1, true, ""),
            p(2, "1b", 1, true, ""),
            p(1, "1c", 1, false, "x"),
            h2("a", "2", 1, true, "x.x.x"),
            p(0, "3", 1, false, ""),
            h2("b", "3", 2, true, "",
                p(1, "3a", 1, true, ""),
                p(2, "3b", 2, true, ""),
                p(3, "3c", 1, true, "x.x"),
                p(1, "3d", 1, true, "")),
            h2("c", "4", 1, false, ""),
            h2("d", "4", 2, true, "",
                h3("xc", "5", 1, true, ""))))
        .addReverseReference(1L).build().normalize(TitleCollapse.always);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(tree);
    Assert.assertEquals(root.id(), publication.id());
    Assert.assertTrue(publication.listReverseReferences().isEmpty());
    Tests.assertDocumentTreeEquals(root, publication.root());
    PublicationConfig config = Tests.parseConfig("publication-config-paras-fixed.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, config, true);
    //tree.print(System.out);
    assertValidPublication(publication, numbering, config);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-1a-1","1.","(a)",8,"0.1.0.0.0.0.0.1.");
    assertHasPrefix(prefixes,"1000-1-1b-1","1.(a)","(i)",9,"0.1.0.0.0.0.0.1.1.");
    assertHasPrefix(prefixes,"1000-1-1c-1",null,"x",8,null);
    assertHasPrefix(prefixes,"1000-1-2-1",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-1-3-2",null,"1.2.",3,"0.1.2.");
    assertHasPrefix(prefixes,"1000-1-3a-1","1.2.","(a)",8,"0.1.2.0.0.0.0.1.");
    assertHasPrefix(prefixes,"1000-1-3b-1","1.2.(a)","(i)",9,"0.1.2.0.0.0.0.1.1.");
    assertHasPrefix(prefixes,"1000-1-3c-1","1.2.(a)(i)","(A)",10,"0.1.2.0.0.0.0.1.1.1.");
    assertHasPrefix(prefixes,"1000-1-3d-1","1.2.","(b)",8,"0.1.2.0.0.0.0.2.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"1.3.",3,"0.1.3.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"1.3.1.",4,"0.1.3.1.");
    assertHasPrefix(prefixes,"1000-1-default",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-2-1-1",null,"1.3.2.",4,"0.1.3.2.");
    assertHasPrefix(prefixes,"1000-2-1a-1","1.3.2.","(a)",8,"0.1.3.2.0.0.0.1.");
    assertHasPrefix(prefixes,"1000-2-1b-1","1.3.2.(a)","(i)",9,"0.1.3.2.0.0.0.1.1.");
    assertHasPrefix(prefixes,"1000-2-1c-1",null,"x",8,null);
    assertHasPrefix(prefixes,"1000-2-2-1",null,"1.3.2.1.",5,"0.1.3.2.1.");
    assertHasPrefix(prefixes,"1000-2-3-2",null,"1.3.2.2.",5,"0.1.3.2.2.");
    assertHasPrefix(prefixes,"1000-2-3a-1","1.3.2.2.","(a)",8,"0.1.3.2.2.0.0.1.");
    assertHasPrefix(prefixes,"1000-2-3b-1","1.3.2.2.(a)","(i)",9,"0.1.3.2.2.0.0.1.1.");
    assertHasPrefix(prefixes,"1000-2-3c-1","1.3.2.2.(a)(i)","(A)",10,"0.1.3.2.2.0.0.1.1.1.");
    assertHasPrefix(prefixes,"1000-2-3d-1","1.3.2.2.","(b)",8,"0.1.3.2.2.0.0.2.");
    assertHasPrefix(prefixes,"1000-2-4-2",null,"1.3.2.3.",5,"0.1.3.2.3.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"1.3.2.3.1.",6,"0.1.3.2.3.1.");
    assertHasPrefix(prefixes,"1000-2-default",null,"1.3.2.",4,"0.1.3.2.");
    Assert.assertEquals(27, prefixes.size());  }

  @Test
  public void testAutoNumberingParasRelative() throws SAXException, IOException, XRefLoopException {
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
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    tree.print(System.out);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"100-1-1-1",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"100-1-default",null,"1.",2,"0.1.");
    assertHasPrefix(prefixes,"1000-1-1-1",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-1-1a-1",null,"",2,null);
    assertHasPrefix(prefixes,"1000-1-1b-1",null,"1.2.",3,"0.1.2.");
    assertHasPrefix(prefixes,"1000-1-1c-1",null,"x",2,null);
    assertHasPrefix(prefixes,"1000-1-2-1",null,"1.2.1.",4,"0.1.2.1.");
    assertHasPrefix(prefixes,"1000-1-3-2",null,"1.2.2.",4,"0.1.2.2.");
    assertHasPrefix(prefixes,"1000-1-3a-1",null,"1.2.3.",4,"0.1.2.3.");
    assertHasPrefix(prefixes,"1000-1-3a-2","1.2.3.","(a)",5,"0.1.2.3.1.");
    assertHasPrefix(prefixes,"1000-1-3b-1",null,"1.3.",3,"0.1.3.");
    assertHasPrefix(prefixes,"1000-1-4-2",null,"1.3.1.",4,"0.1.3.1.");
    assertHasPrefix(prefixes,"1000-1-5-1",null,"x.x.x.x",5,null);
    assertHasPrefix(prefixes,"1000-1-default",null,"1.1.",3,"0.1.1.");
    assertHasPrefix(prefixes,"1000-2-1-1",null,"1.5.1.",4,"0.1.5.1.");
    assertHasPrefix(prefixes,"1000-2-1a-1",null,"",2,null);
    assertHasPrefix(prefixes,"1000-2-1b-1",null,"1.6.",3,"0.1.6.");
    assertHasPrefix(prefixes,"1000-2-1c-1",null,"x",2,null);
    assertHasPrefix(prefixes,"1000-2-3a-1",null,"1.6.1.",4,"0.1.6.1.");
    assertHasPrefix(prefixes,"1000-2-3a-2","1.6.1.","(a)",5,"0.1.6.1.1.");
    assertHasPrefix(prefixes,"1000-2-3b-1",null,"1.7.",3,"0.1.7.");
    assertHasPrefix(prefixes,"1000-2-5-1",null,"x.x.x.x",6,null);
    assertHasPrefix(prefixes,"1000-2-default",null,"1.5.1.",4,"0.1.5.1.");
    assertHasPrefix(prefixes,"1001-1-1-1",null,"1.4.",3,"0.1.4.");
    assertHasPrefix(prefixes,"1001-1-1a-1",null,"",2,null);
    assertHasPrefix(prefixes,"1001-1-1b-1",null,"1.4.1.",4,"0.1.4.1.");
    assertHasPrefix(prefixes,"1001-1-2-1",null,"1.4.2.",4,"0.1.4.2.");
    assertHasPrefix(prefixes,"1001-1-2-2",null,"1.4.3.",4,"0.1.4.3.");
    assertHasPrefix(prefixes,"1001-1-default",null,"1.4.",3,"0.1.4.");
    assertHasPrefix(prefixes,"1001-2-1-1",null,"1.7.1.",4,"0.1.7.1.");
    assertHasPrefix(prefixes,"1001-2-1a-1",null,"",2,null);
    assertHasPrefix(prefixes,"1001-2-1b-1",null,"1.7.2.",4,"0.1.7.2.");
    assertHasPrefix(prefixes,"1001-2-default",null,"1.7.1.",4,"0.1.7.1.");
    assertHasPrefix(prefixes,"101-1-1-1",null,"1.5.",3,"0.1.5.");
    assertHasPrefix(prefixes,"101-1-default",null,"",3,null);
    Assert.assertEquals(36, prefixes.size());
  }

  @Test
  public void testAutoNumberingPerformance() throws SAXException, IOException, XRefLoopException {
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
    PublicationConfig config = Tests.parseConfig("publication-config-paras.xml");
    //Tests.print(publication, -1, number);
    long start = System.currentTimeMillis();
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    long end = System.currentTimeMillis();
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    long pstart = System.currentTimeMillis();
    Tests.print(publication, 1499, -1, numbering, null, true);
    long pend = System.currentTimeMillis();
    System.out.println("Number of prefixes: " + prefixes.size());
    System.out.println("Number of transclude prefixes: " + numbering.getAllTranscludedPrefixes().size());
    long gtime = end - start;
    long ptime = pend - pstart;
    System.out.println("Generation time: " + gtime);
    System.out.println("Print time: " + ptime);
    Assert.assertEquals(10501, prefixes.size());
    Assert.assertEquals(10000, numbering.getAllTranscludedPrefixes().size());
    Assert.assertTrue("Generation time: " + gtime, gtime < 400);
    Assert.assertTrue("Print time: " + ptime, ptime < 40);
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
    Tests.print(publication, -1, -1, null, null, true);
  }

  @Test(expected = XRefLoopException.class)
  public void testLoopDetectionAutonumber() throws SAXException, IOException, XRefLoopException {
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
    new FragmentNumbering(publication, config);
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
  public void testParseXrefLevel1() throws SAXException, IOException, XRefLoopException {
    DocumentTree tree = parse(1, "xref-level1.psml");
    Tests.print(tree);
    tree = tree.normalize(TitleCollapse.auto);
    Tests.print(tree);
    System.out.println(tree.listReverseReferences());
    Map<String, String> fheadings = tree.fragmentheadings();
    String headings = fheadings.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("HEADINGS\n" + headings);
    Assert.assertEquals("Test doc 1", fheadings.get("1"));
    Assert.assertEquals("My link", fheadings.get("3"));
    Assert.assertEquals("Related", fheadings.get("content"));
    Assert.assertEquals(3, fheadings.size());
    Map<String, Integer> flevels = tree.fragmentlevels();
    String levels = flevels.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println("LEVELS\n" + levels);
    Assert.assertEquals(Integer.valueOf(0), flevels.get("1"));
    Assert.assertEquals(Integer.valueOf(2), flevels.get("2"));
    Assert.assertEquals(Integer.valueOf(2), flevels.get("3"));
    Assert.assertEquals(3, fheadings.size());
    PublicationTree publication = new PublicationTree(tree);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, config, true);
    tree.print(System.out);
    assertValidPublication(publication, numbering, config);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"1-1-4-1",null,"",2,null);
    assertHasPrefix(prefixes,"1-1-4-2",null,"",2,null);
    assertHasPrefix(prefixes,"1-1-4-3",null,"",2,null);
    assertHasPrefix(prefixes,"1-1-default",null,"",0,null);
    Assert.assertEquals(4, prefixes.size());
//    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
//    String code = prefixes.entrySet()
//        .stream().sorted(Map.Entry.comparingByKey())
//        .map(entry -> "assertHasPrefix(prefixes,\"" + entry.getKey() + "\"," +
//            (entry.getValue().parentNumber == null ? "null" : "\"" + entry.getValue().parentNumber + "\"") + ",\"" +
//            entry.getValue().value + "\"," +
//            entry.getValue().level + "," +
//            (entry.getValue().canonical == null ? "null" : "\"" + entry.getValue().canonical + "\"") + ");")
//        .collect(Collectors.joining("\n"));
//    System.out.println(code);
//    System.out.println("Assert.assertEquals(" + prefixes.size() + ", prefixes.size());");
  }

  @Test
  public void testParseCompareRef() throws SAXException, IOException, XRefLoopException {
    DocumentTree root = parse(69152, "compare_ref.psml").normalize(TitleCollapse.always);
    DocumentTree tree1 = parse(69153, "compare_1.psml").normalize(TitleCollapse.always);
    DocumentTree tree2 = parse(69154, "compare_2.psml").normalize(TitleCollapse.always);
    //tree1.print(System.out);
    PublicationTree publication = new PublicationTree(root);
    publication = publication.add(tree1);
    publication = publication.add(tree2);
    PublicationConfig config = Tests.parseConfig("publication-config.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
    Tests.print(publication, -1, -1, numbering, null, true);
    Map<String,Prefix> prefixes = numbering.getAllPrefixes();
    String result = prefixes.entrySet()
        .stream().sorted(Map.Entry.comparingByKey())
        .map(entry -> entry.getKey() + " - " + entry.getValue())
        .collect(Collectors.joining("\n"));
    System.out.println(result);
    assertHasPrefix(prefixes,"69152-1-default",null,"",0,null);
    assertHasPrefix(prefixes,"69153-1-1-1",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"69153-1-3-1",null,"1.1.",2,"1.1.");
    assertHasPrefix(prefixes,"69153-1-default",null,"1.",1,"1.");
    assertHasPrefix(prefixes,"69153-2-1-1",null,"3.",1,"3.");
    assertHasPrefix(prefixes,"69153-2-3-1",null,"3.1.",2,"3.1.");
    assertHasPrefix(prefixes,"69153-2-default",null,"3.",1,"3.");
    assertHasPrefix(prefixes,"69154-2-1-1",null,"1.1.1.",3,"1.1.1.");
    assertHasPrefix(prefixes,"69154-3-1-1",null,"2.",1,"2.");
    assertHasPrefix(prefixes,"69154-3-default",null,"2.",1,"2.");
    assertHasPrefix(prefixes,"69154-5-1-1",null,"3.1.1.",3,"3.1.1.");
    Assert.assertEquals(11, prefixes.size());
  }

  private static void assertValidPublication(PublicationTree publication) {
    try {
      Assert.assertThat(Tests.toDOMSource(publication), Tests.validates("publication-tree.xsd"));
    } catch (AssertionError ex) {
      Tests.print(publication);
      throw ex;
    }
  }

  private static void assertValidPublication(PublicationTree publication, @Nullable FragmentNumbering number,
      @Nullable PublicationConfig config) {
    try {
      XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
      try {
        publication.toXML(xml, -1, -1, number, config, true);
      } catch (IOException ex) {
        // Won't happen
      }
      xml.flush();
      Assert.assertThat(toDOMSource(new StringReader(xml.toString())), Tests.validates("publication-tree.xsd"));
    } catch (AssertionError ex) {
      Tests.print(publication);
      throw ex;
    }
  }

  /**
   * Asserts that a prefix has a specified key in the map.
   *
   * @param prefixes   the map of prefixes
   * @param key        the map key
   * @param parent     the parent number (optional)
   * @param val        the prefix value
   * @param lvl        the heading/para level
   * @param canonic    the canonical numbering (optional)
   */
  public static void assertHasPrefix(Map<String,Prefix> prefixes, String key,
      @Nullable String parent, String val, int lvl, @Nullable String canonic) {
    Prefix p = prefixes.get(key);
    Assert.assertNotNull(p);
    Assert.assertEquals(parent, p.parentNumber);
    Assert.assertEquals(val, p.value);
    Assert.assertEquals(lvl, p.level);
    Assert.assertEquals(canonic, p.canonical);
  }

}
