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
 * Represents one of the tokens in the template.
 */
interface Token {

  /**
   * Prints the PSML out using the supplied values.
   *
   * @param psml    Where the PSML is printed.
   * @param values  The external values.
   * @param charset The charset to use.
   */
  void print(PrintWriter psml, Map<String, String> values, Charset charset);

}
