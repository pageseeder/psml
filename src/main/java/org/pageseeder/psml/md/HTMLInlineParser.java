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

import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.html.HTMLNode;
import org.pageseeder.psml.html.HTMLText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This simple parser can produce a list of HTML nodes from textual content
 * using the Markdown format.
 *
 * <p>This parser only matches inline HTML elements from a text inside a
 * block-level element.
 *
 * <p>Implementation note: this algorithm is identical to the PSML namesake,
 * eventually, these should be merged.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class HTMLInlineParser {

  /**
   * Bold text: <code>**text**</code>
   */
  private static final String DOUBLE_EMPHASIS = "(\\*\\*(.*?)\\*\\*)";

  /**
   * Italic text: <code>*text*</code>
   */
  private static final String EMPHASIS = "(\\*(.*?)\\*)";

  /**
   * Bold text: <code>__text__</code>
   */
  private static final String DOUBLE_UNDERSCORE = "(__(.*?)__)";

  /**
   * Italic text: <code>_text_</code>
   */
  private static final String UNDERSCORE = "(\\b_(.*?)_\\b)";

  /**
   * Escaped code: <code>``code``</code>
   */
  private static final String CODE_ESCAPE = "(``\\s?(.*?)\\s?``)";

  /**
   * Code: <code>`code`</code>
   */
  private static final String CODE = "(`(.*?)`)";

  /**
   * Image: <code>![alt](src)</code>
   */
  private static final String IMAGE = "(\\!\\[(.*?)\\]\\((.*?)\\))";

  /**
   * References: <code>[title](url)</code>
   */
  private static final String REF = "(\\[(.*?)\\]\\((.*?)\\))";

  /**
   * Explicit links:
   *  <code>&lt;http://[url]&gt;</code>,
   *  <code>&lt;https://[url]&gt;</code>
   *  or <code>&lt;mailto:[email]&gt;</code>
   */
  private static final String LINK = "(<((https?://|mailto:)(.*?))>)";

  /**
   * Autolinks when text starts with <code>http://</code> or <code>https://</code>
   */
  private static final String LINK_AUTO = "(https?://\\S+[\\w/+=@\\-])";

  /**
   * Define the general pattern to use to match markdown.
   */
  private static final Pattern TOKENS = Pattern.compile(DOUBLE_EMPHASIS+"|"+DOUBLE_UNDERSCORE+"|"+EMPHASIS+"|"+UNDERSCORE+"|"+CODE_ESCAPE+"|"+CODE+"|"+IMAGE+"|"+REF+"|"+LINK+"|"+LINK_AUTO);

  /**
   * Parses the text content and returns the corresponding list of nodes.
   *
   * @param content The text content to parse.
   *
   * @return The list of HTML nodes as a result of parsing
   */
  public List<HTMLNode> parse(String content) {
    return parse(content, false);
  }

  /**
   * Parses the text content and returns the corresponding list of nodes.
   *
   * @param content The text content to parse.
   * @param inLink  Whether parsing text inside a link
   *
   * @return The list of HTML nodes as a result of parsing
   */
  private List<HTMLNode> parse(String content, boolean inLink) {
    List<HTMLNode> nodes = new ArrayList<>();
    Matcher m = TOKENS.matcher(content);
    int previousEnd = 0;
    while (m.find()) {
      // Any text before a match
      if (m.start() > previousEnd) {
        String text = content.substring(previousEnd, m.start());
        // if there is an escape character don't interpret the token
        if (text.endsWith("\\")) {
          nodes.add(new HTMLText(InlineParser.unescape(text + content.substring(m.start(), m.end()))));
          previousEnd = m.end();
          continue;
        } else {
          nodes.add(new HTMLText(InlineParser.unescape(text)));
        }
      }
      previousEnd = m.end();
      // Strong emphases with '**' (appear in bold)
      if (m.group(1) != null) {
        HTMLElement element = new HTMLElement(Name.STRONG);
        element.addNodes(parse(m.group(2)));
        nodes.add(element);
      }
      // Strong emphases with '__' (appear in bold)
      else if (m.group(3) != null) {
        HTMLElement element = new HTMLElement(Name.STRONG);
        element.addNodes(parse(m.group(4)));
        nodes.add(element);
      }
      // Normal emphases with '*' (appear in italic)
      if (m.group(5) != null) {
        HTMLElement element = new HTMLElement(Name.EM);
        element.addNodes(parse(m.group(6)));
        nodes.add(element);
      }
      // Strong emphases with '_' (appear in italic)
      else if (m.group(7) != null) {
        HTMLElement element = new HTMLElement(Name.EM);
        element.addNodes(parse(m.group(8)));
        nodes.add(element);
      }
      // Code with '`'
      else if (m.group(9) != null) {
        String code = m.group(10);
        HTMLElement monospace = new HTMLElement(Name.CODE);
        if (code.length() > 0) {
          monospace.addNode(new HTMLText(code));
        }
        nodes.add(monospace);
      }
      // Code escape with '``'
      else if (m.group(11) != null) {
        String code = m.group(12);
        HTMLElement monospace = new HTMLElement(Name.CODE);
        if (code.length() > 0) {
          monospace.addNode(new HTMLText(code));
        }
        nodes.add(monospace);
      }
      // Images as '![alt](src)'
      else if (m.group(13) != null && !inLink) {
        String alt = m.group(14);
        String src = m.group(15);
        if (src.startsWith("http")) {
          // PageSeeder does not support external images
          HTMLElement link = new HTMLElement(Name.A);
          link.setAttribute("href", InlineParser.unescape(src));
          link.addNodes(parse(alt, true));
          nodes.add(link);
        } else {
          HTMLElement image = new HTMLElement(Name.IMG);
          image.setAttribute("alt", InlineParser.unescape(alt));
          image.setAttribute("src", InlineParser.unescape(src));
          nodes.add(image);
        }
      }
      // References as '[title](url)'
      else if (m.group(16) != null && !inLink) {
        String ref = m.group(18);
        String text = m.group(17);
        HTMLElement link = new HTMLElement(Name.A);
        link.setAttribute("href", InlineParser.unescape(ref));
        link.addNodes(parse(text, true));
        nodes.add(link);
      }
      // Explicit links
      else if (m.group(19) != null && !inLink) {
        String url = m.group(20);
        String text = m.group(22);
        HTMLElement link = new HTMLElement(Name.A);
        link.setAttribute("href", InlineParser.unescape(url));
        link.addNode(new HTMLText(InlineParser.unescape(text)));
        nodes.add(link);
      }
      //  Auto links
      else if (m.group(23) != null && !inLink) {
        String url = m.group(23);
        HTMLElement link = new HTMLElement(Name.A);
        link.setAttribute("href", InlineParser.unescape(url));
        link.addNode(new HTMLText(InlineParser.unescape(url)));
        nodes.add(link);
      }
      // Links inside links as text
      else if (inLink) {
        nodes.add(new HTMLText(InlineParser.unescape(content)));
      }
    }
    // Add the tail end
    if (previousEnd < content.length()) {
      String text = content.substring(previousEnd);
      nodes.add(new HTMLText(InlineParser.unescape(text)));
    }
    return nodes;
  }
}
