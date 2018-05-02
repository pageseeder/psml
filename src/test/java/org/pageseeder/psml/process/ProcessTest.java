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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.process.config.Strip;
import org.pageseeder.psml.process.config.XRefsTransclude;
import org.pageseeder.psml.process.config.XSLTTransformation;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.Tests.Validates;
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
  public void testGenerateTOC() throws IOException, ProcessException {
    String filename = "toc.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    // validate
    Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    // test xpaths
    Assert.assertThat(xml, hasXPath("count(//toc-tree)", equalTo("1")));
    Assert.assertThat(xml, hasXPath("//toc-tree/@title", equalTo("TOC Test")));
    Assert.assertThat(xml, hasXPath("count(//toc-part)", equalTo("15")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[1][@level='1']/@title",  equalTo("Ref 1 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[2][@level='2']/@title",  equalTo("Ref 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[3][@level='2']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[4][@level='3']/@title",  equalTo("Content 3")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[5][@level='3']/@title",  equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[6][@level='1']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[7][@level='2']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[8][@level='3']/@title",  equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[9][@level='4']/@title",  equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[10][@level='5']/@title", equalTo("Ref 2 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[11][@level='6']/@title", equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[12][@level='7']/@title", equalTo("Content 3")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[13][@level='6']/@title", equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[14][@level='1']/@title", equalTo("Ref 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[15][@level='2']/@title", equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("count(//heading)", equalTo("13")));
    Assert.assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id",        equalTo("21926-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[2][@prefix='1.1.'][@level='2']/@id",      equalTo("21927-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[3][@prefix='1.2.'][@level='2']/@id",      equalTo("21927-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[4][@prefix='1.2.1.'][@level='3']/@id",    equalTo("21934-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[5][@prefix='1.2.2.'][@level='3']/@id",    equalTo("21931-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[6][@prefix='2.'][@level='1']/@id",        equalTo("21926-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[7][@prefix='2.1.'][@level='2']/@id",      equalTo("21926-1-2-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[8][@prefix='(a)'][@level='5']/@id",       equalTo("21928-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[9][@prefix='(i)'][@level='6']/@id",       equalTo("21930-4-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[10][@prefix='(A)'][@level='7']/@id",      equalTo("21934-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[11][@prefix='(ii)'][@level='6']/@id",     equalTo("21931-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[12][@prefix='3.'][@level='1']/@id",       equalTo("21926-1-2-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[13][@prefix='3.1.'][@level='2']/@id",     equalTo("21931-3-1-1")));
  }

  @Test
  public void testGenerateTOCRelative() throws IOException, ProcessException {
    String filename = "toc.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    PublicationConfig config = Tests.parseConfig("publication-config-process-relative.xml");
    p.setPublicationConfig(config, filename, true);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    // validate
    Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    // test xpaths
    Assert.assertThat(xml, hasXPath("count(//toc-tree)", equalTo("1")));
    Assert.assertThat(xml, hasXPath("//toc-tree/@title", equalTo("TOC Test")));
    Assert.assertThat(xml, hasXPath("count(//toc-part)", equalTo("15")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[1][@level='1']/@title",  equalTo("Ref 1 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[2][@level='2']/@title",  equalTo("Ref 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[3][@level='2']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[4][@level='3']/@title",  equalTo("Content 3")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[5][@level='3']/@title",  equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[6][@level='1']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[7][@level='2']/@title",  equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[8][@level='3']/@title",  equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[9][@level='4']/@title",  equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[10][@level='5']/@title", equalTo("Ref 2 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[11][@level='6']/@title", equalTo("Content 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[12][@level='7']/@title", equalTo("Content 3")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[13][@level='6']/@title", equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[14][@level='1']/@title", equalTo("Ref 1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[15][@level='2']/@title", equalTo("Content 2")));
    Assert.assertThat(xml, hasXPath("count(//heading)", equalTo("13")));
    Assert.assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id",      equalTo("21926-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[2][@prefix='1.1.'][@level='2']/@id",    equalTo("21927-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[3][@prefix='1.2.'][@level='2']/@id",    equalTo("21927-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[4][@prefix='2.'][@level='1']/@id",      equalTo("21934-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[5][@prefix='3.'][@level='1']/@id",      equalTo("21931-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[6][@prefix='4.'][@level='1']/@id",      equalTo("21926-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[7][@prefix='4.1.'][@level='2']/@id",    equalTo("21926-1-2-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[8][@prefix='4.1.1.'][@level='3']/@id",  equalTo("21928-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[9][@prefix='4.1.2.'][@level='3']/@id",  equalTo("21930-4-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[10][@prefix='4.1.3.'][@level='3']/@id", equalTo("21934-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[11][@prefix='4.1.4.'][@level='3']/@id", equalTo("21931-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[12][@prefix='5.'][@level='1']/@id",     equalTo("21926-1-2-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[13][@prefix='6.'][@level='1']/@id",     equalTo("21931-3-1-1")));
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

  @Test
  public void testProcessXRefsMathml() throws IOException, ProcessException {
    String filename = "ref_3.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("math");
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("count(//xref)",                          equalTo("4")));
    Assert.assertThat(xml, hasXPath("count(//xref/media-fragment)",           equalTo("3")));
    Assert.assertThat(xml, hasXPath("(//xref)[1]/@href",                      equalTo("content/equation_1.mathml")));
    Assert.assertThat(xml, hasXPath("(//xref)[1]/media-fragment/@mediatype",  equalTo("application/mathml+xml")));
    Assert.assertThat(xml, hasXPath("count((//xref)[1]/media-fragment/math)", equalTo("1")));
    Assert.assertThat(xml, hasXPath("(//xref)[2]/@href",                      equalTo("content/equation_2.mml")));
    Assert.assertThat(xml, hasXPath("(//xref)[2]/media-fragment/@mediatype",  equalTo("application/mathml+xml")));
    Assert.assertThat(xml, hasXPath("count((//xref)[2]/media-fragment/math)", equalTo("1")));
    Assert.assertThat(xml, hasXPath("(//xref)[3]/@href",                      equalTo("content/equation_3.psml")));
    Assert.assertThat(xml, hasXPath("(//xref)[3]/@frag",                      equalTo("mathml")));
    Assert.assertThat(xml, hasXPath("(//xref)[3]/media-fragment/@mediatype",  equalTo("application/mathml+xml")));
    Assert.assertThat(xml, hasXPath("count((//xref)[3]/media-fragment/math)", equalTo("1")));
  }

  @Test
  public void testPostTransform() throws IOException, ProcessException {
    String filename = "content_2.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "/content"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform1.xsl");
    xslt.setIncludes(filename);
    p.setPostTransform(xslt);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//heading)[1]/@level", equalTo("3")));
  }

  @Test(expected=ProcessException.class)
  public void testPostTransformFail() throws IOException, ProcessException {
    String filename = "content_2.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "/content"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform2.xsl");
    xslt.setIncludes(filename);
    p.setPostTransform(xslt);
    p.process();
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

  public static Source getSchema(String filename) {
    try {
      String pathToSchema = "/org/pageseeder/psml/process/util/"+filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

}
