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

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pageseeder.psml.process.config.*;
import org.pageseeder.psml.process.config.Images.ImageSrc;
import org.pageseeder.psml.process.math.AsciiMathConverter;
import org.pageseeder.psml.process.math.TexConverter;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessTest {

  private static final String SOURCE_FOLDER = "src/test/data/process";
  private static final String SOURCE_FOLDER_DIFF = "src/test/data/processdiff";
  private static final String SOURCE_FOLDER_META = "src/test/data/processmeta";
  private static final String DEST_FOLDER = "build/test/process/xrefs";
  private static final String MATH_FOLDER = "build/test/process/math";
  private static final String COPY_FOLDER = "build/test/process/copy";
  private static final String IMAGE_FOLDER = "build/test/process/image";

  @BeforeAll
  static void setup() {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
  }

  @Test
  void testNoProcess() throws IOException, ProcessException {
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
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0a.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_0a2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_1.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/ref_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_1.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/content/content_3.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_3.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/META-INF/content.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/content.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(SOURCE_FOLDER + "/META-INF/manifest.xml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/manifest.xml").toPath()));
  }

  @Test
  void testNoProcessPreserve() throws IOException, ProcessException {
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
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0a.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_0a2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_0a2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_1.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/ref_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/ref_2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_1.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_1.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_2.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_2.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/content/content_3.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/content/content_3.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/META-INF/content.psml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/content.psml").toPath()));
    assertArrayEquals(Files.readAllBytes(new File(COPY_FOLDER + "/META-INF/manifest.xml").toPath()),
        Files.readAllBytes(new File(DEST_FOLDER + "/META-INF/manifest.xml").toPath()));
  }

  @Test
  void testManifestDoc() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "mymanifest";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setSrc(copy);
    p.setDest(dest);
    ManifestDocument doc = new ManifestDocument();
    doc.setFilename(filename);
    doc.setExcludes("META-INF/**,*/META-INF/**");
    p.setManifestDoc(doc);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename + ".psml");
    String xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("count(/document/section/xref-fragment/blockxref)", equalTo("27")));
  }

  @Test
  void testManifestDocXrefs() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "mymanifest";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setSrc(copy);
    p.setDest(dest);
    ManifestDocument doc = new ManifestDocument();
    doc.setFilename(filename);
    doc.setIncludes("content/**");
    p.setManifestDoc(doc);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed");
    xrefs.setIncludes(filename + ".psml");
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename + ".psml");
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("count(/document/section/xref-fragment/blockxref/document)", equalTo("6")));
  }

  @Test
  void testStripDocumentInfo() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("not(/document/documentinfo)", equalTo("true")));
  }

  @Test
  void testStripDocumentInfoDocids() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@id)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@docid)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@id)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@docid)", equalTo("true")));
  }

  @Test
  void testStripDocumentInfoLabels() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/labels)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/labels)", equalTo("true")));
  }

  @Test
  void testStripDocumentInfoDescription() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/description)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/description)", equalTo("true")));
  }

  @Test
  void testStripDocumentInfoPublication() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "placeholders_pub.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoPublication(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("count(/document/documentinfo/publication)", equalTo("0")));
  }

  @Test
  void testStripDocumentInfoVersions() throws IOException, ProcessException {
    // make a copy of source docs so they can be moved
    File src = new File(SOURCE_FOLDER);
    File copy = new File(COPY_FOLDER);
    if (copy.exists())
      FileUtils.deleteDirectory(copy);
    FileUtils.copyDirectory(src, copy);
    // process
    String filename = "placeholders_pub.psml";
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    Process p = new Process();
    p.setPreserveSrc(false);
    p.setSrc(copy);
    p.setDest(dest);
    Strip strip = new Strip();
    strip.setStripDocumentInfoVersions(true);
    p.setStrip(strip);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("count(/document/documentinfo/versions)", equalTo("0")));
  }

  @Test
  void testStripDocumentInfoTitle() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@title)", equalTo("true")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/displaytitle)", equalTo("true")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/content.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("not(/document/documentinfo/uri)", equalTo("false")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/@title)", equalTo("true")));
    assertThat(xml, hasXPath("not(/document/documentinfo/uri/displaytitle)", equalTo("true")));
  }

  @Test
  void testStripReverseXRefs() throws IOException, ProcessException {
    String filename = "ref_0.psml";
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
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude,math");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    Strip strip = new Strip();
    strip.setStripReverseXRefs(true);
    p.setStrip(strip);
    p.process();

    // check xref result
    File result = new File(dest, filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("not(//reversexrefs)", equalTo("true")));
    assertThat(xml, hasXPath("not(//reversexref)", equalTo("true")));

    // check document result
    result = new File(dest, "/content/content_3.psml");
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("not(//reversexrefs)", equalTo("true")));
    assertThat(xml, hasXPath("not(//reversexref)", equalTo("true")));

    // check metadata result
    result = new File(dest, "/META-INF/images/diagram1.jpg.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("not(//reversexrefs)", equalTo("true")));
    assertThat(xml, hasXPath("not(//reversexref)", equalTo("true")));
  }

  @Test
  void testStripImageURIIDs() throws IOException, ProcessException {
    String filename = "images.psml";
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
    strip.setStripImagesURIID(true);
    p.setStrip(strip);
    p.process();

    // check xref result
    File result = new File(dest, filename);
    String xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("count(//image/@src)", equalTo("4")));
    assertThat(xml, hasXPath("count(//image/@uriid)", equalTo("0")));
    assertThat(xml, hasXPath("count(//image/@docid)", equalTo("2")));
  }

  @Test
  void testStripImageDocIDs() throws IOException, ProcessException {
    String filename = "images.psml";
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
    strip.setStripImagesDocID(true);
    p.setStrip(strip);
    p.process();

    // check xref result
    File result = new File(dest, filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("count(//image/@src)", equalTo("4")));
    assertThat(xml, hasXPath("count(//image/@uriid)", equalTo("4")));
    assertThat(xml, hasXPath("count(//image/@docid)", equalTo("0")));
  }

  @Test
  void testMathMLNsPrefix() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    //System.out.println(xml);
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    assertThat(xml, hasXPath("namespace-uri(//*[local-name()='math'])", equalTo("http://www.w3.org/1998/Math/MathML")));
  }

  @Test
  void testLevelProcessed() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("images space.psml")));
    assertThat(xml, hasXPath("//image[1]/@src", equalTo("images/diagram space.jpg")));
    assertThat(xml, hasXPath("//xref[1]/@href", equalTo("images space.psml")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/images/diagram space.jpg.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("../images space.psml")));
  }

  @Test
  void testLevelPortable() throws IOException, ProcessException {
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
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/images space.psml");
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("document/@level", equalTo("portable")));
    assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("images%20space.psml")));
    assertThat(xml, hasXPath("//image[1]/@src", equalTo("images/diagram%20space.jpg")));
    assertThat(xml, hasXPath("//xref[1]/@href", equalTo("images%20space.psml")));

    // check metadata result
    result = new File(DEST_FOLDER + "/META-INF/images/diagram space.jpg.psml");
    xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("document/@level", equalTo("metadata")));
    assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("../images%20space.psml")));
  }

  @Test
  void testMathMLNoNsPrefix() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    //System.out.println(xml);
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    assertThat(xml, hasXPath("namespace-uri(//*[local-name()='math'])", equalTo("http://www.w3.org/1998/Math/MathML")));
  }

  @Test
  void testAsciiMathConverter() throws IOException, ProcessException {
    //for (int i = 1; i < 1000; i++) {
    //  String math = "10=-2x+" + (1000 - i);
    //  if (i == 500) AsciiMathConverter.reset();
    //  AsciiMathConverter.convert(math);
    //}
    String math = "10=-2 (x+6)";
    System.out.println(AsciiMathConverter.convert(math));
  }

  @Test
  void testTexConverter() throws IOException, ProcessException {
    //String tex = "y = x ^ 2";
    //String tex = "\\frac{2}{3}";
    //String tex = "\\begin{aligned}a&=b+c\\\\a-c&=b\\end{aligned}";
    //String tex = "\\begin{eqnarray*}\n" +
    //    "a   &=& b+c\\\\\n" +
    //    "a-c &=& b\n" +
    //   "\\end{eqnarray*}";
    //String tex = "\\begin{aligned}a&=b+c\u00A0\\\\a-c&=b\\end{aligned}";
    //System.out.println(TexConverter.convert(tex));
    //tex = "\\begin{aligned}&4x-2=3x+1\\quad (-3x)\\\\&x-2=1\\quad (+2)\\\\&x=3\\end{aligned}";
    //System.out.println(TexConverter.convert(tex));
    //tex = "3(2x+1)=-17-4x\\\\ 6x+3=-17-4x\\hspace{30pt}(\\color{Red}+4x\\color{Black})\\\\ 10x+3=-17\\hspace{30pt}(\\color{Red}-3\\color{Black})\\\\ 10x=-20\\hspace{30pt}(\\color{Red}\\div 10\\color{Black})\\\\ x=-2";
    //System.out.println(TexConverter.convert(tex));
    //for (int i = 1; i < 10000; i++) {
    //  String tex = "10=-2x+" + (10000 - i);
    //  TexConverter.convert(tex);
    //}
    String tex = "10=-2 (x+6)";
    System.out.println(TexConverter.convert(tex));
  }

  @Test
  void testTexConvert() throws IOException, ProcessException {
    String filename = "katex_conversion.psml";
    String filename_expected = "katex_conversion_result.psml";
    Process p = new Process();
    p.setConvertTex(true);
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
    String xml_expected = Files.readString(expected.toPath());
    File result = new File(MATH_FOLDER + "/" + filename);
    String xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
//    System.out.println(xml);
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    assertThat(xml, hasXPath("count(inline[@label = 'tex'])", equalTo("0")));
    assertThat(xml, CompareMatcher.isSimilarTo(xml_expected).normalizeWhitespace());
  }

  @Test
  void testAsciiMathConvert() throws IOException, ProcessException {
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
    String xml_expected = Files.readString(expected.toPath());
    File result = new File(MATH_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
//    System.out.println(xml);
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    assertThat(xml, hasXPath("count(inline[@label = 'asciimath'])", equalTo("0")));
    assertThat(xml, CompareMatcher.isSimilarTo(xml_expected).normalizeWhitespace());
  }

  @Test
  void testAsciiMathConvertClassFail() throws IOException {
    Process p = new Process();
    p.setConvertAsciiMath(true);
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "error", "math1"));
    p.setPreserveSrc(true);
    File dest = new File(MATH_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    ErrorHandling error = new ErrorHandling();
    p.setError(error);
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testAsciiMathConvertIDFail() throws IOException {
    Process p = new Process();
    p.setConvertAsciiMath(true);
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "error", "math2"));
    p.setPreserveSrc(true);
    File dest = new File(MATH_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    ErrorHandling error = new ErrorHandling();
    p.setError(error);
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testGenerateTOC() throws IOException, ProcessException {
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
    String xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    //System.out.println(xml);
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    // test xpaths
    assertThat(xml, hasXPath("count(//toc-tree)", equalTo("1")));
    assertThat(xml, hasXPath("//toc-tree/@title", equalTo("TOC test")));
    assertThat(xml, hasXPath("count(//toc-part)", equalTo("22")));
    assertThat(xml, hasXPath("(//toc-part)[1][@level='1']/@title", equalTo("Ref 1 embed")));
    assertThat(xml, hasXPath("(//toc-part)[2][@level='2']/@title", equalTo("Ref 1")));
    assertThat(xml, hasXPath("(//toc-part)[3][@level='2']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[4][@level='3']/@title", equalTo("Content 3")));
    assertThat(xml, hasXPath("(//toc-part)[5][@level='3']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[6][@level='1']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[7][@level='2']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[8][@level='3']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[9][@level='4']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[10][@level='5']/@title", equalTo("Ref 2 embed")));
    assertThat(xml, hasXPath("(//toc-part)[11][@level='6']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[12][@level='7']/@title", equalTo("Content 3")));
    assertThat(xml, hasXPath("(//toc-part)[13][@level='6']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[14][@level='1']/@title", equalTo("Ref 1")));
    assertThat(xml, hasXPath("(//toc-part)[15][@level='2']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[16][@level='2']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[17][@level='3']/@title", equalTo("Content 4")));
    assertThat(xml, hasXPath("(//toc-part)[18][@level='3']/@title", equalTo("Content 4.1")));
    assertThat(xml, hasXPath("(//toc-part)[19][@level='2']/@title", equalTo("Content 4 embed")));
    assertThat(xml, hasXPath("(//toc-part)[20][@level='3']/@title", equalTo("Content 4")));
    assertThat(xml, hasXPath("(//toc-part)[21][@level='3']/@title", equalTo("Content 4.1")));
    assertThat(xml, hasXPath("(//toc-part)[22][@level='1']/@title", equalTo("TOC heading 2")));
    assertThat(xml, hasXPath("count(//heading)", equalTo("23")));
    assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id", equalTo("21926-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[2][not(@prefix)][@level='2']/@id", equalTo("21926-1-1-2")));
    assertThat(xml, hasXPath("(//heading)[3][not(@prefix)][@level='3']/@id", equalTo("21926-1-1-3")));
    assertThat(xml, hasXPath("(//heading)[4][not(@prefix)]/@level", equalTo("1")));
    assertThat(xml, hasXPath("(//heading)[5][@prefix='1.1.'][@level='2']/@id", equalTo("21927-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[6][not(@prefix)]/@level", equalTo("2")));
    assertThat(xml, hasXPath("(//heading)[7][@prefix='1.2.'][@level='2']/@id", equalTo("21927-1-2-1")));
    assertThat(xml, hasXPath("(//heading)[8][@prefix='1.2.1.'][@level='3']/@id", equalTo("21934-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[9][@prefix='1.2.2.'][@level='3']/@id", equalTo("21931-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[10][@prefix='2.'][@level='1']/@id", equalTo("21926-1-2-1")));
    assertThat(xml, hasXPath("(//heading)[11][@prefix='2.1.'][@level='2']/@id", equalTo("21926-1-2-2")));
    assertThat(xml, hasXPath("(//heading)[12][@prefix='(a)'][@level='5']/@id", equalTo("21928-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[13][not(@prefix)]/@level", equalTo("2")));
    assertThat(xml, hasXPath("(//heading)[14][@prefix='(i)'][@level='6']/@id", equalTo("21930-4-1-1")));
    assertThat(xml, hasXPath("(//heading)[15][@prefix='(A)'][@level='7']/@id", equalTo("21934-2-1-1")));
    assertThat(xml, hasXPath("(//heading)[16][@prefix='(ii)'][@level='6']/@id", equalTo("21931-2-1-1")));
    assertThat(xml, hasXPath("(//heading)[17][@prefix='3.'][@level='1']/@id", equalTo("21926-1-2-3")));
    assertThat(xml, hasXPath("(//heading)[18][@prefix='3.1.'][@level='2']/@id", equalTo("21931-3-1-1")));
    assertThat(xml, hasXPath("(//heading)[19][@prefix='3.1.2.'][@level='3']/@id", equalTo("219350-1-1-2")));
    assertThat(xml, hasXPath("(//heading)[20][@prefix='3.1.3.'][@level='3']/@id", equalTo("219350-1-1-3")));
    assertThat(xml, hasXPath("(//heading)[21][@prefix='3.1.4.'][@level='3']/@id", equalTo("219350-2-1-2")));
    assertThat(xml, hasXPath("(//heading)[22][@prefix='3.1.5.'][@level='3']/@id", equalTo("219350-2-1-3")));
    assertThat(xml, hasXPath("(//heading)[23][@prefix='4.'][@level='1']/@id", equalTo("21926-1-3-3")));
    assertThat(xml, hasXPath("count(//para[@numbered='true'])", equalTo("7")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[1][@indent=4]/@prefix", equalTo("1.2.1.1&")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[2][@indent=8]/@prefix", equalTo("(I)")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[3][@indent=4]/@prefix", equalTo("3.1.1.1&")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[4][@indent=5]/@prefix", equalTo("(a)")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[5][@indent=4]/@prefix", equalTo("3.1.3.2&")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[6][@indent=5]/@prefix", equalTo("(a)")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[7]/@prefix", equalTo("")));
    assertThat(xml, hasXPath("count(//xref[@display='template'])", equalTo("7")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[1]", equalTo("Parentnumber 2.1.1.1&(a) Prefix (i)")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[2]/@type", equalTo("alternate")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[3]", equalTo("1.2.1. Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[3]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[4]", equalTo("2.1.1.1&(a)(i)(A) Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[4]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[5]", equalTo("2.1.1.1&(a)(i)(A)(I)")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[6]", equalTo("2.1.1.1&(a)(i)(A) Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[6]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[7]", equalTo("1.3.2 Accounts")));
  }

  @Test
  void testGenerateTOCRelative() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    // validate
    assertThat(Tests.toDOMSource(new StringReader(xml)), new Validates(getSchema("psml-processed.xsd")));
    // test xpaths
    assertThat(xml, hasXPath("count(//toc-tree)", equalTo("1")));
    assertThat(xml, hasXPath("//toc-tree/@title", equalTo("TOC test")));
    assertThat(xml, hasXPath("count(//toc-part)", equalTo("22")));
    assertThat(xml, hasXPath("(//toc-part)[1][@level='1']/@title", equalTo("Ref 1 embed")));
    assertThat(xml, hasXPath("(//toc-part)[2][@level='2']/@title", equalTo("Ref 1")));
    assertThat(xml, hasXPath("(//toc-part)[3][@level='2']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[4][@level='3']/@title", equalTo("Content 3")));
    assertThat(xml, hasXPath("(//toc-part)[5][@level='3']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[6][@level='1']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[7][@level='2']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[8][@level='3']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[9][@level='4']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[10][@level='5']/@title", equalTo("Ref 2 embed")));
    assertThat(xml, hasXPath("(//toc-part)[11][@level='6']/@title", equalTo("Content 1")));
    assertThat(xml, hasXPath("(//toc-part)[12][@level='7']/@title", equalTo("Content 3")));
    assertThat(xml, hasXPath("(//toc-part)[13][@level='6']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[14][@level='1']/@title", equalTo("Ref 1")));
    assertThat(xml, hasXPath("(//toc-part)[15][@level='2']/@title", equalTo("Content 2")));
    assertThat(xml, hasXPath("(//toc-part)[16][@level='2']/@title", equalTo("")));
    assertThat(xml, hasXPath("(//toc-part)[17][@level='3']/@title", equalTo("Content 4")));
    assertThat(xml, hasXPath("(//toc-part)[18][@level='3']/@title", equalTo("Content 4.1")));
    assertThat(xml, hasXPath("(//toc-part)[19][@level='2']/@title", equalTo("Content 4 embed")));
    assertThat(xml, hasXPath("(//toc-part)[20][@level='3']/@title", equalTo("Content 4")));
    assertThat(xml, hasXPath("(//toc-part)[21][@level='3']/@title", equalTo("Content 4.1")));
    assertThat(xml, hasXPath("(//toc-part)[22][@level='1']/@title", equalTo("TOC heading 2")));
    assertThat(xml, hasXPath("count(//heading)", equalTo("23")));
    assertThat(xml, hasXPath("(//heading)[1][not(@prefix)][@level='1']/@id", equalTo("21926-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[2][not(@prefix)][@level='2']/@id", equalTo("21926-1-1-2")));
    assertThat(xml, hasXPath("(//heading)[3][not(@prefix)][@level='3']/@id", equalTo("21926-1-1-3")));
    assertThat(xml, hasXPath("(//heading)[4][not(@prefix)]/@level", equalTo("1")));
    assertThat(xml, hasXPath("(//heading)[5][@prefix='1.1.'][@level='2']/@id", equalTo("21927-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[6][not(@prefix)]/@level", equalTo("2")));
    assertThat(xml, hasXPath("(//heading)[7][@prefix='1.2.'][@level='2']/@id", equalTo("21927-1-2-1")));
    assertThat(xml, hasXPath("(//heading)[8][@prefix='2.'][@level='1']/@id", equalTo("21934-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[9][@prefix='4.'][@level='1']/@id", equalTo("21931-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[10][@prefix='5.'][@level='1']/@id", equalTo("21926-1-2-1")));
    assertThat(xml, hasXPath("(//heading)[11][@prefix='5.1.'][@level='2']/@id", equalTo("21926-1-2-2")));
    assertThat(xml, hasXPath("(//heading)[12][@prefix='5.1.1.'][@level='3']/@id", equalTo("21928-1-1-1")));
    assertThat(xml, hasXPath("(//heading)[13][not(@prefix)]/@level", equalTo("2")));
    assertThat(xml, hasXPath("(//heading)[14][@prefix='5.1.2.'][@level='3']/@id", equalTo("21930-4-1-1")));
    assertThat(xml, hasXPath("(//heading)[15][@prefix='5.1.3.'][@level='3']/@id", equalTo("21934-2-1-1")));
    assertThat(xml, hasXPath("(//heading)[16][@prefix='5.1.5.'][@level='3']/@id", equalTo("21931-2-1-1")));
    assertThat(xml, hasXPath("(//heading)[17][@prefix='6.'][@level='1']/@id", equalTo("21926-1-2-3")));
    assertThat(xml, hasXPath("(//heading)[18][@prefix='7.'][@level='1']/@id", equalTo("21931-3-1-1")));
    assertThat(xml, hasXPath("(//heading)[19][@prefix='7.2.'][@level='2']/@id", equalTo("219350-1-1-2")));
    assertThat(xml, hasXPath("(//heading)[20][@prefix='7.3.'][@level='2']/@id", equalTo("219350-1-1-3")));
    assertThat(xml, hasXPath("(//heading)[21][@prefix='7.4.'][@level='2']/@id", equalTo("219350-2-1-2")));
    assertThat(xml, hasXPath("(//heading)[22][@prefix='7.5.'][@level='2']/@id", equalTo("219350-2-1-3")));
    assertThat(xml, hasXPath("(//heading)[23][@prefix='8.'][@level='1']/@id", equalTo("21926-1-3-3")));
    assertThat(xml, hasXPath("count(//para[@numbered='true'])", equalTo("7")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[1][@indent=1]/@prefix", equalTo("3.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[2][@indent=3]/@prefix", equalTo("5.1.4.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[3][@indent=3]/@prefix", equalTo("7.1.1.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[4][@indent=3]/@prefix", equalTo("7.3.1.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[5][@indent=3]/@prefix", equalTo("7.3.2.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[6][@indent=3]/@prefix", equalTo("7.5.1.")));
    assertThat(xml, hasXPath("(//para[@numbered='true'])[7]/@prefix", equalTo("9.")));
    assertThat(xml, hasXPath("count(//xref[@display='template'])", equalTo("7")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[1]", equalTo("Parentnumber  Prefix 5.1.2.")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[2]/@type", equalTo("alternate")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[3]", equalTo("2. Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[3]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[4]", equalTo("5.1.3. Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[4]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[5]", equalTo("5.1.4.")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[6]", equalTo("5.1.3. Content 3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[6]/sup", equalTo("3")));
    assertThat(xml, hasXPath("(//xref[@display='template'])[7]", equalTo("1.3.2 Accounts")));
  }

  @Test
  void testProcessDiff() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("count(//diff//xref)", equalTo("9")));
    assertThat(xml, hasXPath("(//diff//xref)[1]", equalTo("1.3. Heading Ax")));
    assertThat(xml, hasXPath("(//diff//xref)[2]", equalTo("1.4. Heading B")));
    assertThat(xml, hasXPath("(//diff//xref)[3]", equalTo("1.2. Compare 2content.")));
    assertThat(xml, hasXPath("(//diff//xref)[4]", equalTo("1.2. Compare 2content.")));
    assertThat(xml, hasXPath("(//diff//xref)[5]", equalTo("2. Compare 2content.")));
    assertThat(xml, hasXPath("(//diff//xref)[6]", equalTo("3.3. Heading Ax")));
    assertThat(xml, hasXPath("(//diff//xref)[7]", equalTo("3.4. Heading B")));
    assertThat(xml, hasXPath("(//diff//xref)[8]", equalTo("3.2. Compare 2content.")));
    assertThat(xml, hasXPath("(//diff//xref)[9]", equalTo("3.2. Compare 2content.")));
  }

  @Test
  void testProcessImagesFilename() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("diagram1.jpg")));
    assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("diagram1-2.jpg")));
    assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("diagram2.jpg")));
    assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("diagram1-3.jpg")));
    assertThat(xml, hasXPath("count(//image/@uriid)", equalTo("4")));

    // check files
    assertTrue(new File(image, "diagram1.jpg").exists(), "Image 1 missing");
    assertTrue(new File(image, "diagram1-2.jpg").exists(), "Image 2 missing");
    assertTrue(new File(image, "diagram2.jpg").exists(), "Image 3 missing");
    assertTrue(new File(image, "diagram1-3.jpg").exists(), "Image 4 missing");
  }

  @Test
  void testProcessImagesFilenameEncode() throws IOException, ProcessException {
    String filename = "images space.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setImageSrc(ImageSrc.FILENAMEENCODE);
    File image = new File(IMAGE_FOLDER);
    if (image.exists())
      FileUtils.deleteDirectory(image);
    images.setLocation(image.getAbsolutePath());
    images.setIncludes(filename);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("document/@level", equalTo("processed")));
    assertThat(xml, hasXPath("//reversexref[1]/@href", equalTo("images space.psml")));
    assertThat(xml, hasXPath("//image[1]/@src", equalTo("diagram%20space.jpg")));
    assertThat(xml, hasXPath("//xref[1]/@href", equalTo("images space.psml")));

    // check files
    assertTrue(new File(image, "diagram%20space.jpg").exists(), "Image 1 missing");
  }

  @Test
  void testProcessImagesLocation() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("images/diagram1.jpg")));
    assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("images/test/diagram1.jpg")));
    assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("images/test/diagram2.jpg")));
    assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("images/test2/diagram1.jpg")));

    // check files
    assertTrue(new File(image, "images/diagram1.jpg").exists(), "Image 1 missing");
    assertTrue(new File(image, "images/test/diagram1.jpg").exists(), "Image 2 missing");
    assertTrue(new File(image, "images/test/diagram2.jpg").exists(), "Image 3 missing");
    assertTrue(new File(image, "images/test2/diagram1.jpg").exists(), "Image 4 missing");
  }

  @Test
  void testProcessImagesPermalink() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("/ps/uri/21941.jpg")));
    assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("/ps/uri/21942.jpg")));
    assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("/ps/uri/21943.jpg")));
    assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("/ps/uri/21944.jpg")));

    // check files
    assertTrue(new File(image, "21941.jpg").exists(), "Image 1 missing");
    assertTrue(new File(image, "21942.jpg").exists(), "Image 2 missing");
    assertTrue(new File(image, "21943.jpg").exists(), "Image 3 missing");
    assertTrue(new File(image, "21944.jpg").exists(), "Image 4 missing");
  }

  @Test
  void testProcessImagesUriid() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("21941.jpg")));
    assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("21942.jpg")));
    assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("21943.jpg")));
    assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("21944.jpg")));

    // check files
    assertTrue(new File(image, "21941.jpg").exists(), "Image 1 missing");
    assertTrue(new File(image, "21942.jpg").exists(), "Image 2 missing");
    assertTrue(new File(image, "21943.jpg").exists(), "Image 3 missing");
    assertTrue(new File(image, "21944.jpg").exists(), "Image 4 missing");
  }

  @Test
  void testProcessImagesUriidFolders() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//image)[1]/@src", equalTo("000/000/021/21941.jpg")));
    assertThat(xml, hasXPath("(//image)[2]/@src", equalTo("000/000/021/21942.jpg")));
    assertThat(xml, hasXPath("(//image)[3]/@src", equalTo("000/000/021/21943.jpg")));
    assertThat(xml, hasXPath("(//image)[4]/@src", equalTo("000/000/021/21944.jpg")));

    // check files
    assertTrue(new File(image, "000/000/021/21941.jpg").exists(), "Image 1 missing");
    assertTrue(new File(image, "000/000/021/21942.jpg").exists(), "Image 2 missing");
    assertTrue(new File(image, "000/000/021/21943.jpg").exists(), "Image 3 missing");
    assertTrue(new File(image, "000/000/021/21944.jpg").exists(), "Image 4 missing");
  }

  @Test
  void testProcessEmbedLinkMetadata() throws IOException, ProcessException {
    String filename = "links.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "/links"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    p.setEmbedLinkMetadata(true);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("alternate");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//link)[1]/document/documentinfo/uri/@id", equalTo("475607")));
    assertThat(xml, hasXPath("(//link)[2]/document/documentinfo/uri/@id", equalTo("475600")));
    assertThat(xml, hasXPath("(//link)[3]/document/documentinfo/uri/@id", equalTo("475609")));
    assertThat(xml, hasXPath("(//link)[4]/document/documentinfo/uri/@id", equalTo("475616")));
    assertThat(xml, hasXPath("(//xref)[1]/document/documentinfo/uri/@id", equalTo("219401")));
  }

  @Test
  void testProcessEmbedLinkMetadataPre() throws IOException, ProcessException {
    String filename = "links.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "/links"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    p.setEmbedLinkMetadata(true);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform1.xsl");
    p.setPreTransform(xslt);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("alternate");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//link)[1]/document/documentinfo/uri/@id", equalTo("475607")));
    assertThat(xml, hasXPath("(//link)[2]/document/documentinfo/uri/@id", equalTo("475600")));
    assertThat(xml, hasXPath("(//link)[3]/document/documentinfo/uri/@id", equalTo("475609")));
    assertThat(xml, hasXPath("(//link)[4]/document/documentinfo/uri/@id", equalTo("475616")));
    assertThat(xml, hasXPath("(//xref)[1]/document/documentinfo/uri/@id", equalTo("219401")));
    assertThat(xml, hasXPath("(//displaytitle)[1]", equalTo("x")));
    assertThat(xml, hasXPath("(//displaytitle)[2]", equalTo("x")));
    assertThat(xml, hasXPath("(//displaytitle)[3]", equalTo("x")));
  }

  @Test
  void testProcessImagesEmbedMetadata() throws IOException, ProcessException {
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER_META));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    Images images = new Images();
    images.setEmbedMetadata(true);
    p.setImages(images);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/content/content_1.psml");
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("//image/@src", equalTo("../images/diagram1.jpg")));
    assertThat(xml, hasXPath("//image//property[@name='hi-res']/xref/@href",
        equalTo("../images/diagram space.jpg")));
  }

  @Test
  void testProcessXRefsMetadataLevel() throws IOException, ProcessException {
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER_META));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("none,alternate");
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/META-INF/images/diagram1.jpg.psml");
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//xref)[1]//documentinfo/uri/@id", equalTo("219290")));
    assertThat(xml, hasXPath("(//xref)[2]//documentinfo/uri/@id", equalTo("219290")));
    assertThat(xml, hasXPath("(//xref)[3]//documentinfo/uri/@id", equalTo("21930")));
  }

  @Test
  void testProcessXRefs() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
    assertThat(xml, hasXPath("(//xref)[1]/@href", equalTo("#21934-1")));
    assertThat(xml, hasXPath("(//xref)[2]/@href", equalTo("#21931")));
    assertThat(xml, hasXPath("(//xref)[3]/@href", equalTo("content/content_5.psml")));
    assertThat(xml, hasXPath("(//xref)[4]/@href", equalTo("#2_21934-1")));
    assertThat(xml, hasXPath("(//xref)[5]/@href", equalTo("#2_21931")));
    assertThat(xml, hasXPath("(//xref)[6]/@href", equalTo("content/content_5.psml")));
  }

  @Test
  void testProcessXRefsAmbiguous() throws IOException, ProcessException {
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
    //ErrorHandling err = new ErrorHandling();
    //err.setXrefAmbiguous(true);
    //p.setError(err);
    //WarningHandling warn = new WarningHandling();
    //warn.setXrefAmbiguous(false);
    //p.setWarning(warn);
    p.setFailOnError(false);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
    assertThat(xml, hasXPath("(//blockxref)[13]/@href", equalTo("#21931")));
  }

  @Test
  void testProcessXRefsAmbiguousFail() throws IOException {
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
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testProcessXRefsAmbiguous2() throws IOException, ProcessException {
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
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//blockxref)[1]/@href", equalTo("ref_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[2]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[3]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[4]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[5]/@href", equalTo("#21930")));
    assertThat(xml, hasXPath("(//blockxref)[6]/@href", equalTo("#21934")));
    assertThat(xml, hasXPath("(//blockxref)[7]/@href", equalTo("ref_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[8]/@href", equalTo("content/content_1.psml")));
    assertThat(xml, hasXPath("(//blockxref)[9]/@href", equalTo("content/content_3.psml")));
    assertThat(xml, hasXPath("(//blockxref)[10]/@href", equalTo("content/content_2.psml")));
    assertThat(xml, hasXPath("(//blockxref)[11]/@href", equalTo("#2_21930")));
    assertThat(xml, hasXPath("(//blockxref)[12]/@href", equalTo("#2_21934")));
    assertThat(xml, hasXPath("(//blockxref)[13]/@href", equalTo("#21930")));
    assertThat(xml, hasXPath("(//blockxref)[14]/@href", equalTo("#21934")));
  }

  @Test
  void testProcessXRefsAmbiguous2Fail() throws IOException {
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
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testProcessXRefsLoopFail() throws IOException {
    String filename = "ref_5.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "error/loop"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testXRefsImagesNotFound() throws IOException, ProcessException {
    String filename = "ref_6.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER + "error/notfound"));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes(filename);
    p.setXrefs(xrefs);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    //ErrorHandling err = new ErrorHandling();
    //err.setImageNotFound(true);
    //p.setError(err);
    WarningHandling warn = new WarningHandling();
    warn.setXrefNotFound(false);
    warn.setImageNotFound(false);
    p.setWarning(warn);
    //p.setFailOnError(false);
    p.process();
  }

  /**
   * Test processing PSML with pre-transcluded content from process-publication=true export.
   */
  @Test
  void testProcessXRefsPretranscluded() throws IOException, ProcessException {
    String filename = "transclude.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed");
    p.setXrefs(xrefs);
    p.setConvertAsciiMath(true);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//fragment)[1]/@id", equalTo("988295-1")));
    assertThat(xml, hasXPath("(//fragment)[2]/@id", equalTo("988295-3")));
    assertThat(xml, hasXPath("(//fragment)[3]/@id", equalTo("921771-3")));
    assertThat(xml, hasXPath("(//fragment)[4]/@id", equalTo("988297-1")));
    assertThat(xml, hasXPath("(//fragment)[5]/@id", equalTo("988297-2")));
    assertThat(xml, hasXPath("(//fragment)[6]/@id", equalTo("988295-4")));
    assertThat(xml, hasXPath("(//fragment)[7]/@id", equalTo("988295-5")));
    assertThat(xml, hasXPath("(//fragment)[8]/@id", equalTo("988295-2")));
    assertThat(xml, hasXPath("(//fragment)[9]/@id", equalTo("2_921771-3")));
    assertThat(xml, hasXPath("(//fragment)[10]/@id", equalTo("2_988297-1")));
    assertThat(xml, hasXPath("(//fragment)[11]/@id", equalTo("2_988297-2")));
    assertThat(xml, hasXPath("(//media-fragment)[1]/@id", equalTo("media")));
    assertThat(xml, hasXPath("(//media-fragment)[2]/@id", equalTo("227-1")));
    assertThat(xml, hasXPath("(//media-fragment)[3]/@id", equalTo("208-1")));
    assertThat(xml, hasXPath("(//heading)[2]/@level", equalTo("2")));
    assertThat(xml, hasXPath("(//heading)[3]/@level", equalTo("1")));
    assertThat(xml, hasXPath("(//heading)[4]/@level", equalTo("3")));
    assertThat(xml, hasXPath("(//heading)[5]/@level", equalTo("1")));
    assertThat(xml, hasXPath("(//xref[@type='none'])[1]/@href", equalTo("#921771-3")));
    assertThat(xml, hasXPath("(//xref[@type='none'])[2]/@href", equalTo("#988297-1")));
    assertThat(xml, hasXPath("(//xref[@type='none'])[3]/@href", equalTo("#2_988297-1")));
  }

  @Test
  void testProcessXRefsMathml() throws IOException, ProcessException {
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
    String xml = new String(Files.readAllBytes(result.toPath()), StandardCharsets.UTF_8);
    assertThat(xml, hasXPath("count(//xref)", equalTo("4")));
    assertThat(xml, hasXPath("count(//xref/media-fragment)", equalTo("3")));
    assertThat(xml, hasXPath("(//xref)[1]/@href", equalTo("content/equation_1.mathml")));
    assertThat(xml, hasXPath("(//xref)[1]/media-fragment/@mediatype", equalTo("application/mathml+xml")));
    assertThat(xml, hasXPath("count((//xref)[1]/media-fragment/math)", equalTo("1")));
    assertThat(xml, hasXPath("(//xref)[2]/@href", equalTo("content/equation_2.mml")));
    assertThat(xml, hasXPath("(//xref)[2]/media-fragment/@mediatype", equalTo("application/mathml+xml")));
    assertThat(xml, hasXPath("count((//xref)[2]/media-fragment/math)", equalTo("1")));
    assertThat(xml, hasXPath("(//xref)[3]/@href", equalTo("content/equation_3.psml")));
    assertThat(xml, hasXPath("(//xref)[3]/@frag", equalTo("mathml")));
    assertThat(xml, hasXPath("(//xref)[3]/media-fragment/@mediatype", equalTo("application/mathml+xml")));
    assertThat(xml, hasXPath("count((//xref)[3]/media-fragment/math)", equalTo("1")));
  }

  @Test
  void testPlaceholders() throws IOException, ProcessException {
    String filename = "placeholders.psml";
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
    p.setXrefs(xrefs);
    p.setPlaceholders(true);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    //System.out.println(xml);
    assertThat(xml, hasXPath("count(//toc-part)", equalTo("5")));
    assertThat(xml, hasXPath("(//toc-part)[1]/@title", equalTo("Content doc first 1")));
    assertThat(xml, hasXPath("(//toc-part)[2]/@title", equalTo("Placeholder 2  here.")));
    assertThat(xml, hasXPath("(//toc-part)[3]/@title", equalTo("Content root first 1")));
    assertThat(xml, hasXPath("(//toc-part)[4]/@title", equalTo("Placeholder 2 root second here.")));
    assertThat(xml, hasXPath("(//toc-part)[5]/@title", equalTo("Placeholder 1 root first here.")));
    assertThat(xml, hasXPath("count(//placeholder[@unresolved='true'])", equalTo("24")));
    assertThat(xml, hasXPath("(//placeholder)[1][@unresolved='true']", equalTo("")));
    assertThat(xml, hasXPath("(//placeholder)[2][@unresolved='true']", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[3][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[4][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[5][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[6][@unresolved='true']", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[7][@unresolved='true']", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[8][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[9][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[10][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[11]", equalTo("doc first")));
    assertThat(xml, hasXPath("(//placeholder)[12]", equalTo("")));
    assertThat(xml, hasXPath("(//placeholder)[13]", equalTo("")));
    assertThat(xml, hasXPath("(//placeholder)[14][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[15][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[16]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[17]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[18][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[19][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[20][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[21]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[22]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[23][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[24][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[25][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[26]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[27]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[28][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[29][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[30][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[31]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[32]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[33][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[34][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[35][@unresolved='true']", equalTo("my prop5")));
  }

  @Test
  void testPlaceholdersPub() throws IOException, ProcessException {
    String filename = "placeholders_pub.psml";
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
    p.setXrefs(xrefs);
    p.setPlaceholders(true);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    //System.out.println(xml);
    assertThat(xml, hasXPath("count(/document/documentinfo/publication)", equalTo("1")));
    assertThat(xml, hasXPath("count(//toc-part)", equalTo("5")));
    assertThat(xml, hasXPath("(//toc-part)[1]/@title", equalTo("Content root first 1")));
    assertThat(xml, hasXPath("(//toc-part)[2]/@title", equalTo("Placeholder 2 root second here.")));
    assertThat(xml, hasXPath("(//toc-part)[3]/@title", equalTo("Content root first 1")));
    assertThat(xml, hasXPath("(//toc-part)[4]/@title", equalTo("Placeholder 2 root second here.")));
    assertThat(xml, hasXPath("(//toc-part)[5]/@title", equalTo("Placeholder 1 root first here.")));
    assertThat(xml, hasXPath("count(//placeholder[@unresolved='true'])", equalTo("21")));
    assertThat(xml, hasXPath("(//placeholder)[1]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[2]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[3][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[4][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[5][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[6]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[7]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[8][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[9][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[10][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[11]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[12]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[13][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[14][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[15][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[16]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[17]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[18][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[19][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[20][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[21]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[22]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[23][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[24][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[25][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[26]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[27]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[28][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[29][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[30][@unresolved='true']", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[31]", equalTo("root first")));
    assertThat(xml, hasXPath("(//placeholder)[32]", equalTo("root second")));
    assertThat(xml, hasXPath("(//placeholder)[33][@unresolved='true']", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[34][@unresolved='true']", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[35][@unresolved='true']", equalTo("my prop5")));
  }

  @Test
  void testPlaceholdersUnresolved() throws IOException, ProcessException {
    String filename = "placeholders.psml";
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
    p.setXrefs(xrefs);
    PublicationConfig config = Tests.parseConfig("publication-config-process.xml");
    p.setPublicationConfig(config, filename, true);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + filename);
    String xml = Files.readString(result.toPath());
    //System.out.println(xml);
    assertThat(xml, hasXPath("count(//toc-part)", equalTo("5")));
    assertThat(xml, hasXPath("(//toc-part)[1]/@title", equalTo("Content [my-prop1] 1")));
    assertThat(xml, hasXPath("(//toc-part)[2]/@title", equalTo("Placeholder 2 [my-prop2] here.")));
    assertThat(xml, hasXPath("(//toc-part)[3]/@title", equalTo("Content [my-prop1] 1")));
    assertThat(xml, hasXPath("(//toc-part)[4]/@title", equalTo("Placeholder 2 [my-prop2] here.")));
    assertThat(xml, hasXPath("(//toc-part)[5]/@title", equalTo("Placeholder 1 [my-prop1] here.")));
    assertThat(xml, hasXPath("count(//placeholder[@unresolved='true'])", equalTo("0")));
    assertThat(xml, hasXPath("(//placeholder)[1]", equalTo("")));
    assertThat(xml, hasXPath("(//placeholder)[2]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[3]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[4]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[5]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[6]", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[7]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[8]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[9]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[10]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[11]", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[12]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[13]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[14]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[15]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[16]", equalTo("")));
    assertThat(xml, hasXPath("(//placeholder)[17]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[18]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[19]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[20]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[21]", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[22]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[23]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[24]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[25]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[26]", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[27]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[28]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[29]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[30]", equalTo("my prop5")));
    assertThat(xml, hasXPath("(//placeholder)[31]", equalTo("my prop1")));
    assertThat(xml, hasXPath("(//placeholder)[32]", equalTo("my prop2")));
    assertThat(xml, hasXPath("(//placeholder)[33]", equalTo("my prop3")));
    assertThat(xml, hasXPath("(//placeholder)[34]", equalTo("my prop4")));
    assertThat(xml, hasXPath("(//placeholder)[35]", equalTo("my prop5")));
  }

  @Test
  void testPreTransform() throws IOException, ProcessException {
    String path1 = "content/content_2.psml";
    String path2 = "META-INF/images/diagram1.jpg.psml";
    String path3 = "images.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform1.xsl");
    p.setPreTransform(xslt);
    Images img = new Images();
    img.setEmbedMetadata(true);
    p.setImages(img);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("alternate");
    xrefs.setIncludes(path3);
    p.setXrefs(xrefs);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + path1);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//heading)[1]/@level", equalTo("3")));
    result = new File(DEST_FOLDER + "/" + path2);
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//displaytitle)[1]", equalTo("x")));
    result = new File(DEST_FOLDER + "/" + path3);
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//displaytitle)[1]", equalTo("x")));
    assertThat(xml, hasXPath("(//displaytitle)[2]", equalTo("x")));
    assertThat(xml, hasXPath("(//displaytitle)[3]", equalTo("x")));
  }

  @Test
  void testPostTransform() throws IOException, ProcessException {
    String path1 = "content/content_2.psml";
    String path2 = "META-INF/images/diagram1.jpg.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform1.xsl");
    xslt.setIncludes(path1 + "," + path2);
    p.setPostTransform(xslt);
    p.process();

    // check result
    File result = new File(DEST_FOLDER + "/" + path1);
    String xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//heading)[1]/@level", equalTo("3")));
    result = new File(DEST_FOLDER + "/" + path2);
    xml = Files.readString(result.toPath());
    assertThat(xml, hasXPath("(//displaytitle)[1]", equalTo("x")));
  }

  @Test
  void testPostTransformFail() throws IOException {
    String path1 = "content/content_2.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform2.xsl");
    xslt.setIncludes(path1);
    p.setPostTransform(xslt);
    assertThrows(ProcessException.class, p::process);
  }

  @Test
  void testPostTransformMetadataFail() throws IOException {
    String path2 = "META-INF/images/diagram1.jpg.psml";
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File(SOURCE_FOLDER));
    File dest = new File(DEST_FOLDER);
    if (dest.exists())
      FileUtils.deleteDirectory(dest);
    dest.mkdirs();
    p.setDest(dest);
    XSLTTransformation xslt = new XSLTTransformation();
    xslt.setXSLT(SOURCE_FOLDER + "/transform2.xsl");
    xslt.setIncludes(path2);
    p.setPostTransform(xslt);
    assertThrows(ProcessException.class, p::process);
  }

  private static EvaluateXPathMatcher hasXPath(String xPath, Matcher<String> valueMatcher) {
    return new EvaluateXPathMatcher(xPath, valueMatcher);
  }

  public static Source getSchema(String filename) {
    try {
      String pathToSchema = "/org/pageseeder/psml/process/util/" + filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

}
