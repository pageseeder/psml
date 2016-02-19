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
 */
final class TFragmentRef implements Token {

  /**
   * The ID of the fragment
   */
  private final String _id;

  /**
   * The actual fragment being referenced
   */
  private final TFragment _fragment;

  /**
   * Creates a new fragment placeholder.
   */
  public TFragmentRef(String id, TFragment fragment) {
    this._id = id;
    this._fragment = fragment;
  }

  @Override
  public void print(PrintWriter psml, Map<String, String> values, Charset charset) {
    String element = this._fragment.kind();
    psml.append('<').append(element);
    psml.append(" id=\"").append(this._id).append('"');
    psml.append(" type=\"").append(this._fragment.type()).append('"');
    if (this._fragment.mediatype() != null) {
      psml.append(" mediatype=\"").append(this._fragment.mediatype()).append('"');
    }
    psml.append('>');
    for (Token token : this._fragment.tokens()) {
      token.print(psml, values, charset);
    }
    psml.append("</").append(element).append('>');
  }
}