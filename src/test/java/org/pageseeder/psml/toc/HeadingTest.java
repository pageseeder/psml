package org.pageseeder.psml.toc;

import org.junit.Assert;
import org.junit.Test;

public final class HeadingTest {

  @Test
  public void testPart() {
    Heading heading = Heading.untitled(1, "2", "2", 3);
    Assert.assertEquals(1, heading.level());
    Assert.assertEquals("2", heading.fragment());
    Assert.assertEquals(3, heading.index());
    Assert.assertEquals(Element.NO_TITLE, heading.title());
    Assert.assertEquals(Heading.NO_PREFIX, heading.prefix());
  }

}
