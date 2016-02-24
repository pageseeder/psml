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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.junit.Test;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;

public class MarkdownParserTest {

  public MarkdownParserTest() {
  }

  @Test
  public void testProcess() throws Exception {
    InputStream in = MarkdownParserTest.class.getResourceAsStream("/org/pageseeder/psml/md/test.md");
    Reader r = new InputStreamReader(in);
    MarkdownParser parser = new MarkdownParser();
    parser.getConfig().setFragmentMode(false);
    PSMLElement document = parser.parse(r);

    XMLWriter xml = new XMLWriterImpl(new PrintWriter(System.out));
    xml.setIndentChars("  ");
    document.toXML(xml);
    xml.flush();
  }

}
