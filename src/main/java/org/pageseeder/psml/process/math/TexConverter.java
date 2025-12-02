package org.pageseeder.psml.process.math;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.process.util.WrappingReader;
import org.pageseeder.psml.util.PSCache;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

/**
 * A utility class for converting TeX strings into MathML format using the KaTeX JavaScript library.
 *
 * <p>This class provides a static method to perform the conversion and uses an in-memory LRU
 * (Least Recently Used) cache to optimize performance by avoiding repeated conversions for the
 * same input.
 *
 * <p>Note: This class is not instantiable and provides utility functions only.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class TexConverter {

  /**
   * No constructor
   */
  private TexConverter() {}
  private static final String JS_SCRIPT = "/org/pageseeder/psml/process/math/katex.0.16.9.min.js";

  /**
   * Note: The script doesn't need to be reset as it does not seem to get slower over time
   */
  private static @Nullable Invocable script = null;

  private static final Map<String, String> cache = Collections.synchronizedMap(new PSCache<>(200));

  /**
   * Convert the provided TeX string to mathml content
   *
   * @param input the input
   *
   * @return the mathml content
   */
  public static String convert(@Nullable String input) {
    // sanity check
    if (input == null || input.trim().isEmpty()) return "";

    input = input.trim();

    // check cache
    String result = cache.get(input);
    if (result == null) {

      // invoke the function named "parse" with the TeX math as the argument
      try {
        synchronized (TexConverter.class) {
          result = script().invokeFunction("parse", input).toString();
        }
        // extract mathml from HTML result
        result = extractMathML(result);
        cache.put(input, result);
      } catch (ScriptException | NoSuchMethodException | IOException ex) {
        throw new IllegalArgumentException("Failed to run KaTex to MathML JS script: " + ex.getMessage());
      }
    }
    return result;
  }

  /**
   * Look for mathml content in the string provided
   *
   * @param result the string from the JS script
   *
   * @return the mathml extracted
   */
  private static String extractMathML(String result) {
    int start = result.indexOf("<math");
    if (start > 0) {
      int semantics = result.indexOf("<semantics>", start);
      if (semantics > 0) {
        start = semantics + 11;
        int annotation = result.indexOf("<annotation encoding=\"application/x-tex\">");
        if (annotation > 0) {
          result = result.substring(start, annotation);
        } else {
          result = result.substring(start, result.indexOf("</semantics>", start + 1));
        }
        return "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">"+result+"</math>";
      } else {
        return result.substring(start, result.indexOf("</math>", start + 1) + 7);
      }
    }
    return result;
  }

  /**
   * Load the script from the internal resource
   *
   * @return the script ready to be invoked
   *
   * @throws ScriptException If loading the script failed
   * @throws IOException If loading the script failed
   */
  private static Invocable script() throws ScriptException, IOException {
    synchronized (TexConverter.class) {
      if (script != null) return script;
    }

    // load script
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("rhino");
    Compilable cengine = (Compilable) engine;

    // evaluate JavaScript code
    try {
      InputStream in = TexConverter.class.getResourceAsStream(JS_SCRIPT);
      if (in != null) {
        // add the Array.fill() method as it seems to be missing
        String scriptPrefix = "Array.prototype.fill = function(arg) { for (var i = 0; i < this.length; i++) { this[i] = arg; } };";
        String scriptSuffix = "var parse = function(str) { return katex.renderToString(str, { output: 'mathml' }); };";
        CompiledScript cscript = cengine.compile(new WrappingReader(new InputStreamReader(in), scriptPrefix, scriptSuffix));
        cscript.eval();
        // create an Invocable object by casting the script engine object
        script = (Invocable) cscript.getEngine();
        return script;
      } else {
        throw new IllegalArgumentException("Failed to load KaTex to MathML JS script");
      }
    } catch (ScriptException | IOException ex) {
      System.err.println("Failed to load KaTex to MathML JS script: "+ex.getMessage());
      throw ex;
    }
  }

}
