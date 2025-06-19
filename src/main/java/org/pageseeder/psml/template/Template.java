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
 * Represents a template that can generate PSML documents with specified placeholder values.
 *
 * <p>This interface defines operations for getting the charset used by the template
 * and processing PSML templates to populate placeholders with specified values.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public interface Template {

  /**
   * The charset use by this template.
   *
   * @return the charset used by this template.
   */
  Charset charset();

  /**
   * Generate a new PSML document by filling out the values in the template using the charset used by the template.
   *
   * @param psml   The PSML template
   * @param values The values to use for the place holders
   */
  void process(PrintWriter psml, Map<String, String> values);

  /**
   * Generate a new PSML document by filling out the values in the template using the charset used by the template.
   *
   * @param psml   The PSML template
   * @param values The values to use for the placeholders
   * @param failOnError When true the process will fail if an error occurs
   *
   * @throws TemplateException If a fatal error occurs during processing
   */
  void process(PrintWriter psml, Map<String, String> values, boolean failOnError) throws TemplateException;

}