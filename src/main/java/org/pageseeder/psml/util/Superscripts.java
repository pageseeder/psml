package org.pageseeder.psml.util;


import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for working with superscript characters. This class provides
 * methods to check for superscript compatibility and perform conversions
 * between regular characters or strings and their superscript equivalents.
 *
 * <p>It relies on a predefined mapping of characters to their corresponding
 * superscript representations, supporting both numeric and alphabetic
 * characters, and some common symbols.
 *
 * <p>This class is not instantiable and is designed for static access only.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.6.0
 */
public final class Superscripts {

  private static final Map<Character, Character> SUPERSCRIPT_MAP = new HashMap<>();
  static {
    SUPERSCRIPT_MAP.put('0', '⁰');
    SUPERSCRIPT_MAP.put('1', '¹');
    SUPERSCRIPT_MAP.put('2', '²');
    SUPERSCRIPT_MAP.put('3', '³');
    SUPERSCRIPT_MAP.put('4', '⁴');
    SUPERSCRIPT_MAP.put('5', '⁵');
    SUPERSCRIPT_MAP.put('6', '⁶');
    SUPERSCRIPT_MAP.put('7', '⁷');
    SUPERSCRIPT_MAP.put('8', '⁸');
    SUPERSCRIPT_MAP.put('9', '⁹');
    SUPERSCRIPT_MAP.put('+', '⁺');
    SUPERSCRIPT_MAP.put('-', '⁻');
    SUPERSCRIPT_MAP.put('=', '⁼');
    SUPERSCRIPT_MAP.put('(', '⁽');
    SUPERSCRIPT_MAP.put(')', '⁾');
    SUPERSCRIPT_MAP.put('a', 'ᵃ');
    SUPERSCRIPT_MAP.put('b', 'ᵇ');
    SUPERSCRIPT_MAP.put('c', 'ᶜ');
    SUPERSCRIPT_MAP.put('d', 'ᵈ');
    SUPERSCRIPT_MAP.put('e', 'ᵉ');
    SUPERSCRIPT_MAP.put('f', 'ᶠ');
    SUPERSCRIPT_MAP.put('g', 'ᵍ');
    SUPERSCRIPT_MAP.put('h', 'ʰ');
    SUPERSCRIPT_MAP.put('i', 'ⁱ');
    SUPERSCRIPT_MAP.put('j', 'ʲ');
    SUPERSCRIPT_MAP.put('k', 'ᵏ');
    SUPERSCRIPT_MAP.put('l', 'ˡ');
    SUPERSCRIPT_MAP.put('m', 'ᵐ');
    SUPERSCRIPT_MAP.put('n', 'ⁿ');
    SUPERSCRIPT_MAP.put('o', 'ᵒ');
    SUPERSCRIPT_MAP.put('p', 'ᵖ');
    SUPERSCRIPT_MAP.put('r', 'ʳ');
    SUPERSCRIPT_MAP.put('s', 'ˢ');
    SUPERSCRIPT_MAP.put('t', 'ᵗ');
    SUPERSCRIPT_MAP.put('u', 'ᵘ');
    SUPERSCRIPT_MAP.put('v', 'ᵛ');
    SUPERSCRIPT_MAP.put('w', 'ʷ');
    SUPERSCRIPT_MAP.put('x', 'ˣ');
    SUPERSCRIPT_MAP.put('y', 'ʸ');
    SUPERSCRIPT_MAP.put('z', 'ᶻ');
    SUPERSCRIPT_MAP.put('A', 'ᴬ');
    SUPERSCRIPT_MAP.put('B', 'ᴮ');
    SUPERSCRIPT_MAP.put('D', 'ᴰ');
    SUPERSCRIPT_MAP.put('E', 'ᴱ');
    SUPERSCRIPT_MAP.put('G', 'ᴳ');
    SUPERSCRIPT_MAP.put('H', 'ᴴ');
    SUPERSCRIPT_MAP.put('I', 'ᴵ');
    SUPERSCRIPT_MAP.put('J', 'ᴶ');
    SUPERSCRIPT_MAP.put('K', 'ᴷ');
    SUPERSCRIPT_MAP.put('L', 'ᴸ');
    SUPERSCRIPT_MAP.put('M', 'ᴹ');
    SUPERSCRIPT_MAP.put('N', 'ᴺ');
    SUPERSCRIPT_MAP.put('O', 'ᴼ');
    SUPERSCRIPT_MAP.put('P', 'ᴾ');
    SUPERSCRIPT_MAP.put('R', 'ᴿ');
    SUPERSCRIPT_MAP.put('T', 'ᵀ');
    SUPERSCRIPT_MAP.put('U', 'ᵁ');
    SUPERSCRIPT_MAP.put('V', 'ⱽ');
    SUPERSCRIPT_MAP.put('W', 'ᵂ');
  }

  private Superscripts() {}

  /**
   * Checks if the input string can be entirely converted to superscript characters.
   * Each character of the input string is evaluated to determine if it has a corresponding superscript mapping.
   *
   * @param s The input string to check for replaceability with superscript characters.
   * @return True if all characters in the input string have corresponding superscript mappings, false otherwise.
   */
  public static boolean isReplaceable(String s) {
    for (char c : s.toCharArray()) {
      if (!hasSuperscript(c)) return false;
    }
    return true;
  }

  /**
   * Checks if a given character has a corresponding superscript character mapping.
   *
   * @param c The character to check for a superscript mapping.
   * @return True if the character has a corresponding superscript mapping, false otherwise.
   */
  public static boolean hasSuperscript(char c) {
    return SUPERSCRIPT_MAP.containsKey(c);
  }

  /**
   * Converts a given character to its corresponding superscript character if a mapping exists.
   * If no mapping exists, the original character is returned.
   *
   * @param c The character to be converted to a superscript.
   * @return The corresponding superscript character if a mapping exists, otherwise the original character.
   */
  public static char toSuperscript(char c) {
    return SUPERSCRIPT_MAP.getOrDefault(c, c);
  }

  /**
   * Converts the characters in the input string to their corresponding superscript characters if available.
   * Characters without a defined superscript mapping remain unchanged.
   *
   * @param input The input string to be converted to superscript characters.
   * @return A string with the corresponding superscript characters, or the original characters if no mapping exists.
   */
  public static String toSuperscript(String input) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      sb.append(SUPERSCRIPT_MAP.getOrDefault(c, c));
    }
    return sb.toString();
  }

}
