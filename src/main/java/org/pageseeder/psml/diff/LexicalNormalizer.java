package org.pageseeder.psml.diff;

import java.text.Normalizer;
import java.util.Collections;
import java.util.EnumSet;
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

  private static final LexicalNormalizer NONE_INSTANCE = new LexicalNormalizer(Set.of());
  private static final LexicalNormalizer ALL_INSTANCE  = new LexicalNormalizer(NormalizationFeature.all());

  private final EnumSet<NormalizationFeature> features;

  private final boolean bracketFolding;
  private final boolean caseFolding;
  private final boolean dashFolding;
  private final boolean diacriticFolding;
  private final boolean punctuationFolding;
  private final boolean quoteFolding;
  private final boolean xmlSpaceFolding;
  private final boolean unicodeSpaceFolding;

  private LexicalNormalizer(Set<NormalizationFeature> features) {
    // defensive copy to preserve immutability even if the caller mutates its EnumSet later
    this.features = features.isEmpty() ? EnumSet.noneOf(NormalizationFeature.class) : EnumSet.copyOf(features);
    this.bracketFolding = this.features.contains(NormalizationFeature.BRACKET_FOLDING);
    this.caseFolding = this.features.contains(NormalizationFeature.CASE_FOLDING);
    this.dashFolding = this.features.contains(NormalizationFeature.DASH_FOLDING);
    this.diacriticFolding = this.features.contains(NormalizationFeature.DIACRITIC_FOLDING);
    this.punctuationFolding = this.features.contains(NormalizationFeature.PUNCTUATION_FOLDING);
    this.quoteFolding = this.features.contains(NormalizationFeature.QUOTE_FOLDING);
    this.xmlSpaceFolding = this.features.contains(NormalizationFeature.XML_SPACE_FOLDING);
    this.unicodeSpaceFolding = this.features.contains(NormalizationFeature.UNICODE_WHITESPACE_FOLDING);
  }

  /**
   * Returns a normalizer that does nothing.
   *
   * @return A LexicalNormalizer instance that applies no normalization features.
   */
  public static LexicalNormalizer none() {
    return NONE_INSTANCE;
  }

  /**
   * Returns a normalizer that applies all the available normalization features.
   *
   * @return A LexicalNormalizer instance that applies all available normalization features.
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
  public static LexicalNormalizer of(Set<NormalizationFeature> features) {
    if (features.isEmpty()) return NONE_INSTANCE;
    if (features.containsAll(NormalizationFeature.all())) return ALL_INSTANCE;
    return new LexicalNormalizer(features);
  }

  /**
   * Returns true if this normalizer has the given feature enabled.
   */
  public boolean hasFeature(NormalizationFeature feature) {
    return this.features.contains(feature);
  }

  /**
   * Returns a defensive copy of the enabled features.
   *
   * <p>Note: the returned set is mutable, but mutating it will not affect this instance.</p>
   */
  public Set<NormalizationFeature> features() {
    if (this.features.isEmpty()) return Set.of();
    return Collections.unmodifiableSet(EnumSet.copyOf(this.features));
  }

  /**
   * Returns a new normalizer with the given feature enabled (or this instance if already enabled).
   *
   * @param feature The normalization feature to enable.
   *
   * @return A new normalizer with the specified feature enabled, or this instance if already enabled.
   */
  public LexicalNormalizer withFeature(NormalizationFeature feature) {
    if (this.features.contains(feature)) return this;
    if (this.features.isEmpty()) return of(Set.of(feature));
    EnumSet<NormalizationFeature> set = EnumSet.copyOf(this.features);
    set.add(feature);
    return of(set);
  }

  /**
   * Returns a new normalizer with the given feature disabled (or this instance if already disabled).
   *
   * @param feature The normalization feature to disable.
   *
   * @return A new normalizer with the specified feature disabled, or this instance if already disabled.
   */
  public LexicalNormalizer withoutFeature(NormalizationFeature feature) {
    if (!this.features.contains(feature)) return this;
    EnumSet<NormalizationFeature> set = EnumSet.copyOf(this.features);
    set.remove(feature);
    return of(set);
  }

  /**
   * Returns a new normalizer with all the given features enabled.
   * If all are already enabled, returns {@code this}.
   *
   * @param features The normalization features to enable.
   *
   * @return A new normalizer with the specified features enabled, or this instance if already enabled.
   */
  public LexicalNormalizer withFeatures(Set<NormalizationFeature> features) {
    if (features.isEmpty()) return this;
    if (this.features.containsAll(features)) return this;
    if (this.features.isEmpty()) return of(features);
    EnumSet<NormalizationFeature> set = EnumSet.copyOf(this.features);
    set.addAll(features);
    return of(set);
  }

  /**
   * Returns a new normalizer with all the given features disabled.
   * If none are enabled, returns {@code this}.
   *
   * @param features The normalization features to disable.
   *
   * @return A new normalizer with the specified features disabled, or this instance if already disabled.
   */
  public LexicalNormalizer withoutFeatures(Set<NormalizationFeature> features) {
    if (features.isEmpty() || this.features.isEmpty()) return this;
    if (Collections.disjoint(this.features, features)) return this;
    EnumSet<NormalizationFeature> set = EnumSet.copyOf(this.features);
    set.removeAll(features);
    return of(set);
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
    return enabled ? withFeature(NormalizationFeature.DIACRITIC_FOLDING) : withoutFeature(NormalizationFeature.DIACRITIC_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with bracket folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether bracket folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified bracket folding setting.
   */
  public LexicalNormalizer withBracketFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.BRACKET_FOLDING) : withoutFeature(NormalizationFeature.BRACKET_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with case folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether case folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified case folding setting.
   */
  public LexicalNormalizer withCaseFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.CASE_FOLDING) : withoutFeature(NormalizationFeature.CASE_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with dash folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether dash folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified dash folding setting.
   */
  public LexicalNormalizer withDashFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.DASH_FOLDING) : withoutFeature(NormalizationFeature.DASH_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with quote folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether quote folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified quote folding setting.
   */
  public LexicalNormalizer withQuoteFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.QUOTE_FOLDING) : withoutFeature(NormalizationFeature.QUOTE_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with space folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether space folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified space folding setting.
   */
  public LexicalNormalizer withSpaceFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.XML_SPACE_FOLDING) : withoutFeature(NormalizationFeature.XML_SPACE_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with Unicode space folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether space folding should be enabled.
   * @return A new LexicalNormalizer instance configured with the specified space folding setting.
   */
  public LexicalNormalizer withUnicodeSpaceFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.UNICODE_WHITESPACE_FOLDING) : withoutFeature(NormalizationFeature.UNICODE_WHITESPACE_FOLDING);
  }

  /**
   * Returns a new LexicalNormalizer instance with punctuation folding enabled or disabled based on the input parameter.
   *
   * @param enabled A boolean flag indicating whether punctuation folding should be enabled.
   *
   * @return A new LexicalNormalizer instance configured with the specified punctuation folding setting.
   */
  public LexicalNormalizer withPunctuationFolding(boolean enabled) {
    return enabled ? withFeature(NormalizationFeature.PUNCTUATION_FOLDING) : withoutFeature(NormalizationFeature.PUNCTUATION_FOLDING);
  }

  /**
   * Indicate whether accent folding is enabled.
   *
   * @return True if accent folding is enabled, false otherwise.
   */
  public boolean isDiacriticFolding()  { return this.diacriticFolding; }

  /**
   * Indicates whether bracket folding is enabled.
   *
   * @return True if bracket folding is enabled, false otherwise.
   */
  public boolean isBracketFolding() { return this.bracketFolding; }

  /**
   * Indicates whether case folding is enabled.
   *
   * @return True if case folding is enabled, false otherwise.
   */
  public boolean isCaseFolding()    { return this.caseFolding; }

  /**
   * Indicates whether dash folding is enabled.
   *
   * @return True if dash folding is enabled, false otherwise.
   */
  public boolean isDashFolding()    { return this.dashFolding; }

  /**
   * Indicates whether punctuation folding is enabled.
   *
   * @return True if punctuation folding is enabled, false otherwise.
   */
  public boolean isQuoteFolding()   { return this.quoteFolding; }

  /**
   * Indicates whether space folding is enabled.
   *
   * @return True if space folding is enabled, false otherwise.
   */
  public boolean isXmlSpaceFolding()   { return this.xmlSpaceFolding; }

  /**
   * Indicates whether punctuation folding is enabled.
   *
   * @return True if punctuation folding is enabled, false otherwise.
   */
  public boolean isPunctuationFolding() { return this.punctuationFolding; }

  /**
   * Indicates whether Unicode space folding is enabled.
   *
   * @return True if Unicode space folding is enabled, false otherwise.
   */
  public boolean isUnicodeWhitespaceFolding()   { return this.unicodeSpaceFolding; }

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
    if (diacriticFolding)
      text = Normalizer.normalize(text, Normalizer.Form.NFD);

    StringBuilder out = new StringBuilder(text.length());
    boolean lastWasSpace = false;

    for (int i = 0; i < text.length();) {
      int cp = text.codePointAt(i);
      i += Character.charCount(cp);

      // Remove Zero-width spaces
      if (xmlSpaceFolding && cp == 0x200B)
        continue;

      int type = Character.getType(cp);

      // Remove combining marks after NFD
      if (diacriticFolding && type == Character.NON_SPACING_MARK)
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
      if (xmlSpaceFolding && LexicalTokenizer.isXMLSpace(cp) || unicodeSpaceFolding && isUnicodeSpace(cp, type)) {
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

    return (xmlSpaceFolding || unicodeSpaceFolding) && out.length() > 1 ? out.toString().trim() : out.toString();
  }

  private static boolean isPunctuation(int cp, int type) {
    return type == Character.OTHER_PUNCTUATION
        && (cp != '"' && cp != '#' && cp != '%' && cp != '&' && cp != '\'' && cp != '*' && cp != '@');
  }

  private static boolean isUnicodeSpace(int cp, int type) {
    return type == Character.SPACE_SEPARATOR
        || cp == 0xA  // NEWLINE
        || cp == 0xD  // CARRIAGE RETURN
        || cp == 0x9  // TAB
        || type == Character.LINE_SEPARATOR
        || type == Character.PARAGRAPH_SEPARATOR;

  }

}
