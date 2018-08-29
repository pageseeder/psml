package org.pageseeder.psml.diff;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pageseeder.psml.process.util.IncludesExcludesMatcher;

public class DiffTest {

  private static final String SOURCE_FOLDER = "src/test/data/diff";
  private static final String DEST_FOLDER = "build/test/diff";

  @Test
  public void testDiffAll() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists()) {
      FileUtils.deleteDirectory(dest);
    }
    dest.mkdirs();
    d.setDest(dest);
    d.diff();
  }

  @Test
  public void testDiffExclude() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists()) {
      FileUtils.deleteDirectory(dest);
    }
    dest.mkdirs();
    d.setDest(dest);
    IncludesExcludesMatcher matcher = new IncludesExcludesMatcher();
    matcher.addExcludePattern("test*");
    d.setFilesMatcher(matcher);
    d.diff();
  }

  @Test
  public void testDiffInclude() throws IOException, DiffException {
    Diff d = new Diff();
    d.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists()) {
      FileUtils.deleteDirectory(dest);
    }
    dest.mkdirs();
    d.setDest(dest);
    IncludesExcludesMatcher matcher = new IncludesExcludesMatcher();
    matcher.addIncludePattern("compare_2.*");
    d.setFilesMatcher(matcher);
    d.diff();
  }

}
