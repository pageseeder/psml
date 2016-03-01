package org.pageseeder.psml.template;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xml.sax.InputSource;

public class ProcessorTest {

  public ProcessorTest() {
    // TODO Auto-generated constructor stub
  }

  @Test
  public void testProcess() throws IOException, TemplateException {
    File template = new File("src/test/data/template/template1.psml");
    Processor processor = new Processor(Charset.forName("UTF-8"));
    processor.setFailOnError(true);
    Map<String, String> parameters = new HashMap<String, String>();
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
    PrintWriter psml = new PrintWriter(System.out);
    processor.process(new InputSource(template.toURI().toASCIIString()), psml, parameters);
  }

}
