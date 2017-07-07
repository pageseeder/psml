/*
 * Copyright 2017 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.process;

import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.config.Strip;
import org.pageseeder.psml.process.config.XRefsTransclude;
import org.xmlunit.matchers.EvaluateXPathMatcher;

public class ProcessTest {

  private static final String SOURCE_FOLDER = "src/test/data/process";
  private static final String DEST_FOLDER = "build/test/process/xrefs";
  private static final String COPY_FOLDER = "build/test/process/copy";

  public ProcessTest() {
  }

  @Test
  public void testNoProcess() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setSrc(copy);
    p.setDest(dest);
    p.process();

    // check results
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0a.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0a2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_1.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_1.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_3.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_3.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/META-INF/content.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/content.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/META-INF/manifest.xml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/manifest.xml").toPath()));
  }

  @Test
  public void testNoProcessPreserve() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(copy);
    p.setDest(dest);
    p.process();

    // check results
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0a.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0a2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_1.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_1.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_2.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_3.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_3.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/META-INF/content.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/content.psml").toPath()));
    Assert.assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/META-INF/manifest.xml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/manifest.xml").toPath()));
  }

  @Test
  public void testStripDocumentInfo() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "ref_0.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfo(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo)", equalTo("true")));
  }

  @Test
  public void testStripDocumentInfoDocids() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "ref_0.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoDocID(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@id)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@docid)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@id)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@docid)", equalTo("true")));
  }

  @Test
  public void testStripDocumentInfoLabels() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "ref_0.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoLabels(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/labels)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/labels)", equalTo("true")));
  }

  @Test
  public void testStripDocumentInfoDescription() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "ref_0.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoDescription(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/description)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/description)", equalTo("true")));
  }

  @Test
  public void testStripDocumentInfoTitle() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "ref_0.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoTitle(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@title)", equalTo("true")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/displaytitle)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/@title)", equalTo("true")));
    Assert.assertThat(xml, hasXPath("not(/document/documentinfo/uri/displaytitle)", equalTo("true")));
  }

  @Test
  public void testProcessXRefs() throws IOException, ProcessException {
    String filename = "ref_0.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
  }

  @Test
  public void testProcessXRefsAmbiguous() throws IOException, ProcessException {
    String filename = "ref_0a.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.setFailOnError(false);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[13]/@href", equalTo("#21931")));
  }

  @Test(expected=ProcessException.class)
  public void testProcessXRefsAmbiguousFail() throws IOException, ProcessException {
    String filename = "ref_0a.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();
  }

  @Test
  public void testProcessXRefsAmbiguous2() throws IOException, ProcessException {
    String filename = "ref_0a2.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.setFailOnError(false);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[13]/@href", equalTo("#21930")));
    Assert.assertThat(xml, hasXPath("(//blockxref)[14]/@href", equalTo("#21934")));
  }


  @Test(expected=ProcessException.class)
  public void testProcessXRefsAmbiguous2Fail() throws IOException, ProcessException {
    String filename = "ref_0a2.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

}
