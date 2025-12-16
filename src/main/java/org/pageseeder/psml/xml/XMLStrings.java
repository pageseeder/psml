package org.pageseeder.psml.xml;

import org.jspecify.annotations.Nullable;

/**
 * Utility class for escaping XML-specific characters in strings to ensure
 * they can be safely used in XML 1.0 elements or attributes.
 *
 * <p>This class provides methods to escape characters for use in character data
 * (text) within XML elements and for use in XML attribute values.
 *
 * <p>It includes functionality to handle both nullable and non-nullable strings.
 *
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 1.7.0
 */
public final class XMLStrings {

  private XMLStrings() {}

  /**
   * Replace characters in the specified string by the corresponding XML entity
   * so that they can be used as character data (text) in elements.
   *
   * <p>If you know that the value is not null, use {@link #text(String)} directly.
   *
   * @param s The String to escape or null
   *
   * @return a valid string or is <code>null</code>.
   */
  public static @Nullable String nullableText(@Nullable String s) {
    return s != null ? text(s) : null;
  }

  /**
   * Replace characters which are invalid in attribute values by the corresponding
   * XML entity.
   *
   * <p>If you know that the value is not null, use {@link #attribute(String)} directly.
   *
   * @param s The String to escape or null
   *
   * @return a valid string or is <code>null</code>.
   */
  public static @Nullable String nullableAttribute(@Nullable String s) {
    return s != null ? attribute(s) : null;
  }

  /**
   * Replace characters in the specified string by the corresponding XML entity
   * so that they can be used as character data (text) in elements.
   *
   * <p>These characters are:</p>
   * <ul>
   *  <li>{@code '&'} by the entity {@code "&amp"}</li>
   *  <li>{@code '<'} by the entity {@code "&lt;"}</li>
   * </ul>
   *
   * <p>Note: this function makes the following assumptions:
   * <ul>
   *   <li>There are no entities in the given String. If there are existing entities,
   *   then the ampersand entity will replace the ampersand character.</li>
   *   <li>There are no other invalid XML 1.0 characters such as controls characters.</li>
   * </ul>
   *
   * <p>This method only creates a new string if there are characters to escape.</p>
   *
   * @param s The String to escape
   *
   * @return a valid string for use in the text value of an element.
   *
   * @throws NullPointerException If the string is null
   */
  public static String text(String s) {
    // bypass null and empty strings
    if (s.isEmpty()) return s;
    // do not process valid strings.
    if (s.indexOf('&') == -1 && s.indexOf('<') == -1) return s;
    // process the rest
    StringBuilder valid = new StringBuilder(s);
    int shift = 0;
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '&' :
          valid.insert(i + shift + 1, "amp;");
          shift += 4;
          break;
        case '<' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&lt;");
          shift += 3;
          break;
        default :
      }
    }
    return valid.toString();
  }

  /**
   * Replaces characters in the specified portion of a character array with their corresponding
   * XML entities so they can be safely used as character data in XML elements.
   *
   * <p>The following characters are replaced:
   * <ul>
   *   <li>{@code '&'} is replaced with {@code "&amp;"}</li>
   *   <li>{@code '<'} is replaced with {@code "&lt;"}</li>
   * </ul>
   *
   * <p>Characters that do not need escaping are appended as-is.
   *
   * @param ch The character array to process.
   * @param start The starting index in the array to begin processing.
   * @param length The ending index in the array to stop processing.
   * @return A string with the appropriate characters replaced by XML entities.
   */
  public static String text(char[] ch, int start, int length) {
    StringBuilder out = new StringBuilder(ch.length);
    final int upto = start+length;
    for (int i = start; i < upto; i++) {
      char c = ch[i];
      switch (c) {
        case '&' :
          out.append("&amp;");
          break;
        case '<' :
          out.append("&lt;");
          break;
        default :
          out.append(c);
      }
    }
    return out.toString();
  }

  /**
   * Replace characters which are invalid in attribute values,
   * by the corresponding entity in a given <code>String</code>.
   *
   * <p>These characters are:</p>
   * <ul>
   *   <li>{@code '&'} by the entity {@code "&amp"}</li>
   *   <li>{@code '<'} by the entity {@code "&lt;"}</li>
   *   <li>{@code '"'} by the entity {@code "&quot;"}</li>
   *   <li>{@code '''} by the entity {@code "&apos;"}</li>
   * </ul>
   *
   * <p>Note: this function makes the following assumptions:
   * <ul>
   *   <li>There are no entities in the given String. If there are existing entities,
   *   then the ampersand entity will replace the ampersand character.</li>
   *   <li>There are no other invalid XML 1.0 characters such as controls characters.</li>
   * </ul>
   *
   * <p>This method only creates a new string if there are characters to escape.</p>
   *
   * @param s The String to escape
   *
   * @return a valid string for use in the attribute value of an element.
   *
   * @throws NullPointerException If the string is null
   */
  public static String attribute(String s) {
    // bypass null and empty strings
    if (s.isEmpty()) return s;
    // do not process valid strings.
    if (s.indexOf('&') == -1 && s.indexOf('<') == -1 && s.indexOf('"') == -1 && s.indexOf('\'') == -1) return s;
    // process the rest
    StringBuilder valid = new StringBuilder(s);
    int shift = 0;
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '&' :
          valid.insert(i + shift + 1, "amp;");
          shift += 4;
          break;
        case '"' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&quot;");
          shift += 5;
          break;
        case '\'' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&apos;");
          shift += 5;
          break;
        case '<' :
          valid.deleteCharAt(i + shift);
          valid.insert(i + shift, "&lt;");
          shift += 3;
          break;
        default :
      }
    }
    return valid.toString();
  }

  /**
   * Replaces characters in the specified portion of a character array with their corresponding
   * XML entity so that they can be safely used in XML attribute values.
   *
   * <p>The following characters are replaced:
   * <ul>
   *   <li>{@code '&'} is replaced with {@code "&amp;"}</li>
   *   <li>{@code '<'} is replaced with {@code "&lt;"}</li>
   *   <li>{@code '"'} is replaced with {@code "&quot;"}</li>
   *   <li>{@code '\''} is replaced with {@code "&apos;"}</li>
   * </ul>
   *
   * <p>Characters that do not need escaping are appended as-is.
   *
   * @param ch The character array to process.
   * @param start The starting index in the array to begin processing.
   * @param length The ending index in the array to stop processing.
   * @return A string with the appropriate characters replaced by XML entities.
   */
  public static String attribute(char[] ch, int start, int length) {
    StringBuilder out = new StringBuilder(ch.length);
    final int upto = start+length;
    for (int i = start; i < upto; i++) {
      char c = ch[i];
      switch (c) {
        case '&' :
          out.append("&amp;");
          break;
        case '"' :
          out.append("&quot;");
          break;
        case '<' :
          out.append("&lt;");
          break;
        case '\'' :
          out.append("&apos;");
          break;
        default :
          out.append(c);
      }
    }
    return out.toString();
  }

}
