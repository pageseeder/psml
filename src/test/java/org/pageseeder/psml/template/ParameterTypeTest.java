package org.pageseeder.psml.template;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterTypeTest {

  /**
   * Tests the matches method for INTEGER type.
   */
  @Test
  void testIntegerMatches() {
    assertTrue(ParameterType.INTEGER.matches("1234"));
    assertTrue(ParameterType.INTEGER.matches("-5678"));
    assertFalse(ParameterType.INTEGER.matches("12a34"));
    assertFalse(ParameterType.INTEGER.matches(""));
  }

  /**
   * Tests the matches method for TEXT type.
   */
  @Test
  void testTextMatches() {
    assertTrue(ParameterType.TEXT.matches("any-value"));
    assertTrue(ParameterType.TEXT.matches(""));
  }

  /**
   * Tests the matches method for DATE type.
   */
  @Test
  void testDateMatches() {
    assertTrue(ParameterType.DATE.matches("2025-12-12"));
    assertFalse(ParameterType.DATE.matches("12-12-2025"));
    assertFalse(ParameterType.DATE.matches("2025/12/12"));
    assertFalse(ParameterType.DATE.matches(""));
  }

  /**
   * Tests the matches method for DATETIME type.
   */
  @Test
  void testDatetimeMatches() {
    assertTrue(ParameterType.DATETIME.matches("2025-12-12T15:30:45"));
    assertFalse(ParameterType.DATETIME.matches("2025-12-12"));
    assertFalse(ParameterType.DATETIME.matches("2025/12/12 15:30:45"));
  }

  /**
   * Tests the matches method for TIME type.
   */
  @Test
  void testTimeMatches() {
    assertTrue(ParameterType.TIME.matches("15:30:45"));
    assertTrue(ParameterType.TIME.matches("15:30:45.123"));
    assertFalse(ParameterType.TIME.matches("25:61:61"));
    assertFalse(ParameterType.TIME.matches(""));
  }

  /**
   * Tests the matches method for XML type.
   */
  @Test
  void testXmlMatches() {
    assertTrue(ParameterType.XML.matches("<fragment/>"));
    assertTrue(ParameterType.XML.matches("<para>a</para><para>B</para>"));
    assertTrue(ParameterType.XML.matches(""));
    assertFalse(ParameterType.XML.matches("<x>"));
    assertFalse(ParameterType.XML.matches("<"));
    assertFalse(ParameterType.XML.matches("A & B"));
  }

}