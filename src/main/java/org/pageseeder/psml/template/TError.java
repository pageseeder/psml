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
package org.pageseeder.psml.template;

import org.pageseeder.psml.xml.XML;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * An error to print as a comment in the output.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
final class TError implements Token {

  /**
   * Error message to be copied.
   */
  private final String message;

  /**
   * Indicates whether it is ASCII safe.
   */
  private final boolean hasNonASCIIChar;

  /**
   * @param message message to be copied.
   */
  public TError(String message) {
    this.message = Objects.requireNonNull(message);
    this.hasNonASCIIChar = XML.hasNonASCIIChar(message);
  }

  /**
   * @return the message
   */
  public String message() {
    return this.message;
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    if (this.hasNonASCIIChar && charset.equals(StandardCharsets.US_ASCII)) {
      XML.toASCII("<!-- Template error: "+this.message+" -->", psml);
    } else {
      psml.print("<!-- Template error: "+this.message+" -->");
    }
  }
}
