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

/**
 * Builds a single fragment or document template.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
interface TemplateBuilder<T extends Template> {

  /**
   * Adds a declared parameter
   *
   * @param fragment The default value for the parameter
   */
  void addFragment(TFragment fragment);

  /**
   * Adds a declared parameter
   *
   * @param name     The name of the parameter
   * @param fallback The default value for the parameter
   * @param type     The type of parameter.
   */
  void addParameter(String name, String fallback, ParameterType type);

  /**
   * A template value.
   *
   * @param name The name of the parameter to use.
   */
  void pushValue(String name, boolean attribute);

  /**
   * A error reported by the parser
   *
   * @param error The name of the parameter to use.
   */
  void pushError(String error);

  /**
   * A reference to a fragment.
   *
   * @param id   ID of the fragment
   * @param type type of the fragment
   */
  void pushFragmentRef(String id, String type);

  /**
   * Data to write out verbatim
   *
   * @param data The data to write.
   */
  void pushData(String data);

  /**
   * Build the template.
   *
   * @return a new template instance.
   */
  T build();

}