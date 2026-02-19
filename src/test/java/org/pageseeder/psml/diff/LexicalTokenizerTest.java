package org.pageseeder.psml.diff;

import org.junit.jupiter.api.Test;
import org.pageseeder.diffx.load.text.TextTokenizer;
import org.pageseeder.diffx.token.TextToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexicalTokenizerTest {

  /**
   * Tests that a <code>NullPointerException</code> is thrown for a </code>null</code>
   * character sequence.
   */
  @Test
  void testNullConstructor() {
    //noinspection DataFlowIssue
    assertThrows(NullPointerException.class, () -> new LexicalTokenizer(null));
  }

  @Test
  void testNull() {
    TextTokenizer tokenizer = new LexicalTokenizer(t->t);
    //noinspection DataFlowIssue
    assertThrows(NullPointerException.class, () -> tokenizer.tokenize(null));
  }

  /**
   * Tests that an empty array is returned for empty string.
   */
  @Test
  void testEmpty() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("", t -> t);
    assertEquals(0, tokens.size());
  }

  @Test
  void testChar() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("a", t -> t);
    assertEquals(toLexicalTokens("a"), tokens);
  }

  @Test
  void testCharWithLeadingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" a", t -> t);
    assertEquals(toLexicalTokens(" a"), tokens);
  }

  @Test
  void testCharWithTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("a ", t -> t);
    assertEquals(toLexicalTokens("a", " "), tokens);
  }

  @Test
  void testCharWithLeadingTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" a ", t -> t);
    assertEquals(toLexicalTokens(" a", " "), tokens);
  }

  @Test
  void testWord() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("story", t -> t);
    assertEquals(toLexicalTokens("story"), tokens);
  }

  @Test
  void testWordWithLeadingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" story", t -> t);
    assertEquals(toLexicalTokens(" story"), tokens);
  }

  @Test
  void testWordWithTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("story ", t -> t);
    assertEquals(toLexicalTokens("story", " "), tokens);
  }

  @Test
  void testWordWithLeadingTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" story ", t -> t);
    assertEquals(toLexicalTokens(" story", " "), tokens);
  }

  @Test
  void testWords() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A great story", t -> t);
    assertEquals(toLexicalTokens("A", " great", " story"), tokens);
  }

  @Test
  void testWordsWithLeadingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" A great story", t -> t);
    assertEquals(toLexicalTokens(" A", " great", " story"), tokens);
  }

  @Test
  void testWordsWithTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A great story ", t -> t);
    assertEquals(toLexicalTokens("A", " great", " story", " "), tokens);
  }

  @Test
  void testWordsWithLeadingTrailingSpace() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" A great story ", t -> t);
    assertEquals(toLexicalTokens(" A", " great", " story", " "), tokens);
  }

  @Test
  void testWordsWithPunctuation1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize(" A great story!", t -> t);
    assertEquals(toLexicalTokens(" A", " great", " story", "!"), tokens);
  }

  @Test
  void testWordsWithPunctuation2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("Blue, white, and red.", t -> t);
    assertEquals(toLexicalTokens("Blue", ",", " white", ",", " and", " red", "."), tokens);
  }

  @Test
  void testNumbers1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1", t -> t);
    assertEquals(toLexicalTokens("1"), tokens);
  }

  @Test
  void testNumbers2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1.0", t -> t);
    assertEquals(toLexicalTokens("1.0"), tokens);
  }

  @Test
  void testNumbers3() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("123.50", t -> t);
    assertEquals(toLexicalTokens("123.50"), tokens);
  }

  @Test
  void testNumbers4() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1,000", t -> t);
    assertEquals(toLexicalTokens("1,000"), tokens);
  }

  @Test
  void testNumbers5() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1,000,000.00", t -> t);
    assertEquals(toLexicalTokens("1,000,000.00"), tokens);
  }

  @Test
  void testNumbersUnit1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("10mg", t -> t);
    assertEquals(toLexicalTokens("10mg"), tokens);
  }

  @Test
  void testNumbersUnit2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1.25g", t -> t);
    assertEquals(toLexicalTokens("1.25g"), tokens);
  }

  @Test
  void testNumbersUnit3() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1,500µg", t -> t);
    assertEquals(toLexicalTokens("1,500µg"), tokens);
  }

  @Test
  void testNumbersOrdinal() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("1st 2nd 3rd", t -> t);
    assertEquals(toLexicalTokens("1st", " 2nd", " 3rd"), tokens);
  }

  @Test
  void testNumbersPrice() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("$1.25", t -> t);
    assertEquals(toLexicalTokens("$1.25"), tokens);
  }

  @Test
  void testNumbersPercent() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("33.33%", t -> t);
    assertEquals(toLexicalTokens("33.33%"), tokens);
  }

  @Test
  void testEmail() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("john.obrien@example.com", t -> t);
    assertEquals(toLexicalTokens("john.obrien@example.com"), tokens);
  }

  @Test
  void testWords_Hyphenated() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A red-hot x-ray", t -> t);
    assertEquals(toLexicalTokens("A", " red-hot", " x-ray"), tokens);
  }

  @Test
  void testWords_Contraction() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("I don't know O'Sullivan", t -> t);
    assertEquals(toLexicalTokens("I"," don't", " know", " O'Sullivan"), tokens);
  }

  @Test
  void testWords_Ampersand() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("AT&T", t -> t);
    assertEquals(toLexicalTokens("AT&T"), tokens);
  }

  @Test
  void testWords_Underscore() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("user_name", t -> t);
    assertEquals(toLexicalTokens("user_name"), tokens);
  }

  @Test
  void testMix1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("My email is bill@example.com. 10% of 24 is 2.4!", t -> t);
    assertEquals(toLexicalTokens("My", " email", " is", " bill@example.com", ".", " 10%", " of", " 24", " is", " 2.4", "!"), tokens);
  }

  @Test
  void testMix2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("I'm Joe (joe@example.org)", t -> t);
    assertEquals(toLexicalTokens("I'm", " Joe", " (", "joe@example.org", ")"), tokens);
  }

  @Test
  void testQuoted1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("This is a \"test\"", t -> t);
    assertEquals(toLexicalTokens("This", " is", " a", " \"", "test", "\""), tokens);
  }

  @Test
  void testQuoted2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("This is a \"test", t -> t);
    assertEquals(toLexicalTokens("This", " is", " a", " \"", "test"), tokens);
  }

  @Test
  void testQuoted3() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("This is a test\"", t -> t);
    assertEquals(toLexicalTokens("This", " is", " a", " test", "\""), tokens);
  }

  @Test
  void testBracket1() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A (test)", t -> t);
    assertEquals(toLexicalTokens("A", " (", "test", ")"), tokens);
  }

  @Test
  void testBracket2() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A (test", t -> t);
    assertEquals(toLexicalTokens("A", " (", "test"), tokens);
  }

  @Test
  void testBracket3() {
    List<TextToken> tokens = LexicalTokenizer.tokenize("A test)", t -> t);
    assertEquals(toLexicalTokens("A", " test", ")"), tokens);
  }

  public static List<TextToken> toLexicalTokens(String... words) {
    Map<String, TextToken> recycling = new HashMap<>();
    List<TextToken> tokens = new ArrayList<>();
    for (String word : words) {
      TextToken t = recycling.computeIfAbsent(word, w -> new LexicalToken(w, w));
      tokens.add(t);
    }
    return tokens;
  }

}
