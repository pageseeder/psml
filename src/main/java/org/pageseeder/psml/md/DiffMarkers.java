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
package org.pageseeder.psml.md;

import org.pageseeder.psml.model.PSMLElement.DiffOperation;

import java.util.regex.Pattern;

/**
 * Diff markers used by the Markdown serializer in {@link MarkdownOutputOptions.DiffFormat#INS_DEL} mode.
 *
 * <p>While serializing, markers are written as private-use sentinel characters so that they
 * survive trimming, line splitting, whitespace normalization and XML escaping. Once the whole
 * output is available, {@link #finish(CharSequence)} removes empty marker pairs, escapes literal
 * marker-like text and converts the sentinels to {@code <ins>}, {@code </ins>}, {@code <del>} and
 * {@code </del>}.
 *
 * @author Christophe Lauret
 *
 * @since 1.8.6
 */
final class DiffMarkers {

  /** Sentinel for {@code <ins>}. */
  static final char INS_OPEN = '';

  /** Sentinel for {@code </ins>}. */
  static final char INS_CLOSE = '';

  /** Sentinel for {@code <del>}. */
  static final char DEL_OPEN = '';

  /** Sentinel for {@code </del>}. */
  static final char DEL_CLOSE = '';

  /** Matches any sentinel character. */
  private static final Pattern SENTINELS = Pattern.compile("[-]");

  /**
   * Matches the start of literal text that looks like an ins/del tag: the name must be followed by
   * whitespace, {@code /}, {@code >}, a sentinel or the end of the text, so that code such as
   * {@code a<del)} is left alone.
   */
  private static final Pattern MARKER_LIKE = Pattern.compile("<(?=/?(?i:ins|del)(?:[\\s/>\\uE000-\\uE003]|$))");

  private DiffMarkers() {
  }

  /**
   * @param op INSERT or DELETE
   * @return The opening sentinel for the operation.
   */
  static char open(DiffOperation op) {
    return op == DiffOperation.INSERT ? INS_OPEN : DEL_OPEN;
  }

  /**
   * @param op INSERT or DELETE
   * @return The closing sentinel for the operation.
   */
  static char close(DiffOperation op) {
    return op == DiffOperation.INSERT ? INS_CLOSE : DEL_CLOSE;
  }

  /**
   * @param text Some text
   * @return {@code true} if the text contains any sentinel character.
   */
  static boolean hasSentinels(String text) {
    return SENTINELS.matcher(text).find();
  }

  /**
   * @param text Some text
   * @return The text without any sentinel character.
   */
  static String strip(String text) {
    return SENTINELS.matcher(text).replaceAll("");
  }

  /**
   * Escapes literal text that looks like an {@code <ins>} or {@code <del>} tag so that it cannot
   * impersonate a generated marker: {@code <ins}, {@code </ins}, {@code <del} and {@code </del}
   * (case-insensitive) become {@code &lt;ins}, etc.
   *
   * @param text Some source text
   * @return The escaped text.
   */
  static String escape(String text) {
    if (text.indexOf('<') < 0) return text;
    return MARKER_LIKE.matcher(text).replaceAll("&lt;");
  }

  /**
   * Removes empty marker pairs, escapes literal marker-like text and converts the sentinels to tags.
   *
   * <p>Generated markers are still sentinels at this point, so any marker-like text is literal. This
   * covers text that only becomes marker-like once its pieces are joined in the output.
   *
   * @param serialized The serialized output containing sentinels
   * @return The final Markdown
   */
  static String finish(CharSequence serialized) {
    String text = escape(removeEmptyPairs(serialized.toString()));
    StringBuilder out = new StringBuilder(text.length() + 32);
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case INS_OPEN: out.append("<ins>"); break;
        case INS_CLOSE: out.append("</ins>"); break;
        case DEL_OPEN: out.append("<del>"); break;
        case DEL_CLOSE: out.append("</del>"); break;
        default: out.append(c);
      }
    }
    return out.toString();
  }

  /**
   * Removes marker pairs with no content, repeatedly so that pairs left empty by the removal of
   * nested pairs are removed too.
   */
  private static String removeEmptyPairs(String text) {
    String insEmpty = String.valueOf(INS_OPEN) + INS_CLOSE;
    String delEmpty = String.valueOf(DEL_OPEN) + DEL_CLOSE;
    String previous;
    String current = text;
    do {
      previous = current;
      current = current.replace(insEmpty, "").replace(delEmpty, "");
    } while (!current.equals(previous));
    return current;
  }

}
