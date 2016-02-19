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

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Holds data to print out verbatim.
 */
final class TData implements Token {

  /**
   * Data to be copied.
   */
  private final String _data;

  /**
   * Indicates whether it is ASCII safe.
   */
  private final boolean _hasNonASCIIChar;

  /**
   * @param data data to be copied.
   */
  public TData(String data) {
    this._data = data;
    this._hasNonASCIIChar = XML.hasNonASCIIChar(data);
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    if (this._hasNonASCIIChar && charset.equals(Constants.ASCII)) {
      XML.toASCII(this._data, psml);
    } else {
      psml.print(this._data);
    }
  }
}
