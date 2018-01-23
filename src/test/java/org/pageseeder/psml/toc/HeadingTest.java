package org.pageseeder.psml.toc;

import org.junit.Assert;
import org.junit.Test;

public final class HeadingTest {

  @Test
  public void testPart() {
    Heading heading = Heading.untitled(1, "2", 3);
    Assert.assertEquals(1, heading.level());
    Assert.assertEquals("2", heading.fragment());
    Assert.assertEquals(3, heading.index());
    Assert.assertEquals(Element.NO_TITLE, heading.title());
    Assert.assertEquals(Heading.NO_PREFIX, heading.prefix());
  }

  @Test
  public void testAdjustLevel_0() {
    Heading heading = new Heading(1, "A", "1", 1);
    Assert.assertEquals(1, heading.level());
    Assert.assertEquals(1, heading.adjustLevel(0).level());
    Assert.assertSame(heading, heading.adjustLevel(0));
  }

  @Test
  public void testAdjustLevel_1() {
    Heading heading = new Heading(1, "A", "1", 1);
    Heading adjusted = heading.adjustLevel(1);
    Assert.assertEquals(1, heading.level());
    Assert.assertEquals(2, adjusted.level());
  }

  @Test
  public void testAdjustLevel_minus1() {
    Heading heading = new Heading(2, "A", "1", 1);
    Heading adjusted = heading.adjustLevel(-1);
    Assert.assertEquals(2, heading.level());
    Assert.assertEquals(1, adjusted.level());
  }

}
