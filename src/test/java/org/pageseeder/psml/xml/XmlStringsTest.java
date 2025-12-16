package org.pageseeder.psml.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class XmlStringsTest {

  @Test
  void testText_withEmptyString() {
    // Test with empty string
    String input = "";
    String actual = XmlStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withNoSpecialCharacters() {
    // Test with a string that contains no '&' or '<'
    String input = "HelloWorld";
    String actual = XmlStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withNonEscapableCharacters() {
    // Test with a string that contains characters other than '&' or '<'
    String input = "ABC123!";
    String actual = XmlStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withAmpersand() {
    // Test with a string that contains '&'
    String input = "Hello & World";
    String expected = "Hello &amp; World";
    String actual = XmlStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withLessThan() {
    // Test with a string that contains '<'
    String input = "Hello < World";
    String expected = "Hello &lt; World";
    String actual = XmlStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withAmpersandAndLessThan() {
    // Test with a string that contains both '&' and '<'
    String input = "A & B < C & D";
    String expected = "A &amp; B &lt; C &amp; D";
    String actual = XmlStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withConsecutiveSpecialCharacters() {
    // Test with a string that contains consecutive '&' and '<'
    String input = "A&& B<<";
    String expected = "A&amp;&amp; B&lt;&lt;";
    String actual = XmlStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withEmptyString() {
    // Test with empty string
    String input = "";
    String actual = XmlStrings.attribute(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testAttribute_withNoSpecialCharacters() {
    // Test with a string that contains no special characters
    String input = "SimpleString";
    String actual = XmlStrings.attribute(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testAttribute_withAmpersand() {
    // Test with a string that contains '&'
    String input = "Hello & World";
    String expected = "Hello &amp; World";
    String actual = XmlStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withDoubleQuote() {
    // Test with a string that contains '"'
    String input = "He said, \"Hello\"!";
    String expected = "He said, &quot;Hello&quot;!";
    String actual = XmlStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withSingleQuote() {
    // Test with a string that contains '\''
    String input = "It's a test!";
    String expected = "It&apos;s a test!";
    String actual = XmlStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withLessThan() {
    // Test with a string that contains '<'
    String input = "2 < 3";
    String expected = "2 &lt; 3";
    String actual = XmlStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withMultipleSpecialCharacters() {
    // Test with a string that contains multiple special characters
    String input = "\"A&B<C'";
    String expected = "&quot;A&amp;B&lt;C&apos;";
    String actual = XmlStrings.attribute(input);
    assertEquals(expected, actual);
  }

}