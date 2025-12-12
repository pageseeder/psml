/*
 * Copyright 2016 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.template;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A collection of utility classes for XML.
 *
 * @author  Christophe Lauret
 *
 * @version 1.6.9
 * @since 1.0
 */
public final class XML {

  /**
   * Last ASCII character.
   */
  private static final int ASCII_LAST_CHAR = 0x80;

  private XML() {}

  /**
   * Checks if the specified string is valid XML.
   *
   * @param xml The xml to check.
   * @return true if well-formed XML; false otherwise.
   */
  public static boolean isWellFormed(String xml) {
    XMLInputFactory f = XMLInputFactory.newFactory();

    // Security: disable DTDs/external entities to prevent XXE.
    try { f.setProperty(XMLInputFactory.SUPPORT_DTD, false); } catch (IllegalArgumentException ignored) { /* ignored */ }
    try { f.setProperty("javax.xml.stream.isSupportingExternalEntities", false); } catch (IllegalArgumentException ignored) { /* ignored */ }

    try (StringReader sr = new StringReader(xml)) {
      XMLStreamReader r = f.createXMLStreamReader(sr);
      while (r.hasNext()) r.next();
      r.close();
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Checks if the the specified string is a valid XML fragment.
   *
   * @param fragment The fragment to check.
   * @return true if well-formed XML; false otherwise.
   */
  public static boolean isWellFormedFragment(String fragment) {
    // Wrap fragment so it can be parsed as a single document.
    return isWellFormed("<r>" + fragment + "</r>");
  }

  /**
   * @param cs the charset to use for the target XML output.
   * @return The corresponding XML encoder.
   */
  public static Encoder getEncoder(Charset cs) {
    if (cs.equals(StandardCharsets.UTF_8) || cs.equals(StandardCharsets.UTF_16)) return UNICODE_ENCODER;
    return ASCII_ENCODER;
  }

  /**
   * Indicates whether the has a non ASCII character.
   *
   * @param xml The XML to test.
   * @return <code>true</code> if this XML contains a non-ASCII character;
   *         <code>false</code> if if all characters are ASCII (less than 0x80)
   */
  public static boolean hasNonASCIIChar(String xml) {
    final int upto = xml.length();
    for (int i = 0; i < upto; i++) {
      if (xml.charAt(i) >= ASCII_LAST_CHAR) return true;
    }
    return false;
  }

  /**
   * Make the specified XML ASCII safe.
   *
   * @param unicode The XML as a unicode string.
   * @param ascii   The same XML with all non-ASCII characters converted to ASCII.
   */
  public static void toASCII(String unicode, PrintWriter ascii) {
    final int upto = unicode.length();
    for (int i = 0; i < upto; i++) {
      char c = unicode.charAt(i);
      if (c >= ASCII_LAST_CHAR) {
        ascii.append("&#x");
        ascii.append(Integer.toHexString(c));
        ascii.append(';');
      } else {
        ascii.append(c);
      }
    }
  }

  /**
   * Encodes the XML text and attribute values using the appropriate numeric character entity when required
   */
  public interface Encoder {

    /**
     * Note: XML encoders can assume that attributes values are wrapped in double quotes.
     *
     * @param value Attribute value to XML encode.
     * @param xml   The XML output
     */
    void attribute(String value, StringBuilder xml);

    /**
     * XML encode for text value (reported by SAX <code>characters</code> method).
     *
     * @param ch     Character array (reported by)
     * @param start  The start index in array to encode
     * @param length The number of characters to encode.
     * @param xml    The XML output
     */
    void text(char[] ch, int start, int length, StringBuilder xml);

  }

  /**
   * XML encode a character in an attribute node.
   *
   * @param c   The character to check
   * @param xml The XML output
   */
  private static void attributeChar(char c, StringBuilder xml) {
    switch (c) {
      case '&' :
        xml.append("&amp;");
        break;
      case '<' :
        xml.append("&lt;");
        break;
      case '"' :
        xml.append("&quot;");
        break;
        // output by default
      default:
        xml.append(c);
    }
  }

  /**
   * XML encode a character in a text node.
   *
   * @param c   The character to check
   * @param xml The XML output
   */
  private static void textChar(char c, StringBuilder xml) {
    switch (c) {
      case '&' :
        xml.append("&amp;");
        break;
      case '<' :
        xml.append("&lt;");
        break;
        // output by default
      default:
        xml.append(c);
    }
  }

  /**
   * The encoder to use for writing XML as unicode encoding only the characters which are illegal
   * in some parts of the markup.
   */
  private static final Encoder UNICODE_ENCODER = new Encoder() {

    @Override
    public void attribute(String value, StringBuilder xml) {
      final int upto = value.length();
      for (int i = 0; i < upto; i++) {
        attributeChar(value.charAt(i), xml);
      }
    }

    @Override
    public void text(char[] ch, int start, int length, StringBuilder xml) {
      final int upto = start+length;
      for (int i = start; i < upto; i++) {
        textChar(ch[i], xml);
      }
    }
  };

  /**
   * The encoder to use for writing XML as ASCII encoding any characters which is illegal or
   * outside the ASCII range.
   */
  private static final Encoder ASCII_ENCODER = new Encoder() {

    @Override
    public void attribute(String value, StringBuilder xml) {
      final int upto = value.length();
      for (int i = 0; i < upto; i++) {
        char c = value.charAt(i);
        if (c < ASCII_LAST_CHAR) {
          attributeChar(value.charAt(i), xml);
        } else {
          xml.append("&#x");
          xml.append(Integer.toHexString(c));
          xml.append(';');
        }
      }
    }

    @Override
    public void text(char[] ch, int start, int length, StringBuilder xml) {
      final int upto = start+length;
      for (int i = start; i < upto; i++) {
        if (ch[i] < ASCII_LAST_CHAR) {
          textChar(ch[i], xml);
        } else {
          xml.append("&#x");
          xml.append(Integer.toHexString(ch[i]));
          xml.append(';');
        }
      }
    }

  };
}
