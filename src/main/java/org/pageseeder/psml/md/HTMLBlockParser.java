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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.html.HTMLElement;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.html.HTMLNode;

/**
 * The block parser parses Markdown and generates the block-level elements
 * delegating inline elements to the HTML inline parser.
 *
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 1.0
 */
public class HTMLBlockParser {

  /**
   * Represents the configuration options used for parsing Markdown input.
   */
  private MarkdownInputOptions options;

  /**
   * Constructs a new instance of the parser with the default Markdown input options.
   *
   * <p>This constructor initializes the parser using the default configuration provided by
   * {@link MarkdownInputOptions#defaultFragmentOptions}.
   */
  public HTMLBlockParser() {
    this.options = MarkdownInputOptions.defaultFragmentOptions();
  }

  /**
   * Constructs a new instance of the parser with the specified Markdown input options.
   *
   * @param options The {@link MarkdownInputOptions} instance defining custom configurations
   *                for parsing Markdown input.
   */
  public HTMLBlockParser(MarkdownInputOptions options) {
    this.options = Objects.requireNonNull(options);
  }

  /**
   * Iterate over the lines and return corresponding HTML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of HTML elements
   */
  public List<HTMLElement> parse(List<String> lines) {
    return parse(lines, this.options);
  }

  /**
   * Iterate over the lines and return corresponding HTML elements.
   *
   * @param lines The lines to parse
   * @param options The markdown options to use
   *
   * @return The corresponding list of HTML elements
   */
  public List<HTMLElement> parse(List<String> lines, MarkdownInputOptions options) {
    State state = new State();
    for (int i=0; i < lines.size(); i++) {
      String line = lines.get(i);
      String next = i < lines.size()-1? lines.get(i+1) : null;
      processLine(line, next, state, options);
    }
    state.commitAll();
    return state.elements;
  }

  /**
   * Retrieves the current Markdown input options used for parsing.
   *
   * @return The current {@link MarkdownInputOptions} instance.
   */
  public MarkdownInputOptions getOptions() {
    return options;
  }

  /**
   * Sets the Markdown input options to configure parsing behavior.
   *
   * @param options The {@link MarkdownInputOptions} instance to define custom parsing configurations.
   */
  public void setOptions(MarkdownInputOptions options) {
    this.options = options;
  }

  /**
   * Process a single line
   *
   * @param line  The current line
   * @param next  The next line
   * @param state The state of the parser
   * @param options The markdown options to use
   */
  public static void processLine(String line, @Nullable String next, State state, MarkdownInputOptions options) {
    // Lines made entirely of '=' or '-' are used for heading 1 and 2
    if (line.matches("\\s?(==+|--+)\\s*")) {
      // DO nothing, we've already handled it
    }

    // Separators
    else if (line.matches("\\s*\\*\\s?\\*\\s?\\*[\\s\\*]*")) {
      if (options.isDocument()) {
        state.ensureFragment();
        state.newFragment();
      }
    }

    // Empty lines are used to separate the different kinds of blocks, except inside fenced (```) code
    else if (line.matches("\\s*") && !state.isFenced()) {
      state.commitUpto(Name.SECTION);
    }

    // New list items starting with '+', '-', '*' or number followed by a '.'
    else if (line.matches("\\s*(-|\\+|\\*|\\d+\\.)\\s.+")) {
      processListItem(line, state, options);
    }

    // Continuation of a list item
    else if (state.isInList()) {
      if (options.isDocument()) {
        state.ensureFragment();
      }
      if (!state.context.isEmpty() && state.text != null) {
        state.append(line.trim());
      }
    }

    // Lines starting with four spaces: preformatted code
    else if (line.matches("\\s{4}.*") && !state.isFenced()) {
      if (options.isDocument()) {
        state.ensureFragment();
      }
      if (!state.isElement(Name.PRE)) {
        state.commitUpto(Name.SECTION);
        state.push(Name.PRE, line.substring(4));
      } else {
        state.append(line.substring(4));
      }
    }

    // Beginning/end of fenced code block
    else if (line.startsWith("```")) {
      processFencedCode(line, state, options);
    }

    // Beginning/end of fenced block labels
    else if (line.startsWith("~~~")) {
      processFencedLabel(line, state, options);
    }

    // Lines starting with '>': quoted content
    else if (line.matches("\\s*>+\\s*.*") && !state.isElement(Name.PRE)) {
      processQuoteBlock(line, state, options);
    }

    // Tables starting with `|`
    else if (line.startsWith("|") && !state.isElement(Name.PRE)) {
      processTableRow(line, next, state, options);
    }

    // Metadata (document mode only)
    else if (options.isDocument() && !state.isDescendantOf(Name.SECTION) && line.matches("^\\w+:\\s.*")) {
      processMetadataDefinition(line, state, options);
    }

    // Probably a paragraph or heading
    else {
      if (options.isDocument()) {
        state.ensureFragment();
      }

      // We're in a fenced code block
      if (state.isElement(Name.PRE) || (state.isElement(Name.CODE) && state.isDescendantOf(Name.PRE))) {

        // Just add the line
        state.append(line);

      } else {

        // Heading using ATX style
        Pattern headingPattern = Pattern.compile("^\\s*(#{1,6})\\s+(.*?)(#{1,6})?$");
        Matcher m = headingPattern.matcher(line);
        if (m.matches()) {
          state.commitUpto(Name.SECTION);
          int level = m.group(1).length();
          String text = m.group(2).trim();
          HTMLElement heading = newHeadingElement(level);
          state.push(heading, text);
          state.commit();

        } else {

          boolean isTitle = false;

          // Assume paragraph, but check whether we have a heading using SetExt style
          HTMLElement element = new HTMLElement(Name.P);
          if (next != null) {
            if (next.matches("\\s*==+\\s*")) {
              // We use the '====' as a marker for a new section
              if (options.isDocument() && !state.current().isEmpty()) {
                state.newSection();
              }
              element = newHeadingElement(1);

              // Special case for title section
              if (options.isDocument()) {
                HTMLElement section = state.ancestor(Name.SECTION);
                if (section != null && "title".equals(section.getAttribute("id"))) {
                  isTitle = true;
                }
              }

            } else if (next.matches("\\s*--+\\s*")) {
              // We use the '----' as a marker for a new fragment
              if (options.isDocument() && !state.current().isEmpty()) {
                state.newFragment();
              }
              element = newHeadingElement(2);
            }
          }

          // Check whether the current element matches what we found (h1, h2 or p)
          if (!state.isElement(element.getElement())) {
            state.commitUpto(Name.SECTION);
            state.push(element, line.trim());
          } else {
            if (state.lineBreak) {
              state.lineBreak();
            }
            state.append(line.trim());
          }

          // If the line break occurs before 66 characters, we assume it is intentional and insert a line break
          state.lineBreak = line.length() < options.getLineBreakThreshold();

          // Special case: we terminate the section title
          if (isTitle) {
            state.commitUpto(Name.ARTICLE);
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
      case 1: return new HTMLElement(Name.H1);
      case 2: return new HTMLElement(Name.H2);
      case 3: return new HTMLElement(Name.H3);
      case 4: return new HTMLElement(Name.H4);
      case 5: return new HTMLElement(Name.H5);
      case 6: return new HTMLElement(Name.H6);
      default: return new HTMLElement(Name.P);
    }
  }

  private static void processListItem(String line, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }

    // Create a new item
    Pattern x  = Pattern.compile("^\\s*(-|\\+|\\*|\\d+\\.)\\s+(.+)$");
    Matcher m = x.matcher(line);
    if (m.matches()) {
      String no = m.group(1);
      if (state.isInList()) {
        // Already in a list, let's commit the previous item
        state.commit();
      } else {
        // A new list! Clear the context...
        state.commitUpto(Name.SECTION);
        // An create a new list
        HTMLElement list;
        if (no.matches("\\d+\\.")) {
          list = new HTMLElement(Name.OL);
          String initial = no.substring(0, no.length()-1);
          if (!"1".equals(initial)) {
            list.setAttribute("start", initial);
          }
        } else {
          list = new HTMLElement(Name.UL);
        }
        state.push(list);
      }
      // Create a new item
      state.push(Name.LI, m.group(2).trim());
    }
  }

  private static void processFencedCode(String line, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }
    if (state.isElement(Name.PRE) || (state.isElement(Name.CODE) && state.isDescendantOf(Name.PRE))) {
      state.setFenced(false);
      state.append("");
      state.commitUpto(Name.SECTION);
    } else {
      state.commitUpto(Name.SECTION);
      HTMLElement pre = new HTMLElement(Name.PRE);
      state.push(pre, "");
      state.setFenced(true);
      if (line.length() > 3) {
        HTMLElement code = new HTMLElement(Name.CODE);
        String language = line.substring(3).trim();
        if (!language.isEmpty()) {
          code.setAttribute("class", "lang-"+language);
        }
        state.push(code, "");
      }
    }
  }

  private static void processFencedLabel(String line, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }
    if (state.isElement(Name.DIV)) {
      state.commitUpto(Name.SECTION);
    } else {
      state.commitUpto(Name.SECTION);
      HTMLElement pre = new HTMLElement(Name.DIV);
      if (line.length() > 3) {
        String label = line.substring(3).trim();
        if (!label.isEmpty()) {
          pre.setAttribute("label", label);
          pre.setAttribute("class", "label-"+label);
        }
      }
      state.push(pre, "");
    }
  }

  private static void processQuoteBlock(String line, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }
    String text = line.substring(line.indexOf('>') + 1).replaceFirst("^\\s+", "");
    // check if already in a blockquote
    HTMLElement current = state.current();
    if (current != null && current.isElement(Name.BLOCKQUOTE)) {
      List<HTMLNode> children = current.getNodes();
      HTMLNode last = children.isEmpty() ? null : children.get(children.size()-1);
      if (last instanceof HTMLElement) {
        HTMLElement lastElement = (HTMLElement) last;
        if (lastElement.isElement(Name.P)) {
          if (text.matches("\\s*")) {
            current.addNode(new HTMLElement(Name.P));
          } else {
            lastElement.addText((lastElement.getText().isEmpty() ? "" : " ")+text);
          }
        }
      }
    } else {
      state.commitUpto(Name.SECTION);
      // create new blockquote
      HTMLElement block = new HTMLElement(Name.BLOCKQUOTE);
      HTMLElement p = new HTMLElement(Name.P);
      p.setText(text);
      block.addNode(p);
      state.push(block);
    }
  }

  private static void processMetadataDefinition(String line, State state, MarkdownInputOptions options) {
    int colon = line.indexOf(':');
    if (!state.isDescendantOf(Name.DL)) {
      state.push(Name.DL);
    }
    // Create and commit a definition list
    state.push(Name.DT, line.substring(0,colon));
    state.push(Name.DD, line.substring(colon+2).trim());
    state.commit();
  }

  private static void processTableRow(String line, @Nullable String next, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }
    assert line.startsWith("|");
    String[] columns = line.substring(1).split("\\|");
    boolean inTable = state.isDescendantOf(Name.TABLE);
    boolean isHeaderRow = false;
    if (!inTable && next != null && next.startsWith("|") && next.matches("^\\|([\\s:-]+\\|){"+columns.length+"}")) {
      HTMLElement table = new HTMLElement(Name.TABLE);
      String[] cols = next.substring(1).split("\\|");
      for (String col : cols) {
        String align = toColAlign(col);
        HTMLElement colElement = new HTMLElement(Name.COL);
        if (align != null) colElement.setAttribute("align", align);
        table.addNode(colElement);
      }
      state.push(table);
      inTable = true;
      isHeaderRow = true;
    }

    if (inTable) {
      if (!line.matches("^\\|([\\s:-]+\\|){"+columns.length+"}")) {
        HTMLElement row = new HTMLElement(Name.TR);
        state.push(row);

        for (String col : columns) {
          String text = col.trim();
          if (isHeaderRow && text.matches("^\\*\\*(.*)\\*\\*$")) {
            text = text.substring(2, text.length() - 2);
          }
          state.push(isHeaderRow ? Name.TH : Name.TD, text);
          state.commit();
        }

        state.commit();
      }
    } else {
      // Not a table
      state.push(Name.P, line.trim());
    }
  }

  /**
   * Determines the text alignment for a column based on its specification.
   *
   * <p>The method analyzes if the column definition starts and/or ends with a colon
   * to infer the alignment: "center", "left", "right", or null if not specified.
   *
   * @param col The column specification string to evaluate. It may include colons
   *            to indicate text alignment.
   * @return A string indicating the column alignment ("center", "left", "right")
   *         or null if the alignment is not specified.
   */
  private static @Nullable String toColAlign(String col) {
    String colSpec = col.trim();
    boolean startWithColon = colSpec.startsWith(":");
    boolean endsWithColon = colSpec.endsWith(":");
    if (startWithColon && endsWithColon) return "center";
    if (startWithColon) return "left";
    if (endsWithColon) return "right";
    return null;
  }

  /**
   * Maintains the state of the parser during processing.
   */
  public static final class State {

    /**
     * List of element that have been committed.
     */
    private final List<HTMLElement> elements = new ArrayList<>();

    /**
     * The inline parser to use.
     */
    private final HTMLInlineParser inline = new HTMLInlineParser();

    /**
     * The current context, before it is committed
     */
    private final List<HTMLElement> context = new ArrayList<>(4);

    /**
     * The section identifiers.
     */
    private final String[] sectionIds = new String[]{"title", "content"};

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
    private @Nullable StringBuilder text = null;

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
     * @param fence whether inside fenced code.
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
      HTMLElement current = size > 0? this.context.get(size-1) : null;
      HTMLElement parent = size > 1? this.context.get(size-2) : null;
      boolean isCurrentList = current != null && (current.isElement(Name.UL) || current.isElement(Name.OL));
      boolean isParentList = parent != null && (parent.isElement(Name.UL) || parent.isElement(Name.OL));
      return isCurrentList || isParentList;
    }

    /**
     * @return the current element.
     */
    public @Nullable HTMLElement current() {
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
    public @Nullable HTMLElement ancestor(Name name) {
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
        HTMLElement section = new HTMLElement(Name.SECTION);
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
      commitUpto(Name.SECTION);
      HTMLElement fragment = new HTMLElement(Name.SECTION);
      fragment.setAttribute("id", ++this.fragmentId);
      push(fragment);
    }

    /**
     * Ensure that we are in a fragment
     */
    public void ensureFragment() {
      if (!isDescendantOf(Name.ARTICLE)) {
        newSection();
      }
      if (!isDescendantOf(Name.SECTION)) {
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
     * @param element The element to push
     */
    public void push(HTMLElement element) {
      this.context.add(element);
      this.text = null;
    }

    /**
     * Add a new element to the context and add some text.
     *
     * @param element The element to push
     * @param text Text for the element (not committed)
     */
    public void push(HTMLElement element, String text) {
      this.context.add(element);
      this.text = new StringBuilder(text);
    }

    /**
     * Append text to the current text node preceded by a new line.
     * @param text The text to append
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
     * @param name The name of the element where we stop committing.
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
      HTMLElement current = current();
      if (current != null) current.addNode(new HTMLElement(Name.BR));
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
