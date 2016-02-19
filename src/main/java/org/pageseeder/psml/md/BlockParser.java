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

import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;

public class BlockParser {


  public BlockParser() {
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
    if (line.matches("^\\s?(==+|\\-\\-+)\\s*$")) {
      // DO nothing
    }

    // Empty lines are used to separate the different kinds of blocks
    else if (line.matches("\\s*")) {
      state.commitAll();
    }

    // New list items starting with '+', '-', '*' or number followed by a '.'
    else if (line.matches("^\\s*(-|\\+|\\*|\\d+\\.)\\s.+")) {

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
          state.commitAll();
          // An create a new list
          PSMLElement list;
          if (no.matches("\\d+\\.")) {
            list = new PSMLElement(Name.Nlist);
            list.setAttribute("start", no.substring(0, no.length()-1));
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
      if (!state.context.isEmpty() && state.text != null) {
        state.append(line.trim());
      }
      state.endMetadata();
    }

    // Lines starting with two spaces: preformatted code
    else if (line.matches("^\\s{2}")) {
      if (!state.isElement(Name.Preformat)) {
        state.commitAll();
        state.push(Name.Preformat, line.substring(3));
      } else {
        state.append(line.substring(3));
      }

      // Metadata no longer allowed
      state.endMetadata();
    }

    // Lines starting with '>': quoted content
    else if (line.matches("^\\s*>+\\s?")) {
      if (!state.isElement(Name.Block)) {
        state.commitAll();
        state.push(Name.Block, line.substring(line.indexOf('>')));
      } else {
        state.text.append(line.substring(line.indexOf('>')));
      }
      // Metadata no longer allowed
      state.endMetadata();
    }

    // Metadata
    else if (state.metadata && line.matches("^\\w+\\:\\s.*")) {
      int colon = line.indexOf(':');
      if (state.context.isEmpty()) {
        state.push(new PSMLElement(Name.Metadata), "");
        state.push(new PSMLElement(Name.Properties), "");
      }
      PSMLElement element = new PSMLElement(Name.Property);
      element.setAttribute("name", line.substring(0,colon));
      element.setAttribute("value", line.substring(colon+2).trim());
      state.push(element);
      state.commit();
    }

    // Probably a paragraph or heading
    else {
      Pattern heading = Pattern.compile("^\\s*(#{1,6})\\s+(.*)$");
      Matcher m = heading.matcher(line);
      if (m.matches()) {
        state.commitAll();
        String h = Integer.toString(m.group(1).length());
        String t = m.group(2).trim();
        PSMLElement element = new PSMLElement(Name.Heading);
        element.setAttribute("level", h);
        element.addNode(new PSMLText(t));
        state.elements.add(element);

      } else {

        // Let's check whether we have a heading
        PSMLElement element = new PSMLElement(Name.Para);
        if (next != null) {
          if (next.matches("\\s*==+\\s*")) {
            element = new PSMLElement(Name.Heading);
            element.setAttribute("level", "1");
          } else if (next.matches("\\s*--+\\s*")) {
            element = new PSMLElement(Name.Heading);
            element.setAttribute("level", "2");
          }
        }

        if (!state.isElement(element.getElement())) {
          state.commitAll();
          state.push(element, line.trim());
        } else {
          state.append(line.trim());
        }

        // If the line breaks occurs before 66 characters, we assume it is intentional and insert a break
        if (line.length() < config.getLineBreakThreshold()) {
          state.commitAll();
        }

      }
      state.endMetadata();
    }

  }


  /**
   * Maintains the state of the parser during processing.
   */
  public static final class State {

    /**
     * List of element found by this parser
     */
    private List<PSMLElement> elements = new ArrayList<>();

    /**
     * Whether it is possible to define metadata.
     */
    private boolean metadata = true;

    private InlineParser inline = new InlineParser();

    /**
     * The current context, before it is committed
     */
    private List<PSMLElement> context = new ArrayList<>(4);

    /**
     * String of text for the current element
     */
    private StringBuilder text = null;

    public boolean isInList() {
      final int size = this.context.size();
      PSMLElement current = size > 0? this.context.get(size-1) : null;
      PSMLElement parent = size > 1? this.context.get(size-2) : null;
      boolean isCurrentList = current != null && (current.isElement(Name.List) || current.isElement(Name.Nlist));
      boolean isParentList = parent != null && (parent.isElement(Name.List) || parent.isElement(Name.Nlist));
      return isCurrentList || isParentList;
    }

    private PSMLElement peek() {
      if (this.context.isEmpty()) return null;
      return this.context.get(this.context.size()-1);
    }


    public boolean isElement(Name name) {
      PSMLElement current = peek();
      return current != null && current.isElement(name);
    }

    public void push(Name name, String text) {
      push(new PSMLElement(name), text);
    }

    public void push(PSMLElement element) {
      this.context.add(element);
      this.text = null;
    }

    public void push(PSMLElement element, String text) {
      this.context.add(element);
      this.text = new StringBuilder(text);
    }

    public void append(String text) {
      this.text.append('\n').append(text);
    }

    /**
     * Empty the current stack and attach the text to the
     * current node.
     */
    public void commitAll() {
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

    public void commitText() {
      PSMLElement current = peek();
      if (this.text != null && current != null) {
        List<PSMLNode> nodes = this.inline.parse(this.text.toString());
        current.addNodes(nodes);
        this.text = null;
      }
    }

    public void endMetadata() {
      this.metadata = false;
    }

  }

}
