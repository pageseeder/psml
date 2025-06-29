package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.ref;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class ReferenceTest {

  @Test
  void testRef_basic() {
    Part<Reference> ref = ref(1, "Hello", 23L);
    assertEquals(1, ref.level());
    assertEquals(23L, ref.element().uri());
    assertEquals("Hello", ref.title());
  }

  @Test
  void testFind() {
    Part<Reference> ref = ref(1, "ROOT", 10L, ref(2, "A", 11L), ref(2, "B", 12L), ref(2, "C", 13L));
    Part<Reference> root = Part.find(ref, 10L);
    Part<Reference> a = Part.find(ref, 11L);
    Part<Reference> b = Part.find(ref, 12L);
    Part<Reference> c = Part.find(ref, 13L);
    assertNotNull(root);
    assertEquals("ROOT", root.title());
    assertNotNull(a);
    assertEquals("A", a.title());
    assertNotNull(b);
    assertEquals("B", b.title());
    assertNotNull(c);
    assertEquals("C", c.title());
    assertNull(Part.find(ref, 20L));
  }

}
