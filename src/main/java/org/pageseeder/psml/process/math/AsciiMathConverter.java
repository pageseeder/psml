package org.pageseeder.psml.process.math;

import org.pageseeder.psml.process.util.WrappingReader;
import org.pageseeder.psml.util.PSCache;

import javax.script.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

public class AsciiMathConverter {

  private static final String JS_SCRIPT = "/org/pageseeder/psml/process/math/ASCIIMathML.js";

  private static Invocable SCRIPT = null;

  private final static Map<String, String> cache = Collections.synchronizedMap(new PSCache<>(200));

  public static String convert(String asciimath) {
    // sanity check
    if (asciimath == null || asciimath.isEmpty()) return "";

    // remove '`' quotes around the string if any
    String am = asciimath;
    if (am.charAt(0) == '`' && am.charAt(am.length()-1) == '`')
      am = am.substring(1, am.length()-1);

    // disable 'id' and 'class' as they are not handled properly in MathJax
    if (am.contains("class"))
      throw new IllegalArgumentException(
              "The AsciiMath \""+am+"\" could not be converted to MathML because \"class\" is not supported.");
    if (am.contains("id"))
      throw new IllegalArgumentException(
              "The AsciiMath \""+am+"\" could not be converted to MathML because \"id\" is not supported.");

    // check cache
    String result = cache.get(am);
    if (result == null) {

      // invoke the function named "parse" with the ascii math as the argument
      try {
        synchronized (AsciiMathConverter.class) {
          result = script().invokeFunction("parse", am).toString();
        }
        cache.put(am, result);
      } catch (ScriptException | NoSuchMethodException | IOException ex) {
        ex.printStackTrace();
        throw new IllegalArgumentException(
                "Failed to run ASCIIMath to MathML JS script: " + ex.getMessage());
      }
    }
    return result;
  }

  /**
   * Clears the script engine.
   * This should be done before each process,
   * otherwise the conversion will get slower over time.
   */
  public static void reset() {
    synchronized (AsciiMathConverter.class) {
      SCRIPT = null;
    }
  }

  private static Invocable script() throws ScriptException, IOException {
    synchronized (AsciiMathConverter.class) {
      if (SCRIPT != null) return SCRIPT;
    }

    // load script
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("javascript");
    Compilable cengine = (Compilable) engine;

    // evaluate JavaScript code
    try {
      CompiledScript cscript = cengine.compile(new WrappingReader(
          new InputStreamReader(AsciiMathConverter.class.getResourceAsStream(JS_SCRIPT)),
          prefix(), "var parse = function(str) {asciimath.initSymbols(); return asciimath.parseMath(str, false).toXML();};"));
      cscript.eval();
      // create an Invocable object by casting the script engine object
      SCRIPT = (Invocable) cscript.getEngine();
      return (Invocable) cscript.getEngine(); // don't return SCRIPT as it may have been reset by another thread
    } catch (ScriptException | IOException ex) {
      System.err.println("Failed to load ASCIIMath to MathML JS script: "+ex.getMessage());
      throw ex;
    }
  }

  private static String prefix() {
    return "indent = true;"+
           "navigator = {appName: ''};" +
           "text = function(v) { return {" +
           "  nodeValue: v,"+
           "  parentNode: {}," +
           "  hasChildNodes: function() { return false; }," +
           "  toXML: function() { return xml_encode(this.nodeValue); }" +
           "}};" +
           "element = function(ns, n) { return {" +
           "  namespace: ns," +
           "  nodeName: n," +
           "  fragment: !n," +
           "  atts: []," +
           "  childNodes: []," +
           "  firstChild: null," +
           "  lastChild: null," +
           "  nextSibling: null," +
           "  parentNode: null," +
           "  replaceChild: function(n, o) {" +
           "    for (var i = 0; i < this.childNodes.length; i++) {" +
           "      if (this.childNodes[i] == o) {" +
           "        if (n.parentNode && n.parentNode.removeChild) { n.parentNode.removeChild(n); }"+
           "        this.childNodes[i] = n;" +
           "        this.childNodes[i].parentNode = null;" +
           "        this.childNodes[i].nextSibling = null;" +
           "        n.parentNode = this;" +
           "        if (i > 0) { this.childNodes[i-1].nextSibling = n; }" +
           "        if (i < this.childNodes.length-1) { n.nextSibling = this.childNodes[i+1]; }" +
           "        break;" +
           "      }" +
           "    }" +
           "    this.firstChild = !this.childNodes.length ? null : this.childNodes[0];" +
           "    this.lastChild  = !this.childNodes.length ? null : this.childNodes[this.childNodes.length-1];" +
           "  }," +
           "  appendChild: function(n) {"+
           "    if (n.fragment) {"+
           "      while (n.hasChildNodes()) { this.appendChild(n.firstChild); }"+
           "    } else {"+
           "      if (n.parentNode && n.parentNode.removeChild) { n.parentNode.removeChild(n); }"+
           "      this.childNodes.push(n);"+
           "      n.parentNode = this;"+
           "      n.nextSibling = null;"+
           "      if (this.lastChild) { this.lastChild.nextSibling = n; }" +
           "      this.firstChild = this.childNodes[0];" +
           "      this.lastChild  = !this.childNodes.length ? null : this.childNodes[this.childNodes.length-1];" +
           "    }"+
           "  }," +
           "  removeChild: function(n) {" +
           "    var ind = this.childNodes.indexOf(n);" +
           "    if (ind >= 0) {"+
           "      if (ind != 0) { this.childNodes[ind-1].nextSibling = ind == this.childNodes.length-1 ? null : this.childNodes[ind+1]; }" +
           "      this.childNodes.splice(ind, 1);"+
           "      n.parentNode = null;"+
           "      n.nextSibling = null;"+
           "    }" +
           "    this.firstChild = !this.childNodes.length ? null : this.childNodes[0];" +
           "    this.lastChild  = !this.childNodes.length ? null : this.childNodes[this.childNodes.length-1];" +
           "  }," +
           "  hasChildNodes: function() { return this.childNodes.length > 0; }," +
           "  setAttribute: function(n, v) { this.atts.push({name: n, value: v}); }," +
           "  toXML: function() {" +
           "    var xml = '';" +
           "    if (this.nodeName) {"+
           "      var p = !indent ? null : this.parentNode;" +
           "      while (p) { if (p.nodeName) { xml += '  '; } p = p.parentNode; }" +
           "      xml += '<'+this.nodeName;" +
           "      if (this.namespace && this.nodeName == 'math') {" +
           "        xml += ' xmlns=\"' + this.namespace + '\"';" +
           "      }" +
           "      for (var i = 0; i < this.atts.length; i++) {" +
           "        var value = this.atts[i].value.toString().replace(/(\")|(&#34;)/g, '&quot;');" +
           "        xml += ' ' + this.atts[i].name + '=\"' + value + '\"';" +
           "      }" +
           "      xml += '>';" +
           "    }" +
           "    if (indent && this.childNodes.length > 0 && this.childNodes[0].nodeName) { xml += '\\n'; }" +
           "    for (var i = 0; i < this.childNodes.length; i++) {" +
           "      if (this.childNodes[i].toXML) { xml += this.childNodes[i].toXML(); }" +
           "      else xml += 'unknown element: '+this.childNodes[i];" +
           "    }" +
           "    if (this.nodeName) {"+
           "      if (indent && this.childNodes.length > 0 && this.childNodes[0].nodeName) {" +
           "        var p = this.parentNode;" +
           "        while (p) { if (p.nodeName) { xml += '  '; } p = p.parentNode; }" +
           "      }" +
           "      xml += '</'+this.nodeName+'>' + ( indent ? '\\n' : '');" +
           "    }" +
           "    return xml;" +
           "  }" +
           "}};" +
           "document = {" +
           "  getElementById         : function()      { return ''; }," +
           "  getElementsByTagName   : function(n)     { return [element(n)]; }," +
           "  createTextNode         : function(v)     { return text(v); }," +
           "  createElement          : function(n)     { return element('', n); }," +
           "  createElementNS        : function(ns, n) { return element(ns, n); }," +
           "  createDocumentFragment : function()      { return element(); }" +
           "};" +
           "console = { log : function(e) { if (e.toXML) { print('console: ' + e.toXML()); } else { print('console: ' + e); } } }," +
           "window = {};"+
           "var codePointAt = function (s, p) {" +
           "  var code = s.charCodeAt(p);" +
           "  var next = s.charCodeAt(p + 1);" +
           "  if (0xD800 <= code && code <= 0xDBFF && 0xDC00 <= next && next <= 0xDFFF) {" +
           "    return ((code - 0xD800) * 0x400) + (next - 0xDC00) + 0x10000;" +
           "  }" +
           "  return code;" +
           "};" +
           "var xml_encode = function(s) {" +
           "  var v = '';" +
           "  for (var i = 0; i < s.length; i++) { " +
           "    var cp = codePointAt(s, i);"+
           "    var c  = s.charAt(i);"+
           "    if (cp > 127) { v += '&#' + cp + ';'; }" +
           "    else if (c == '<') { v += '&lt;'; }" +
           "    else if (c == '>') { v += '&gt;'; }" +
           "    else if (c == '&') { v += '&amp;'; }" +
           "    else               { v += s.charAt(i); }" +
           "  }" +
           "  return v;" +
           "}";
  }
}
