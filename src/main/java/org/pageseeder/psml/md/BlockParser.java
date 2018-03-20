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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The block parser parses Markdown and generates the block-level elements
 * delegating inline elements to the inline parser.
 *
 * @author Christophe Lauret
 */
public class BlockParser {

  private Configuration configuration;

  public BlockParser() {
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of PSML elements
   */
  public List<PSMLElement> parse(List<String> lines) {
    return parse(lines, new Configuration());
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of PSML elements
   */
  public List<PSMLElement> parse(List<String> lines, Configuration config) {
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

    // Empty lines are used to separate the different kinds of blocks, except inside fenced (```) code
    else if (line.matches("\\s*") && !state.isFenced()) {
      state.commitUpto(Name.Fragment);
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
          state.commitUpto(Name.Fragment);
          // An create a new list
          PSMLElement list;
          if (no.matches("\\d+\\.")) {
            list = new PSMLElement(Name.Nlist);
            String initial = no.substring(0, no.length()-1);
            if (!"1".equals(initial)) {
              list.setAttribute("start", initial);
            }
          } else {
            list = new PSMLElement(Name.List);
          }
          state.push(list);
        }
        // Create a new item
        state.push(Name.Item, m.group(2).trim());
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

    // Lines starting with four spaces: preformatted code
    else if (line.matches("\\s{4}.*")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (!state.isElement(Name.Preformat)) {
        state.commitUpto(Name.Fragment);
        state.push(Name.Preformat, line.substring(4));
      } else {
        state.append(line.substring(4));
      }
    }

    // Beginning/end of fenced code block
    else if (line.startsWith("```")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (state.isElement(Name.Preformat)) {
        state.setFenced(false);
        state.append("");
        state.commitUpto(Name.Fragment);
      } else {
        state.commitUpto(Name.Fragment);
        PSMLElement pre = new PSMLElement(Name.Preformat);
        if (line.length() > 3) {
          String language = line.substring(3).trim();
          if (language.length() > 0) {
            pre.setAttribute("role", language);
          }
        }
        state.push(pre, "");
        state.setFenced(true);
      }
    }

    // Beginning/end of fenced block labels
    else if (line.startsWith("~~~")) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      if (state.isElement(Name.Block)) {
        state.commitUpto(Name.Fragment);
      } else {
        state.commitUpto(Name.Fragment);
        PSMLElement pre = new PSMLElement(Name.Block);
        if (line.length() > 3) {
          String label = line.substring(3).trim();
          if (label.length() > 0) {
            pre.setAttribute("label", label);
          }
        }
        state.push(pre, "");
      }
    }

    // Lines starting with '>': quoted content
    else if (line.matches("\\s*>+\\s*.*") && !state.isElement(Name.Preformat)) {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }
      // remove chevron and leading space
      String text = line.substring(line.indexOf('>') + 1).replaceFirst("^\\s+", "");
      // check if already in a blockquote
      PSMLElement current = state.current();
      if (current != null && current.isElement(Name.Block)) {
        List<PSMLNode> children = current.getNodes();
        PSMLNode last = children.isEmpty() ? null : children.get(children.size()-1);
        if (last instanceof PSMLElement) {
          PSMLElement lastElement = (PSMLElement) last;
          if (lastElement.isElement(Name.Para)) {
            if (text.matches("\\s*")) {
              current.addNode(new PSMLElement(Name.Para));
            } else {
              lastElement.addText((lastElement.getText().isEmpty() ? "" : " ")+text);
            }
          }
        }
      } else {
        state.commitUpto(Name.Fragment);
        // create new blockquote
        PSMLElement block = new PSMLElement(Name.Block);
        block.setAttribute("label", "quoted");
        PSMLElement p = new PSMLElement(Name.Para);
        p.setText(text);
        block.addNode(p);
        state.push(block);
      }
    }

    // Metadata (document mode only)
    else if (config.isDocumentMode() && !state.isDescendantOf(Name.Section) && line.matches("^\\w+\\:\\s.*")) {
      int colon = line.indexOf(':');
      if (!state.isDescendantOf(Name.Metadata)) {
        state.push(Name.Metadata);
        state.push(Name.Properties);
      }
      // Create and commit a property
      PSMLElement property = new PSMLElement(Name.Property);
      property.setAttribute("name", line.substring(0,colon));
      property.setAttribute("value", line.substring(colon+2).trim());
      state.push(property);
      state.commit();
    }

    // Probably a paragraph or heading
    else {
      if (config.isDocumentMode()) {
        state.ensureFragment();
      }

      // We're in a fenced code block
      if (state.isElement(Name.Preformat)) {

        // Just add the line
        state.append(line);

      } else {

        // Heading using ATX style
        Pattern headingPattern = Pattern.compile("^\\s*(#{1,6})\\s+(.*?)(#{1,6})?$");
        Matcher m = headingPattern.matcher(line);
        if (m.matches()) {
          state.commitUpto(Name.Fragment);
          String level = Integer.toString(m.group(1).length());
          String text = m.group(2).trim();
          PSMLElement heading = new PSMLElement(Name.Heading);
          heading.setAttribute("level", level);
          state.push(heading, text);
          state.commit();

        } else {

          boolean isTitle = false;

          // Let's check whether we have a heading using SetExt style
          PSMLElement element = new PSMLElement(Name.Para);
          if (next != null) {
            if (next.matches("\\s*==+\\s*")) {
              // We use the '====' as a marker for a new section
              if (config.isDocumentMode() && !state.current().isEmpty()) {
                state.newSection();
              }
              element = new PSMLElement(Name.Heading);
              element.setAttribute("level", "1");

              // Special case for title section
              if (config.isDocumentMode()) {
                PSMLElement section = state.ancestor(Name.Section);
                if ("title".equals(section.getAttribute("id"))) {
                  isTitle = true;
                }
              }

            } else if (next.matches("\\s*--+\\s*")) {
              // We use the '----' as a marker for a new fragment
              if (config.isDocumentMode() && !state.current().isEmpty()) {
                state.newFragment();
              }
              element = new PSMLElement(Name.Heading);
              element.setAttribute("level", "2");
            }
          }

          if (!state.isElement(element.getElement())) {
            state.commitUpto(Name.Fragment);
            state.push(element, line.trim());
          } else {
            if (state.lineBreak) {
              state.lineBreak();
            }
            state.append(line.trim());
          }

          // If the line breaks occurs before 66 characters, we assume it is intentional and insert a break
          state.lineBreak = line.length() < config.getLineBreakThreshold();

          // Special case: we terminate the section title
          if (isTitle) {
            state.commitUpto(Name.Document);
          }
        }
      }
    }

  }


  /**
   * Maintains the state of the parser during processing.
   */
  public static final class State {

    /**
     * List of element that have been committed.
     */
    private List<PSMLElement> elements = new ArrayList<>();

    /**
     * The inline parser to use.
     */
    private InlineParser inline = new InlineParser();

    /**
     * The current context, before it is committed
     */
    private List<PSMLElement> context = new ArrayList<>(4);

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
     * Boolean flag for being inside fenced (```) code.
     */
    private boolean fenced = false;

    /**
     * Indicates whether we are inside fenced (```) code.
     *
     * @return <code>true</code> if inside fenced code.
     */
    public boolean isFenced() {
      return this.fenced;
    }

    /**
     * Sets whether we are inside fenced (```) code.
     *
     * @param whether inside fenced code.
     */
    public void setFenced(boolean fence) {
      this.fenced = fence;
    }

    /**
     * Indicates whether we are within an ordered or unordered list.
     *
     * @return <code>true</code> if the current or parent element is either a list or an nlist.
     */
    public boolean isInList() {
      final int size = this.context.size();
      PSMLElement current = size > 0? this.context.get(size-1) : null;
      PSMLElement parent = size > 1? this.context.get(size-2) : null;
      boolean isCurrentList = current != null && (current.isElement(Name.List) || current.isElement(Name.Nlist));
      boolean isParentList = parent != null && (parent.isElement(Name.List) || parent.isElement(Name.Nlist));
      return isCurrentList || isParentList;
    }

    /**
     * @return the current element.
     */
    public PSMLElement current() {
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
    public PSMLElement ancestor(Name name) {
      final int size = this.context.size();
      if (size == 0) return null;
      for (int i = size-1; i >= 0; i--) {
        PSMLElement element = this.context.get(i);
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
      PSMLElement current = current();
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
        PSMLElement section = new PSMLElement(Name.Section);
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
      commitUpto(Name.Section);
      PSMLElement fragment = new PSMLElement(Name.Fragment);
      fragment.setAttribute("id", ++this.fragmentId);
      push(fragment);
    }

    /**
     * Ensure that we are in a fragment
     */
    public void ensureFragment() {
      if (!isDescendantOf(Name.Section)) {
        newSection();
      }
      if (!isDescendantOf(Name.Fragment)) {
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
      push(new PSMLElement(name), text);
    }

    /**
     * Shorthand method to add a new element to the context and reset the text.
     *
     * @param name Name of the element to push
     */
    public void push(Name name) {
      push(new PSMLElement(name));
    }

    /**
     * Add a new element to the context and reset the text.
     *
     * @param name Name of the element to push
     */
    public void push(PSMLElement element) {
      this.context.add(element);
      this.text = null;
    }

    /**
     * Add a new element to the context and add some text.
     *
     * @param name Name of the element to push
     * @param text Text for the element (not committed)
     */
    public void push(PSMLElement element, String text) {
      this.context.add(element);
      this.text = new StringBuilder(text);
    }

    /**
     * Append text to the current text node preceded by a new line.
     */
    public void append(String text) {
      if (this.text == null) throw new NullPointerException("Failed to add text "+text);
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
        PSMLElement current = this.context.remove(size-1);
        if (size > 1) {
          PSMLElement parent = this.context.get(size-2);
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
        PSMLElement current = this.context.remove(size-1);
        if (size > 1) {
          PSMLElement parent = this.context.get(size-2);
          parent.addNode(current);
        } else {
          this.elements.add(current);
        }
        size = this.context.size();
      }
    }

    /**
     * Commit the text on the current element and add the current element to
     * its parent before removing this element from current context.
     */
    public void commit() {
      commitText();
      int size = this.context.size();
      if (size > 0) {
        PSMLElement current = this.context.remove(size-1);
        if (size > 1) {
          PSMLElement parent = this.context.get(size-2);
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
      PSMLElement current = current();
      if (this.text != null && current != null) {
        List<PSMLNode> nodes = this.inline.parse(this.text.toString());
        current.addNodes(nodes);
        this.text = null;
      }
    }

    /**
     * Insert a line break in a paragraph
     */
    public void lineBreak() {
      commitText();
      current().addNode(new PSMLElement(Name.Br));
      this.text = new StringBuilder();
    }

  }

}
