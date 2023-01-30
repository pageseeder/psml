package org.pageseeder.psml.process.math;

import uk.ac.ed.ph.snuggletex.*;
import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;

import java.io.IOException;
import java.util.List;

public class TexConverter {

  public static String convert(String input) {
    /* Create vanilla SnuggleEngine and new SnuggleSession */
    SnuggleEngine engine = new SnuggleEngine();
    SnuggleSession session = engine.createSession();
    // stop at first error
    session.getConfiguration().setFailingFast(true);

    String newInput = input;
    // replace all non-breaking space (caused an error in snuggle tex 1.2.2)
    //newInput = newInput.replaceAll("[\\u00A0]", " ");
    // aligned is not supported but eqnarray is
    if (newInput.startsWith("\\begin{aligned}")) {
      newInput = newInput.replaceAll("\\{aligned}", "{eqnarray*}");
      newInput = newInput.replaceAll("&=", "&=&");
    }
    // eqnarray can't be used in math mode
    if (!newInput.startsWith("\\begin{eqnarray*}"))
      newInput = "$$ "+newInput+" $$";

    /* Parse some LaTeX input */
    SnuggleInput snuggleInput = new SnuggleInput(newInput);
    try {
      session.parseInput(snuggleInput);
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new IllegalArgumentException("The Tex \""+input+"\" could not be converted to MathML because: " + ex.getMessage());
    }

    /* Specify how we want the resulting XML */
    XMLStringOutputOptions options = new XMLStringOutputOptions();
    options.setSerializationMethod(SerializationMethod.XML);
    options.setEncoding("UTF-8");
    options.setIncludingXMLDeclaration(false);
    //options.setIndenting(true);
    //options.setAddingMathSourceAnnotations(true);
    //options.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */

    /* Convert the results to an XML String, which in this case will
     * be a single MathML <math>...</math> element. */
    String mathml = session.buildXMLString(options);
    List<InputError> errors = session.getErrors();
    if (!errors.isEmpty()) {
      String message2 = MessageFormatter.formatErrorAsString(errors.get(0));
      // add input for non-ascii character error
      String message1 = message2.contains("TTEG02") ? "\"" + input + "\" " : "";
      throw new IllegalArgumentException("The Tex " + message1 + "could not be converted to MathML because: " + message2);
    }
    if (mathml.startsWith("<math ") && mathml.endsWith("</math>")) {
      mathml = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">" + mathml.substring(mathml.indexOf('>')+1);
    }
    return mathml;
  }
}
