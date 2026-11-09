/*
 * Copyright 2016 Allette Systems (Australia)
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
package org.pageseeder.psml.model;

import org.junit.jupiter.api.Test;
import org.pageseeder.psml.model.PSMLElement.Name;

import org.pageseeder.psml.model.PSMLElement.DiffOperation;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class LoaderTest {

  @Test
  void testParse_Document() throws IOException {
    Loader loader = new Loader();
    String psml = "<document/>";
    PSMLElement element = loader.parse(new StringReader(psml));
    assertNotNull(element);
    assertEquals(Name.DOCUMENT, element.getElement());
    assertEquals(psml, element.toString());
  }

  @Test
  void testParse_Metadata() throws IOException {
    Loader loader = new Loader();
    String psml = "<document><metadata></metadata></document>";
    PSMLElement element = loader.parse(new StringReader(psml));
    assertNotNull(element);
    assertEquals(Name.DOCUMENT, element.getElement());
  }

  @Test
  void testParse_ParaMixedContent() throws IOException {
    Loader loader = new Loader();
    String psml = "<para>A <bold>test</bold>!</para>";
    PSMLElement element = loader.parse(new StringReader(psml));
    assertNotNull(element);
    assertEquals(Name.PARA, element.getElement());
  }

  @Test
  void testParse_List() throws IOException {
    Loader loader = new Loader();
    String psml = "<list><item>A</item><item>B</item></list>";
    PSMLElement element = loader.parse(new StringReader(psml));
    assertNotNull(element);
    assertEquals(Name.LIST, element.getElement());
    assertEquals(2, element.getNodes().size());
    assertEquals(Name.ITEM, ((PSMLElement) element.getNodes().get(0)).getElement());
    assertEquals(Name.ITEM, ((PSMLElement) element.getNodes().get(1)).getElement());
  }

  private static final String NS_CURRENT = " xmlns:diff='https://www.pageseeder.org/diffx'"
      + " xmlns:ins='https://www.pageseeder.org/diffx/insert'"
      + " xmlns:del='https://www.pageseeder.org/diffx/delete'";

  private static final String NS_LEGACY = " xmlns:dfx='http://www.topologi.com/2005/Diff-X'"
      + " xmlns:ins='http://www.topologi.com/2005/Diff-X/Insert'"
      + " xmlns:del='http://www.topologi.com/2005/Diff-X/Delete'";

  private static PSMLElement parse(String psml) throws IOException {
    return new Loader().parse(new StringReader(psml));
  }

  private static PSMLElement child(PSMLElement element, int index) {
    return (PSMLElement) element.getNodes().get(index);
  }

  @Test
  void testParse_DiffCurrentNamespaces() throws IOException {
    PSMLElement fragment = parse("<fragment" + NS_CURRENT + "><para diff:insert='true'><diff:ins>New</diff:ins></para>"
        + "<para>Within<diff:del> 30</diff:del><diff:ins> 14</diff:ins> days.</para></fragment>");
    PSMLElement para1 = child(fragment, 0);
    assertEquals(DiffOperation.INSERT, para1.getDiffOperation());
    assertEquals(Name.DIFF_INS, child(para1, 0).getElement());
    PSMLElement para2 = child(fragment, 1);
    assertEquals(DiffOperation.NONE, para2.getDiffOperation());
    assertEquals(Name.DIFF_DEL, child(para2, 1).getElement());
    assertEquals(" 30", child(para2, 1).getText());
    assertEquals(Name.DIFF_INS, child(para2, 2).getElement());
    assertTrue(fragment.hasDiffMarkup());
    assertFalse(child(fragment, 0).getNodes().isEmpty());
  }

  @Test
  void testParse_DiffLegacyNamespaces() throws IOException {
    PSMLElement fragment = parse("<fragment" + NS_LEGACY + "><heading dfx:delete='true' del:level='2'>"
        + "<dfx:del>Old</dfx:del></heading></fragment>");
    PSMLElement heading = child(fragment, 0);
    assertEquals(DiffOperation.DELETE, heading.getDiffOperation());
    assertEquals("true", heading.getAttribute("diff:delete"));
    assertNull(heading.getAttribute("dfx:delete"));
    assertEquals("2", heading.getDeletedAttribute("level"));
    assertNull(heading.getAttribute("level"));
    assertEquals(Name.DIFF_DEL, child(heading, 0).getElement());
  }

  @Test
  void testParse_DiffAlternatePrefixes() throws IOException {
    String a = "<fragment" + NS_CURRENT + "><para diff:insert='true'><diff:ins>New</diff:ins></para></fragment>";
    String b = "<fragment xmlns:x='https://www.pageseeder.org/diffx'><para x:insert='true'>"
        + "<y:ins xmlns:y='https://www.pageseeder.org/diffx'>New</y:ins></para></fragment>";
    String c = "<fragment><para xmlns:q='http://www.topologi.com/2005/Diff-X' q:insert='true'><q:ins>New</q:ins></para></fragment>";
    String expected = parse(a).toString();
    assertEquals(expected, parse(b).toString());
    assertEquals(expected, parse(c).toString());
  }

  @Test
  void testParse_DiffAttributes() throws IOException {
    PSMLElement fragment = parse("<properties-fragment" + NS_CURRENT + ">"
        + "<property name='effective' del:value='1 July 2026' value='1 August 2026' ins:value='true'/>"
        + "<property diff:delete='true' del:name='b' del:value='beta'/></properties-fragment>");
    PSMLElement changed = child(fragment, 0);
    assertEquals(DiffOperation.NONE, changed.getDiffOperation());
    assertEquals("1 August 2026", changed.getAttribute("value"));
    assertTrue(changed.isInsertedAttribute("value"));
    assertFalse(changed.isInsertedAttribute("name"));
    assertEquals("1 July 2026", changed.getDeletedAttribute("value"));
    PSMLElement deleted = child(fragment, 1);
    assertEquals(DiffOperation.DELETE, deleted.getDiffOperation());
    assertEquals("b", deleted.getDeletedAttribute("name"));
    assertNull(deleted.getAttribute("name"));
  }

  @Test
  void testParse_DiffConflict() throws IOException {
    PSMLElement para = parse("<para" + NS_CURRENT + " diff:insert='true' diff:delete='true'>x</para>");
    assertEquals(DiffOperation.NONE, para.getDiffOperation());
    assertTrue(para.isConflictingDiffOperation());
    assertEquals(java.util.Set.of("diff:insert", "diff:delete"), para.getAttributeNames());
  }

  @Test
  void testParse_NotDiff() throws IOException {
    // Unqualified, unrelated namespace and unbound-looking prefixes are not Diff-X
    PSMLElement para = parse("<para xmlns:x='urn:other'><ins>a</ins><x:ins>b</x:ins><x:del>c</x:del></para>");
    assertEquals(Name.UNKNOWN, child(para, 0).getElement());
    assertEquals(Name.UNKNOWN, child(para, 1).getElement());
    assertEquals(Name.UNKNOWN, child(para, 2).getElement());
    assertFalse(para.hasDiffMarkup());
    PSMLElement attr = parse("<para xmlns:diff='urn:other' diff:insert='true'>a</para>");
    assertEquals(DiffOperation.NONE, attr.getDiffOperation());
    assertFalse(attr.hasDiffMarkup());
    assertEquals("true", attr.getAttribute("diff_:insert"));
    assertEquals(Name.UNKNOWN, Name.forElement("diff:ins"));
    assertEquals(Name.UNKNOWN, Name.forElement("ins"));
  }

  @Test
  void testParse_UnknownDiffElement() throws IOException {
    PSMLElement para = parse("<para" + NS_CURRENT + "><diff:other>a</diff:other><ins:x>b</ins:x></para>");
    assertEquals(Name.DIFF_UNKNOWN, child(para, 0).getElement());
    assertEquals(Name.DIFF_UNKNOWN, child(para, 1).getElement());
    assertTrue(para.hasDiffMarkup());
    assertEquals(Name.UNKNOWN, Name.forElement("diff:unknown"));
  }

  @Test
  void testToXML_DiffRoundTrip() throws IOException {
    String psml = "<fragment" + NS_LEGACY + " id='1'><para dfx:insert='true'><dfx:ins>New</dfx:ins></para>"
        + "<property name='a' del:value='x' value='y' ins:value='true'/></fragment>";
    PSMLElement first = parse(psml);
    String xml = first.toString();
    assertTrue(xml.startsWith("<fragment xmlns:diff=\"https://www.pageseeder.org/diffx\""
        + " xmlns:ins=\"https://www.pageseeder.org/diffx/insert\""
        + " xmlns:del=\"https://www.pageseeder.org/diffx/delete\""), xml);
    assertEquals(1, xml.split("xmlns:diff=", -1).length - 1, "Namespaces declared once: " + xml);
    PSMLElement second = parse(xml);
    assertEquals(xml, second.toString());
    // Writing a descendant declares the namespaces on it
    assertTrue(child(second, 0).toString().startsWith("<para xmlns:diff="));
  }

  @Test
  void testToXML_GeneratedFixturesRoundTrip() throws IOException {
    Path generated = Paths.get("src/test/data/mddiff/generated");
    try (java.util.stream.Stream<Path> files = Files.list(generated)) {
      for (Path file : (Iterable<Path>) files::iterator) {
        PSMLElement first = parse(Files.readString(file));
        assertEquals(Name.DIFF, first.getElement());
        assertTrue(first.hasDiffMarkup(), file.toString());
        String xml = first.toString();
        assertEquals(xml, parse(xml).toString(), file.toString());
      }
    }
  }

  @Test
  void testToXML_NoDiffUnchanged() throws IOException {
    String psml = "<fragment id=\"1\"><para>A <bold>test</bold>!</para><property name=\"a\" value=\"b\"/></fragment>";
    PSMLElement element = parse(psml);
    assertFalse(element.hasDiffMarkup());
    assertEquals(psml, element.toString());
  }

}
