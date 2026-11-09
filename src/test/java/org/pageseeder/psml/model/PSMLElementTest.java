package org.pageseeder.psml.model;

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PSMLElementTest {

  @Test
  void testToXML_NestedSubclassOverride() {
    PSMLElement child = new PSMLElement(Name.PARA) {
      @Override
      public void toXML(XMLWriter xml) throws IOException {
        new PSMLElement(Name.PARA).setText("custom").toXML(xml);
      }
    };
    child.setText("base");
    PSMLElement root = new PSMLElement(Name.FRAGMENT)
        .addNode(new PSMLElement(Name.BLOCK).addNode(child));

    assertEquals("<fragment><block><para>custom</para></block></fragment>", root.toString());
  }

  @Test
  void testToXML_NestedSubclassDelegatesWithDiffMarkup() throws IOException {
    int[] calls = {0};
    PSMLElement child = new PSMLElement(Name.PARA) {
      @Override
      public void toXML(XMLWriter xml) throws IOException {
        calls[0]++;
        super.toXML(xml);
      }
    };
    child.setAttribute("diff:insert", "true")
        .addNode(new PSMLElement(Name.DIFF_INS).setText("new"));
    PSMLElement root = new PSMLElement(Name.FRAGMENT).addNode(child);

    String xml = root.toString();

    assertEquals(1, calls[0]);
    PSMLElement parsed = new Loader().parse(new StringReader(xml));
    PSMLElement para = parsed.getFirstChildElement(Name.PARA);
    assertEquals(PSMLElement.DiffOperation.INSERT, para.getDiffOperation());
    assertEquals("new", para.getFirstChildElement(Name.DIFF_INS).getText());
    assertEquals(xml, root.toString());
    assertEquals(2, calls[0]);
  }
}
