package org.pageseeder.psml.toc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HeadingTest {

  @Test
  void testPart() {
    Heading heading = Heading.untitled(1, "2", "2", 3);
    assertEquals(1, heading.level());
    assertEquals("2", heading.fragment());
    assertEquals(3, heading.index());
    assertEquals(Element.NO_TITLE, heading.title());
    assertEquals(Heading.NO_PREFIX, heading.prefix());
  }

}
