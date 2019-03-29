package org.pageseeder.psml.split;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.split.PSMLSplitter.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PSMLSplitterTest {
  private static final String SOURCE_FOLDER = "src/test/data/split";
  private static final String DEST_FOLDER = "build/test/split/result";
  private static final String COPY_FOLDER = "build/test/split/copy";
  private static final String WORKING_FOLDER = "build/test/split/working";

  @Test
  public void testConfigEmpty() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_single.psml");
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(new File(src, "psml-split-config-empty.xml"));
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/empty"), new File(DEST_FOLDER));
  }

  @Test
  public void testConfigSingleContainer() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_single.psml");
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(new File(src, "psml-split-config-single.xml"));
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/single"), new File(DEST_FOLDER));
  }

  @Test
  public void testConfigMultipleContainer() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_multiple.psml");
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(new File(src, "psml-split-config-multiple.xml"));
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/multiple"), new File(DEST_FOLDER));
  }

  /**
   * Compare .psml files in expected folder with actual folder including all subfolders.
   *
   * @param expected  the expected folder
   * @param actual    the actual folder
   *
   * @throws IOException  if problem reading files
   */
  private void compareFileTree(File expected, File actual) throws IOException {
    File[] files = expected.listFiles();
    for (File file : files) {
      File actual_file = new File (actual, file.getName());
      if (file.isDirectory()) {
        compareFileTree(file, actual_file);
      } else if (file.getName().endsWith(".psml")) {
        Assert.assertTrue("Expected file does not exist:\n" + actual_file.getAbsolutePath() + "\n", actual_file.exists());
        String expected_cont = new String (Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String actual_cont = new String (Files.readAllBytes(actual_file.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals("Expected file does not match:\n" + actual_file.getAbsolutePath() + "\n", expected_cont, actual_cont);
      } else {
        Assert.assertTrue("Expected file does not exist:\n" + actual_file.getAbsolutePath() + "\n", actual_file.exists());
      }
    }
  }
}
