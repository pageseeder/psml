package org.pageseeder.psml.toc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.psml.toc.PublicationNumbering.ElementName;
import org.pageseeder.psml.toc.PublicationNumbering.NumberType;
import org.pageseeder.psml.toc.PublicationNumbering.SkippedLevels;

final class PublicationNumberingTest {

  // ---------------------------------------------------------------------------
  // NumberType enum
  // ---------------------------------------------------------------------------

  @Test
  void testNumberTypeFromString_knownValues() {
    assertEquals(NumberType.DECIMAL,    NumberType.fromString("decimal"));
    assertEquals(NumberType.LOWERALPHA, NumberType.fromString("loweralpha"));
    assertEquals(NumberType.UPPERALPHA, NumberType.fromString("upperalpha"));
    assertEquals(NumberType.LOWERROMAN, NumberType.fromString("lowerroman"));
    assertEquals(NumberType.UPPERROMAN, NumberType.fromString("upperroman"));
  }

  @Test
  void testNumberTypeFromString_unknownDefaultsToDecimal() {
    assertEquals(NumberType.DECIMAL, NumberType.fromString("unknown"));
    assertEquals(NumberType.DECIMAL, NumberType.fromString(""));
  }

  @Test
  void testNumberTypeToString() {
    assertEquals("decimal",    NumberType.DECIMAL.toString());
    assertEquals("loweralpha", NumberType.LOWERALPHA.toString());
    assertEquals("upperalpha", NumberType.UPPERALPHA.toString());
    assertEquals("lowerroman", NumberType.LOWERROMAN.toString());
    assertEquals("upperroman", NumberType.UPPERROMAN.toString());
  }

  // ---------------------------------------------------------------------------
  // ElementName enum
  // ---------------------------------------------------------------------------

  @Test
  void testElementNameFromString_knownValues() {
    assertEquals(ElementName.HEADING, ElementName.fromString("heading"));
    assertEquals(ElementName.PARA,    ElementName.fromString("para"));
    assertEquals(ElementName.ANY,     ElementName.fromString("any"));
  }

  @Test
  void testElementNameFromString_unknownDefaultsToHeading() {
    assertEquals(ElementName.HEADING, ElementName.fromString("unknown"));
    assertEquals(ElementName.HEADING, ElementName.fromString(""));
  }

  @Test
  void testElementNameToString() {
    assertEquals("heading", ElementName.HEADING.toString());
    assertEquals("para",    ElementName.PARA.toString());
    assertEquals("any",     ElementName.ANY.toString());
  }

  // ---------------------------------------------------------------------------
  // SkippedLevels enum
  // ---------------------------------------------------------------------------

  @Test
  void testSkippedLevelsFromString() {
    assertEquals(SkippedLevels.ONE,  SkippedLevels.fromString("1"));
    assertEquals(SkippedLevels.ZERO, SkippedLevels.fromString("0"));
    assertEquals(SkippedLevels.STRIP, SkippedLevels.fromString("strip"));
  }

  @Test
  void testSkippedLevelsFromString_unknownDefaultsToOne() {
    assertEquals(SkippedLevels.ONE, SkippedLevels.fromString("unknown"));
    assertEquals(SkippedLevels.ONE, SkippedLevels.fromString(""));
  }

  // ---------------------------------------------------------------------------
  // Label and SkippedLevels accessors
  // ---------------------------------------------------------------------------

  @Test
  void testSetGetLabel() {
    PublicationNumbering pn = new PublicationNumbering();
    assertEquals("", pn.getLabel());
    pn.setLabel("chapter");
    assertEquals("chapter", pn.getLabel());
  }

  @Test
  void testSetLabelNull_throws() {
    PublicationNumbering pn = new PublicationNumbering();
    assertThrows(IllegalArgumentException.class, () -> pn.setLabel(null));
  }

  @Test
  void testSetGetSkippedLevels() {
    PublicationNumbering pn = new PublicationNumbering();
    assertEquals(SkippedLevels.ONE, pn.getSkippedLevels());
    pn.setSkippedLevels(SkippedLevels.ZERO);
    assertEquals(SkippedLevels.ZERO, pn.getSkippedLevels());
    pn.setSkippedLevels(SkippedLevels.STRIP);
    assertEquals(SkippedLevels.STRIP, pn.getSkippedLevels());
  }

  // ---------------------------------------------------------------------------
  // addNumberFormat / getNumberFormat
  // ---------------------------------------------------------------------------

  @Test
  void testAddGetNumberFormat() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addNumberFormat(1, null, "[1].");
    assertEquals("[1].", pn.getNumberFormat(1, ""));
    assertNull(pn.getNumberFormat(2, ""));
  }

  @Test
  void testAddGetNumberFormat_withBlocklabel() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addNumberFormat(2, "chapter", "[1].[2].");
    assertEquals("[1].[2].", pn.getNumberFormat(2, "chapter"));
    assertNull(pn.getNumberFormat(2, ""));
  }

  // ---------------------------------------------------------------------------
  // addNumberType / getNumberType
  // ---------------------------------------------------------------------------

  @Test
  void testGetNumberType_fallsBackToNoBlocklabel() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addNumberType(1, null, "lowerroman");
    // exact blocklabel match
    assertEquals(NumberType.LOWERROMAN, pn.getNumberType(1, ""));
    // unknown blocklabel falls back to no-blocklabel entry
    assertEquals(NumberType.LOWERROMAN, pn.getNumberType(1, "chapter"));
  }

  @Test
  void testGetNumberType_blocklabelTakesPrecedence() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addNumberType(1, null, "decimal");
    pn.addNumberType(1, "chapter", "upperalpha");
    assertEquals(NumberType.UPPERALPHA, pn.getNumberType(1, "chapter"));
    assertEquals(NumberType.DECIMAL,    pn.getNumberType(1, ""));
  }

  @Test
  void testGetNumberType_returnsNullWhenNotConfigured() {
    PublicationNumbering pn = new PublicationNumbering();
    assertNull(pn.getNumberType(1, ""));
  }

  // ---------------------------------------------------------------------------
  // addRestart / hasRestart / hasRestarts
  // ---------------------------------------------------------------------------

  @Test
  void testHasRestarts_emptyByDefault() {
    assertFalse(new PublicationNumbering().hasRestarts());
  }

  @Test
  void testAddHasRestart() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addRestart(2, "chapter");
    assertTrue(pn.hasRestarts());
    assertTrue(pn.hasRestart(2, "chapter"));
    assertFalse(pn.hasRestart(2, ""));
    assertFalse(pn.hasRestart(1, "chapter"));
  }

  @Test
  void testAddRestart_nullBlocklabel() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addRestart(1, null);
    assertTrue(pn.hasRestart(1, ""));
  }

  // ---------------------------------------------------------------------------
  // hasScheme
  // ---------------------------------------------------------------------------

  @Test
  void testHasScheme_exactMatch() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addElement(1, "chapter", "heading");
    assertTrue(pn.hasScheme(1, "chapter", "heading"));
    assertFalse(pn.hasScheme(1, "chapter", "para"));
    assertFalse(pn.hasScheme(2, "chapter", "heading"));
  }

  @Test
  void testHasScheme_any_matchesAll() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addElement(1, null, "any");
    assertTrue(pn.hasScheme(1, "", "heading"));
    assertTrue(pn.hasScheme(1, "", "para"));
  }

  @Test
  void testHasScheme_fallsBackToNoBlocklabel() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addElement(1, null, "heading");
    // blocklabel "chapter" has no entry → should fall back to no-blocklabel entry
    assertTrue(pn.hasScheme(1, "chapter", "heading"));
    assertFalse(pn.hasScheme(1, "chapter", "para"));
  }

  @Test
  void testHasScheme_returnsFalseWhenNotConfigured() {
    assertFalse(new PublicationNumbering().hasScheme(1, "", "heading"));
  }

  // ---------------------------------------------------------------------------
  // numbering – DECIMAL and zero
  // ---------------------------------------------------------------------------

  @Test
  void testNumberingDecimal() {
    assertEquals("0",  PublicationNumbering.numbering(0, NumberType.DECIMAL));
    assertEquals("1",  PublicationNumbering.numbering(1, NumberType.DECIMAL));
    assertEquals("10", PublicationNumbering.numbering(10, NumberType.DECIMAL));
  }

  @Test
  void testNumberingZero_alwaysReturnsZero() {
    // value == 0 short-circuits to "0" regardless of type
    assertEquals("0", PublicationNumbering.numbering(0, NumberType.LOWERALPHA));
    assertEquals("0", PublicationNumbering.numbering(0, NumberType.LOWERROMAN));
  }

  // ---------------------------------------------------------------------------
  // getPrefix – no scheme (canonical pass-through)
  // ---------------------------------------------------------------------------

  @Test
  void testGetPrefix_noScheme_canonical() {
    PublicationNumbering pn = new PublicationNumbering();
    Prefix p = pn.getPrefix("1.", "");
    assertEquals("1.", p.value);
    assertEquals("1.", p.canonical);
    assertEquals(1,    p.level);
    assertNull(p.parentNumber);
  }

  @Test
  void testGetPrefix_noScheme_multiLevel() {
    PublicationNumbering pn = new PublicationNumbering();
    Prefix p = pn.getPrefix("2.3", "");
    assertEquals("2.3.", p.value);
    assertEquals("2.3",  p.canonical);
    assertEquals(2,      p.level);
  }

  // ---------------------------------------------------------------------------
  // getPrefix – with scheme
  // ---------------------------------------------------------------------------

  @Test
  void testGetPrefix_withScheme_singleLevel() {
    PublicationNumbering pn = new PublicationNumbering();
    // separators go inside the brackets: "[1.]" → "3."
    pn.addNumberFormat(1, null, "[1.]");
    Prefix p = pn.getPrefix("3.", "");
    assertEquals("3.", p.value);
    assertEquals("3.", p.canonical);
    assertEquals(1,    p.level);
    assertNull(p.parentNumber);
  }

  @Test
  void testGetPrefix_withScheme_multiLevel() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.addNumberFormat(2, null, "[1.][2.]");
    Prefix p = pn.getPrefix("2.3", "");
    assertEquals("2.3.", p.value);
    assertEquals(2,      p.level);
  }

  @Test
  void testGetPrefix_withScheme_parentNumber() {
    PublicationNumbering pn = new PublicationNumbering();
    // level-2 scheme references only level-2 → level-1 is computed as parentNumber
    pn.addNumberFormat(1, null, "[1.]");
    pn.addNumberFormat(2, null, "[2.]");
    Prefix p = pn.getPrefix("2.3", "");
    assertEquals("3.", p.value);
    assertEquals("2.", p.parentNumber);
  }

  // ---------------------------------------------------------------------------
  // getPrefix – SkippedLevels.STRIP
  // ---------------------------------------------------------------------------

  @Test
  void testGetPrefix_stripSkippedLevels_noScheme() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.setSkippedLevels(SkippedLevels.STRIP);
    // "0.3." → leading zero level stripped → "3."
    Prefix p = pn.getPrefix("0.3", "");
    assertEquals("3.", p.value);
  }

  @Test
  void testGetPrefix_stripSkippedLevels_middleZero_noScheme() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.setSkippedLevels(SkippedLevels.STRIP);
    // "2.0.1." → middle zero stripped → "2.1."
    Prefix p = pn.getPrefix("2.0.1", "");
    assertEquals("2.1.", p.value);
  }

  @Test
  void testGetPrefix_stripSkippedLevels_withScheme() {
    PublicationNumbering pn = new PublicationNumbering();
    pn.setSkippedLevels(SkippedLevels.STRIP);
    // scheme references levels 1 and 2; value at level 2 is 0 → stripped
    pn.addNumberFormat(2, null, "[1.][2.]");
    Prefix p = pn.getPrefix("3.0", "");
    assertEquals("3.", p.value);
  }

  @Test
  void testLowerRoman() {
    assertEquals("i",    PublicationNumbering.numbering(1, NumberType.LOWERROMAN));
    assertEquals("ii",   PublicationNumbering.numbering(2, NumberType.LOWERROMAN));
    assertEquals("iii",  PublicationNumbering.numbering(3, NumberType.LOWERROMAN));
    assertEquals("iv",   PublicationNumbering.numbering(4, NumberType.LOWERROMAN));
    assertEquals("v",    PublicationNumbering.numbering(5, NumberType.LOWERROMAN));
    assertEquals("vi",   PublicationNumbering.numbering(6, NumberType.LOWERROMAN));
    assertEquals("vii",  PublicationNumbering.numbering(7, NumberType.LOWERROMAN));
    assertEquals("viii", PublicationNumbering.numbering(8, NumberType.LOWERROMAN));
    assertEquals("ix",   PublicationNumbering.numbering(9, NumberType.LOWERROMAN));
    assertEquals("x",    PublicationNumbering.numbering(10, NumberType.LOWERROMAN));
    assertEquals("xi",    PublicationNumbering.numbering(11, NumberType.LOWERROMAN));
    assertEquals("xii",   PublicationNumbering.numbering(12, NumberType.LOWERROMAN));
    assertEquals("xiii",  PublicationNumbering.numbering(13, NumberType.LOWERROMAN));
    assertEquals("xiv",   PublicationNumbering.numbering(14, NumberType.LOWERROMAN));
    assertEquals("xv",    PublicationNumbering.numbering(15, NumberType.LOWERROMAN));
    assertEquals("xvi",   PublicationNumbering.numbering(16, NumberType.LOWERROMAN));
    assertEquals("xvii",  PublicationNumbering.numbering(17, NumberType.LOWERROMAN));
    assertEquals("xviii", PublicationNumbering.numbering(18, NumberType.LOWERROMAN));
    assertEquals("xix",   PublicationNumbering.numbering(19, NumberType.LOWERROMAN));
    assertEquals("xx",    PublicationNumbering.numbering(20, NumberType.LOWERROMAN));
    assertEquals("xxi",    PublicationNumbering.numbering(21, NumberType.LOWERROMAN));
    assertEquals("xxii",   PublicationNumbering.numbering(22, NumberType.LOWERROMAN));
    assertEquals("xxiii",  PublicationNumbering.numbering(23, NumberType.LOWERROMAN));
    assertEquals("xxiv",   PublicationNumbering.numbering(24, NumberType.LOWERROMAN));
    assertEquals("xxv",    PublicationNumbering.numbering(25, NumberType.LOWERROMAN));
    assertEquals("xxvi",   PublicationNumbering.numbering(26, NumberType.LOWERROMAN));
    assertEquals("xxvii",  PublicationNumbering.numbering(27, NumberType.LOWERROMAN));
    assertEquals("xxviii", PublicationNumbering.numbering(28, NumberType.LOWERROMAN));
    assertEquals("xxix",   PublicationNumbering.numbering(29, NumberType.LOWERROMAN));
    assertEquals("xxx",    PublicationNumbering.numbering(30, NumberType.LOWERROMAN));
  }

  @Test
  void testUpperRoman() {
    assertEquals("I",    PublicationNumbering.numbering(1, NumberType.UPPERROMAN));
    assertEquals("II",   PublicationNumbering.numbering(2, NumberType.UPPERROMAN));
    assertEquals("III",  PublicationNumbering.numbering(3, NumberType.UPPERROMAN));
    assertEquals("IV",   PublicationNumbering.numbering(4, NumberType.UPPERROMAN));
    assertEquals("V",    PublicationNumbering.numbering(5, NumberType.UPPERROMAN));
    assertEquals("VI",   PublicationNumbering.numbering(6, NumberType.UPPERROMAN));
    assertEquals("VII",  PublicationNumbering.numbering(7, NumberType.UPPERROMAN));
    assertEquals("VIII", PublicationNumbering.numbering(8, NumberType.UPPERROMAN));
    assertEquals("IX",   PublicationNumbering.numbering(9, NumberType.UPPERROMAN));
    assertEquals("X",    PublicationNumbering.numbering(10, NumberType.UPPERROMAN));
    assertEquals("XI",    PublicationNumbering.numbering(11, NumberType.UPPERROMAN));
    assertEquals("XII",   PublicationNumbering.numbering(12, NumberType.UPPERROMAN));
    assertEquals("XIII",  PublicationNumbering.numbering(13, NumberType.UPPERROMAN));
    assertEquals("XIV",   PublicationNumbering.numbering(14, NumberType.UPPERROMAN));
    assertEquals("XV",    PublicationNumbering.numbering(15, NumberType.UPPERROMAN));
    assertEquals("XVI",   PublicationNumbering.numbering(16, NumberType.UPPERROMAN));
    assertEquals("XVII",  PublicationNumbering.numbering(17, NumberType.UPPERROMAN));
    assertEquals("XVIII", PublicationNumbering.numbering(18, NumberType.UPPERROMAN));
    assertEquals("XIX",   PublicationNumbering.numbering(19, NumberType.UPPERROMAN));
    assertEquals("XX",    PublicationNumbering.numbering(20, NumberType.UPPERROMAN));
    assertEquals("XXI",    PublicationNumbering.numbering(21, NumberType.UPPERROMAN));
    assertEquals("XXII",   PublicationNumbering.numbering(22, NumberType.UPPERROMAN));
    assertEquals("XXIII",  PublicationNumbering.numbering(23, NumberType.UPPERROMAN));
    assertEquals("XXIV",   PublicationNumbering.numbering(24, NumberType.UPPERROMAN));
    assertEquals("XXV",    PublicationNumbering.numbering(25, NumberType.UPPERROMAN));
    assertEquals("XXVI",   PublicationNumbering.numbering(26, NumberType.UPPERROMAN));
    assertEquals("XXVII",  PublicationNumbering.numbering(27, NumberType.UPPERROMAN));
    assertEquals("XXVIII", PublicationNumbering.numbering(28, NumberType.UPPERROMAN));
    assertEquals("XXIX",   PublicationNumbering.numbering(29, NumberType.UPPERROMAN));
    assertEquals("XXX",    PublicationNumbering.numbering(30, NumberType.UPPERROMAN));
  }

  @Test
  void testLowerAlpha() {
    assertEquals("a",  PublicationNumbering.numbering(1, NumberType.LOWERALPHA));
    assertEquals("b",  PublicationNumbering.numbering(2, NumberType.LOWERALPHA));
    assertEquals("c",  PublicationNumbering.numbering(3, NumberType.LOWERALPHA));
    assertEquals("d",  PublicationNumbering.numbering(4, NumberType.LOWERALPHA));
    assertEquals("e",  PublicationNumbering.numbering(5, NumberType.LOWERALPHA));
    assertEquals("f",  PublicationNumbering.numbering(6, NumberType.LOWERALPHA));
    assertEquals("g",  PublicationNumbering.numbering(7, NumberType.LOWERALPHA));
    assertEquals("h",  PublicationNumbering.numbering(8, NumberType.LOWERALPHA));
    assertEquals("i",  PublicationNumbering.numbering(9, NumberType.LOWERALPHA));
    assertEquals("j",  PublicationNumbering.numbering(10, NumberType.LOWERALPHA));
    assertEquals("k",  PublicationNumbering.numbering(11, NumberType.LOWERALPHA));
    assertEquals("l",  PublicationNumbering.numbering(12, NumberType.LOWERALPHA));
    assertEquals("m",  PublicationNumbering.numbering(13, NumberType.LOWERALPHA));
    assertEquals("n",  PublicationNumbering.numbering(14, NumberType.LOWERALPHA));
    assertEquals("o",  PublicationNumbering.numbering(15, NumberType.LOWERALPHA));
    assertEquals("p",  PublicationNumbering.numbering(16, NumberType.LOWERALPHA));
    assertEquals("q",  PublicationNumbering.numbering(17, NumberType.LOWERALPHA));
    assertEquals("r",  PublicationNumbering.numbering(18, NumberType.LOWERALPHA));
    assertEquals("s",  PublicationNumbering.numbering(19, NumberType.LOWERALPHA));
    assertEquals("t",  PublicationNumbering.numbering(20, NumberType.LOWERALPHA));
    assertEquals("u",  PublicationNumbering.numbering(21, NumberType.LOWERALPHA));
    assertEquals("v",  PublicationNumbering.numbering(22, NumberType.LOWERALPHA));
    assertEquals("w",  PublicationNumbering.numbering(23, NumberType.LOWERALPHA));
    assertEquals("x",  PublicationNumbering.numbering(24, NumberType.LOWERALPHA));
    assertEquals("y",  PublicationNumbering.numbering(25, NumberType.LOWERALPHA));
    assertEquals("z",  PublicationNumbering.numbering(26, NumberType.LOWERALPHA));
    assertEquals("aa", PublicationNumbering.numbering(27, NumberType.LOWERALPHA));
    assertEquals("ab", PublicationNumbering.numbering(28, NumberType.LOWERALPHA));
    assertEquals("ac", PublicationNumbering.numbering(29, NumberType.LOWERALPHA));
    assertEquals("ad", PublicationNumbering.numbering(30, NumberType.LOWERALPHA));
    assertEquals("ae",  PublicationNumbering.numbering(31, NumberType.LOWERALPHA));
    assertEquals("af",  PublicationNumbering.numbering(32, NumberType.LOWERALPHA));
    assertEquals("ag",  PublicationNumbering.numbering(33, NumberType.LOWERALPHA));
    assertEquals("ah",  PublicationNumbering.numbering(34, NumberType.LOWERALPHA));
    assertEquals("ai",  PublicationNumbering.numbering(35, NumberType.LOWERALPHA));
    assertEquals("aj",  PublicationNumbering.numbering(36, NumberType.LOWERALPHA));
    assertEquals("ak",  PublicationNumbering.numbering(37, NumberType.LOWERALPHA));
    assertEquals("al",  PublicationNumbering.numbering(38, NumberType.LOWERALPHA));
    assertEquals("am",  PublicationNumbering.numbering(39, NumberType.LOWERALPHA));
    assertEquals("an",  PublicationNumbering.numbering(40, NumberType.LOWERALPHA));
    assertEquals("ao",  PublicationNumbering.numbering(41, NumberType.LOWERALPHA));
    assertEquals("ap",  PublicationNumbering.numbering(42, NumberType.LOWERALPHA));
    assertEquals("aq",  PublicationNumbering.numbering(43, NumberType.LOWERALPHA));
    assertEquals("ar",  PublicationNumbering.numbering(44, NumberType.LOWERALPHA));
    assertEquals("as",  PublicationNumbering.numbering(45, NumberType.LOWERALPHA));
    assertEquals("at",  PublicationNumbering.numbering(46, NumberType.LOWERALPHA));
    assertEquals("au",  PublicationNumbering.numbering(47, NumberType.LOWERALPHA));
    assertEquals("av",  PublicationNumbering.numbering(48, NumberType.LOWERALPHA));
    assertEquals("aw",  PublicationNumbering.numbering(49, NumberType.LOWERALPHA));
    assertEquals("ax",  PublicationNumbering.numbering(50, NumberType.LOWERALPHA));
    assertEquals("ay",  PublicationNumbering.numbering(51, NumberType.LOWERALPHA));
    assertEquals("az",  PublicationNumbering.numbering(52, NumberType.LOWERALPHA));
    assertEquals("ba",  PublicationNumbering.numbering(53, NumberType.LOWERALPHA));
    assertEquals("bz",  PublicationNumbering.numbering(78, NumberType.LOWERALPHA));
    assertEquals("zz",  PublicationNumbering.numbering(702, NumberType.LOWERALPHA));
    assertEquals("aaa",  PublicationNumbering.numbering(703, NumberType.LOWERALPHA));
  }

  @Test
  void testUpperAlpha() {
    assertEquals("a".toUpperCase(),  PublicationNumbering.numbering(1, NumberType.UPPERALPHA));
    assertEquals("b".toUpperCase(),  PublicationNumbering.numbering(2, NumberType.UPPERALPHA));
    assertEquals("c".toUpperCase(),  PublicationNumbering.numbering(3, NumberType.UPPERALPHA));
    assertEquals("d".toUpperCase(),  PublicationNumbering.numbering(4, NumberType.UPPERALPHA));
    assertEquals("e".toUpperCase(),  PublicationNumbering.numbering(5, NumberType.UPPERALPHA));
    assertEquals("f".toUpperCase(),  PublicationNumbering.numbering(6, NumberType.UPPERALPHA));
    assertEquals("g".toUpperCase(),  PublicationNumbering.numbering(7, NumberType.UPPERALPHA));
    assertEquals("h".toUpperCase(),  PublicationNumbering.numbering(8, NumberType.UPPERALPHA));
    assertEquals("i".toUpperCase(),  PublicationNumbering.numbering(9, NumberType.UPPERALPHA));
    assertEquals("j".toUpperCase(),  PublicationNumbering.numbering(10, NumberType.UPPERALPHA));
    assertEquals("k".toUpperCase(),  PublicationNumbering.numbering(11, NumberType.UPPERALPHA));
    assertEquals("l".toUpperCase(),  PublicationNumbering.numbering(12, NumberType.UPPERALPHA));
    assertEquals("m".toUpperCase(),  PublicationNumbering.numbering(13, NumberType.UPPERALPHA));
    assertEquals("n".toUpperCase(),  PublicationNumbering.numbering(14, NumberType.UPPERALPHA));
    assertEquals("o".toUpperCase(),  PublicationNumbering.numbering(15, NumberType.UPPERALPHA));
    assertEquals("p".toUpperCase(),  PublicationNumbering.numbering(16, NumberType.UPPERALPHA));
    assertEquals("q".toUpperCase(),  PublicationNumbering.numbering(17, NumberType.UPPERALPHA));
    assertEquals("r".toUpperCase(),  PublicationNumbering.numbering(18, NumberType.UPPERALPHA));
    assertEquals("s".toUpperCase(),  PublicationNumbering.numbering(19, NumberType.UPPERALPHA));
    assertEquals("t".toUpperCase(),  PublicationNumbering.numbering(20, NumberType.UPPERALPHA));
    assertEquals("u".toUpperCase(),  PublicationNumbering.numbering(21, NumberType.UPPERALPHA));
    assertEquals("v".toUpperCase(),  PublicationNumbering.numbering(22, NumberType.UPPERALPHA));
    assertEquals("w".toUpperCase(),  PublicationNumbering.numbering(23, NumberType.UPPERALPHA));
    assertEquals("x".toUpperCase(),  PublicationNumbering.numbering(24, NumberType.UPPERALPHA));
    assertEquals("y".toUpperCase(),  PublicationNumbering.numbering(25, NumberType.UPPERALPHA));
    assertEquals("z".toUpperCase(),  PublicationNumbering.numbering(26, NumberType.UPPERALPHA));
    assertEquals("aa".toUpperCase(), PublicationNumbering.numbering(27, NumberType.UPPERALPHA));
    assertEquals("ab".toUpperCase(), PublicationNumbering.numbering(28, NumberType.UPPERALPHA));
    assertEquals("ac".toUpperCase(), PublicationNumbering.numbering(29, NumberType.UPPERALPHA));
    assertEquals("ad".toUpperCase(), PublicationNumbering.numbering(30, NumberType.UPPERALPHA));
    assertEquals("ae".toUpperCase(),  PublicationNumbering.numbering(31, NumberType.UPPERALPHA));
    assertEquals("af".toUpperCase(),  PublicationNumbering.numbering(32, NumberType.UPPERALPHA));
    assertEquals("ag".toUpperCase(),  PublicationNumbering.numbering(33, NumberType.UPPERALPHA));
    assertEquals("ah".toUpperCase(),  PublicationNumbering.numbering(34, NumberType.UPPERALPHA));
    assertEquals("ai".toUpperCase(),  PublicationNumbering.numbering(35, NumberType.UPPERALPHA));
    assertEquals("aj".toUpperCase(),  PublicationNumbering.numbering(36, NumberType.UPPERALPHA));
    assertEquals("ak".toUpperCase(),  PublicationNumbering.numbering(37, NumberType.UPPERALPHA));
    assertEquals("al".toUpperCase(),  PublicationNumbering.numbering(38, NumberType.UPPERALPHA));
    assertEquals("am".toUpperCase(),  PublicationNumbering.numbering(39, NumberType.UPPERALPHA));
    assertEquals("an".toUpperCase(),  PublicationNumbering.numbering(40, NumberType.UPPERALPHA));
    assertEquals("ao".toUpperCase(),  PublicationNumbering.numbering(41, NumberType.UPPERALPHA));
    assertEquals("ap".toUpperCase(),  PublicationNumbering.numbering(42, NumberType.UPPERALPHA));
    assertEquals("aq".toUpperCase(),  PublicationNumbering.numbering(43, NumberType.UPPERALPHA));
    assertEquals("ar".toUpperCase(),  PublicationNumbering.numbering(44, NumberType.UPPERALPHA));
    assertEquals("as".toUpperCase(),  PublicationNumbering.numbering(45, NumberType.UPPERALPHA));
    assertEquals("at".toUpperCase(),  PublicationNumbering.numbering(46, NumberType.UPPERALPHA));
    assertEquals("au".toUpperCase(),  PublicationNumbering.numbering(47, NumberType.UPPERALPHA));
    assertEquals("av".toUpperCase(),  PublicationNumbering.numbering(48, NumberType.UPPERALPHA));
    assertEquals("aw".toUpperCase(),  PublicationNumbering.numbering(49, NumberType.UPPERALPHA));
    assertEquals("ax".toUpperCase(),  PublicationNumbering.numbering(50, NumberType.UPPERALPHA));
    assertEquals("ay".toUpperCase(),  PublicationNumbering.numbering(51, NumberType.UPPERALPHA));
    assertEquals("az".toUpperCase(),  PublicationNumbering.numbering(52, NumberType.UPPERALPHA));
    assertEquals("ba".toUpperCase(),  PublicationNumbering.numbering(53, NumberType.UPPERALPHA));
    assertEquals("bz".toUpperCase(),  PublicationNumbering.numbering(78, NumberType.UPPERALPHA));
    assertEquals("zz".toUpperCase(),  PublicationNumbering.numbering(702, NumberType.UPPERALPHA));
    assertEquals("aaa".toUpperCase(),  PublicationNumbering.numbering(703, NumberType.UPPERALPHA));
  }

}
