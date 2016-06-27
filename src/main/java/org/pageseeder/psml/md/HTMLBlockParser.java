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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.html.HTMLNode;

/**
 * The block parser parses Markdown and generates the block-level elements
 * delegating inline elements to the HTML inline parser.
 *
 * @author Christophe Lauret
 */
public class HTMLBlockParser {

  private Configuration configuration;

  public HTMLBlockParser() {
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Iterate over the lines and return corresponding HTML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of HTML elements
   */
  public List<HTMLElement> parse(List<String> lines) {
    return parse(lines, new Configuration());
  }

  /**
   * Iterate over the lines and return corresponding HTML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of HTML elements
   */
  public List<HTMLElement> parse(List<String> lines, Configuration config) {
    State state = new State();
    for (int i=0; i < lines.size(); i++) {
      String line = lines.get(i);
      String next = i < lines.size()-1? lines.get(i+1) : null;
      processLine(line, next, state, config);
    }
    state.commitAll();
    return state.elements;
  }

  /**
   * Process a single line
   *
   * @param line  The current line
   * @param next  The next line
   * @param state The state of the parser
   */
  public void processLine(String line, String next, State state, Configuration config) {

    // Lines made entirely of '=' or '-' are used for heading 1 and 2
    if (line.matches("\\s?(==+|\\-\\-+)\\s*")) {
      // DO nothing, we've already handled it
    }

    // Separators
    else if (line.matches("\\s*\\*\\s?\\*\\s?\\*[\\s\\*]*")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
        state.newFragment();
      }
    }

    // Empty lines are used to separate the different kinds of blocks
    else if (line.matches("\\s*")) {
      state.commitUpto(Name.section);
    }

    // New list items starting with '+', '-', '*' or number followed by a '.'
    else if (line.matches("\\s*(-|\\+|\\*|\\d+\\.)\\s.+")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }

      // Create a new item
      Pattern x  = Pattern.compile("^\\s*(-|\\+|\\*|\\d+\\.)\\s+(.+)$");
      Matcher m = x.matcher(line);
      if (m.matches()) {
        String no = m.group(1); // TODO
        if (state.isInList()) {
          // Already in a list, let's commit the previous item
          state.commit();
        } else {
          // A new list! Clear the context...
          state.commitUpto(Name.section);
          // An create a new list
          HTMLElement list;
          if (no.matches("\\d+\\.")) {
            list = new HTMLElement(Name.ol);
            String initial = no.substring(0, no.length()-1);
            if (!"1".equals(initial)) {
              list.setAttribute("start", initial);
            }
          } else {
            list = new HTMLElement(Name.ul);
          }
          state.push(list);
        }
        // Create a new item
        state.push(Name.li, m.group(2).trim());
      }
    }

    // Continuation of a list item
    else if (state.isInList()) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (!state.context.isEmpty() && state.text != null) {
        state.append(line.trim());
      }
    }

    // Lines starting with two spaces: preformatted code
    else if (line.matches("\\s{4}.*")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (!state.isElement(Name.pre)) {
        state.commitUpto(Name.section);
        state.push(Name.pre, line.substring(4));
      } else {
        state.append(line.substring(4));
      }
    }

    // Beginning/end of fenced code block
    else if (line.startsWith("```")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (state.isElement(Name.pre) || (state.isElement(Name.code) && state.isDescendantOf(Name.pre))) {
        state.append("");
        state.commitUpto(Name.section);
      } else {
        state.commitUpto(Name.section);
        HTMLElement pre = new HTMLElement(Name.pre);
        state.push(pre, "");
        if (line.length() > 3) {
          HTMLElement code = new HTMLElement(Name.code);
          String language = line.substring(3).trim();
          if (language.length() > 0) {
            code.setAttribute("class", language);
          }
          state.push(code, "");
        }
      }
    }

    // Beginning/end of fenced block labels
    else if (line.startsWith("~~~")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (state.isElement(Name.div)) {
        state.commitUpto(Name.section);
      } else {
        state.commitUpto(Name.section);
        HTMLElement pre = new HTMLElement(Name.div);
        if (line.length() > 3) {
          String label = line.substring(3).trim();
          if (label.length() > 0) {
            pre.setAttribute("label", label);
            pre.setAttribute("class", "label-"+label);
          }
        }
        state.push(pre, "");
      }
    }

    // Lines starting with '>': quoted content
    else if (line.matches("\\s*>+\\s+.*")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (!state.isElement(Name.div)) {
        state.commitUpto(Name.section);
        HTMLElement block = new HTMLElement(Name.blockquote);
        state.push(block, line.substring(line.indexOf('>')+2));
      } else {
        state.append(line.substring(line.indexOf('>')+2));
      }
    }

    // Metadata (document mode only)
    else if (config.isDocumentMode() && !state.isDescendantOf(Name.section) && line.matches("^\\w+\\:\\s.*")) {
      int colon = line.indexOf(':');
      if (!state.isDescendantOf(Name.dl)) {
        state.push(Name.dl);
      }
      // Create and commit a definition list
      state.push(Name.dt, line.substring(0,colon));
      state.push(Name.dd, line.substring(colon+2).trim());
      state.commit();
    }

    // Probably a paragraph or heading
    else {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }

      // We're in a fenced code block
      if (state.isElement(Name.pre) || (state.isElement(Name.code) && state.isDescendantOf(Name.pre))) {

        // Just add the line
        state.append(line);

        // We're in a fenced block label
      } else if (state.isElement(Name.div)) {

        // Just add the line
        state.append(line);

      } else {

        // Heading using ATX style
        Pattern headingPattern = Pattern.compile("^\\s*(#{1,6})\\s+(.*?)(#{1,6})?$");
        Matcher m = headingPattern.matcher(line);
        if (m.matches()) {
          state.commitUpto(Name.section);
          int level = m.group(1).length();
          String text = m.group(2).trim();
          HTMLElement heading = newHeadingElement(level);
          state.push(heading, text);
          state.commit();

        } else {

          boolean isTitle = false;

          // Assume paragraph, but check whether we have a heading using SetExt style
          HTMLElement element = new HTMLElement(Name.p);
          if (next != null) {
            if (next.matches("\\s*==+\\s*")) {
              // We use the '====' as a marker for a new section
              if (config.isDocumentMode() && !state.current().isEmpty()) {
                state.newSection();
              }
              element = newHeadingElement(1);

              // Special case for title section
              if (config.isDocumentMode()) {
                HTMLElement section = state.ancestor(Name.section);
                if ("title".equals(section.getAttribute("id"))) {
                  isTitle = true;
                }
              }

            } else if (next.matches("\\s*--+\\s*")) {
              // We use the '----' as a marker for a new fragment
              if (config.isDocumentMode() && !state.current().isEmpty()) {
                state.newFragment();
              }
              element = newHeadingElement(2);
            }
          }

          // Check whether the current element matches what we found (h1, h2 or p)
          if (!state.isElement(element.getElement())) {
            state.commitUpto(Name.section);
            state.push(element, line.trim());
          } else {
            if (state.lineBreak) {
              state.lineBreak();
            }
            state.append(line.trim());
          }

          // If the line breaks occurs before 66 characters, we assume it is intentional and insert a line break
          state.lineBreak = line.length() < config.getLineBreakThreshold();

          // Special case: we terminate the section title
          if (isTitle) {
            state.commitUpto(Name.article);
          }
        }
      }
    }

  }

  /**
   * @param level The heading level
   *
   * @return a new heading element
   */
  public static HTMLElement newHeadingElement(final int level) {
    switch (level) {
      case 1: return new HTMLElement(Name.h1);
      case 2: return new HTMLElement(Name.h2);
      case 3: return new HTMLElement(Name.h3);
      case 4: return new HTMLElement(Name.h4);
      case 5: return new HTMLElement(Name.h5);
      case 6: return new HTMLElement(Name.h6);
      default: return new HTMLElement(Name.p);
    }
  }


  /**
   * Maintains the state of the parser during processing.
   */
  public static final class State {

    /**
     * List of element that have been committed.
     */
    private List<HTMLElement> elements = new ArrayList<>();

    /**
     * The inline parser to use.
     */
    private HTMLInlineParser inline = new HTMLInlineParser();

    /**
     * The current context, before it is committed
     */
    private List<HTMLElement> context = new ArrayList<>(4);

    /**
     * The section identifiers.
     */
    private String[] sectionIds = new String[]{"title", "content"};

    /**
     * Position of the section
     */
    private int sectionPosition = 0;

    /**
     * Id of the fragment being processed
     */
    private int fragmentId = 0;

    /**
     * String of text for the current element
     */
    private StringBuilder text = null;

    /**
     * Boolean flag to possibly include a line break.
     */
    private boolean lineBreak = false;

    /**
     * Indicates whether we are within an ordered or unordered list.
     *
     * @return <code>true</code> if the current or parent element is either a list or an nlist.
     */
    public boolean isInList() {
      final int size = this.context.size();
      HTMLElement current = size > 0? this.context.get(size-1) : null;
      HTMLElement parent = size > 1? this.context.get(size-2) : null;
      boolean isCurrentList = current != null && (current.isElement(Name.ul) || current.isElement(Name.ol));
      boolean isParentList = parent != null && (parent.isElement(Name.ul) || parent.isElement(Name.ol));
      return isCurrentList || isParentList;
    }

    /**
     * @return the current element.
     */
    public HTMLElement current() {
      if (this.context.isEmpty()) return null;
      return this.context.get(this.context.size()-1);
    }

    /**
     * Indicates whether the current element is a descendant of the specified name.
     *
     * @param name the name of the element to match
     * @return <code>true</code> if the current element matches the specified name;
     *         <code>false</code> otherwise.
     */
    public HTMLElement ancestor(Name name) {
      final int size = this.context.size();
      if (size == 0) return null;
      for (int i = size-1; i >= 0; i--) {
        HTMLElement element = this.context.get(i);
        if (element.getElement() == name)
          return element;
      }
      return null;
    }

    /**
     *
     * @param name the name of the element to match
     * @return <code>true</code> if the current element matches the specified name;
     *         <code>false</code> otherwise.
     */
    public boolean isElement(Name name) {
      HTMLElement current = current();
      return current != null && current.isElement(name);
    }

    /**
     * Indicates whether the current element is a descendant of the specified name.
     *
     * @param name the name of the element to match
     * @return <code>true</code> if the current element matches the specified name;
     *         <code>false</code> otherwise.
     */
    public boolean isDescendantOf(Name name) {
      return ancestor(name) != null;
    }

    /**
     * Set the state to a new section if possible
     */
    public void newSection() {
      if (this.sectionPosition < this.sectionIds.length) {
        commitAll();
        HTMLElement section = new HTMLElement(Name.section);
        String sectionId = this.sectionIds[this.sectionPosition];
        section.setAttribute("id", sectionId);
        push(section);
        this.sectionPosition++;
      }
    }

    /**
     * Set the state to a new fragment
     */
    public void newFragment() {
      commitUpto(Name.section);
      HTMLElement fragment = new HTMLElement(Name.section);
      fragment.setAttribute("id", ++this.fragmentId);
      push(fragment);
    }

    /**
     * Ensure that we are in a fragment
     */
    public void ensureFragment() {
      if (!isDescendantOf(Name.article)) {
        newSection();
      }
      if (!isDescendantOf(Name.section)) {
        newFragment();
      }
    }

    /**
     * Shorthand method to add a new element to the context and add some text.
     *
     * @param name Name of the element to push
     * @param text Text for the element (not committed)
     */
    public void push(Name name, String text) {
      push(new HTMLElement(name), text);
    }

    /**
     * Shorthand method to add a new element to the context and reset the text.
     *
     * @param name Name of the element to push
     */
    public void push(Name name) {
      push(new HTMLElement(name));
    }

    /**
     * Add a new element to the context and reset the text.
     *
     * @param name Name of the element to push
     */
    public void push(HTMLElement element) {
      this.context.add(element);
      this.text = null;
    }

    /**
     * Add a new element to the context and add some text.
     *
     * @param name Name of the element to push
     * @param text Text for the element (not committed)
     */
    public void push(HTMLElement element, String text) {
      this.context.add(element);
      this.text = new StringBuilder(text);
    }

    /**
     * Append text to the current text node preceded by a new line.
     */
    public void append(String text) {
      this.text.append('\n').append(text);
    }

    /**
     * Empty the current stack and attach the text to the current node.
     */
    public void commitAll() {
      this.lineBreak = false;
      commitText();
      int size = this.context.size();
      while (size > 0) {
        HTMLElement current = this.context.remove(size-1);
        if (size > 1) {
          HTMLElement parent = this.context.get(size-2);
          parent.addNode(current);
        } else {
          this.elements.add(current);
        }
        size = this.context.size();
      }
    }

    /**
     * Commit the elements in the current stack up to the specified element
     * and attach the text to the current node.
     */
    public void commitUpto(Name name) {
      this.lineBreak = false;
      commitText();
      int size = this.context.size();
      while (size > 0) {
        // We stop committing when we encounter the element
        if (isElement(name)) {
          break;
        }
        HTMLElement current = this.context.remove(size-1);
        if (size > 1) {
          HTMLElement parent = this.context.get(size-2);
          parent.addNode(current);
        } else {
          this.elements.add(current);
        }
        size = this.context.size();
      }
    }

    /**
     * Insert a line break in a paragraph
     */
    public void lineBreak() {
      commitText();
      current().addNode(new HTMLElement(Name.br));
      this.text = new StringBuilder();
    }

    /**
     * Commit the text on the current element and add the current element to
     * its parent before removing this element from current context.
     */
    public void commit() {
      commitText();
      int size = this.context.size();
      if (size > 0) {
        HTMLElement current = this.context.remove(size-1);
        if (size > 1) {
          HTMLElement parent = this.context.get(size-2);
          parent.addNode(current);
        } else {
          this.elements.add(current);
        }
      }
    }

    /**
     * Process the text using the inline parser and append the result to the
     * current element.
     */
    public void commitText() {
      HTMLElement current = current();
      if (this.text != null && current != null) {
        List<HTMLNode> nodes = this.inline.parse(this.text.toString());
        current.addNodes(nodes);
        this.text = null;
      }
    }

  }

}
