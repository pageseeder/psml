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
 * A variable in the template to be replaced by the correct value.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
class TValue implements Token {

  /**
   * The name of the variable.
   */
  private final String name;

  /**
   * The type of variable.
   */
  private final ParameterType type;

  /**
   * The fallback value
   */
  private final String fallback;

  /**
   * <code>true</code> for an attribute value; <code>false</code> for regular text.
   */
  private final boolean attribute;

  /**
   * Creates a new value.
   *
   * @param name      The name of the parameter
   * @param fallback  The default value for the parameter
   * @param type      The type of parameter.
   * @param attribute <code>true</code> for an attribute value; <code>false</code> for text.
   */
  public TValue(String name, String fallback, ParameterType type, boolean attribute) {
    this.name = name;
    this.fallback = fallback;
    this.type = type;
    this.attribute = attribute;
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    String value = values.get(this.name);
    if (value != null) {
      if (!this.type.matches(value)) {
        value = this.fallback;
      }
    } else {
      value = this.fallback;
    }

    // Prints the value out
    if (this.type == ParameterType.XML) {
      psml.print(value);
    } else {
      XML.Encoder encoder = XML.getEncoder(charset);
      StringBuilder xml = new StringBuilder();
      if (this.attribute) {
        encoder.attribute(value, xml);
      } else {
        encoder.text(value.toCharArray(), 0, value.length(), xml);
      }
      psml.print(xml.toString());
    }
  }
}