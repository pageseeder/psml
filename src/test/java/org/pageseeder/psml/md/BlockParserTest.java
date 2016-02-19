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
package org.pageseeder.psml.md;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.xmlwriter.XMLStringWriter;

public class BlockParserTest {

  public BlockParserTest() {
  }

  @Test
  public void testHeadingSetext() throws Exception {
    List<String> h1 = Arrays.asList("This is an H1", "=============");
    List<String> h2 = Arrays.asList("This is an H2", "-------------");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
  }

  @Test
  public void testHeading() throws Exception {
    List<String> h1 = Arrays.asList("# This is an H1");
    List<String> h2 = Arrays.asList("## This is an H2");
    List<String> h3 = Arrays.asList("### This is an H3");
    List<String> h4 = Arrays.asList("#### This is an H4");
    List<String> h5 = Arrays.asList("##### This is an H5");
    List<String> h6 = Arrays.asList("###### This is an H6");
    Assert.assertEquals("<heading level=\"1\">This is an H1</heading>", toPSML(h1));
    Assert.assertEquals("<heading level=\"2\">This is an H2</heading>", toPSML(h2));
    Assert.assertEquals("<heading level=\"3\">This is an H3</heading>", toPSML(h3));
    Assert.assertEquals("<heading level=\"4\">This is an H4</heading>", toPSML(h4));
    Assert.assertEquals("<heading level=\"5\">This is an H5</heading>", toPSML(h5));
    Assert.assertEquals("<heading level=\"6\">This is an H6</heading>", toPSML(h6));
  }

  /**
   * Returns the Markdown text as PSML using the block parser.
   *
   * @param text The text to parse
   *
   * @return The corresponding PSML as a string.
   *
   * @throws IOException If thrown by
   */
  private static String toPSML(List<String> lines) {
    try {
      BlockParser parser = new BlockParser();
      List<PSMLElement> nodes = parser.parse(lines);
      XMLStringWriter xml = new XMLStringWriter(false);
      for (PSMLNode n : nodes) {
        n.toXML(xml);
      }
      xml.flush();
      return xml.toString();
    } catch (IOException ex) {
      // Should never happen!
      throw new RuntimeException(ex);
    }
  }

}
