package org.pageseeder.psml.split;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pageseeder.psml.process.ProcessException;
import org.pageseeder.psml.split.PSMLSplitter.Builder;

import java.io.File;
import java.io.IOException;

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
    File copyfile = new File(copy, "split_source_1.psml");
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
  }

  @Test
  public void testConfigSingleContainer() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_1.psml");
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
  }

  @Test
  public void testConfigMultipleContainer() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_2.psml");
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
  }

}
