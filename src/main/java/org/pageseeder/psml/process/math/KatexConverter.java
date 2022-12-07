package org.pageseeder.psml.process.math;

import uk.ac.ed.ph.snuggletex.*;

import java.io.IOException;

public class KatexConverter {

  public static String convert(String input) {
    /* Create vanilla SnuggleEngine and new SnuggleSession */
    SnuggleEngine engine = new SnuggleEngine();
    SnuggleSession session = engine.createSession();

    /* Parse some LaTeX input */
    SnuggleInput snuggleInput = new SnuggleInput("$$ "+input+" $$");
    try {
      session.parseInput(snuggleInput);
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new IllegalArgumentException("Failed to convert Katex to MathML: " + ex.getMessage());
    }

    /* Specify how we want the resulting XML */
    XMLStringOutputOptions options = new XMLStringOutputOptions();
    options.setSerializationMethod(SerializationMethod.XML);
    options.setEncoding("UTF-8");
    options.setIncludingXMLDeclaration(false);
//    options.setIndenting(true);
//    options.setAddingMathSourceAnnotations(true);
//    options.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */

    /* Convert the results to an XML String, which in this case will
     * be a single MathML <math>...</math> element. */
    String mathml = session.buildXMLString(options);
    if (mathml.startsWith("<math ") && mathml.endsWith("</math>")) {
      mathml = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">" + mathml.substring(mathml.indexOf('>')+1);
    }
    return mathml;
  }
}
