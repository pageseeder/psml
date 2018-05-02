package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.ref;

import org.junit.Assert;
import org.junit.Test;

public final class ReferenceTest {

  @Test
  public void testRef_basic() {
    Part<Reference> ref = ref(1, "Hello", 23L);
    Assert.assertEquals(1, ref.level());
    Assert.assertEquals(23L, ref.element().uri());
    Assert.assertEquals("Hello", ref.title());
  }

  @Test
  public void testFind() {
    Part<Reference> ref = ref(1, "ROOT", 10L, ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Reference> root = Part.find(ref, 10L);
    Part<Reference> a = Part.find(ref, 11L);
    Part<Reference> b = Part.find(ref, 12L);
    Part<Reference> c = Part.find(ref, 13L);
    Assert.assertNotNull(root);
    Assert.assertEquals("ROOT", root.title());
    Assert.assertNotNull(a);
    Assert.assertEquals("A", a.title());
    Assert.assertNotNull(b);
    Assert.assertEquals("B", b.title());
    Assert.assertNotNull(c);
    Assert.assertEquals("C", c.title());
    Assert.assertNull(Part.find(ref, 20L));
  }

}
