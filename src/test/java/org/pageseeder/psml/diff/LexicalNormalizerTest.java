package org.pageseeder.psml.diff;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexicalNormalizerTest {

  private static final List<String> DEFAULT_TEST = List.of(
      " ", " \n \t ", "\u00A0",
      "Café", "CRÊPE", "maçã",
      "-", "–", "—", "‑", "-",
      "'", "\"", "»", "“",
      "don't", "I'm",
      " item",
      "red-hot",
      "{", "]", "(",
      "bob.smith@gmail.com",
      "http://www.example.com/",
      ",", "a:b", ";", "!!!"
  );

  @Test
  void testNone() {
    LexicalNormalizer normalizer = LexicalNormalizer.none();
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(DEFAULT_TEST, got);
  }

  @Test
  void testAll() {
    List<String> exp = List.of(
        " ", " ", " ",
        "cafe", "crepe", "maca",
        "-", "-", "-", "-", "-",
        "'", "'", "'", "'",
        "don't", "i'm",
        "item",
        "red-hot",
        "(", ")", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ".", "a:b", ".", "...");
    LexicalNormalizer normalizer = LexicalNormalizer.all();
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testAccentFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "Cafe", "CREPE", "maca",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        " item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withAccentFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testBracketFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "Café", "CRÊPE", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        " item",
        "red-hot",
        "(", ")", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withBracketFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testCaseFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "café", "crêpe", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "i'm",
        " item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withCaseFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testDashFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "Café", "CRÊPE", "maçã",
        "-", "-", "-", "-", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        " item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withDashFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testQuoteFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "Café", "CRÊPE", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "'", "'", "'",
        "don't", "I'm",
        " item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withQuoteFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testXmlSpaceFolding() {
    List<String> exp = List.of(" ", " ", " ",
        "Café", "CRÊPE", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        "item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withSpaceFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testUnicodeWhitespaceFolding() {
    List<String> exp = List.of(" ", " ", " ",
        "Café", "CRÊPE", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        "item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ",", "a:b", ";", "!!!");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withUnicodeSpaceFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

  @Test
  void testPunctuationFolding() {
    List<String> exp = List.of(" ", " \n \t ", "\u00A0",
        "Café", "CRÊPE", "maçã",
        "-", "–", "—", "‑", "-",
        "'", "\"", "»", "“",
        "don't", "I'm",
        " item",
        "red-hot",
        "{", "]", "(",
        "bob.smith@gmail.com",
        "http://www.example.com/",
        ".", "a:b", ".", "...");
    LexicalNormalizer normalizer = LexicalNormalizer.none().withPunctuationFolding(true);
    List<String> got = DEFAULT_TEST.stream().map(normalizer::normalize).collect(Collectors.toList());
    assertEquals(exp, got);
  }

}
