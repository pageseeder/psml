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

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pageseeder.psml.process.config.*;
import org.pageseeder.psml.process.config.Images.ImageSrc;
import org.pageseeder.psml.toc.PublicationConfig;
import org.pageseeder.psml.toc.Tests;
import org.pageseeder.psml.toc.Tests.Validates;
import org.xmlunit.matchers.CompareMatcher;
import org.xmlunit.matchers.EvaluateXPathMatcher;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ProcessTest {

  private static final String SOURCE_FOLDER = "src/test/data/process";
  private static final String SOURCE_FOLDER_DIFF = "src/test/data/processdiff";
  private static final String DEST_FOLDER = "build/test/process/xrefs";
  private static final String MATH_FOLDER = "build/test/process/math";
  private static final String COPY_FOLDER = "build/test/process/copy";
  private static final String IMAGE_FOLDER = "build/test/process/image";

  public ProcessTest() {
  }

  @BeforeClass
  public static void setup() {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
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
  public void testStripReverseXRefs() throws IOException, ProcessException {
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
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripReverseXRefs(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/content/content_3.psml");
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    Assert.assertThat(xml, hasXPath("not(//reversexrefs)", equalTo("true")));
    Assert.assertThat(xml, hasXPath("not(//reversexref)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/images/diagram1.jpg.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    Assert.assertThat(xml, hasXPath("not(//reversexrefs)", equalTo("true")));
    Assert.assertThat(xml, hasXPath("not(//reversexref)", equalTo("true")));
  }

  @Test
  public void testMathMLNsPrefix() throws IOException, ProcessException {
    String filename = "mathml_ns_prefix.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER, "math"));
    File dest = new File(MATH_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File result = new File(MATH_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    //System.out.println(xml);
    // validate
    Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    Assert.assertThat(xml, hasXPath("namespace-uri(//*[local-name()='math'])", equalTo("http://www.w3.org/1998/Math/MathML")));
  }

  @Test
  public void testLevelProcessed() throws IOException, ProcessException {
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
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    p.setProcessed(true);
    Strip strip = new Strip();
    p.setStrip(strip); // set empty strip so metadata is processed
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/images space.psml");
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    Assert.assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("images space.psml")));
    Assert.assertThat(xml, hasXPath("//image[1]/@src", equalTo("images/diagram space.jpg")));
    Assert.assertThat(xml, hasXPath("//xref[1]/@href", equalTo("images space.psml")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/images/diagram space.jpg.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    Assert.assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("../images space.psml")));
  }

  @Test
  public void testLevelPortable() throws IOException, ProcessException {
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
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    p.setProcessed(false);
    Strip strip = new Strip();
    p.setStrip(strip); // set empty strip so metadata is processed
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/images space.psml");
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("portable")));
    Assert.assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("images%20space.psml")));
    Assert.assertThat(xml, hasXPath("//image[1]/@src", equalTo("images/diagram%20space.jpg")));
    Assert.assertThat(xml, hasXPath("//xref[1]/@href", equalTo("images%20space.psml")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/images/diagram space.jpg.psml");
    xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("document/@level", equalTo("metadata")));
    Assert.assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("../images%20space.psml")));
  }

  @Test
  public void testMathMLNoNsPrefix() throws IOException, ProcessException {
    String filename = "mathml_no_ns_prefix.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER, "math"));
    File dest = new File(MATH_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File result = new File(MATH_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    //System.out.println(xml);
    // validate
    Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    Assert.assertThat(xml, hasXPath("namespace-uri(//*[local-name()='math'])", equalTo("http://www.w3.org/1998/Math/MathML")));
  }

  @Test
  public void testAsciiMathConvert() throws IOException, ProcessException {
    String filename = "asciimath_conversion.psml";
    String filename_expected = "asciimath_conversion_result.psml";
    Process p = new Process();
    p.setConvertAsciiMath(true);
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER, "math"));
    File dest = new File(MATH_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File expected = new File(SOURCE_FOLDER + "/" + filename_expected);
    String xml_expected = new String (Files.readAllBytes(expected.toPath()), StandardCharsets.UTF_8);
    File result = new File(MATH_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
//    System.out.println(xml);
    // validate
    Assert.assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    Assert.assertThat(xml, hasXPath("count(inline[@label = 'asciimath'])", equalTo("0")));
    Assert.assertThat(xml, CompareMatcher.isSimilarTo(xml_expected).normalizeWhitespace());
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
    xrefs.setTypes("embed,transclude,alternate");
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
    Assert.assertThat(xml, hasXPath("count(//toc-part)", equalTo("22")));
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
    Assert.assertThat(xml, hasXPath("(//toc-part)[16][@level='2']/@title", equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[17][@level='3']/@title", equalTo("Content 4")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[18][@level='3']/@title", equalTo("Content 4.1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[19][@level='2']/@title", equalTo("Content 4 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[20][@level='3']/@title", equalTo("Content 4")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[21][@level='3']/@title", equalTo("Content 4.1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[22][@level='1']/@title", equalTo("TOC heading 2")));
    Assert.assertThat(xml, hasXPath("count(//heading)", equalTo("21")));
    Assert.assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id",        equalTo("21926-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[2][@prefix='1.1.'][@level='2']/@id",      equalTo("21927-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[3][not(@prefix)]/@level",                 equalTo("2")));
    Assert.assertThat(xml, hasXPath("(//heading)[4][@prefix='1.2.'][@level='2']/@id",      equalTo("21927-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[5][@prefix='1.2.1.'][@level='3']/@id",    equalTo("21934-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[6][@prefix='1.2.2.'][@level='3']/@id",    equalTo("21931-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[7][@prefix='2.'][@level='1']/@id",        equalTo("21926-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[8][@prefix='2.1.'][@level='2']/@id",      equalTo("21926-1-2-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[9][@prefix='(a)'][@level='5']/@id",       equalTo("21928-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[10][not(@prefix)]/@level",                equalTo("2")));
    Assert.assertThat(xml, hasXPath("(//heading)[11][@prefix='(i)'][@level='6']/@id",      equalTo("21930-4-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[12][@prefix='(A)'][@level='7']/@id",      equalTo("21934-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[13][@prefix='(ii)'][@level='6']/@id",     equalTo("21931-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[14][@prefix='3.'][@level='1']/@id",       equalTo("21926-1-2-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[15][@prefix='3.1.'][@level='2']/@id",     equalTo("21931-3-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[16][@prefix='3.1.2.'][@level='3']/@id",   equalTo("219350-1-1-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[17][@prefix='3.1.3.'][@level='3']/@id",   equalTo("219350-1-1-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[18][@prefix='3.1.4.'][@level='3']/@id",   equalTo("219350-2-1-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[19][@prefix='3.1.5.'][@level='3']/@id",   equalTo("219350-2-1-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[20][not(@prefix)]/@level",                equalTo("1")));
    Assert.assertThat(xml, hasXPath("(//heading)[21][@prefix='4.'][@level='1']/@id",       equalTo("21926-1-3-5")));
    Assert.assertThat(xml, hasXPath("count(//para[@numbered='true'])", equalTo("6")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[1][@indent=4]/@prefix",    equalTo("1.2.1.1.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[2][@indent=8]/@prefix",    equalTo("(I)")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[3][@indent=4]/@prefix",    equalTo("3.1.1.1.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[4][@indent=5]/@prefix",    equalTo("(a)")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[5][@indent=4]/@prefix",    equalTo("3.1.3.2.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[6][@indent=5]/@prefix",    equalTo("(a)")));
    Assert.assertThat(xml, hasXPath("count(//xref[@display='template'])",     equalTo("6")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[1]",       equalTo("1.2.1. Content 3")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[2]",       equalTo("2.1.1.1.(a)(i)(A) Content 3")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[3]",       equalTo("Parentnumber 2.1.1.1.(a) Prefix (i)")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[4]/@type", equalTo("alternate")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[5]",       equalTo("2.1.1.1.(a)(i)(A)(I)")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[6]",       equalTo("2.1.1.1.(a)(i)(A) Content 3")));
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
    xrefs.setTypes("embed,transclude,alternate");
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
    Assert.assertThat(xml, hasXPath("count(//toc-part)", equalTo("22")));
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
    Assert.assertThat(xml, hasXPath("(//toc-part)[16][@level='2']/@title", equalTo("")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[17][@level='3']/@title", equalTo("Content 4")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[18][@level='3']/@title", equalTo("Content 4.1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[19][@level='2']/@title", equalTo("Content 4 embed")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[20][@level='3']/@title", equalTo("Content 4")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[21][@level='3']/@title", equalTo("Content 4.1")));
    Assert.assertThat(xml, hasXPath("(//toc-part)[22][@level='1']/@title", equalTo("TOC heading 2")));
    Assert.assertThat(xml, hasXPath("count(//heading)", equalTo("21")));
    Assert.assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id",      equalTo("21926-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[2][@prefix='1.1.'][@level='2']/@id",    equalTo("21927-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[3][not(@prefix)]/@level",               equalTo("2")));
    Assert.assertThat(xml, hasXPath("(//heading)[4][@prefix='1.2.'][@level='2']/@id",    equalTo("21927-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[5][@prefix='2.'][@level='1']/@id",      equalTo("21934-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[6][@prefix='4.'][@level='1']/@id",      equalTo("21931-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[7][@prefix='5.'][@level='1']/@id",      equalTo("21926-1-2-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[8][@prefix='5.1.'][@level='2']/@id",    equalTo("21926-1-2-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[9][@prefix='5.1.1.'][@level='3']/@id",  equalTo("21928-1-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[10][not(@prefix)]/@level",              equalTo("2")));
    Assert.assertThat(xml, hasXPath("(//heading)[11][@prefix='5.1.2.'][@level='3']/@id", equalTo("21930-4-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[12][@prefix='5.1.3.'][@level='3']/@id", equalTo("21934-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[13][@prefix='5.1.5.'][@level='3']/@id", equalTo("21931-2-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[14][@prefix='6.'][@level='1']/@id",     equalTo("21926-1-2-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[15][@prefix='7.'][@level='1']/@id",     equalTo("21931-3-1-1")));
    Assert.assertThat(xml, hasXPath("(//heading)[16][@prefix='7.2.'][@level='2']/@id",   equalTo("219350-1-1-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[17][@prefix='7.3.'][@level='2']/@id",   equalTo("219350-1-1-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[18][@prefix='7.4.'][@level='2']/@id",   equalTo("219350-2-1-2")));
    Assert.assertThat(xml, hasXPath("(//heading)[19][@prefix='7.5.'][@level='2']/@id",   equalTo("219350-2-1-3")));
    Assert.assertThat(xml, hasXPath("(//heading)[20][not(@prefix)]/@level",              equalTo("1")));
    Assert.assertThat(xml, hasXPath("(//heading)[21][@prefix='8.'][@level='1']/@id",     equalTo("21926-1-3-5")));
    Assert.assertThat(xml, hasXPath("count(//para[@numbered='true'])", equalTo("6")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[1][@indent=1]/@prefix",    equalTo("3.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[2][@indent=3]/@prefix",    equalTo("5.1.4.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[3][@indent=3]/@prefix",    equalTo("7.1.1.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[4][@indent=3]/@prefix",    equalTo("7.3.1.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[5][@indent=3]/@prefix",    equalTo("7.3.2.")));
    Assert.assertThat(xml, hasXPath("(//para[@numbered='true'])[6][@indent=3]/@prefix",    equalTo("7.5.1.")));
    Assert.assertThat(xml, hasXPath("count(//xref[@display='template'])", equalTo("6")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[1]",       equalTo("2. Content 3")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[2]",       equalTo("5.1.3. Content 3")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[3]",       equalTo("Parentnumber  Prefix 5.1.2.")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[4]/@type", equalTo("alternate")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[5]",       equalTo("5.1.4.")));
    Assert.assertThat(xml, hasXPath("(//xref[@display='template'])[6]",       equalTo("5.1.3. Content 3")));
  }

  @Test
  public void testProcessDiff() throws IOException, ProcessException {
    String filename = "compare_ref.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER_DIFF));
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
    Assert.assertThat(xml, hasXPath("count(//diff//xref)", equalTo("9")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[1]", equalTo("1.3. Heading Ax")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[2]", equalTo("1.4. Heading B")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[3]", equalTo("1.2. Compare 2content.")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[4]", equalTo("1.2. Compare 2content.")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[5]", equalTo("2. Compare 2content.")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[6]", equalTo("3.3. Heading Ax")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[7]", equalTo("3.4. Heading B")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[8]", equalTo("3.2. Compare 2content.")));
    Assert.assertThat(xml, hasXPath("(//diff//xref)[9]", equalTo("3.2. Compare 2content.")));
   }

  @Test
  public void testProcessImagesFilename() throws IOException, ProcessException {
    String filename = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.FILENAME);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("diagram1.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("diagram1-2.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("diagram2.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("diagram1-3.jpg")));

    // check files
    Assert.assertTrue("Image 1 missing", new File(image, "diagram1.jpg").exists());
    Assert.assertTrue("Image 2 missing", new File(image, "diagram1-2.jpg").exists());
    Assert.assertTrue("Image 3 missing", new File(image, "diagram2.jpg").exists());
    Assert.assertTrue("Image 4 missing", new File(image, "diagram1-3.jpg").exists());
  }

  @Test
  public void testProcessImagesLocation() throws IOException, ProcessException {
    String filename = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.LOCATION);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("images/diagram1.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("images/test/diagram1.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("images/test/diagram2.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("images/test2/diagram1.jpg")));

    // check files
    Assert.assertTrue("Image 1 missing", new File(image, "images/diagram1.jpg").exists());
    Assert.assertTrue("Image 2 missing", new File(image, "images/test/diagram1.jpg").exists());
    Assert.assertTrue("Image 3 missing", new File(image, "images/test/diagram2.jpg").exists());
    Assert.assertTrue("Image 4 missing", new File(image, "images/test2/diagram1.jpg").exists());
  }

  @Test
  public void testProcessImagesPermalink() throws IOException, ProcessException {
    String filename = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.PERMALINK);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setSitePrefix("/ps");
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("/ps/uri/21941.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("/ps/uri/21942.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("/ps/uri/21943.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("/ps/uri/21944.jpg")));

    // check files
    Assert.assertTrue("Image 1 missing", new File(image, "21941.jpg").exists());
    Assert.assertTrue("Image 2 missing", new File(image, "21942.jpg").exists());
    Assert.assertTrue("Image 3 missing", new File(image, "21943.jpg").exists());
    Assert.assertTrue("Image 4 missing", new File(image, "21944.jpg").exists());
  }

  @Test
  public void testProcessImagesUriid() throws IOException, ProcessException {
    String filename = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.URIID);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("21941.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("21942.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("21943.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("21944.jpg")));

    // check files
    Assert.assertTrue("Image 1 missing", new File(image, "21941.jpg").exists());
    Assert.assertTrue("Image 2 missing", new File(image, "21942.jpg").exists());
    Assert.assertTrue("Image 3 missing", new File(image, "21943.jpg").exists());
    Assert.assertTrue("Image 4 missing", new File(image, "21944.jpg").exists());
  }

  @Test
  public void testProcessImagesUriidFolders() throws IOException, ProcessException {
    String filename = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.URIIDFOLDERS);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = new String (Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    Assert.assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("000/000/021/21941.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("000/000/021/21942.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("000/000/021/21943.jpg")));
    Assert.assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("000/000/021/21944.jpg")));

    // check files
    Assert.assertTrue("Image 1 missing", new File(image, "000/000/021/21941.jpg").exists());
    Assert.assertTrue("Image 2 missing", new File(image, "000/000/021/21942.jpg").exists());
    Assert.assertTrue("Image 3 missing", new File(image, "000/000/021/21943.jpg").exists());
    Assert.assertTrue("Image 4 missing", new File(image, "000/000/021/21944.jpg").exists());
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
    ErrorHandling error = new ErrorHandling();
    error.setXrefAmbiguous(true);
    p.setError(error);
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
    ErrorHandling error = new ErrorHandling();
    error.setXrefAmbiguous(true);
    p.setError(error);
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
