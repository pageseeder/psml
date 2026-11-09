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
package org.pageseeder.psml.md;

import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser can produce a list of PSML nodes from textual content
 * using the Markdown format.
 *
 * <p>This parser only matches inline PSML elements from a text inside a
 * block level element.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class InlineParser {

  /**
   * Bold text: <code>**text**</code>
   */
  private static final String DOUBLE_EMPHASIS = "(\\*\\*([^*]*)\\*\\*)";

  /**
   * Italic text: <code>*text*</code>
   */
  private static final String EMPHASIS = "(\\*([^*]*)\\*)";

  /**
   * Bold text: <code>__text__</code>
   */
  private static final String DOUBLE_UNDERSCORE = "(__([^_]*)__)";

  /**
   * Italic text: <code>_text_</code>
   */
  private static final String UNDERSCORE = "(\\b_([^_]*)_\\b)";

  /**
   * Escaped code: <code>``code``</code>
   */
  private static final String CODE_ESCAPE = "(``\\s?((?:[^`]++|`(?!`))*+)\\s?``)";

  /**
   * Code: <code>`code`</code>
   */
  private static final String CODE = "(`([^`]*)`)";

  /**
   * Image: <code>![alt](src)</code>
   */
  private static final String IMAGE = "(\\!\\[([^\\]]*)\\]\\(([^)]*)\\))";

  /**
   * References: <code>[title](url)</code>
   */
  private static final String REF = "(\\[([^\\]]*)\\]\\(([^)]*)\\))";

  /**
   * Explicit links:
   *  <code>&lt;http://[url]&gt;</code>,
   *  <code>&lt;https://[url]&gt;</code>
   *  or <code>&lt;mailto:[email]&gt;</code>
   */
  private static final String LINK = "(<((https?://|mailto:)([^>]*))>)";

  /**
   * Autolinks when text starts with <code>http://</code> or <code>https://</code>
   */
  private static final String LINK_AUTO = "(https?://\\S+[\\w/+=@\\-])";

  /**
   * Define the general pattern to use to match Markdown.
   */
  private static final Pattern TOKENS = Pattern.compile(DOUBLE_EMPHASIS+"|"+DOUBLE_UNDERSCORE+"|"+EMPHASIS+"|"+UNDERSCORE+"|"+CODE_ESCAPE+"|"+CODE+"|"+IMAGE+"|"+REF+"|"+LINK+"|"+LINK_AUTO);

  /**
   * Define the pattern to match escaped characters in Markdown.
   */
  private static final Pattern ESCAPED = Pattern.compile("\\\\([-`*_\\[\\]\\\\!<>.])");

  /**
   * Parses the text content and returns the corresponding list of nodes.
   *
   * @param content The text content to parse.
   *
   * @return the list of nodes as a result of parsing
   */
  public List<PSMLNode> parse(String content) {
    return parse(content, false);
  }

  /**
   * Parses the text content and returns the corresponding list of nodes.
   *
   * @param content The text content to parse.
   * @param inLink  Whether parsing text inside a link
   *
   * @return the list of nodes as a result of parsing
   */
  private List<PSMLNode> parse(String content, boolean inLink) {
    List<PSMLNode> nodes = new ArrayList<>();
    Matcher m = TOKENS.matcher(content);
    int previousEnd = 0;
    while (m.find()) {
      // Any text before a match
      if (m.start() > previousEnd) {
        String text = content.substring(previousEnd, m.start());
        // if there is an escape character don't interpret the token
        if (text.endsWith("\\")) {
          nodes.add(new PSMLText(unescape(text + m.group())));
          previousEnd = m.end();
          continue;
        }
        nodes.add(new PSMLText(unescape(text)));
      }
      previousEnd = m.end();
      PSMLNode node = toNode(m, content, inLink);
      if (node != null) {
        nodes.add(node);
      }
    }

    // Add the tail end
    if (previousEnd < content.length()) {
      String text = content.substring(previousEnd);
      nodes.add(new PSMLText(unescape(text)));
    }
    return nodes;
  }

  /**
   * Converts the current match into the corresponding PSML node.
   *
   * @param m       The matcher positioned on a token
   * @param content The full content being parsed
   * @param inLink  Whether parsing text inside a link
   *
   * @return the corresponding node or <code>null</code> if the token should be ignored
   */
  private PSMLNode toNode(Matcher m, String content, boolean inLink) {
    // Strong emphases with '**' or '__' (appear in bold)
    if (m.group(1) != null) return inline(Name.BOLD, m.group(2));
    if (m.group(3) != null) return inline(Name.BOLD, m.group(4));
    // Normal emphases with '*' or '_' (appear in italic)
    if (m.group(5) != null) return inline(Name.ITALIC, m.group(6));
    if (m.group(7) != null) return inline(Name.ITALIC, m.group(8));
    // Code escape with '``' or code with '`'
    if (m.group(9) != null) return monospace(m.group(10).strip());
    if (m.group(11) != null) return monospace(m.group(12));
    // Links inside links as text
    if (inLink) return new PSMLText(unescape(content));
    // Images as '![alt](src)'
    if (m.group(13) != null) return image(m.group(14), m.group(15));
    // References as '[title](url)'
    if (m.group(16) != null) return reference(m.group(17), m.group(18));
    // Explicit links
    if (m.group(19) != null) return link(m.group(20), m.group(22));
    // Auto links
    if (m.group(23) != null) return link(m.group(23), m.group(23));
    return null;
  }

  private PSMLElement inline(Name name, String text) {
    PSMLElement element = new PSMLElement(name);
    element.addNodes(parse(text));
    return element;
  }

  private static PSMLElement monospace(String code) {
    PSMLElement monospace = new PSMLElement(Name.MONOSPACE);
    if (!code.isEmpty()) {
      monospace.addNode(new PSMLText(code));
    }
    return monospace;
  }

  private PSMLElement image(String alt, String src) {
    if (src.startsWith("http")) {
      // PageSeeder does not support external images
      return externalLink(src, alt);
    }
    PSMLElement image = new PSMLElement(Name.IMAGE);
    image.setAttribute("alt", unescape(alt));
    image.setAttribute("src", unescape(src));
    return image;
  }

  private PSMLElement reference(String text, String ref) {
    if (ref.startsWith("http")) {
      return externalLink(ref, text);
    }
    int hash = ref.indexOf('#');
    String url = hash < 0? ref : ref.substring(0, hash);
    String fragment = hash < 0? "default" : ref.substring(hash+1);
    PSMLElement xref = new PSMLElement(Name.XREF);
    // Markdown has no syntax for these, so we use the PageSeeder defaults for manual links
    xref.setAttribute("display", "manual");
    xref.setAttribute("reverselink", "true");
    xref.setAttribute("title", unescape(text));
    xref.setAttribute("href", unescape(url));
    xref.setAttribute("frag", unescape(fragment));
    xref.addNode(new PSMLText(unescape(text)));
    return xref;
  }

  private PSMLElement externalLink(String href, String text) {
    PSMLElement link = new PSMLElement(Name.LINK);
    link.setAttribute("href", unescape(href));
    link.addNodes(parse(text, true));
    return link;
  }

  private static PSMLElement link(String href, String text) {
    PSMLElement link = new PSMLElement(Name.LINK);
    link.setAttribute("href", unescape(href));
    link.addNode(new PSMLText(unescape(text)));
    return link;
  }

  /**
   * Removes the first {@code '\'} from
   * {@code '\-'},
   * {@code '\`'},
   * {@code '\*'},
   * {@code '\_'},
   * {@code '\['},
   * {@code '\]'},
   * {@code '\\'},
   * {@code '\!'},
   * {@code '\<'},
   * {@code '\>'},
   * {@code '\.'}.
   *
   * @param text  the original text
   *
   * @return the unescaped text
   */
  public static String unescape(String text) {
    if (text.indexOf('\\') == -1) return text;
    Matcher m = ESCAPED.matcher(text);
    StringBuilder out = new StringBuilder();
    while (m.find()) {
      m.appendReplacement(out, "$1");
    }
    m.appendTail(out);
    return out.toString();
  }

}
