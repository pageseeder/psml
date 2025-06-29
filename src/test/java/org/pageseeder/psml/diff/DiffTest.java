package org.pageseeder.psml.diff;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;
import org.xmlunit.matchers.EvaluateXPathMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffTest {

  private static final String SOURCE_FOLDER = "src/test/data/diff";
  private static final String SOURCE_FOLDER_XREFS = "src/test/data/diffxrefs";
  private static final String DEST_FOLDER = "build/test/diff";

  private static final File DEST = new File(DEST_FOLDER);
  private static final File CR = new File(DEST, "compare_ref.psml");
  private static final File C1 = new File(DEST, "test_compare_1.psml");
  private static final File C2 = new File(DEST, "compare_2.psml");
  private static final File C3 = new File(DEST, "compare_3.psml");
  private static final File C4 = new File(DEST, "compare_4.psml");
  private static final File MI = new File(DEST, "META-INF");

  private static final File DEST2 = new File(DEST_FOLDER + "2");
  private static final File CM = new File(DEST2, "compare_mathml.psml");

  @Test
  void testDiffAll() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(false);

    // check results
    assertTrue(C1.exists());
    assertTrue(C2.exists());
    assertTrue(C3.exists());
    assertFalse(C4.exists());
    String xml = Files.readString(C1.toPath());
    assertThat(xml, hasXPath("count(//diff)", equalTo("1")));
    xml = Files.readString(C2.toPath());
    assertThat(xml, hasXPath("count(//diff)", equalTo("1")));
    xml = Files.readString(C3.toPath());
    assertThat(xml, hasXPath("count(//diff)", equalTo("2")));
  }

  @Test
  void testDiffOutputAll() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(true);

    // check results
    assertTrue(C1.exists());
    assertTrue(C2.exists());
    assertTrue(C3.exists());
    assertTrue(C4.exists());
  }

  @Test
  void testDiffExclude() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    IncludesExcludesMatcher matcher = new IncludesExcludesMatcher();
    matcher.addExcludePattern("test*");
    d.setFilesMatcher(matcher);
    d.addDiffElements(false);

    // check results
    assertFalse(C1.exists());
    assertTrue(C2.exists());
    assertTrue(C3.exists());
    assertFalse(C4.exists());
  }

  @Test
  void testDiffInclude() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    IncludesExcludesMatcher matcher = new IncludesExcludesMatcher();
    matcher.addIncludePattern("compare_2.*");
    d.setFilesMatcher(matcher);
    d.addDiffElements(false);

    // check results
    assertFalse(C1.exists());
    assertTrue(C2.exists());
    assertFalse(C3.exists());
    assertFalse(C4.exists());
  }

  @Test
  void testDiffXRefs() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER_XREFS));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(false);

    // check results
    assertFalse(CR.exists());
    assertTrue(C1.exists());
    assertTrue(C2.exists());
    assertFalse(MI.exists());
  }

  @Test
  void testDiffMathML() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER + "2"));
    if (DEST2.exists()) {
      FileUtils.deleteDirectory(DEST2);
    }
    DEST2.mkdirs();
    d.setDest(DEST2);
    d.addDiffElements(false);

    // check results
    assertTrue(CM.exists());
    String xml = Files.readString(CM.toPath());
    System.out.println(xml);
    assertThat(xml, hasXPath("count(//diff)", equalTo("2")));
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

}
