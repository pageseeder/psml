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
 * A fragment placeholder to resolve.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
final class TFragmentRef implements Token {

  /**
   * The ID of the fragment
   */
  private final String id;

  /**
   * The actual fragment being referenced
   */
  private final TFragment fragment;

  /**
   * Creates a new fragment placeholder.
   */
  public TFragmentRef(String id, TFragment fragment) {
    this.id = id;
    this.fragment = fragment;
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    String element = this.fragment.kind();
    psml.append('<').append(element)
        .append(" id=\"").append(this.id).append('"')
        .append(" type=\"").append(this.fragment.type()).append('"');
    if (this.fragment.mediatype() != null) {
      psml.append(" mediatype=\"").append(this.fragment.mediatype()).append('"');
    }
    psml.append('>');
    for (Token token : this.fragment.tokens()) {
      token.print(psml, values, charset);
    }
    psml.append("</").append(element).append('>');
  }
}