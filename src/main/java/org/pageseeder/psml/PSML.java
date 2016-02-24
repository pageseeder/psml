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
package org.pageseeder.psml;

import java.io.IOException;
import java.io.Reader;

import org.pageseeder.psml.model.Loader;
import org.pageseeder.psml.model.PSMLElement;

/**
 * Reserving this class for future use.
 */
public class PSML {

  private PSML() {
  }

  /**
   * Generate a PSML
   *
   * @param reader The reader containing PSML data to load.
   *
   * @return The corresponding PSML element.
   *
   * @throws IOException Should an I/O error occur while reading the input.
   */
  public static PSMLElement load(Reader reader) throws IOException {
    Loader loader = new Loader();
    return loader.parse(reader);
  }

}
