package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.h1;
import static org.pageseeder.psml.toc.Tests.h2;
import static org.pageseeder.psml.toc.Tests.h3;
import static org.pageseeder.psml.toc.Tests.h4;

import org.junit.Assert;
import org.junit.Test;

public final class PartTest {

  @Test
  public void testPart() {
    Part<Heading> part = h1(Element.NO_TITLE, "2", 3);
    Assert.assertEquals(1, part.level());
    Assert.assertEquals("2", part.element().fragment());
    Assert.assertEquals(3, part.element().index());
    Assert.assertEquals(Element.NO_TITLE, part.title());
    Assert.assertEquals(Heading.NO_PREFIX, part.element().prefix());
  }

  // Level consistency
  // --------------------------------------------------------------------------

  @Test
  public void testLevelConsistent_1() {
    Part<Heading> part = h1("Title", "1", 1);
    Assert.assertTrue(part.isLevelConsistent());
  }

  @Test
  public void testLevelConsistent_2_true() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1));
    Assert.assertTrue(part.isLevelConsistent());
  }

  @Test
  public void testLevelConsistent_2_false() {
    Part<Heading> part = h1("Title", "1", 1, h3("A", "1", 1));
    Assert.assertFalse(part.isLevelConsistent());
  }

  @Test
  public void testLevelConsistent_3_true() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1, h3("x", "1", 1)));
    Assert.assertTrue(part.isLevelConsistent());
  }


  @Test
  public void testLevelConsistent_3_false1() {
    Part<Heading> part = h1("Title", "1", 1, h3("A", "1", 1));
    Assert.assertFalse(part.isLevelConsistent());
  }

  @Test
  public void testLevelConsistent_3_false2() {
    Part<Heading> part = h1("Title", "1", 1, h2("A", "1", 1, h4("x", "1", 1)));
    Assert.assertFalse(part.isLevelConsistent());
  }

  @Test
  public void testLevelConsistent_4() {
    Part<Heading> part = h1("Title", "1", 1,
        h2("A", "1", 1),
        h2("B", "1", 1,
            h3("x", "1", 1)),
        h2("A", "1", 1,
            h3("x", "1", 1),
            h3("y", "1", 1,
                h4("i", "1", 1))));
    Assert.assertTrue(part.isLevelConsistent());
  }

}
