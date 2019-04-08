package org.pageseeder.psml.process;

import static org.pageseeder.psml.toc.Tests.*;

import org.junit.Test;
import org.pageseeder.psml.toc.*;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class NumberedTOCGeneratorTest {

  @Test
  public void testAutoNumbering() throws SAXException, IOException, XRefLoopException {
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
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    // Generate fragment numbering
    FragmentNumbering numbering = new FragmentNumbering(publication, config);
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

}
