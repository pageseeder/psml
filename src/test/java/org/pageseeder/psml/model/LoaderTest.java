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

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.pageseeder.psml.model.PSMLElement.Name;

public class LoaderTest {

  @Test
  public void testParse_Document() throws IOException {
    Loader loader = new Loader();
    String psml = "<document/>";
    PSMLElement element = loader.parse(new StringReader(psml));
    Assert.assertNotNull(element);
    Assert.assertEquals(Name.DOCUMENT, element.getElement());
    Assert.assertEquals(psml, element.toString());
  }

  @Test
  public void testParse_Metadata() throws IOException {
    Loader loader = new Loader();
    String psml = "<document><metadata></metadata></document>";
    PSMLElement element = loader.parse(new StringReader(psml));
    Assert.assertNotNull(element);
    Assert.assertEquals(Name.DOCUMENT, element.getElement());
    System.out.println(element);
  }

  @Test
  public void testParse_ParaMixedContent() throws IOException {
    Loader loader = new Loader();
    String psml = "<para>A <bold>test</bold>!</para>";
    PSMLElement element = loader.parse(new StringReader(psml));
    Assert.assertNotNull(element);
    Assert.assertEquals(Name.PARA, element.getElement());
    System.out.println(element);
  }

  @Test
  public void testParse_List() throws IOException {
    Loader loader = new Loader();
    String psml = "<list><item>A</item><item>B</item></list>";
    PSMLElement element = loader.parse(new StringReader(psml));
    Assert.assertNotNull(element);
    Assert.assertEquals(Name.LIST, element.getElement());
    Assert.assertEquals(2, element.getNodes().size());
    Assert.assertEquals(Name.ITEM, ((PSMLElement)element.getNodes().get(0)).getElement());
    Assert.assertEquals(Name.ITEM, ((PSMLElement)element.getNodes().get(1)).getElement());
    System.out.println(element);
  }

}
