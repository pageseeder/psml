/*
 * Copyright 2026 Allette Systems (Australia)
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
package org.pageseeder.psml.model;

import org.jspecify.annotations.Nullable;

/**
 * Namespaces and fixed prefixes used to represent Diff-X markup in the PSML model.
 *
 * <p>Diff-X output uses either the current namespaces ({@code https://www.pageseeder.org/diffx...})
 * or the legacy ones ({@code http://www.topologi.com/2005/Diff-X...}). Both are recognised on load,
 * whatever prefix the source uses. In the model, diff markup always uses the prefixes
 * {@value #PREFIX}, {@value #INS_PREFIX} and {@value #DEL_PREFIX}, and XML output declares the
 * current namespaces.
 *
 * @author Christophe Lauret
 *
 * @since 1.8.6
 */
final class DiffX {

  /** Namespace for text and element operations. */
  static final String NS_URI = "https://www.pageseeder.org/diffx";

  /** Namespace for inserted attribute annotations. */
  static final String INS_NS_URI = "https://www.pageseeder.org/diffx/insert";

  /** Namespace for deleted attribute annotations. */
  static final String DEL_NS_URI = "https://www.pageseeder.org/diffx/delete";

  /** Legacy namespace for text and element operations. */
  static final String LEGACY_NS_URI = "http://www.topologi.com/2005/Diff-X";

  /** Legacy namespace for inserted attribute annotations. */
  static final String LEGACY_INS_NS_URI = "http://www.topologi.com/2005/Diff-X/Insert";

  /** Legacy namespace for deleted attribute annotations. */
  static final String LEGACY_DEL_NS_URI = "http://www.topologi.com/2005/Diff-X/Delete";

  /** Prefix used in the model for text and element operations. */
  static final String PREFIX = "diff";

  /** Prefix used in the model for inserted attribute annotations. */
  static final String INS_PREFIX = "ins";

  /** Prefix used in the model for deleted attribute annotations. */
  static final String DEL_PREFIX = "del";

  /** Attribute marking an inserted element. */
  static final String INSERT_ATTRIBUTE = PREFIX + ":insert";

  /** Attribute marking a deleted element. */
  static final String DELETE_ATTRIBUTE = PREFIX + ":delete";

  private DiffX() {
  }

  /**
   * Returns the model prefix for the specified namespace URI.
   *
   * @param uri The namespace URI (may be null or empty)
   *
   * @return {@value #PREFIX}, {@value #INS_PREFIX} or {@value #DEL_PREFIX}; or {@code null} if not a Diff-X namespace.
   */
  static @Nullable String prefixFor(@Nullable String uri) {
    if (uri == null || uri.isEmpty()) return null;
    switch (uri) {
      case NS_URI:
      case LEGACY_NS_URI:
        return PREFIX;
      case INS_NS_URI:
      case LEGACY_INS_NS_URI:
        return INS_PREFIX;
      case DEL_NS_URI:
      case LEGACY_DEL_NS_URI:
        return DEL_PREFIX;
      default:
        return null;
    }
  }

  /**
   * Indicates whether the specified attribute name, as stored in the model, is diff markup.
   *
   * @param name The attribute name
   *
   * @return {@code true} if it starts with one of the model prefixes followed by a colon.
   */
  static boolean isDiffAttribute(String name) {
    return name.startsWith(PREFIX + ':') || name.startsWith(INS_PREFIX + ':') || name.startsWith(DEL_PREFIX + ':');
  }

}
