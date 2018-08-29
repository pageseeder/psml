package org.pageseeder.psml.diff;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.Tests.Validates;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.EvaluateXPathMatcher;

public final class DiffHandlerTest {

  private static final String SOURCE_FOLDER = "src/test/data/diff";

  @Test
  public void testParseCompare3() throws SAXException, IOException {
    File src = new File(SOURCE_FOLDER, "compare_3.psml");
    try {
      // get compare fragments
      CompareHandler handler = new CompareHandler();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(new FileInputStream(src), handler);
      Map<String,String> fragments = handler.getCompareFragments();
      //System.out.println(fragments);

      // add diff elements
      StringWriter out = new StringWriter();
      DiffHandler handler2 = new DiffHandler(out, fragments, new PSMLDiffer(4000000));
      SAXParser parser2 = factory.newSAXParser();
      parser2.parse(new FileInputStream(src), handler2);
      String xml = out.toString();
      //System.out.println(xml);
      // validate
      Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
      // test xpaths
      Assert.assertThat(xml, hasXPath("count(//diff)", equalTo("2")));
      Assert.assertThat(xml, hasXPath("/document/fragmentinfo/locator[@fragment='2']/compare/diff",
          equalTo("Some new content.")));
      Assert.assertThat(xml, hasXPath("/document/fragmentinfo/locator[@fragment='3']/compare/diff",
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
      String pathToSchema = "/org/pageseeder/psml/process/util/"+filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

}
