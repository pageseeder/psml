package org.pageseeder.psml.template;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class TemplateFactoryTest {

  @Test
  void testStaticOnly() throws IOException, TemplateException {
    assertTemplateEquals("<document/>", "<document/>");
  }

  @Test
  void testPlaceholder1() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"\"/>", "<document docid='{$a}'/>");
  }

  @Test
  void testPlaceholder2() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"\"/>", "<document docid='{$a}{$b}'/>");
  }

  @Test
  void testPlaceholder3() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"\"/>", "<document docid='{$a}{$b}{$c}'/>");
  }

  @Test
  void testPlaceholder4() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"--\"/>", "<document docid='{$a}-{$b}-{$c}'/>");
  }

  @Test
  void testPlaceholder5() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"x\"/>", "<document docid='{$a}'/>", toMap("a=x"));
  }

  @Test
  void testPlaceholder6() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"xy\"/>", "<document docid='{$a}{$b}'/>", toMap("a=x", "b=y"));
  }

  @Test
  void testPlaceholder7() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"xyz\"/>", "<document docid='{$a}{$b}{$c}'/>", toMap("a=x", "b=y", "c=z"));
  }

  @Test
  void testPlaceholder8() throws IOException, TemplateException {
    assertTemplateEquals("<document docid=\"x-y-z\"/>", "<document docid='{$a}-{$b}-{$c}'/>", toMap("a=x", "b=y", "c=z"));
  }

  private void assertTemplateEquals(String expected, String template) throws IOException, TemplateException {
    TemplateFactory factory = new TemplateFactory();
    Template t = factory.parse(new StringReader(template));
    assertTemplateEquals(expected, t);
  }

  private void assertTemplateEquals(String expected, String template, Map<String, String> parameters) throws IOException, TemplateException {
    TemplateFactory factory = new TemplateFactory();
    Template t = factory.parse(new StringReader(template));
    assertTemplateEquals(expected, t, parameters);
  }

  private void assertTemplateEquals(String expected, Template template) {
    Map<String, String> parameters = Collections.emptyMap();
    assertTemplateEquals(expected, template, parameters);
  }

  private void assertTemplateEquals(String expected, Template template, Map<String, String> parameters) {
    StringWriter psml = new StringWriter();
    template.process(new PrintWriter(psml), parameters);
    assertEquals(expected, psml.toString());
  }

  private Map<String, String> toMap(String... parameters) {
    Map<String, String> map = new HashMap<>();
    for (String p : parameters) {
      String[] pair = p.split("=");
      map.put(pair[0], pair[1]);
    }
    return map;
  }
}
