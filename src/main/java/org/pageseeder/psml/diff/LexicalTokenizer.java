package org.pageseeder.psml.diff;

import org.pageseeder.diffx.load.text.TextTokenizer;
import org.pageseeder.diffx.token.TextToken;

import java.util.*;
import java.util.function.IntPredicate;

/**
 * Tokenizer that splits text into lexical tokens.
 *
 * <p>Tokens can be split into words, numbers, and other types of lexical units, with support for Unicode spaces
 * and custom normalization functions.</p>
 *
 * <p>By default, the tokenizer uses Unicode spaces for tokenization.</p>
 *
 * @author Christophe Lauret
 *
 * @since 1.7.2
 * @version 1.7.2
 */
public final class LexicalTokenizer implements TextTokenizer {

  /**
   * The token pool for reusing tokens when they are identical.
   */
  private final Map<String, LexicalToken> pool = new HashMap<>();

  /**
   * The normalizer to apply to the text when generating lexical tokens.
   */
  private final TextNormalizer normalizer;

  /**
   * Whether to use Unicode spaces for tokenization or XML spaces.
   */
  private boolean useUnicodeSpace = false;

  /**
   * Predicate to determine if a character is a space character.
   */
  private IntPredicate isSpace = LexicalTokenizer::isXMLSpace;

  /**
   * Creates a new tokenizer with no normalization.
   */
  public LexicalTokenizer() {
    this.normalizer = t -> t;
  }

  /**
   * Creates a new tokenizer with a custom normalizer.
   *
   * @param normalizer The custom normalizer to apply to the text when generating lexical tokens.
   */
  public LexicalTokenizer(TextNormalizer normalizer) {
    this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
  }

  /**
   * Sets whether to use Unicode spaces for tokenization or XML spaces.
   *
   * @param useUnicodeSpace whether to use Unicode spaces for tokenization or XML spaces
   */
  public void setUseUnicodeSpace(boolean useUnicodeSpace) {
    this.useUnicodeSpace = useUnicodeSpace;
    this.isSpace = useUnicodeSpace ? LexicalTokenizer::isUnicodeSpace : LexicalTokenizer::isXMLSpace;
  }

  /**
   * Returns whether to use Unicode spaces for tokenization or XML spaces.
   *
   * @return whether to use Unicode spaces for tokenization or XML spaces
   */
  public boolean useUnicodeSpace() {
    return useUnicodeSpace;
  }

  /**
   * Determines if a character is a space character.
   *
   * @param cp the character to check
   * @return true if the character is a space character, false otherwise
   */
  private boolean isSpace(int cp) {
    return this.isSpace.test(cp);
  }

  @Override
  public List<TextToken> tokenize(CharSequence s) {
    List<TextToken> out = new ArrayList<>();
    int i = 0;
    int n = s.length();

    while (i < n) {

      // collect leading whitespace (Unicode aware)
      int wsStart = i;
      while (i < n) {
        int cp = codePointAt(s, i);
        if (!isSpace(cp)) break;
        i += Character.charCount(cp);
      }
      String leading = s.subSequence(wsStart, i).toString();
      if (i >= n) {
        // Trailing whitespace
        emit(out, leading, s, i, i, normalizer);
        break;
      }

      int tokenStart = i;
      int cp = codePointAt(s, i);

      // URL
      if (startsWithUrl(s, i)) {
        i = readUntilSpace(s, i);
        emit(out, leading, s, tokenStart, i, normalizer);
        continue;
      }

      // Numbers
      if (isNumberStart(s, i)) {
        i = readNumberWithUnit(s, i);
        emit(out, leading, s, tokenStart, i, normalizer);
        continue;
      }

      // Words
      if (Character.isLetter(cp)) {
        i = readWord(s, i);
        emit(out, leading, s, tokenStart, i, normalizer);
        continue;
      }

      // single codepoint token
      i += Character.charCount(cp);
      emit(out, leading, s, tokenStart, i, normalizer);

    }

    return out;
  }

  public static List<TextToken> tokenize(CharSequence s, TextNormalizer normalizer) {
    LexicalTokenizer tokenizer = new LexicalTokenizer(normalizer);
    return tokenizer.tokenize(s);
  }

  private void emit(List<TextToken> out, String leading, CharSequence s, int start, int end, TextNormalizer norm) {
    String original = leading + s.subSequence(start, end);
    LexicalToken token = this.pool.computeIfAbsent(original, text -> new LexicalToken(text, norm.normalize(text)));
    out.add(token);
  }

  private static int codePointAt(CharSequence s, int i) {
    char c1 = s.charAt(i);
    if (Character.isHighSurrogate(c1) && i + 1 < s.length()) {
      char c2 = s.charAt(i + 1);
      if (Character.isLowSurrogate(c2)) {
        return Character.toCodePoint(c1, c2);
      }
    }
    return c1;
  }

  /**
   * Determines whether the provided code point is classified as an XML space character.
   *
   * <p>The method checks if the code point corresponds to a NEWLINE, CARRIAGE_RETURN,
   * TAB, or SPACE character.
   *
   * @param cp the Unicode code point to check
   * @return {@code true} if the code point is recognized as an XML space character,
   *         {@code false} otherwise
   */
  static boolean isXMLSpace(int cp) {
    return cp == 0x20 || cp == 0xA || cp == 0x9 || cp == 0xD;
  }

  /**
   * Determines whether the provided code point is classified as a Unicode space character.
   *
   * <p>The method checks if the code point corresponds to a SPACE_SEPARATOR, LINE_SEPARATOR,
   * or a PARAGRAPH_SEPARATOR.
   *
   * @param cp the Unicode code point to check
   * @return {@code true} if the code point is recognized as a Unicode space character,
   *         {@code false} otherwise
   */
  static boolean isUnicodeSpace(int cp) {
    if (isXMLSpace(cp)) return true;
    int t = Character.getType(cp);
    return t == Character.SPACE_SEPARATOR
        || t == Character.LINE_SEPARATOR
        || t == Character.PARAGRAPH_SEPARATOR;
  }

  /**
   * Checks if the given character sequence starts with a URL-like prefix at the specified index.
   *
   * <p>The method considers the prefixes "http://", "https://", and "file://".
   *
   * @param s the character sequence to check for a URL-like prefix
   * @param i the starting index in the character sequence to perform the check
   * @return {@code true} if the character sequence starts with a URL-like prefix at the specified index,
   *         {@code false} otherwise
   */
  private static boolean startsWithUrl(CharSequence s, int i) {
    return regionMatches(s, i, "http://")
        || regionMatches(s, i, "file://")
        || regionMatches(s, i, "https://");
  }

  private static boolean regionMatches(CharSequence s, int i, String lit) {
    if (i + lit.length() > s.length()) return false;
    for (int k = 0; k < lit.length(); k++) {
      if (s.charAt(i + k) != lit.charAt(k)) return false;
    }
    return true;
  }

  /**
   * Determines whether a character sequence appears to start with a number at a specified index.
   *
   * <p>The method checks if the character at the given index is a digit or,
   * optionally, a '+' or '-' sign followed by a digit.
   *
   * @param s the character sequence to inspect
   * @param i the index in the character sequence to check
   * @return {@code true} if the character sequence starts with a number at the specified index,
   *         {@code false} otherwise
   */
  private static boolean isNumberStart(CharSequence s, int i) {
    int cp = codePointAt(s, i);
    if (Character.isDigit(cp)) return true;

    if ((cp == '+' || cp == '-' || Character.getType(cp) == Character.CURRENCY_SYMBOL) && i < s.length()) {
      int j = i + Character.charCount(cp);
      if (j < s.length()) {
        return Character.isDigit(codePointAt(s, j));
      }
    }
    return false;
  }

  /**
   * Reads characters in the given character sequence starting from the specified index
   * until a Unicode space character or the end of the sequence is encountered.
   *
   * @param s the character sequence to be scanned
   * @param i the starting index from which to begin reading
   * @return the index of the first Unicode space character encountered or the length of the sequence
   *         if no space characters are found
   */
  private int readUntilSpace(CharSequence s, final int i) {
    int n = s.length();
    int j = i;
    while (j < n && !isSpace(codePointAt(s, j))) {
      j += Character.charCount(codePointAt(s, j));
    }
    return j;
  }

  /**
   * Reads a number, optional decimal point, unit characters, or percentage sign
   * starting from the given index within the specified character sequence.
   *
   * @param s the character sequence to be scanned for a number or number with a unit
   * @param i the starting index in the character sequence to begin reading
   * @return the index after the last character that forms part of the number or unit
   */
  private static int readNumberWithUnit(CharSequence s, final int i) {
    int n = s.length();
    int j = i;

    int cp = codePointAt(s, j);
    if (cp == '+' || cp == '-' || cp == '$') {
      j += Character.charCount(cp);
    }

    while (j < n && (Character.isDigit(codePointAt(s, j)) || codePointAt(s, j) == ',')) {
      j += Character.charCount(codePointAt(s, j));
    }

    if (j < n && (codePointAt(s, j) == '.')) {
      j++;
      while (j < n && Character.isDigit(codePointAt(s, j))) {
        j += Character.charCount(codePointAt(s, j));
      }
    }

    while (j < n && Character.isLetter(codePointAt(s, j))) {
      j += Character.charCount(codePointAt(s, j));
    }

    if (j < n && codePointAt(s, j) == '%') j++;

    return j;
  }


  private static int readWord(CharSequence s, final int i) {
    int n = s.length();
    int j = i;

    // Skip the first character (always a letter)
    int firstCp = codePointAt(s, j);
    j += Character.charCount(firstCp);

    // Track the last position (index) right after a letter/digit,
    // so the word is guaranteed to end with [A-Za-z0-9] (Unicode-aware).
    int lastLetterOrDigitEnd = j;

    while (j < n) {
      int c = codePointAt(s, j);

      boolean allowed = Character.isLetter(c) || Character.isDigit(c)
          || c == '\'' || c == '-' || c == '&' || c == '.' || c == '/' || c == '_' || c == '@' || c == '\u200B';
      if (!allowed) break;

      j += Character.charCount(c);

      // We must end with a letter or digit
      if (Character.isLetter(c) || Character.isDigit(c)) {
        lastLetterOrDigitEnd = j;
      }
    }

    return lastLetterOrDigitEnd;
  }

}
