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
package org.pageseeder.psml.html;

import java.io.IOException;
import java.io.Reader;

import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.spi.Parser;

/**
 * Implementation of the Parser for HTML content, specifically for XHTML.
 *
 * <p>This class is designed to parse XHTML content and generate corresponding PSML elements.
 * It specifies the mediatype it supports as "application/xhtml+xml".
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class HTMLParser extends Parser {

  @Override
  public String getMediatype() {
    return "application/xhtml+xml";
  }

  @Override
  public PSMLElement parse(Reader reader) throws IOException {
    throw new UnsupportedOperationException("Not yet implemented");
    // TODO Implement!
  }
}
