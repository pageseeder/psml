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
 */
class TValue implements Token {

  /**
   * The name of the variable.
   */
  private final String _name;

  /**
   * The type of variable.
   */
  private final ParameterType _type;

  /**
   * The fallback value
   */
  private final String _fallback;

  /**
   * <code>true</code> for an attribute value; <code>false</code> for regular text.
   */
  private final boolean _attribute;

  /**
   * Creates a new value.
   *
   * @param name      The name of the parameter
   * @param fallback  The default value for the parameter
   * @param type      The type of parameter.
   * @param attribute <code>true</code> for an attribute value; <code>false</code> for text.
   */
  public TValue(String name, String fallback, ParameterType type, boolean attribute) {
    this._name = name;
    this._fallback = fallback;
    this._type = type;
    this._attribute = attribute;
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    String value = values.get(this._name);
    if (value != null) {
      if (!this._type.matches(value)) {
        value = this._fallback;
      }
    } else {
      value = this._fallback;
    }

    // Prints the value out
    if (this._type == ParameterType.XML) {
      psml.print(value);
    } else {
      XML.Encoder encoder = XML.getEncoder(charset);
      StringBuilder xml = new StringBuilder();
      if (this._attribute) {
        encoder.attribute(value, xml);
      } else {
        encoder.text(value.toCharArray(), 0, value.length(), xml);
      }
      psml.print(xml.toString());
    }
  }
}