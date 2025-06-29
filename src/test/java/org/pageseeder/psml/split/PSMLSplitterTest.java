package org.pageseeder.psml.split;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.pageseeder.psml.split.PSMLSplitter.Builder;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.Tests.Validates;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PSMLSplitterTest {
  private static final String SOURCE_FOLDER = "src/test/data/split";
  private static final String DEST_FOLDER = "build/test/split/result";
  private static final String COPY_FOLDER = "build/test/split/copy";
  private static final String WORKING_FOLDER = "build/test/split/working";

  @Test
  void testConfigEmpty() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_single.psml");
    File config = new File(src, "psml-split-config-empty.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/empty"), new File(DEST_FOLDER));
  }

  @Test
  void testConfigNoComponent() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_single.psml");
    File config = new File(src, "psml-split-config-no-component.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/no-component"), new File(DEST_FOLDER));
  }

  @Test
  void testConfigSingleContainer() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_single.psml");
    File config = new File(src, "psml-split-config-single.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/single"), new File(DEST_FOLDER));
  }

  @Test
  void testConfigSinglePropertiesMedia() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_properties_media.psml");
    File config = new File(src, "psml-split-config-single.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/props-media"), new File(DEST_FOLDER));
  }

  @Test
  void testConfigMultiplePropertiesMedia() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_properties_media.psml");
    File config = new File(src, "psml-split-config-props-media.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/multiple-props-media"), new File(DEST_FOLDER));
  }

  @Test
  void testConfigMultipleContainer() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_multiple.psml");
    File config = new File(src, "psml-split-config-multiple.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/multiple"), new File(DEST_FOLDER));
  }

  @Test
  void testXRefsPreserved() throws IOException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    File copyfile = new File(copy, "split_source_xrefs.psml");
    File config = new File(src, "psml-split-config-xrefs.xml");
    String config_xml = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);
    assertThat(Tests.toDOMSource(new StringReader(config_xml)), new Validates(getSchema("psml-split-config.xsd")));
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Builder b = new PSMLSplitter.Builder();
    b.source(copyfile);
    b.destination(dest);
    b.config(config);
    b.working(new File(WORKING_FOLDER));
    PSMLSplitter s = b.build();
    s.process();
    compareFileTree(new File(SOURCE_FOLDER, "expected/xrefs"), new File(DEST_FOLDER));
  }

  /**
   * Compare .psml files in expected folder with actual folder including all subfolders.
   *
   * @param expected the expected folder
   * @param actual   the actual folder
   * @throws IOException if problem reading files
   */
  private void compareFileTree(File expected, File actual) throws IOException {
    File[] files = expected.listFiles();
    for (File file : files) {
      File actual_file = new File(actual, file.getName());
      if (file.isDirectory()) {
        compareFileTree(file, actual_file);
      } else if (file.getName().endsWith(".psml")) {
        assertTrue(actual_file.exists(), "Expected file does not exist:\n" + actual_file.getAbsolutePath() + "\n");
        String expected_cont = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String actual_cont = new String(Files.readAllBytes(actual_file.toPath()), StandardCharsets.UTF_8);
        // normalize EOL chars
        expected_cont = expected_cont.replaceAll("\\r\\n", "\n");
        actual_cont = actual_cont.replaceAll("\\r\\n", "\n");
        assertEquals(expected_cont, actual_cont, "Expected file does not match:\n" + actual_file.getAbsolutePath() + "\n");
      } else {
        assertTrue(actual_file.exists(), "Expected file does not exist:\n" + actual_file.getAbsolutePath() + "\n");
      }
    }
  }

  static Source getSchema(String filename) {
    try {
      String pathToSchema = "/org/pageseeder/psml/split/" + filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

}
