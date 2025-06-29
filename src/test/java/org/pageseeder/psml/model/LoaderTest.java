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

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    System.out.println(element);
  }

  @Test
  void testParse_ParaMixedContent() throws IOException {
    Loader loader = new Loader();
    String psml = "<para>A <bold>test</bold>!</para>";
    PSMLElement element = loader.parse(new StringReader(psml));
    assertNotNull(element);
    assertEquals(Name.PARA, element.getElement());
    System.out.println(element);
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
    System.out.println(element);
  }

}
