package org.pageseeder.psml.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PSMLProcessHandler2Test {

  // relativisePath(path, currentLocation) computes the relative path from
  // currentLocation's directory to path.

  @Test
  void testRelativisePathSameDirectory() {
    assertEquals("target.psml", PSMLProcessHandler2.relativisePath("a/b/target.psml", "a/b/source.psml"));
  }

  @Test
  void testRelativisePathTargetInSubdirectory() {
    assertEquals("c/target.psml", PSMLProcessHandler2.relativisePath("a/b/c/target.psml", "a/b/source.psml"));
  }

  @Test
  void testRelativisePathTargetInParentDirectory() {
    assertEquals("../target.psml", PSMLProcessHandler2.relativisePath("a/target.psml", "a/b/source.psml"));
  }

  @Test
  void testRelativisePathTargetAtRoot() {
    assertEquals("../../target.psml", PSMLProcessHandler2.relativisePath("target.psml", "a/b/source.psml"));
  }

  @Test
  void testRelativisePathNoCommonPrefix() {
    assertEquals("../../x/y/target.psml", PSMLProcessHandler2.relativisePath("x/y/target.psml", "a/b/source.psml"));
  }

  @Test
  void testRelativisePathPartialCommonPrefix() {
    assertEquals("../c/d/target.psml", PSMLProcessHandler2.relativisePath("a/b/c/d/target.psml", "a/b/e/source.psml"));
  }

  @Test
  void testRelativisePathBothAtRoot() {
    assertEquals("target.psml", PSMLProcessHandler2.relativisePath("target.psml", "source.psml"));
  }

  @Test
  void testRelativisePathEmptyCurrentLocation() {
    assertEquals("a/b/target.psml", PSMLProcessHandler2.relativisePath("a/b/target.psml", ""));
  }

}
