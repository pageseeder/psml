package org.pageseeder.psml.diff;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Normalizes text for lexical comparison, including accent, bracket, case, dash, quote, and space folding.
 *
 * @author Christophe Lauret
 *
 * @since 1.7.2
 * @version 1.7.2
 */
public final class LexicalNormalizer implements TextNormalizer {

  /**
   * Applicable normalization features.
   */
  public enum Feature {

    /**
     * Accent folding removes diacritical marks (such as accents) from characters, for example, replacing é with e.
     */
    ACCENT,

    /**
     * Bracket folding: replace any bracket (Ps and Pe Unicode categories) with either '(' or ')'.
     */
    BRACKET,

    /**
     * Case folding: replace uppercase letters with lowercase.
     */
    CASE,

    /**
     * Dash folding: replace any dash (Pd Unicode category) with a soft hyphen (U+00AD).
     */
    DASH,

    /**
     * Quote folding: replace any quotation mark (Pi and Pf Unicode categories) with an apostrophe.
     */
    QUOTE,

    /**
     * Space folding: replace multiple space (Zs Unicode category) or XML space with a single space (U+0020).
     */
    SPACE,

    /**
     * Punctuation folding: replace punctuation marks with a single dot.
     */
    PUNCTUATION

  }

  private static final int ACCENT_BIT  = 1;
  private static final int BRACKET_BIT = 1 << 1;
  private static final int CASE_BIT    = 1 << 2;
  private static final int DASH_BIT    = 1 << 3;
  private static final int QUOTE_BIT   = 1 << 4;
  private static final int SPACE_BIT   = 1 << 5;
  private static final int PUNCTUATION_BIT = 1 << 6;

  private static final int ALL_FLAGS = ACCENT_BIT | BRACKET_BIT | CASE_BIT | DASH_BIT | QUOTE_BIT | SPACE_BIT | PUNCTUATION_BIT;

  private static final LexicalNormalizer NONE_INSTANCE = new LexicalNormalizer(0);
  private static final LexicalNormalizer ALL_INSTANCE  = new LexicalNormalizer(ALL_FLAGS);

  private final int flags;

  private final boolean caseFolding;
  private final boolean accentFolding;
  private final boolean bracketFolding;
  private final boolean dashFolding;
  private final boolean quoteFolding;
  private final boolean spaceFolding;
  private final boolean punctuationFolding;

  private LexicalNormalizer(int flags) {
    this.flags = flags;
    this.caseFolding = (this.flags & CASE_BIT) != 0;
    this.accentFolding = (this.flags & ACCENT_BIT) != 0;
    this.bracketFolding = (this.flags & BRACKET_BIT) != 0;
    this.dashFolding = (this.flags & DASH_BIT) != 0;
    this.quoteFolding = (this.flags & QUOTE_BIT) != 0;
    this.spaceFolding = (this.flags & SPACE_BIT) != 0;
    this.punctuationFolding = (this.flags & PUNCTUATION_BIT) != 0;
  }

  /**
   * Returns a normalizer that does nothing.
   */
  public static LexicalNormalizer none() {
    return NONE_INSTANCE;
  }

  /**
   * Returns a normalizer that applies all the available normalization features.
   */
  public static LexicalNormalizer all() {
    return ALL_INSTANCE;
  }

  /**
   * Returns a normalizer that applies the specified normalization features.
   *
   * @param features The normalization features to apply.
   *
   * @return A LexicalNormalizer instance with the specified features.
   */
  public static LexicalNormalizer of(Set<Feature> features) {
    int flags = 0;
    if (features.contains(Feature.ACCENT))  flags |= ACCENT_BIT;
    if (features.contains(Feature.BRACKET)) flags |= BRACKET_BIT;
    if (features.contains(Feature.CASE))    flags |= CASE_BIT;
    if (features.contains(Feature.DASH))    flags |= DASH_BIT;
    if (features.contains(Feature.QUOTE))   flags |= QUOTE_BIT;
    if (features.contains(Feature.SPACE))   flags |= SPACE_BIT;
    if (features.contains(Feature.PUNCTUATION)) flags |= PUNCTUATION_BIT;
    return new LexicalNormalizer(flags);
  }

  /**
   * Returns a new LexicalNormalizer instance with accent folding enabled or disabled based on the input parameter.
   * Accent folding removes diacritical marks (such as accents) from characters to simplify text normalization.
   *
   * @param enabled A boolean flag indicating whether accent folding should be enabled.
   *                If true, accent folding will be applied; if false, it will be disabled.
   * @return A new LexicalNormalizer instance configured with the specified accent folding setting.
   */
  public LexicalNormalizer withAccentFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | ACCENT_BIT) : (this.flags & ~ACCENT_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with bracket folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether bracket folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified bracket folding setting.
   */
  public LexicalNormalizer withBracketFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | BRACKET_BIT) : (this.flags & ~BRACKET_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with case folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether case folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified case folding setting.
   */
  public LexicalNormalizer withCaseFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | CASE_BIT) : (this.flags & ~CASE_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with dash folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether dash folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified dash folding setting.
   */
  public LexicalNormalizer withDashFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | DASH_BIT) : (this.flags & ~DASH_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with quote folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether quote folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified quote folding setting.
   */
  public LexicalNormalizer withQuoteFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | QUOTE_BIT) : (this.flags & ~QUOTE_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with space folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether space folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified space folding setting.
   */
  public LexicalNormalizer withSpaceFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | SPACE_BIT) : (this.flags & ~SPACE_BIT));
  }

  /**
   * Returns a new LexicalNormalizer instance with punctuation folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether punctuation folding should be enabled.
   *
   * @return A new LexicalNormalizer instance configured with the specified punctuation folding setting.
   */
  public LexicalNormalizer withPunctuationFolding(boolean enabled) {
    return new LexicalNormalizer(enabled ? (this.flags | PUNCTUATION_BIT) : (this.flags & ~PUNCTUATION_BIT));
  }

  public boolean isAccentFolding()  { return this.accentFolding; }
  public boolean isBracketFolding() { return this.bracketFolding; }
  public boolean isCaseFolding()    { return this.caseFolding; }
  public boolean isDashFolding()    { return this.dashFolding; }
  public boolean isQuoteFolding()   { return this.quoteFolding; }
  public boolean isSpaceFolding()   { return this.spaceFolding; }
  public boolean isPunctuationFolding() { return this.punctuationFolding; }

  /**
   * Normalizes the specified text based on the current configuration.
   *
   * @param text The text to normalize.
   *
   * @return The normalized text.
   */
  @Override
  public String normalize(String text) {
    if (caseFolding)
      text = text.toLowerCase(Locale.ROOT);
    if (accentFolding)
      text = Normalizer.normalize(text, Normalizer.Form.NFD);

    StringBuilder out = new StringBuilder(text.length());
    boolean lastWasSpace = false;

    for (int i = 0; i < text.length();) {
      int cp = text.codePointAt(i);
      i += Character.charCount(cp);

      int type = Character.getType(cp);

      // Remove combining marks after NFD
      if (accentFolding && type == Character.NON_SPACING_MARK)
        continue;

      // Remove Zero-width spaces
      if (spaceFolding && cp == 0x200B)
        continue;

      // dash folding
      if (dashFolding && type == Character.DASH_PUNCTUATION) {
        out.append('-');
        lastWasSpace = false;
        continue;
      }

      // quote folding using Pi and Pf
      if (quoteFolding && (type == Character.INITIAL_QUOTE_PUNCTUATION || type == Character.FINAL_QUOTE_PUNCTUATION || cp == '"' || cp == '\'')) {
        out.append('\'');
        lastWasSpace = false;
        continue;
      }

      // Brackets folding using Ps and Pe
      if (bracketFolding) {
        if (type == Character.START_PUNCTUATION) {
          out.append('(');
          lastWasSpace = false;
          continue;
        }
        if (type == Character.END_PUNCTUATION) {
          out.append(')');
          lastWasSpace = false;
          continue;
        }
      }

      // space folding
      if (spaceFolding && (type == Character.SPACE_SEPARATOR || cp == 0xA || cp == 0xD ||cp == 0x9)) {
        if (!lastWasSpace) {
          out.append(' ');
          lastWasSpace = true;
        }
        continue;
      }

      // punctuation folding
      if (punctuationFolding && isPunctuation(cp, type)) {
        boolean onlyPunctuation = true;
        for (int j = 0; j < i; j++) {
          onlyPunctuation &= isPunctuation(text.codePointAt(j), Character.getType(text.codePointAt(j)));
        }
        if (onlyPunctuation) {
          out.append('.');
          lastWasSpace = false;
          continue;
        }
      }

      out.appendCodePoint(cp);
      lastWasSpace = false;
    }

    return spaceFolding && out.length() > 1 ? out.toString().trim() : out.toString();
  }

  private static boolean isPunctuation(int cp, int type) {
    return type == Character.OTHER_PUNCTUATION
        && (cp != '"' && cp != '#' && cp != '%' && cp != '&' && cp != '\'' && cp != '*' && cp != '@');
  }
}
