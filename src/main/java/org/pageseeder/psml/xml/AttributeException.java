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
package org.pageseeder.psml.xml;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Superclass for exceptions caused by error during the processing of attributes.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 0.10.2
 */
public abstract class AttributeException extends RuntimeException {

  /** As per required for Serializable */
  private static final long serialVersionUID = 2L;

  /**
   * The name of the attribute.
   */
  private final String name;

  /**
   * @param name The name of the attribute.
   */
  protected AttributeException(String name) {
    this.name = name;
  }

  /**
   * Constructs a new attribute exception with the specified detail message and
   * cause.
   *
   * @param name    the name of the attribute.
   * @param message the detail message.
   */
  protected AttributeException(String name, String message) {
    super(message);
    this.name = name;
  }

  /**
     * Constructs a new attribute exception with the specified detail message and
     * cause.
     *
     * @param name    the name of the attribute.
     * @param message the detail message.
     * @param cause   the cause (<code>null</code> value is permitted)
   */
  protected AttributeException(String name, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.name = name;
  }

  /**
   * @return the name of the attribute
   */
  public String getAttributeName() {
    return this.name;
  }

}
