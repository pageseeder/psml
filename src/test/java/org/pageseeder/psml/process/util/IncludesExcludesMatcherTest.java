package org.pageseeder.psml.process.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncludesExcludesMatcherTest {

  /**
   * Tests for the createRegex method of the IncludesExcludesMatcher class.
   * The createRegex method transforms a pattern with wildcards into a valid
   * regular expression that can be used to match paths.
   */

  @Test
  void testCreateRegex_forBasicWildcardPattern() {
    assertCreateRegexEquals("^([^/]*?)\\.txt$", "*.txt");
  }

  @Test
  void testCreateRegex_forDoubleAsteriskWildcard() {
    assertCreateRegexEquals("^(.*?)/([^/]*?)\\.txt$", "**/*.txt");
  }

  @Test
  void testCreateRegex_forPatternWithSpecialCharacters() {
    assertCreateRegexEquals("^file\\.\\(txt|csv\\)$", "file.(txt|csv)");
  }

  @Test
  void testCreateRegex_forComplexPattern() {
    assertCreateRegexEquals("^/home/(.*?)/files/([^/]*?)\\.log$", "/home/**/files/*.log");
  }

  @Test
  void testCreateRegex_forEmptyPattern() {
    assertCreateRegexEquals("^$", "");
  }

  @Test
  void testCreateRegex_forPatternWithoutWildcards() {
    assertCreateRegexEquals("^example\\.txt$", "example.txt");
  }

  @Test
  void testCreateRegex_forPatternWithOnlyDoubleAsterisk() {
    assertCreateRegexEquals("^(.*?)$", "**");
  }

  @Test
  void testCreateRegex_forPatternWithOnlySingleAsterisk() {
    assertCreateRegexEquals("^([^/]*?)$", "*");
  }


  void assertCreateRegexEquals(String expected, String pattern) {
    String regex = IncludesExcludesMatcher.createRegex(pattern);
    assertEquals(expected, regex);
  }
}