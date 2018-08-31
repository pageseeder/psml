package org.pageseeder.psml.diff;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;
import org.xmlunit.matchers.EvaluateXPathMatcher;

public class DiffTest {

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

  @Test
  public void testDiffAll() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(false);

    // check results
    Assert.assertTrue(C1.exists());
    Assert.assertTrue(C2.exists());
    Assert.assertTrue(C3.exists());
    Assert.assertFalse(C4.exists());
    String xml = new String (Files.readAllBytes(C1.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("count(//diff)", equalTo("1")));
    xml = new String (Files.readAllBytes(C2.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("count(//diff)", equalTo("1")));
    xml = new String (Files.readAllBytes(C3.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("count(//diff)", equalTo("2")));
  }

  @Test
  public void testDiffOutputAll() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(true);

    // check results
    Assert.assertTrue(C1.exists());
    Assert.assertTrue(C2.exists());
    Assert.assertTrue(C3.exists());
    Assert.assertTrue(C4.exists());
  }

  @Test
  public void testDiffExclude() throws IOException, DiffException {
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
    Assert.assertFalse(C1.exists());
    Assert.assertTrue(C2.exists());
    Assert.assertTrue(C3.exists());
    Assert.assertFalse(C4.exists());
  }

  @Test
  public void testDiffInclude() throws IOException, DiffException {
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
    Assert.assertFalse(C1.exists());
    Assert.assertTrue(C2.exists());
    Assert.assertFalse(C3.exists());
    Assert.assertFalse(C4.exists());
  }

  @Test
  public void testDiffXRefs() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER_XREFS));
    if (DEST.exists()) {
      FileUtils.deleteDirectory(DEST);
    }
    DEST.mkdirs();
    d.setDest(DEST);
    d.addDiffElements(false);

    // check results
    Assert.assertFalse(CR.exists());
    Assert.assertTrue(C1.exists());
    Assert.assertTrue(C2.exists());
    Assert.assertFalse(MI.exists());
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

}
