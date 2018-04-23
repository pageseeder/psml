package org.pageseeder.psml.toc;

import static org.pageseeder.psml.toc.Tests.assertPartEquals;
import static org.pageseeder.psml.toc.Tests.part;
import static org.pageseeder.psml.toc.Tests.phantom;
import static org.pageseeder.psml.toc.Tests.treeify;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public final class TreeExpanderTest {

  @Test
  public void testEmpty() {
    TreeExpander expander = new TreeExpander();
    List<Part<?>> top = expander.parts();
    Assert.assertTrue(top.size() == 0);
  }

  @Test
  public void testHeading1_single() {
    Heading heading = new Heading(1, "Hello", "title", "title", 0);
    Part<Heading> expected = part(heading);
    List<Part<?>> top = treeify(heading);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }

  @Test
  public void testHeading2_single() {
    Heading heading = new Heading(2, "Hello", "title", "title", 1);
    Part<Phantom> expected = phantom(1, part(heading));
    List<Part<?>> top = treeify(heading);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }

  @Test
  public void testHeading3_single() {
    Heading heading = new Heading(3, "Hello", "title", "title", 1);
    Part<Phantom> expected = phantom(1, phantom(2, part(heading)));
    List<Part<?>> top = treeify(heading);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }

  @Test
  public void testHeading4_single() {
    Heading heading = new Heading(4, "Hello", "title", "title", 1);
    Part<Phantom> expected = phantom(1, phantom(2, phantom(3, part(heading))));
    List<Part<?>> top = treeify(heading);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }

  @Test
  public void testHeading1_multiple() {
    Heading ha = new Heading(1, "A", "title", "title", 1);
    Heading hb = new Heading(1, "B", "content", "content", 1);
    Heading hc = new Heading(1, "C", "content", "content", 1);
    Part<Heading> pa = part(ha);
    Part<Heading> pb = part(hb);
    Part<Heading> pc = part(hc);
    List<Part<?>> top = treeify(ha, hb, hc);
    Assert.assertTrue(top.size() == 3);
    assertPartEquals(pa, top.get(0));
    assertPartEquals(pb, top.get(1));
    assertPartEquals(pc, top.get(2));
  }

  @Test
  public void testHeading2_multiple() {
    Heading ha = new Heading(2, "A", "title", "title", 1);
    Heading hb = new Heading(2, "B", "content", "content", 1);
    Heading hc = new Heading(2, "C", "content", "content", 1);
    Part<Phantom> expected = phantom(1, part(ha), part(hb), part(hc));
    List<Part<?>> top = treeify(ha, hb, hc);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }

  @Test
  public void testHeading1_and_2_multiple() {
    Heading ht = new Heading(1, "Title", "title", "title", 1);
    Heading ha = new Heading(2, "A", "content", "content", 1);
    Heading hb = new Heading(2, "B", "content", "content", 1);
    Heading hc = new Heading(2, "C", "content", "content", 1);
    Part<Heading> expected = part(ht, part(ha), part(hb), part(hc));
    List<Part<?>> top = treeify(ht, ha, hb, hc);
    Assert.assertTrue(top.size() == 1);
    assertPartEquals(expected, top.get(0));
  }


}
