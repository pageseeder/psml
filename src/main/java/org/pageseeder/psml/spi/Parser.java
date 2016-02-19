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
package org.pageseeder.psml.spi;

import java.io.IOException;
import java.io.Reader;

import org.pageseeder.psml.model.PSMLElement;

/**
 * The service provider interface (SPI) for parsers.
 *
 * <p>This SPI provides a pluggable interface to add parsers that can generate PSML content.
 *
 * <p>Implementations must specify the expected mediatype of the content to parse.
 *
 * @author Christophe Lauret
 */
public abstract class Parser {

  /**
   * Initializes a new instance of this class.
   */
  protected  Parser() {
  }

  /**
   * Parses the content in the reader and returns the
   *
   * @param reader The reader containing the content to parse.
   *
   * @return The corresponding PSML element
   *
   * @throws IOException Should an I/O error occur while parsing the content.
   */
  public abstract PSMLElement parse(Reader reader) throws IOException;

  /**
   * Returns the mediatype that the parser implementation supports.
   *
   * @return the mediatype that the parser implementation supports.
   */
  public abstract String getMediatype();

}
