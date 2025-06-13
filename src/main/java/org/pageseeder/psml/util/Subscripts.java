package org.pageseeder.psml.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for character conversion to subscript forms.
 *
 * <p>This class provides methods to check and convert strings or individual characters
 * into their corresponding subscript representations based on a predefined mapping.
 *
 * <p>This class is not instantiable and is designed for static access only.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.6.0
 */
public final class Subscripts {

  private static final Map<Character, Character> SUBSCRIPT_MAP = new HashMap<>();
  static {
    SUBSCRIPT_MAP.put('0', '₀');
    SUBSCRIPT_MAP.put('1', '₁');
    SUBSCRIPT_MAP.put('2', '₂');
    SUBSCRIPT_MAP.put('3', '₃');
    SUBSCRIPT_MAP.put('4', '₄');
    SUBSCRIPT_MAP.put('5', '₅');
    SUBSCRIPT_MAP.put('6', '₆');
    SUBSCRIPT_MAP.put('7', '₇');
    SUBSCRIPT_MAP.put('8', '₈');
    SUBSCRIPT_MAP.put('9', '₉');
    SUBSCRIPT_MAP.put('+', '₊');
    SUBSCRIPT_MAP.put('-', '₋');
    SUBSCRIPT_MAP.put('=', '₌');
    SUBSCRIPT_MAP.put('(', '₍');
    SUBSCRIPT_MAP.put(')', '₎');
    SUBSCRIPT_MAP.put('a', 'ₐ');
    SUBSCRIPT_MAP.put('e', 'ₑ');
    SUBSCRIPT_MAP.put('o', 'ₒ');
    SUBSCRIPT_MAP.put('x', 'ₓ');
  }

  private Subscripts() {}

  /**
   * Checks if the input string can be entirely converted to subscript characters.
   * Each character of the input string is evaluated to determine if it has a corresponding subscript mapping.
   *
   * @param s The input string to check for replaceability with subscript characters.
   * @return True if all characters in the input string have corresponding subscript mappings, false otherwise.
   */
  public static boolean isReplaceable(String s) {
    for (char c : s.toCharArray()) {
      if (!hasSubscript(c)) return false;
    }
    return true;
  }

  /**
   * Checks if a given character has a corresponding subscript character mapping.
   *
   * @param c The character to check for a subscript mapping.
   * @return True if the character has a corresponding subscript mapping, false otherwise.
   */
  public static boolean hasSubscript(char c) {
    return SUBSCRIPT_MAP.containsKey(c);
  }

  /**
   * Converts a given character to its corresponding subscript character if a mapping exists.
   * If no mapping exists, the original character is returned.
   *
   * @param c The character to be converted to a subscript.
   * @return The corresponding subscript character if a mapping exists, otherwise the original character.
   */
  public static char toSubscript(char c) {
    return SUBSCRIPT_MAP.getOrDefault(c, c);
  }

  /**
   * Converts the characters in the input string to their corresponding subscript characters if available.
   * Characters without a defined subscript mapping remain unchanged.
   *
   * @param input The input string to be converted to subscript characters.
   * @return A string with the corresponding subscript characters, or the original characters if no mapping exists.
   */
  public static String toSubscript(String input) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      sb.append(SUBSCRIPT_MAP.getOrDefault(c, c));
    }
    return sb.toString();
  }

}
