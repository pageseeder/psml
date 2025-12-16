package org.pageseeder.psml.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class XMLStringsTest {

  @Test
  void testText_withEmptyString() {
    // Test with empty string
    String input = "";
    String actual = XMLStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withNoSpecialCharacters() {
    // Test with a string that contains no '&' or '<'
    String input = "HelloWorld";
    String actual = XMLStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withNonEscapableCharacters() {
    // Test with a string that contains characters other than '&' or '<'
    String input = "ABC123!";
    String actual = XMLStrings.text(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testText_withAmpersand() {
    // Test with a string that contains '&'
    String input = "Hello & World";
    String expected = "Hello &amp; World";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withLessThan() {
    // Test with a string that contains '<'
    String input = "Hello < World";
    String expected = "Hello &lt; World";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withAmpersandAndLessThan() {
    // Test with a string that contains both '&' and '<'
    String input = "A & B < C & D";
    String expected = "A &amp; B &lt; C &amp; D";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withOnlySpecialCharacters() {
    // Test with a string containing only special characters
    String input = "<<&&<<";
    String expected = "&lt;&lt;&amp;&amp;&lt;&lt;";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withMixedCharacters() {
    // Test with a string containing a mix of special and non-special characters
    String input = "Mix & Match < Everything";
    String expected = "Mix &amp; Match &lt; Everything";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testText_withLongString() {
    // Test with a long string containing multiple special characters
    String input = "This is a very long string with special characters like & and < scattered throughout. Another & here and < there.";
    String expected = "This is a very long string with special characters like &amp; and &lt; scattered throughout. Another &amp; here and &lt; there.";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testNullableText_withNullInput() {
    // Test with null input for nullableText
    String input = null;
    String actual = XMLStrings.nullableText(input);
    assertEquals(null, actual);
  }

  @Test
  void testText_withConsecutiveSpecialCharacters() {
    // Test with a string that contains consecutive '&' and '<'
    String input = "A&& B<<";
    String expected = "A&amp;&amp; B&lt;&lt;";
    String actual = XMLStrings.text(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withEmptyString() {
    // Test with empty string
    String input = "";
    String actual = XMLStrings.attribute(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testAttribute_withNoSpecialCharacters() {
    // Test with a string that contains no special characters
    String input = "SimpleString";
    String actual = XMLStrings.attribute(input);
    assertEquals(input, actual);
    assertSame(input, actual);
  }

  @Test
  void testAttribute_withAmpersand() {
    // Test with a string that contains '&'
    String input = "Hello & World";
    String expected = "Hello &amp; World";
    String actual = XMLStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withDoubleQuote() {
    // Test with a string that contains '"'
    String input = "He said, \"Hello\"!";
    String expected = "He said, &quot;Hello&quot;!";
    String actual = XMLStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withSingleQuote() {
    // Test with a string that contains '\''
    String input = "It's a test!";
    String expected = "It&apos;s a test!";
    String actual = XMLStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withLessThan() {
    // Test with a string that contains '<'
    String input = "2 < 3";
    String expected = "2 &lt; 3";
    String actual = XMLStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testAttribute_withMultipleSpecialCharacters() {
    // Test with a string that contains multiple special characters
    String input = "\"A&B<C'";
    String expected = "&quot;A&amp;B&lt;C&apos;";
    String actual = XMLStrings.attribute(input);
    assertEquals(expected, actual);
  }

  @Test
  void testTextWithCharArray_noSpecialCharacters() {
    // Test with char array containing no special characters
    char[] input = "HelloWorld".toCharArray();
    String actual = XMLStrings.text(input, 0, input.length);
    String expected = "HelloWorld";
    assertEquals(expected, actual);
    assertEquals(XMLStrings.text(new String(input)), actual);
  }

  @Test
  void testTextWithCharArray_withSpecialCharacters() {
    // Test with char array containing special characters
    char[] input = "A & B < C".toCharArray();
    String actual = XMLStrings.text(input, 0, input.length);
    String expected = "A &amp; B &lt; C";
    assertEquals(expected, actual);
    assertEquals(XMLStrings.text(new String(input)), actual);
  }

  @Test
  void testTextWithCharArray_onlySpecialCharacters() {
    // Test with char array containing only special characters
    char[] input = "<&&<".toCharArray();
    String actual = XMLStrings.text(input, 0, input.length);
    String expected = "&lt;&amp;&amp;&lt;";
    assertEquals(expected, actual);
    assertEquals(XMLStrings.text(new String(input)), actual);
  }

  @Test
  void testTextWithCharArray_partialInput() {
    // Test with a partial range of characters
    char[] input = "Some <random> text & values".toCharArray();
    String actual = XMLStrings.text(input, 5, 15); // Substring: "<random> text &"
    String expected = "&lt;random> text &amp;";
    assertEquals(expected, actual);
    assertEquals(XMLStrings.text(new String(input, 5, 15)), actual);
  }
}