package org.pageseeder.psml.process;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;
import static org.pageseeder.psml.toc.Tests.h5;
import static org.pageseeder.psml.toc.Tests.phantom;
import static org.pageseeder.psml.toc.Tests.ref;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.pageseeder.psml.toc.DocumentTree;
import org.pageseeder.psml.toc.FragmentNumbering;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.psml.toc.PublicationTree;
import org.pageseeder.psml.toc.Reference;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.TitleCollapse;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.xml.sax.SAXException;

public class NumberedTOCGeneratorTest {

  @Test
  public void testAutoNumbering() throws SAXException, IOException {
    DocumentTree root = new DocumentTree.Builder(1).title("T")
        .part(h1("T", "1", 1,
            phantom(2,
            ref(3, "A", 100L),
            ref(3, "B", 101L)))).build().normalize(TitleCollapse.auto);
    DocumentTree inter = new DocumentTree.Builder(100).title("A")
        .part(h1("A", "1", 1, true, "",
            ref(2, "X", 1000L),
            ref(2, "Y", 1001L)))
        .addReverseReference(1L).build().normalize(TitleCollapse.auto);
    DocumentTree inter2 = new DocumentTree.Builder(101).title("B")
        .part(phantom(1,
              ref(0, "BX", 1000L),
              ref(0, "BY", 1001L, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, "2"),
              ref(0, "BZ2", 1001L, Reference.Type.TRANSCLUDE, Reference.DEFAULT_TYPE, Reference.DEFAULT_FRAGMENT)))
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
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config, true, new ArrayList<Long>());
    NumberedTOCGenerator toc = new NumberedTOCGenerator(publication);
    toc.setFragmentNumbering(numbering);
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    try {
      toc.toXML(xml);
    } catch (IOException ex) {
      // Won't happen
    }
    xml.flush();
    System.out.println(xml.toString());
  }

}
