package org.pageseeder.psml.diff;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.Tests.Validates;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.EvaluateXPathMatcher;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DiffHandlerTest {

  private static final String SOURCE_FOLDER = "src/test/data/diff";

  @Test
  void testParseCompare3() throws SAXException, IOException {
    File src = new File(SOURCE_FOLDER, "compare_3.psml");
    try {
      // get compare fragments
      CompareHandler handler = new CompareHandler();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(new FileInputStream(src), handler);
      Map<String, String> fragments = handler.getCompareFragments();
      //System.out.println(fragments);

      // add diff elements
      StringWriter out = new StringWriter();
      DiffHandler handler2 = new DiffHandler(out, fragments, new PSMLDiffer(4000000));
      SAXParser parser2 = factory.newSAXParser();
      parser2.parse(new FileInputStream(src), handler2);
      String xml = out.toString();
      //System.out.println(xml);
      // validate
      assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
      // test xpaths
      assertThat(xml, hasXPath("count(//diff)", equalTo("2")));
      assertThat(xml, hasXPath("/document/fragmentinfo/locator[@fragment='2']/compare/diff",
          equalTo("Some new content.")));
      assertThat(xml, hasXPath("/document/fragmentinfo/locator[@fragment='3']/compare/diff",
          equalTo("Some new fragment.")));
    } catch (ParserConfigurationException | IOException ex) {
      throw new SAXException(ex);
    }
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

  public static Source getSchema(String filename) {
    try {
      String pathToSchema = "/org/pageseeder/psml/process/util/" + filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

}
