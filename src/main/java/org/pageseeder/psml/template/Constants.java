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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Some useful constants
 *
 * @author Christophe Lauret
 *
 * @version 1.2.0
 * @since 1.0
 */
public final class Constants {

  /** Utility */
  private Constants() {
  }

  /**
   * Constant for US-ASCII charset.
   *
   * @deprecated Use {@link StandardCharsets#US_ASCII} instead
   */
  @Deprecated(forRemoval = true, since = "1.2.0")
  public static final Charset ASCII = StandardCharsets.US_ASCII;

  /**
   * Constant for UTF-8 charset.
   *
   * @deprecated Use {@link StandardCharsets#UTF_8} instead
   */
  @Deprecated(forRemoval = true, since = "1.2.0")
  public static final Charset UTF8 = StandardCharsets.UTF_8;

  /**
   * The namespace URI.
   */
  public static final String NS_URI = "http://pageseeder.com/psml/template";

}
