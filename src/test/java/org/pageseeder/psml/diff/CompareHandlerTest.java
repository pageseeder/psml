package org.pageseeder.psml.diff;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CompareHandlerTest {

  private static final String SOURCE_FOLDER = "src/test/data/diff";

  @Test
  void testParseCompare3() throws SAXException, IOException {
    File src = new File(SOURCE_FOLDER, "compare_3.psml");
    try {
      CompareHandler handler = new CompareHandler();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(new FileInputStream(src), handler);
      Map<String, String> fragments = handler.getCompareFragments();
      //System.out.println(fragments);
      assertEquals(2, fragments.size());
      assertEquals("<fragment id=\"2\"><para>Some new content.</para></fragment>", fragments.get("2"));
      assertEquals("<fragment id=\"3\"><para>Some new fragment.</para></fragment>", fragments.get("3"));
    } catch (ParserConfigurationException | IOException ex) {
      throw new SAXException(ex);
    }
  }

}
