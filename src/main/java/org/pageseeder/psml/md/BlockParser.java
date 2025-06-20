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

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.util.DiagnosticCollector;
import org.pageseeder.psml.util.NilDiagnosticCollector;

/**
 * The {@code BlockParser} class provides functionality for parsing and processing
 * Markdown input into structured PSML elements. This class supports customizable
 * configuration options through the {@link MarkdownInputOptions} class.
 *
 * <p>It generates the block-level elements delegates inline elements to the inline parser.</p>
 *
 * <p>The {@code BlockParser} supports both instance-based parsing with options provided
 * at construction, and static parsing methods. Some deprecated methods retain backward
 * compatibility and are marked for eventual removal.
 *
 * <p>Deprecated methods include legacy behavior for configurations that have been replaced
 * by {@link MarkdownInputOptions}.
 *
 * <p>Instances of this class are thread-safe</p>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class BlockParser {

  /**
   * Represents the configuration options used by the {@code BlockParser} for parsing Markdown input.
   * This variable holds an instance of {@link MarkdownInputOptions}, which defines the behavior
   * and rules applied during parsing, such as line break thresholds, document mode, and fragment handling.
   */
  private MarkdownInputOptions options;

  /**
   * Constructs a new instance of the BlockParser class with the default Markdown input options.
   * This constructor initializes the parser using the default configuration provided by
   * {@link MarkdownInputOptions#defaultFragmentOptions}.
   */
  public BlockParser() {
    options = MarkdownInputOptions.defaultFragmentOptions();
  }

  /**
   * Constructs a new instance of the BlockParser class with the specified Markdown input options.
   *
   * @param options The {@link MarkdownInputOptions} instance defining custom configurations
   *                for parsing Markdown input.
   */
  public BlockParser(MarkdownInputOptions options) {
    this.options = options;
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of PSML elements
   */
  public List<PSMLElement> parse(List<String> lines) {
    return parse(lines, this.options);
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   * @param collector Capture warnings and errors
   *
   * @return The corresponding list of PSML elements
   */
  public List<PSMLElement> parse(List<String> lines, DiagnosticCollector collector) {
    return parse(lines, this.options, collector);
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
   * @param configuration The configuration to use as Markdown options
   * @deprecated Use {@link #setOptions(MarkdownInputOptions)} instead
   */
  @Deprecated(forRemoval = true, since = "1.6.0")
  public void setConfiguration(Configuration configuration) {
    this.options = configuration.toMarkdownInputOptions();
  }

  /**
   * @return The configuration corresponding the Markdown options used in this class
   * @deprecated Use {@link #getOptions()} instead
   */
  @Deprecated(forRemoval = true, since = "1.6.0")
  public Configuration getConfiguration() {
    return Configuration.fromMarkdownInputOptions(this.options);
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   * @param config The configuration to use
   *
   * @return The corresponding list of PSML elements
   *
   * @deprecated Use {@link #parse(List, MarkdownInputOptions)} instead.
   */
  @Deprecated(forRemoval = true, since = "1.6.0")
  public List<PSMLElement> parse(List<String> lines, Configuration config) {
    return parse(lines, config.toMarkdownInputOptions());
  }

  /**
   * Process a single line
   *
   * @param line  The current line
   * @param next  The next line
   * @param state The state of the parser
   * @param config The configuration to use
   *
   * @deprecated Use {@link #processLine(String, String, State, MarkdownInputOptions)} instead.
   */
  @Deprecated(forRemoval = true, since = "1.6.0")
  public void processLine(String line, @Nullable String next, State state, Configuration config) {
    processLine(line, next, state, config.toMarkdownInputOptions());
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   * @param options Markdown options to use
   *
   * @return The corresponding list of PSML elements
   */
  public static List<PSMLElement> parse(List<String> lines, MarkdownInputOptions options) {
    return parse(lines, options, new NilDiagnosticCollector());
  }

  /**
   * Iterate over the lines and return corresponding PSML elements.
   *
   * @param lines The lines to parse
   *
   * @return The corresponding list of PSML elements
   */
  private static List<PSMLElement> parse(List<String> lines, MarkdownInputOptions options, DiagnosticCollector collector) {
    State state = new State(collector);
    for (int i=0; i < lines.size(); i++) {
      String line = lines.get(i);
      String next = i < lines.size()-1? lines.get(i+1) : null;
      processLine(line, next, state, options);
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
   * @param options Markdown options to use
   */
  public static void processLine(String line, @Nullable String next, State state, MarkdownInputOptions options) {
    state.line++;

    // Lines made entirely of '=' or '-' are used for heading 1 and 2
    if (line.matches("\\s?(==+|--+)\\s*")) {
      // DO nothing, we've already handled it

      // Ensure that metadata is committed before we start with content
      if (options.isDocument() && !state.isDescendantOf(Name.SECTION)) {
        state.commitAll();
      }
    }

    // Separators
    else if (line.matches("\\s*\\*\\s?\\*\\s?\\*[\\s\\*]*")) {
      if (options.isDocument()) {
        state.ensureFragment();
        state.newFragment();
      }
    }

    // Empty lines are used to separate the different kinds of blocks, except inside fenced (```) code
    else if (line.matches("\\s*") && !state.isCodeFenced()) {
      state.commitUpToBlockOrFragment();
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
    else if (line.matches("\\s{4}.*") && !state.isCodeFenced()) {
      if (options.isDocument()) {
        state.ensureFragment();
      }
      if (!state.isElement(Name.PREFORMAT)) {
        state.commitUpToBlockOrFragment();
        state.push(Name.PREFORMAT, line.substring(4));
      } else {
        state.append(line.substring(4));
      }
    }

    // Beginning/end of a fenced code block
    else if (line.startsWith("```")) {
      processFencedCodeBoundary(line, state, options);
    }

    // Beginning/end of fenced block labels
    else if (line.startsWith("~~~")) {
      processFencedBlockBoundary(line, state, options);
    }

    // Lines starting with '>': quoted content
    else if (line.matches("\\s*>+\\s*.*") && !state.isElement(Name.PREFORMAT)) {
      processQuotedContent(line, state, options);
    }

    // Tables starting with `|`
    else if (line.startsWith("|") && !state.isElement(Name.PREFORMAT)) {
      processTableRow(line, next, state);
    }

    // Metadata (document mode only)
    else if (options.isDocument() && !state.isDescendantOf(Name.SECTION) && line.matches("^[^:]+:\\s.*")) {
      processMetadataProperty(line, state, options);
    }

    // Probably a paragraph or heading
    else {
      if (options.isDocument()) {
        state.ensureFragment();
      }

      // We're in a fenced code block
      if (state.isElement(Name.PREFORMAT)) {

        // Just add the line
        state.append(line);

      } else {

        // Heading using ATX style
        Pattern headingPattern = Pattern.compile("^\\s*(#{1,6})\\s+(.*?)(#{1,6})?$");
        Matcher m = headingPattern.matcher(line);
        if (m.matches()) {
          state.commitUpToBlockOrFragment();
          String level = Integer.toString(m.group(1).length());
          String text = m.group(2).trim();

          if (options.isNewFragmentPerHeading()) {
            state.ensureFragment();
            state.newFragment();
          }

          PSMLElement heading = new PSMLElement(Name.HEADING);
          heading.setAttribute("level", level);
          state.push(heading, text);
          state.commit();

        } else {

          boolean isTitle = false;

          // Let's check whether we have a heading using SetExt style
          PSMLElement element = new PSMLElement(Name.PARA);
          if (next != null) {
            if (next.matches("\\s*==+\\s*")) {
              // We use the '====' as a marker for a new section
              if (options.isDocument() && !state.isEmpty()) {
                state.newSection();
              }
              element = new PSMLElement(Name.HEADING);
              element.setAttribute("level", "1");

              // Special case for title section
              if (options.isDocument()) {
                PSMLElement section = state.ancestor(Name.SECTION);
                if (section != null && "title".equals(section.getAttribute("id"))) {
                  isTitle = true;
                }
              }

            } else if (next.matches("\\s*--+\\s*")) {
              // We use the '---' as a marker for a new fragment
              if (options.isDocument() && !state.isEmpty()) {
                state.newFragment();
              }
              element = new PSMLElement(Name.HEADING);
              element.setAttribute("level", "2");
            }
          }

          if (!state.isElement(element.getElement())) {
            state.commitUpToBlockOrFragment();
            state.push(element, line.trim());
          } else {
            if (state.lineBreak) {
              state.lineBreak();
            }
            state.append(line.trim());
          }

          // If the line break occurs before 66 characters, we assume it is intentional and insert a break
          state.lineBreak = line.length() < options.getLineBreakThreshold();

          // Special case: we terminate the section title
          if (isTitle) {
            state.commitUpto(Name.DOCUMENT);
          }
        }
      }
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
        state.commitUpToBlockOrFragment();
        // And create a new list
        PSMLElement list;
        if (no.matches("\\d+\\.")) {
          list = new PSMLElement(Name.NLIST);
          String initial = no.substring(0, no.length()-1);
          if (!"1".equals(initial)) {
            list.setAttribute("start", initial);
          }
        } else {
          list = new PSMLElement(Name.LIST);
        }
        state.push(list);
      }
      // Create a new item
      state.push(Name.ITEM, m.group(2).trim());
    }
  }

  private static void processQuotedContent(String line, State state, MarkdownInputOptions options) {
    if (options.isDocument()) {
      state.ensureFragment();
    }
    // remove chevron and leading space
    String text = line.substring(line.indexOf('>') + 1).replaceFirst("^\\s+", "");
    // check if already in a blockquote
    PSMLElement current = state.current();
    if (current != null && current.isElement(Name.BLOCK)) {
      List<PSMLNode> children = current.getNodes();
      PSMLNode last = children.isEmpty() ? null : children.get(children.size()-1);
      if (last instanceof PSMLElement) {
        PSMLElement lastElement = (PSMLElement) last;
        if (lastElement.isElement(Name.PARA)) {
          if (text.matches("\\s*")) {
            current.addNode(new PSMLElement(Name.PARA));
          } else {
            lastElement.addText((lastElement.getText().isEmpty() ? "" : " ")+text);
          }
        }
      }
    } else {
      state.commitUpToBlockOrFragment();
      // create new blockquote
      PSMLElement block = new PSMLElement(Name.BLOCK);
      block.setAttribute("label", "quoted");
      PSMLElement p = new PSMLElement(Name.PARA);
      p.setText(text);
      block.addNode(p);
      state.push(block);
    }
  }

  private static void processTableRow(String line, @Nullable String next, State state) {
    assert line.startsWith("|");
    String[] columns = line.substring(1).split("\\|");
    boolean inTable = state.isDescendantOf(Name.TABLE);
    boolean isHeaderRow = false;
    if (!inTable && next != null && next.startsWith("|") && next.matches("^\\|([\\s:-]+\\|){"+columns.length+"}")) {
      PSMLElement table = new PSMLElement(Name.TABLE);
      String[] cols = next.substring(1).split("\\|");
      for (String col : cols) {
        String align = toColAlign(col);
        PSMLElement colElement = new PSMLElement(Name.COL);
        if (align != null) colElement.setAttribute("align", align);
        table.addNode(colElement);
      }
      state.push(table);
      inTable = true;
      isHeaderRow = true;
    }

    if (inTable) {
      if (!line.matches("^\\|([\\s:-]+\\|){"+columns.length+"}")) {
        PSMLElement row = new PSMLElement(Name.ROW);
        if (isHeaderRow) row.setAttribute("part", "header");
        state.push(row);

        for (String col : columns) {
          String text = col.trim();
          if (isHeaderRow && text.matches("^\\*\\*(.*)\\*\\*$")) {
            text = text.substring(2, text.length() - 2);
          }
          state.push(Name.CELL, text);
          state.commit();
        }

        state.commit();
      }
    } else {
      // Not a table
      state.push(Name.PARA, line.trim());
    }
  }

  private static void processMetadataProperty(String line, State state, MarkdownInputOptions options) {
    int colon = line.indexOf(':');
    String title = line.substring(0, colon).trim();
    String name = title.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
    String value = line.substring(colon+2).trim();

    // URI metadata
    if (!state.isDescendantOf(Name.METADATA) && name.toLowerCase().matches("^(type|title|description|docid)$")) {
      if (!state.isDescendantOf(Name.DOCUMENTINFO)) {
        state.push(Name.DOCUMENTINFO);
        state.push(Name.URI);
      }
      PSMLElement uri = state.current();
      if (uri != null && uri.isElement(Name.URI)) {
        if ("type".equals(name)) {
          uri.setAttribute("documenttype", value.replaceAll("[^A-Za-z0-9_]", "_"));
        } else if ("docid".equals(name)) {
          uri.setAttribute(name, value.replaceAll("[^A-Za-z0-9_-]", "_"));
        } else if ("title".equals(name)) {
          PSMLElement displayTitle = new PSMLElement(Name.DISPLAYTITLE);
          displayTitle.setText(value);
          uri.addNode(displayTitle);
        } else if ("description".equals(name)) {
          PSMLElement description = new PSMLElement(Name.DESCRIPTION);
          description.setText(value);
          uri.addNode(description);
        }
      }
    }

    // Metadata properties
    else {
      if (!state.isDescendantOf(Name.METADATA)) {
        state.commitAll();
        state.push(Name.METADATA);
        state.push(Name.PROPERTIES);
      }
      // Create and commit a property
      PSMLElement property = new PSMLElement(Name.PROPERTY);
      property.setAttribute("title", title);
      property.setAttribute("name", name);
      if (value.startsWith("[") && value.endsWith("]")) {
        property.setAttribute("multiple", "true");
        String[] values = value.substring(1, value.length()-1).split("\\s*,\\s*");
        for (String v : values) {
          property.addNode(new PSMLElement(Name.VALUE).setText(v.trim()));
        }
      } else {
        property.setAttribute("value", value);
      }
      state.push(property);
      state.commit();
    }
  }

  private static void processFencedCodeBoundary(String line, State state, MarkdownInputOptions options) {
    assert line.startsWith("```");
    if (options.isDocument()) {
      state.ensureFragment();
    }
    if (state.isElement(Name.PREFORMAT)) {
      state.setCodeFence(false);
      state.append("");
      state.commitUpToBlockOrFragment();
    } else {
      state.commitUpToBlockOrFragment();
      PSMLElement pre = new PSMLElement(Name.PREFORMAT);
      if (line.length() > 3) {
        String language = line.substring(3).trim();
        if (!language.isEmpty()) {
          pre.setAttribute("role", "lang-"+language);
        }
      }
      state.push(pre, "");
      state.setCodeFence(true);
    }
  }

  private static void processFencedBlockBoundary(String line, State state, MarkdownInputOptions options) {
    assert line.startsWith("~~~");
    if (options.isDocument()) {
      state.ensureFragment();
    }
    String label = line.substring(3).trim();
    if (state.isDescendantOf(Name.BLOCK) && label.isEmpty()) {
      // End block
      state.commitUpto(Name.BLOCK);
      state.commit();
      state.fencedLabel--;
    } else {
      // Start block
      state.commitUpto(Name.BLOCK);
      state.fencedLabel++;
      PSMLElement block = new PSMLElement(Name.BLOCK);
      if (line.length() > 3) {
        if (!label.isEmpty()) {
          block.setAttribute("label", label.replaceAll("[^a-zA-Z0-9_-]", "_"));
        } else {
          state.warn("No label given for fenced block");
        }
      }
      state.push(block);
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

    private final DiagnosticCollector collector;

    /**
     * List of elements that have been committed.
     */
    private final List<PSMLElement> elements = new ArrayList<>();

    /**
     * The inline parser to use.
     */
    private final InlineParser inline = new InlineParser();

    /**
     * The current context, before it is committed
     */
    private final List<PSMLElement> context = new ArrayList<>(4);

    /**
     * The section identifiers.
     */
    private final String[] sectionIds = new String[]{"title", "content"};

    private int line = 0;

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
     * Represents the state of being inside a fenced code block with (```)
     */
    private boolean codeFence = false;

    private int fencedLabel = 0;

    public State() {
      this.collector = new NilDiagnosticCollector();
    }

    public State(DiagnosticCollector collector) {
      this.collector = collector;
    }

    public void warn(String message) {
      this.collector.warn(message+" at line "+this.line);
    }

    /**
     * @return <code>true</code> if inside fenced code.
     */
    public boolean isCodeFenced() {
      return this.codeFence;
    }

    public boolean isLabelFenced() {
      return this.fencedLabel > 0;
    }

    public void setCodeFence(boolean fence) {
      this.codeFence = fence;
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
      boolean isCurrentList = current != null && (current.isElement(Name.LIST) || current.isElement(Name.NLIST));
      boolean isParentList = parent != null && (parent.isElement(Name.LIST) || parent.isElement(Name.NLIST));
      return isCurrentList || isParentList;
    }

    /**
     * @return the current element.
     */
    public @Nullable PSMLElement current() {
      if (this.context.isEmpty()) return null;
      return this.context.get(this.context.size()-1);
    }

    public boolean isEmpty() {
      PSMLElement element = current();
      return element == null || element.isEmpty();
    }

    /**
     * Indicates whether the current element is a descendant of the specified name.
     *
     * @param name the name of the element to match
     * @return <code>true</code> if the current element matches the specified name;
     *         <code>false</code> otherwise.
     */
    public @Nullable PSMLElement ancestor(Name name) {
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
        PSMLElement section = new PSMLElement(Name.SECTION);
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
      PSMLElement fragment = new PSMLElement(Name.FRAGMENT);
      fragment.setAttribute("id", ++this.fragmentId);
      push(fragment);
    }

    /**
     * Ensure that we are in a fragment
     */
    public void ensureFragment() {
      if (!isDescendantOf(Name.SECTION)) {
        newSection();
      }
      if (!isDescendantOf(Name.FRAGMENT)) {
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
     * @param element The element to push
     */
    public void push(PSMLElement element) {
      this.context.add(element);
      this.text = null;
    }

    /**
     * Add a new element to the context and add some text.
     *
     * @param element The element to push
     * @param text Text for the element (not committed)
     */
    public void push(PSMLElement element, String text) {
      this.context.add(element);
      this.text = new StringBuilder(text);
    }

    /**
     * Append text to the current text node preceded by a new line.
     * @param text Text to append
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

    public void commitUpToBlockOrFragment() {
      commitUpto(fencedLabel > 0 && isDescendantOf(Name.BLOCK) ? Name.BLOCK : Name.FRAGMENT);
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
      PSMLElement current = current();
      if (current != null) {
        current.addNode(new PSMLElement(Name.BR));
      }
      this.text = new StringBuilder();
    }

  }

}
