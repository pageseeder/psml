package org.pageseeder.psml.diff;

import java.util.Set;

/**
 * Applicable text normalization features.
 *
 * @author Christophe Lauret
 *
 * @version 1.7.2
 * @since 1.7.2
 */
public enum NormalizationFeature {

  /**
   * Accent folding removes diacritical marks (such as accents) from characters, for example,
   * replacing 'é' with 'e' to ignore diacritical marks.
   */
  DIACRITIC_FOLDING,

  /**
   * Bracket folding: replace any bracket (Ps and Pe Unicode categories) with either '(' or ')'
   * to ignore differences between brackets.
   */
  BRACKET_FOLDING,

  /**
   * Case folding: replace uppercase letters with lowercase to ignore differences in case.
   */
  CASE_FOLDING,

  /**
   * Dash folding: replace any dash (Pd Unicode category) with a soft hyphen (U+00AD) to
   * ignore differences in dashes.
   */
  DASH_FOLDING,

  /**
   * Punctuation folding: replace punctuation marks with a single dot to ignore differences in punctuation.
   */
  PUNCTUATION_FOLDING,

  /**
   * Quote folding: replace any quotation mark (Pi and Pf Unicode categories) with an apostrophe
   * to ignore differences between quotation marks.
   */
  QUOTE_FOLDING,

  /**
   * Space folding: replace multiple consecutive XML spaces with a single space (U+0020)
   * to ignore differences in spaces.
   */
  XML_SPACE_FOLDING,

  /**
   * Space folding: replace multiple consecutive Unicode spaces (Zs, Zl, and Zp Unicode categories)
   * or XML space with a single space (U+0020) to ignore differences in any type of spaces.
   */
  UNICODE_WHITESPACE_FOLDING;

  /**
   * Returns a set containing all the normalization features defined in this enumeration.
   *
   * @return a set of all {@code NormalizationFeature} values.
   */
  public static Set<NormalizationFeature> all() {
    return Set.of(values());
  }

}
