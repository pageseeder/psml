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
 * Represents an exception specific to template-related operations.
 *
 * <p>This exception is primarily used to indicate errors that occur when working with
 * templates in the application.
 *
 * <p>It provides constructors to create an exception instance with a message, a cause,
 * or a combination of both.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class TemplateException extends Exception {

  /** As per recommendation */
  private static final long serialVersionUID = 325955388752631764L;

  /**
   * Constructs a new exception with the specified detail message.
   */
  public TemplateException(String message) {
    super(message);
  }

  public TemplateException(String message, Throwable cause) {
    super(message, cause);
  }

  public TemplateException(Throwable cause) {
    super(cause);
  }


}
