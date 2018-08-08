package org.pageseeder.psml.toc;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.toc.PublicationNumbering.NumberType;

public final class PublicationNumberingTest {

  @Test
  public void testLowerRoman() {
    Assert.assertEquals("i",    PublicationNumbering.numbering(1, NumberType.LOWERROMAN));
    Assert.assertEquals("ii",   PublicationNumbering.numbering(2, NumberType.LOWERROMAN));
    Assert.assertEquals("iii",  PublicationNumbering.numbering(3, NumberType.LOWERROMAN));
    Assert.assertEquals("iv",   PublicationNumbering.numbering(4, NumberType.LOWERROMAN));
    Assert.assertEquals("v",    PublicationNumbering.numbering(5, NumberType.LOWERROMAN));
    Assert.assertEquals("vi",   PublicationNumbering.numbering(6, NumberType.LOWERROMAN));
    Assert.assertEquals("vii",  PublicationNumbering.numbering(7, NumberType.LOWERROMAN));
    Assert.assertEquals("viii", PublicationNumbering.numbering(8, NumberType.LOWERROMAN));
    Assert.assertEquals("ix",   PublicationNumbering.numbering(9, NumberType.LOWERROMAN));
    Assert.assertEquals("x",    PublicationNumbering.numbering(10, NumberType.LOWERROMAN));
    Assert.assertEquals("xi",    PublicationNumbering.numbering(11, NumberType.LOWERROMAN));
    Assert.assertEquals("xii",   PublicationNumbering.numbering(12, NumberType.LOWERROMAN));
    Assert.assertEquals("xiii",  PublicationNumbering.numbering(13, NumberType.LOWERROMAN));
    Assert.assertEquals("xiv",   PublicationNumbering.numbering(14, NumberType.LOWERROMAN));
    Assert.assertEquals("xv",    PublicationNumbering.numbering(15, NumberType.LOWERROMAN));
    Assert.assertEquals("xvi",   PublicationNumbering.numbering(16, NumberType.LOWERROMAN));
    Assert.assertEquals("xvii",  PublicationNumbering.numbering(17, NumberType.LOWERROMAN));
    Assert.assertEquals("xviii", PublicationNumbering.numbering(18, NumberType.LOWERROMAN));
    Assert.assertEquals("xix",   PublicationNumbering.numbering(19, NumberType.LOWERROMAN));
    Assert.assertEquals("xx",    PublicationNumbering.numbering(20, NumberType.LOWERROMAN));
    Assert.assertEquals("xxi",    PublicationNumbering.numbering(21, NumberType.LOWERROMAN));
    Assert.assertEquals("xxii",   PublicationNumbering.numbering(22, NumberType.LOWERROMAN));
    Assert.assertEquals("xxiii",  PublicationNumbering.numbering(23, NumberType.LOWERROMAN));
    Assert.assertEquals("xxiv",   PublicationNumbering.numbering(24, NumberType.LOWERROMAN));
    Assert.assertEquals("xxv",    PublicationNumbering.numbering(25, NumberType.LOWERROMAN));
    Assert.assertEquals("xxvi",   PublicationNumbering.numbering(26, NumberType.LOWERROMAN));
    Assert.assertEquals("xxvii",  PublicationNumbering.numbering(27, NumberType.LOWERROMAN));
    Assert.assertEquals("xxviii", PublicationNumbering.numbering(28, NumberType.LOWERROMAN));
    Assert.assertEquals("xxix",   PublicationNumbering.numbering(29, NumberType.LOWERROMAN));
    Assert.assertEquals("xxx",    PublicationNumbering.numbering(30, NumberType.LOWERROMAN));
  }

  @Test
  public void testUpperRoman() {
    Assert.assertEquals("I",    PublicationNumbering.numbering(1, NumberType.UPPERROMAN));
    Assert.assertEquals("II",   PublicationNumbering.numbering(2, NumberType.UPPERROMAN));
    Assert.assertEquals("III",  PublicationNumbering.numbering(3, NumberType.UPPERROMAN));
    Assert.assertEquals("IV",   PublicationNumbering.numbering(4, NumberType.UPPERROMAN));
    Assert.assertEquals("V",    PublicationNumbering.numbering(5, NumberType.UPPERROMAN));
    Assert.assertEquals("VI",   PublicationNumbering.numbering(6, NumberType.UPPERROMAN));
    Assert.assertEquals("VII",  PublicationNumbering.numbering(7, NumberType.UPPERROMAN));
    Assert.assertEquals("VIII", PublicationNumbering.numbering(8, NumberType.UPPERROMAN));
    Assert.assertEquals("IX",   PublicationNumbering.numbering(9, NumberType.UPPERROMAN));
    Assert.assertEquals("X",    PublicationNumbering.numbering(10, NumberType.UPPERROMAN));
    Assert.assertEquals("XI",    PublicationNumbering.numbering(11, NumberType.UPPERROMAN));
    Assert.assertEquals("XII",   PublicationNumbering.numbering(12, NumberType.UPPERROMAN));
    Assert.assertEquals("XIII",  PublicationNumbering.numbering(13, NumberType.UPPERROMAN));
    Assert.assertEquals("XIV",   PublicationNumbering.numbering(14, NumberType.UPPERROMAN));
    Assert.assertEquals("XV",    PublicationNumbering.numbering(15, NumberType.UPPERROMAN));
    Assert.assertEquals("XVI",   PublicationNumbering.numbering(16, NumberType.UPPERROMAN));
    Assert.assertEquals("XVII",  PublicationNumbering.numbering(17, NumberType.UPPERROMAN));
    Assert.assertEquals("XVIII", PublicationNumbering.numbering(18, NumberType.UPPERROMAN));
    Assert.assertEquals("XIX",   PublicationNumbering.numbering(19, NumberType.UPPERROMAN));
    Assert.assertEquals("XX",    PublicationNumbering.numbering(20, NumberType.UPPERROMAN));
    Assert.assertEquals("XXI",    PublicationNumbering.numbering(21, NumberType.UPPERROMAN));
    Assert.assertEquals("XXII",   PublicationNumbering.numbering(22, NumberType.UPPERROMAN));
    Assert.assertEquals("XXIII",  PublicationNumbering.numbering(23, NumberType.UPPERROMAN));
    Assert.assertEquals("XXIV",   PublicationNumbering.numbering(24, NumberType.UPPERROMAN));
    Assert.assertEquals("XXV",    PublicationNumbering.numbering(25, NumberType.UPPERROMAN));
    Assert.assertEquals("XXVI",   PublicationNumbering.numbering(26, NumberType.UPPERROMAN));
    Assert.assertEquals("XXVII",  PublicationNumbering.numbering(27, NumberType.UPPERROMAN));
    Assert.assertEquals("XXVIII", PublicationNumbering.numbering(28, NumberType.UPPERROMAN));
    Assert.assertEquals("XXIX",   PublicationNumbering.numbering(29, NumberType.UPPERROMAN));
    Assert.assertEquals("XXX",    PublicationNumbering.numbering(30, NumberType.UPPERROMAN));
  }

  @Test
  public void testLowerAlpha() {
    Assert.assertEquals("a",  PublicationNumbering.numbering(1, NumberType.LOWERALPHA));
    Assert.assertEquals("b",  PublicationNumbering.numbering(2, NumberType.LOWERALPHA));
    Assert.assertEquals("c",  PublicationNumbering.numbering(3, NumberType.LOWERALPHA));
    Assert.assertEquals("d",  PublicationNumbering.numbering(4, NumberType.LOWERALPHA));
    Assert.assertEquals("e",  PublicationNumbering.numbering(5, NumberType.LOWERALPHA));
    Assert.assertEquals("f",  PublicationNumbering.numbering(6, NumberType.LOWERALPHA));
    Assert.assertEquals("g",  PublicationNumbering.numbering(7, NumberType.LOWERALPHA));
    Assert.assertEquals("h",  PublicationNumbering.numbering(8, NumberType.LOWERALPHA));
    Assert.assertEquals("i",  PublicationNumbering.numbering(9, NumberType.LOWERALPHA));
    Assert.assertEquals("j",  PublicationNumbering.numbering(10, NumberType.LOWERALPHA));
    Assert.assertEquals("k",  PublicationNumbering.numbering(11, NumberType.LOWERALPHA));
    Assert.assertEquals("l",  PublicationNumbering.numbering(12, NumberType.LOWERALPHA));
    Assert.assertEquals("m",  PublicationNumbering.numbering(13, NumberType.LOWERALPHA));
    Assert.assertEquals("n",  PublicationNumbering.numbering(14, NumberType.LOWERALPHA));
    Assert.assertEquals("o",  PublicationNumbering.numbering(15, NumberType.LOWERALPHA));
    Assert.assertEquals("p",  PublicationNumbering.numbering(16, NumberType.LOWERALPHA));
    Assert.assertEquals("q",  PublicationNumbering.numbering(17, NumberType.LOWERALPHA));
    Assert.assertEquals("r",  PublicationNumbering.numbering(18, NumberType.LOWERALPHA));
    Assert.assertEquals("s",  PublicationNumbering.numbering(19, NumberType.LOWERALPHA));
    Assert.assertEquals("t",  PublicationNumbering.numbering(20, NumberType.LOWERALPHA));
    Assert.assertEquals("u",  PublicationNumbering.numbering(21, NumberType.LOWERALPHA));
    Assert.assertEquals("v",  PublicationNumbering.numbering(22, NumberType.LOWERALPHA));
    Assert.assertEquals("w",  PublicationNumbering.numbering(23, NumberType.LOWERALPHA));
    Assert.assertEquals("x",  PublicationNumbering.numbering(24, NumberType.LOWERALPHA));
    Assert.assertEquals("y",  PublicationNumbering.numbering(25, NumberType.LOWERALPHA));
    Assert.assertEquals("z",  PublicationNumbering.numbering(26, NumberType.LOWERALPHA));
    Assert.assertEquals("aa", PublicationNumbering.numbering(27, NumberType.LOWERALPHA));
    Assert.assertEquals("ab", PublicationNumbering.numbering(28, NumberType.LOWERALPHA));
    Assert.assertEquals("ac", PublicationNumbering.numbering(29, NumberType.LOWERALPHA));
    Assert.assertEquals("ad", PublicationNumbering.numbering(30, NumberType.LOWERALPHA));
    Assert.assertEquals("ae",  PublicationNumbering.numbering(31, NumberType.LOWERALPHA));
    Assert.assertEquals("af",  PublicationNumbering.numbering(32, NumberType.LOWERALPHA));
    Assert.assertEquals("ag",  PublicationNumbering.numbering(33, NumberType.LOWERALPHA));
    Assert.assertEquals("ah",  PublicationNumbering.numbering(34, NumberType.LOWERALPHA));
    Assert.assertEquals("ai",  PublicationNumbering.numbering(35, NumberType.LOWERALPHA));
    Assert.assertEquals("aj",  PublicationNumbering.numbering(36, NumberType.LOWERALPHA));
    Assert.assertEquals("ak",  PublicationNumbering.numbering(37, NumberType.LOWERALPHA));
    Assert.assertEquals("al",  PublicationNumbering.numbering(38, NumberType.LOWERALPHA));
    Assert.assertEquals("am",  PublicationNumbering.numbering(39, NumberType.LOWERALPHA));
    Assert.assertEquals("an",  PublicationNumbering.numbering(40, NumberType.LOWERALPHA));
    Assert.assertEquals("ao",  PublicationNumbering.numbering(41, NumberType.LOWERALPHA));
    Assert.assertEquals("ap",  PublicationNumbering.numbering(42, NumberType.LOWERALPHA));
    Assert.assertEquals("aq",  PublicationNumbering.numbering(43, NumberType.LOWERALPHA));
    Assert.assertEquals("ar",  PublicationNumbering.numbering(44, NumberType.LOWERALPHA));
    Assert.assertEquals("as",  PublicationNumbering.numbering(45, NumberType.LOWERALPHA));
    Assert.assertEquals("at",  PublicationNumbering.numbering(46, NumberType.LOWERALPHA));
    Assert.assertEquals("au",  PublicationNumbering.numbering(47, NumberType.LOWERALPHA));
    Assert.assertEquals("av",  PublicationNumbering.numbering(48, NumberType.LOWERALPHA));
    Assert.assertEquals("aw",  PublicationNumbering.numbering(49, NumberType.LOWERALPHA));
    Assert.assertEquals("ax",  PublicationNumbering.numbering(50, NumberType.LOWERALPHA));
    Assert.assertEquals("ay",  PublicationNumbering.numbering(51, NumberType.LOWERALPHA));
    Assert.assertEquals("az",  PublicationNumbering.numbering(52, NumberType.LOWERALPHA));
    Assert.assertEquals("ba",  PublicationNumbering.numbering(53, NumberType.LOWERALPHA));
    Assert.assertEquals("bz",  PublicationNumbering.numbering(78, NumberType.LOWERALPHA));
    Assert.assertEquals("zz",  PublicationNumbering.numbering(702, NumberType.LOWERALPHA));
    Assert.assertEquals("aaa",  PublicationNumbering.numbering(703, NumberType.LOWERALPHA));
  }

  @Test
  public void testUpperAlpha() {
    Assert.assertEquals("a".toUpperCase(),  PublicationNumbering.numbering(1, NumberType.UPPERALPHA));
    Assert.assertEquals("b".toUpperCase(),  PublicationNumbering.numbering(2, NumberType.UPPERALPHA));
    Assert.assertEquals("c".toUpperCase(),  PublicationNumbering.numbering(3, NumberType.UPPERALPHA));
    Assert.assertEquals("d".toUpperCase(),  PublicationNumbering.numbering(4, NumberType.UPPERALPHA));
    Assert.assertEquals("e".toUpperCase(),  PublicationNumbering.numbering(5, NumberType.UPPERALPHA));
    Assert.assertEquals("f".toUpperCase(),  PublicationNumbering.numbering(6, NumberType.UPPERALPHA));
    Assert.assertEquals("g".toUpperCase(),  PublicationNumbering.numbering(7, NumberType.UPPERALPHA));
    Assert.assertEquals("h".toUpperCase(),  PublicationNumbering.numbering(8, NumberType.UPPERALPHA));
    Assert.assertEquals("i".toUpperCase(),  PublicationNumbering.numbering(9, NumberType.UPPERALPHA));
    Assert.assertEquals("j".toUpperCase(),  PublicationNumbering.numbering(10, NumberType.UPPERALPHA));
    Assert.assertEquals("k".toUpperCase(),  PublicationNumbering.numbering(11, NumberType.UPPERALPHA));
    Assert.assertEquals("l".toUpperCase(),  PublicationNumbering.numbering(12, NumberType.UPPERALPHA));
    Assert.assertEquals("m".toUpperCase(),  PublicationNumbering.numbering(13, NumberType.UPPERALPHA));
    Assert.assertEquals("n".toUpperCase(),  PublicationNumbering.numbering(14, NumberType.UPPERALPHA));
    Assert.assertEquals("o".toUpperCase(),  PublicationNumbering.numbering(15, NumberType.UPPERALPHA));
    Assert.assertEquals("p".toUpperCase(),  PublicationNumbering.numbering(16, NumberType.UPPERALPHA));
    Assert.assertEquals("q".toUpperCase(),  PublicationNumbering.numbering(17, NumberType.UPPERALPHA));
    Assert.assertEquals("r".toUpperCase(),  PublicationNumbering.numbering(18, NumberType.UPPERALPHA));
    Assert.assertEquals("s".toUpperCase(),  PublicationNumbering.numbering(19, NumberType.UPPERALPHA));
    Assert.assertEquals("t".toUpperCase(),  PublicationNumbering.numbering(20, NumberType.UPPERALPHA));
    Assert.assertEquals("u".toUpperCase(),  PublicationNumbering.numbering(21, NumberType.UPPERALPHA));
    Assert.assertEquals("v".toUpperCase(),  PublicationNumbering.numbering(22, NumberType.UPPERALPHA));
    Assert.assertEquals("w".toUpperCase(),  PublicationNumbering.numbering(23, NumberType.UPPERALPHA));
    Assert.assertEquals("x".toUpperCase(),  PublicationNumbering.numbering(24, NumberType.UPPERALPHA));
    Assert.assertEquals("y".toUpperCase(),  PublicationNumbering.numbering(25, NumberType.UPPERALPHA));
    Assert.assertEquals("z".toUpperCase(),  PublicationNumbering.numbering(26, NumberType.UPPERALPHA));
    Assert.assertEquals("aa".toUpperCase(), PublicationNumbering.numbering(27, NumberType.UPPERALPHA));
    Assert.assertEquals("ab".toUpperCase(), PublicationNumbering.numbering(28, NumberType.UPPERALPHA));
    Assert.assertEquals("ac".toUpperCase(), PublicationNumbering.numbering(29, NumberType.UPPERALPHA));
    Assert.assertEquals("ad".toUpperCase(), PublicationNumbering.numbering(30, NumberType.UPPERALPHA));
    Assert.assertEquals("ae".toUpperCase(),  PublicationNumbering.numbering(31, NumberType.UPPERALPHA));
    Assert.assertEquals("af".toUpperCase(),  PublicationNumbering.numbering(32, NumberType.UPPERALPHA));
    Assert.assertEquals("ag".toUpperCase(),  PublicationNumbering.numbering(33, NumberType.UPPERALPHA));
    Assert.assertEquals("ah".toUpperCase(),  PublicationNumbering.numbering(34, NumberType.UPPERALPHA));
    Assert.assertEquals("ai".toUpperCase(),  PublicationNumbering.numbering(35, NumberType.UPPERALPHA));
    Assert.assertEquals("aj".toUpperCase(),  PublicationNumbering.numbering(36, NumberType.UPPERALPHA));
    Assert.assertEquals("ak".toUpperCase(),  PublicationNumbering.numbering(37, NumberType.UPPERALPHA));
    Assert.assertEquals("al".toUpperCase(),  PublicationNumbering.numbering(38, NumberType.UPPERALPHA));
    Assert.assertEquals("am".toUpperCase(),  PublicationNumbering.numbering(39, NumberType.UPPERALPHA));
    Assert.assertEquals("an".toUpperCase(),  PublicationNumbering.numbering(40, NumberType.UPPERALPHA));
    Assert.assertEquals("ao".toUpperCase(),  PublicationNumbering.numbering(41, NumberType.UPPERALPHA));
    Assert.assertEquals("ap".toUpperCase(),  PublicationNumbering.numbering(42, NumberType.UPPERALPHA));
    Assert.assertEquals("aq".toUpperCase(),  PublicationNumbering.numbering(43, NumberType.UPPERALPHA));
    Assert.assertEquals("ar".toUpperCase(),  PublicationNumbering.numbering(44, NumberType.UPPERALPHA));
    Assert.assertEquals("as".toUpperCase(),  PublicationNumbering.numbering(45, NumberType.UPPERALPHA));
    Assert.assertEquals("at".toUpperCase(),  PublicationNumbering.numbering(46, NumberType.UPPERALPHA));
    Assert.assertEquals("au".toUpperCase(),  PublicationNumbering.numbering(47, NumberType.UPPERALPHA));
    Assert.assertEquals("av".toUpperCase(),  PublicationNumbering.numbering(48, NumberType.UPPERALPHA));
    Assert.assertEquals("aw".toUpperCase(),  PublicationNumbering.numbering(49, NumberType.UPPERALPHA));
    Assert.assertEquals("ax".toUpperCase(),  PublicationNumbering.numbering(50, NumberType.UPPERALPHA));
    Assert.assertEquals("ay".toUpperCase(),  PublicationNumbering.numbering(51, NumberType.UPPERALPHA));
    Assert.assertEquals("az".toUpperCase(),  PublicationNumbering.numbering(52, NumberType.UPPERALPHA));
    Assert.assertEquals("ba".toUpperCase(),  PublicationNumbering.numbering(53, NumberType.UPPERALPHA));
    Assert.assertEquals("bz".toUpperCase(),  PublicationNumbering.numbering(78, NumberType.UPPERALPHA));
    Assert.assertEquals("zz".toUpperCase(),  PublicationNumbering.numbering(702, NumberType.UPPERALPHA));
    Assert.assertEquals("aaa".toUpperCase(),  PublicationNumbering.numbering(703, NumberType.UPPERALPHA));
  }

}
