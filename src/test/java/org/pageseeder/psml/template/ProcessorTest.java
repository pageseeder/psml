package org.pageseeder.psml.template;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

class ProcessorTest {

  @Test
  void testProcess() throws IOException, TemplateException {
    File template = new File("src/test/data/template/template1.psml");
    Processor processor = new Processor(StandardCharsets.UTF_8);
    processor.setFailOnError(true);
    Map<String, String> parameters = new HashMap<>();
    parameters.put("ps.title", "My Title");
    parameters.put("ps.filename", "instance.psml");
    parameters.put("ps.description", "A description");
    parameters.put("ps.author", "John Smith");
    parameters.put("ps.docid", "TD1234");
    parameters.put("ps.group", "test-example");
    parameters.put("ps.path", "/ps/test/example/instance.psml");
    parameters.put("ps.currentdate", "2016-03-01+10:00");
    parameters.put("ps.currentdatetime", "2016-03-01T13:14:15+10:00");
    parameters.put("ps.currenttime", "13:14:15+10:00");
    StringWriter raw = new StringWriter();
    PrintWriter psml = new PrintWriter(raw);
    processor.process(new InputSource(template.toURI().toASCIIString()), psml, parameters);
    String actual = raw.toString();
    String expected = "<document type=\"SST\" level=\"portable\">\n" +
        "\n" +
        "  <section id=\"title\">\n" +
        "    <fragment id=\"1\">\n" +
        "      <heading level=\"1\">Social Security and Tenancy Record</heading>\n" +
        "    </fragment>\n" +
        "    <properties-fragment id=\"2\">\n" +
        "      <property name=\"ps.author\" title=\"Answered\" value=\"John Smith\"/>\n" +
        "      <property name=\"ps.title\" title=\"Contact\" value=\"My Title\"/>\n" +
        "      <property name=\"date\" title=\"Created\" value=\"2016-03-01T13:14:15+10:00\" datatype=\"date\"/>\n" +
        "      <property name=\"phone\" title=\"Phone\" value=\"\"/>\n" +
        "      <property name=\"dob\" title=\"Date of Birth\" value=\"\" datatype=\"date\"/>\n" +
        "      <property name=\"composed\" title=\"composition\" value=\"\" datatype=\"date\"/>\n" +
        "      <property name=\"bio\" title=\"Short bio\" datatype=\"markdown\"/>\n" +
        "    </properties-fragment>\n" +
        "  </section>\n" +
        "\n" +
        "  <section id=\"related\">\n" +
        "    <title>Related</title>\n" +
        "    <xref-fragment id=\"5\"/>\n" +
        "  </section>\n" +
        "\n" +
        "</document>";
    try {
      assertThat(actual, CompareMatcher.isSimilarTo(expected).normalizeWhitespace());
    } catch (AssertionError error) {
      System.out.println(actual);
      throw error;
    }
  }

}
