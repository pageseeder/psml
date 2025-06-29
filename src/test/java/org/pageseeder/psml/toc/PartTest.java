package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class PartTest {

  @Test
  void testPart() {
    Part<Heading> part = h1(Element.NO_TITLE, "2", 3);
    assertEquals(1, part.level());
    assertEquals("2", part.element().fragment());
    assertEquals(3, part.element().index());
    assertEquals(Element.NO_TITLE, part.title());
    assertEquals(Heading.NO_PREFIX, part.element().prefix());
  }

  // Level consistency
  // --------------------------------------------------------------------------

  @Test
  void testLevelConsistent_1() {
    Part<Heading> part = h1("Title", "1", 1);
    assertTrue(part.isLevelConsistent());
  }

  @Test
  void testLevelConsistent_2_true() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1));
    assertTrue(part.isLevelConsistent());
  }

  @Test
  void testLevelConsistent_2_false() {
    Part<Heading> part = h1("Title", "1", 1, h3("A", "1", 1));
    assertFalse(part.isLevelConsistent());
  }

  @Test
  void testLevelConsistent_3_true() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1, h3("x", "1", 1)));
    assertTrue(part.isLevelConsistent());
  }


  @Test
  void testLevelConsistent_3_false1() {
    Part<Heading> part = h1("Title", "1", 1, h3("A", "1", 1));
    assertFalse(part.isLevelConsistent());
  }

  @Test
  void testLevelConsistent_3_false2() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1, h4("x", "1", 1)));
    assertFalse(part.isLevelConsistent());
  }

  @Test
  void testLevelConsistent_4() {
    Part<Heading> part = h1("Title", "1", 1,
        h2("A", "1", 1),
        h2("B", "1", 1,
            h3("x", "1", 1)),
        h2("A", "1", 1,
            h3("x", "1", 1),
            h3("y", "1", 1,
                h4("i", "1", 1))));
    assertTrue(part.isLevelConsistent());
  }

}
