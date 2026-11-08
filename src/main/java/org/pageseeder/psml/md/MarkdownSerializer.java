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

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.md.MarkdownOutputOptions.DiffFormat;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.DiffOperation;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;
import org.pageseeder.psml.util.DiagnosticCollector;
import org.pageseeder.psml.util.NilDiagnosticCollector;
import org.pageseeder.psml.util.Subscripts;
import org.pageseeder.psml.util.Superscripts;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is responsible for serializing PSML content into Markdown format.
 *
 * <h2>Diff output</h2>
 * <p>With {@link DiffFormat#INS_DEL}, combined Diff-X PSML (for example a {@code <diff>} element
 * or a fragment produced by {@link org.pageseeder.psml.diff.PSMLDiffer}) is serialized with
 * {@code <ins>} and {@code <del>} markers:
 * <pre>{@code
 * MarkdownSerializer serializer = new MarkdownSerializer();
 * serializer.setOptions(MarkdownOutputOptions.defaultOptions().diff(DiffFormat.INS_DEL));
 * serializer.serialize(diffFragment, out);
 * }</pre>
 * <p>For example, {@code <para diff:insert="true"><diff:ins>New paragraph.</diff:ins></para>}
 * becomes {@code <ins>New paragraph.</ins>}, and {@code ## <ins>New heading</ins>} keeps the
 * heading syntax outside the marker. The {@code <compare>} element is ignored in both modes.
 *
 * @version 1.8.6
 * @since 1.0
 */
@SuppressWarnings("java:S1192")
public class MarkdownSerializer {

  /**
   * The default configuration for generating Markdown content.
   */
  MarkdownOutputOptions options = MarkdownOutputOptions.defaultOptions();

  /**
   * Sets the configuration options for customizing the Markdown output.
   *
   * @param options the {@code MarkdownOutputOptions} instance containing settings
   *                for customizing the Markdown output, such as metadata inclusion
   *                or handling of image URLs and cross-references.
   */
  public void setOptions(MarkdownOutputOptions options) {
    this.options = options;
  }

  /**
   * Retrieves the current configuration options used for customizing the Markdown output.
   *
   * @return the {@code MarkdownOutputOptions} instance that contains the settings
   *         for generating the Markdown output.
   */
  public MarkdownOutputOptions getOptions() {
    return options;
  }

  /**
   * Serializes the given PSMLElement into Markdown format and writes the output
   * to the specified Appendable.
   *
   * @param element the {@code PSMLElement} to be serialized into Markdown format
   * @param out the {@code Appendable} where the serialized Markdown output
   *            will be written
   * @throws IOException if an I/O error occurs during serialization
   */
  public void serialize(PSMLElement element, Appendable out) throws IOException {
    Instance instance = new Instance(this.options, new NilDiagnosticCollector());
    serialize(instance, element, out);
  }

  /**
   * Serializes the provided PSMLElement into Markdown format and writes the output
   * to the specified Appendable. Additionally, diagnostic messages generated during
   * the serialization process are collected in the provided DiagnosticCollector.
   *
   * @param element the {@code PSMLElement} to be serialized into Markdown format
   * @param out the {@code Appendable} where the serialized Markdown output will be written
   * @param collector the {@code DiagnosticCollector} used to collect diagnostic
   *                  messages during the serialization process
   * @throws IOException if an I/O error occurs during serialization
   */
  public void serialize(PSMLElement element, Appendable out, DiagnosticCollector collector) throws IOException {
    Instance instance = new Instance(this.options, collector);
    serialize(instance, element, out);
  }

  /**
   * In diff mode, serializes into a buffer so that diff markers can be finished before being written.
   */
  private static void serialize(Instance instance, PSMLElement element, Appendable out) throws IOException {
    if (instance.diff) {
      StringBuilder buffer = new StringBuilder();
      instance.serialize(element, buffer);
      out.append(DiffMarkers.finish(buffer));
    } else {
      instance.serialize(element, out);
    }
  }

  /**
   * Normalizes the input text by collapsing consecutive whitespace characters into a single space.
   */
  static String normalizeText(String text) {
    return text.replaceAll("\\s+", " ");
  }

  private static class State {

    private final Deque<Name> context = new ArrayDeque<>();

    private int imageCounter = 0;

    private int tableCounter = 0;

    private int propertiesCounter = 0;

    private final int[] headingPrefix = {0, 0, 0, 0, 0, 0};

    private final int[] paraPrefix = {0, 0, 0, 0, 0};

    private String host = "";
    private String path = "";

    /**
     * The diff operation that applies to the visible content being serialized (diff mode only).
     */
    private DiffOperation inherited = DiffOperation.NONE;

    /**
     * The diff marker currently open in the output (diff mode only).
     */
    private DiffOperation open = DiffOperation.NONE;

    public void push(Name name) {
      this.context.push(name);
    }

    public Name pop() {
      return this.context.pop();
    }

    String nextImage() {
      return "Image " + (++this.imageCounter);
    }

    String nextTable() {
      return "Table " + (++this.tableCounter);
    }

    String nextProperties() {
      return "Properties " + (++this.propertiesCounter);
    }

    String toRelativeLocalPath(String href, DiagnosticCollector collector) {
      if (href.startsWith("http://") || href.startsWith("https://")) return href;
      if (this.path.isEmpty()) return href;
      // Compute path relative to state.path
      try {
        Path base = Paths.get(this.path).getParent();
        Path target = Paths.get(href);
        if (base == null) {
          return href;
        }
        Path relative = base.relativize(target);
        return relative.toString().replace('\\', '/');
      } catch (IllegalArgumentException ex) {
        collector.warn("Unable to relativize path " + href + " to " + this.path);
        return href;
      }
    }

    @SuppressWarnings("java:S1075") // We want to use the path separator used on Web so '/' only
    String toExternalUrl(String href) {
      if (href.startsWith("http://") || href.startsWith("https://")) return href;
      if (this.host.isEmpty()) return href;
      String baseUrl = "https://" + this.host;
      if (href.startsWith("/")) return baseUrl + href;
      // Compute absolute path based on state.path
      Path basePath = Paths.get(this.path).getParent();
      Path resolvedPath = (basePath != null)
          ? basePath.resolve(href).normalize()
          : Paths.get(href).normalize();
      String externalPath = resolvedPath.toString().replace('\\', '/');
      if (!externalPath.startsWith("/")) {
        externalPath = "/" + externalPath;
      }
      return baseUrl + externalPath;
    }

    String nextHeadingPrefix(int level) {
      if (level <= 0 || level >= headingPrefix.length) return "";
      // Update levels
      headingPrefix[level - 1] = headingPrefix[level - 1] + 1;
      for (int i = level; i < headingPrefix.length; i++) {
        headingPrefix[i] = 0;
      }
      StringBuilder prefix = new StringBuilder();
      prefix.append(headingPrefix[0]);
      if (level > 1) prefix.append('.').append(headingPrefix[1]);
      if (level > 2) prefix.append('.').append(headingPrefix[2]);
      if (level > 3) prefix.append('.').append(headingPrefix[3]);
      if (level > 4) prefix.append('.').append(headingPrefix[4]);
      if (level > 5) prefix.append('.').append(headingPrefix[5]);
      return prefix.append(" ").toString();
    }

    String nextParaPrefix(int indent) {
      if (indent < 0 || indent >= paraPrefix.length) return "";
      // Update levels
      paraPrefix[indent] = paraPrefix[indent] + 1;
      for (int i = indent + 1; i < paraPrefix.length; i++) {
        paraPrefix[i] = 0;
      }
      switch (indent) {
        case 0:
          return "(" + ((char) ('a' + (paraPrefix[0] - 1))) + ") ";
        case 1:
          return "(" + ((char) ('a' + (paraPrefix[1] - 1))) + ") ";
        case 2:
          return "(" + ((char) ('i' + (paraPrefix[2] - 1))) + ") ";
        case 3:
          return "(" + ((char) ('A' + (paraPrefix[3] - 1))) + ") ";
        case 4:
          return "(" + ((char) ('I' + (paraPrefix[4] - 1))) + ") ";
        default:
          return "";
      }
    }

    boolean isDescendantOf(Name name) {
      for (Name n : this.context) {
        if (n == name) return true;
      }
      return false;
    }

    int listLevel() {
      int level = -1;
      for (Name n : this.context) {
        if (n == Name.LIST || n == Name.NLIST) level++;
      }
      return level;
    }

  }

  private static class Instance {

    private final MarkdownOutputOptions options;

    private final State state = new State();

    private final DiagnosticCollector collector;

    /**
     * Whether diff markers are emitted ({@link DiffFormat#INS_DEL}).
     */
    private final boolean diff;

    /**
     * Whether a warning was already reported for sentinel characters in the source.
     */
    private boolean sentinelWarning = false;

    /**
     * Elements already checked for diff annotations, so each is reported at most once.
     */
    private final Set<PSMLElement> checked = Collections.newSetFromMap(new IdentityHashMap<>());

    Instance(MarkdownOutputOptions options, DiagnosticCollector collector) {
      this.options = options;
      this.collector = Objects.requireNonNull(collector);
      this.diff = options.diff() == DiffFormat.INS_DEL;
    }

    void serialize(PSMLElement element, Appendable out) throws IOException {
      if (!this.diff) {
        serializeElement(element, out);
        return;
      }
      DiffOperation inherited = state.inherited;
      Name name = element.getElement();
      checkDiffMarkup(element);
      DiffOperation op = opOf(element);
      if (op != DiffOperation.NONE) state.inherited = op;
      try {
        if (op != DiffOperation.NONE && op != state.open && isWrappedWhole(name)) {
          wrap(op, out, () -> serializeElement(element, out));
        } else {
          serializeElement(element, out);
        }
      } finally {
        state.inherited = inherited;
      }
    }

    /**
     * Reports unsupported or contradictory diff annotations on the element.
     *
     * <ul>
     *   <li>{@code diff:insert} and {@code diff:delete} both {@code true}: no operation is inherited
     *   from the element; only the text markers inside it apply.</li>
     *   <li>{@code diff:insert} or {@code diff:delete} other than {@code true}/{@code false}: ignored.</li>
     *   <li>Any other element or attribute in the Diff-X namespace: ignored, the content is
     *   serialized without markers of its own.</li>
     * </ul>
     */
    private void checkDiffMarkup(PSMLElement element) {
      if (!this.checked.add(element)) return;
      Name name = element.getElement();
      String label = "<" + name.element() + ">";
      if (name == Name.DIFF_UNKNOWN) {
        this.collector.warn("Unsupported Diff-X element: its content is serialized without diff markers");
      }
      if (element.isConflictingDiffOperation()) {
        this.collector.warn("Element " + label + " is marked as both inserted and deleted: only the text markers inside it apply");
      }
      for (String attribute : element.getAttributeNames()) {
        if (!attribute.startsWith("diff:")) continue;
        if ("diff:insert".equals(attribute) || "diff:delete".equals(attribute)) {
          String value = element.getAttribute(attribute);
          if (!"true".equals(value) && !"false".equals(value)) {
            this.collector.warn("Element " + label + " has an invalid value for " + attribute + " ('" + value + "'): it is ignored");
          }
        } else {
          this.collector.warn("Element " + label + " has an unsupported Diff-X attribute " + attribute + ": it is ignored");
        }
      }
    }

    /**
     * Returns the diff operation that an element applies to its content.
     */
    private static DiffOperation opOf(PSMLElement element) {
      Name name = element.getElement();
      if (name == Name.DIFF_INS) return DiffOperation.INSERT;
      if (name == Name.DIFF_DEL) return DiffOperation.DELETE;
      return element.getDiffOperation();
    }

    /**
     * Returns the text wrapped in a marker for the operation, unless it is empty, the operation
     * is NONE, or a marker for the same operation is already open.
     */
    private String marked(DiffOperation op, String text) {
      if (text.isEmpty() || op == DiffOperation.NONE || op == state.open) return text;
      String marked = DiffMarkers.open(op) + text + DiffMarkers.close(op);
      if (state.open == DiffOperation.NONE) return marked;
      // Close the marker for the other operation and reopen it after
      return DiffMarkers.close(state.open) + marked + DiffMarkers.open(state.open);
    }

    /**
     * Collects the text of an element as segments tagged with the diff operation that applies to them.
     *
     * <p>Text markers ({@code diff:ins}, {@code diff:del}) are the innermost operation, so they take
     * priority over the operation inherited from structural annotations. Sentinels are removed from
     * the text but it is not escaped.
     */
    private void collectSegments(PSMLElement element, DiffOperation inherited, List<Segment> segments) {
      checkDiffMarkup(element);
      DiffOperation op = opOf(element);
      DiffOperation current = op == DiffOperation.NONE ? inherited : op;
      for (PSMLNode node : element.getNodes()) {
        if (node instanceof PSMLText) {
          segments.add(new Segment(current, stripSentinels(node.getText())));
        } else {
          collectSegments((PSMLElement) node, current, segments);
        }
      }
    }

    /**
     * Returns the text of the element with sentinels for diff markers, merging adjacent segments
     * with the same operation. The text is not escaped or normalized.
     *
     * @param element   The element
     * @param container An element containing it whose diff operation also applies (may be null)
     */
    String markedText(PSMLElement element, @Nullable PSMLElement container) {
      DiffOperation inherited = state.inherited;
      if (container != null) {
        checkDiffMarkup(container);
        if (opOf(container) != DiffOperation.NONE) inherited = opOf(container);
      }
      List<Segment> segments = new ArrayList<>();
      collectSegments(element, inherited, segments);
      return markedText(segments, text -> text);
    }

    /**
     * Returns the text of the segments with sentinels for diff markers, merging adjacent segments
     * with the same operation.
     *
     * <p>A marker already open in the output is not repeated; a segment with another operation
     * closes it and reopens it after.
     *
     * @param segments  The segments of text
     * @param transform A transformation applied to the text of each segment
     */
    private String markedText(List<Segment> segments, TextTransform transform) {
      DiffOperation base = state.open;
      StringBuilder out = new StringBuilder();
      DiffOperation current = base;
      for (Segment segment : segments) {
        if (segment.text.isEmpty()) continue;
        DiffOperation target = segment.op == DiffOperation.NONE ? base : segment.op;
        current = switchMarker(out, base, current, target);
        out.append(transform.apply(segment.text));
      }
      switchMarker(out, base, current, base);
      return out.toString();
    }

    /**
     * Switches the marker in the output from the current operation to the target operation,
     * given the operation of the marker open outside.
     *
     * @return the target operation
     */
    private static DiffOperation switchMarker(StringBuilder out, DiffOperation base, DiffOperation current, DiffOperation target) {
      if (target == current) return current;
      if (current != base) {
        out.append(DiffMarkers.close(current));
        if (base != DiffOperation.NONE) out.append(DiffMarkers.open(base));
      }
      if (target != base) {
        if (base != DiffOperation.NONE) out.append(DiffMarkers.close(base));
        out.append(DiffMarkers.open(target));
      }
      return target;
    }

    /**
     * Returns the visible text of an inline element whose markup is flattened (xref, link, placeholder).
     *
     * <p>In diff mode, the text keeps its diff markers.
     */
    private String inlineText(PSMLElement element) {
      if (!this.diff) return normalizeText(element.getText());
      return DiffMarkers.escape(normalizeText(markedText(element, null)));
    }

    /**
     * Returns the old or new version of the text of an element.
     *
     * @param segments The segments of text
     * @param excluded The operation to leave out: INSERT for the old version, DELETE for the new one
     */
    private static String projection(List<Segment> segments, DiffOperation excluded) {
      StringBuilder out = new StringBuilder();
      for (Segment segment : segments) {
        if (segment.op != excluded) out.append(segment.text);
      }
      return out.toString();
    }

    /**
     * Formats code-like content as {@code <del>old</del><ins>new</ins>}, where each version is
     * formatted in its own code syntax.
     *
     * <p>Unchanged content is returned as is. Content that only exists in one version is wrapped once.
     */
    private String codeChange(PSMLElement element, TextTransform format) {
      List<Segment> segments = new ArrayList<>();
      collectSegments(element, state.inherited, segments);
      String oldText = projection(segments, DiffOperation.INSERT);
      String newText = projection(segments, DiffOperation.DELETE);
      if (oldText.equals(newText)) return format.apply(newText);
      StringBuilder out = new StringBuilder();
      if (!oldText.isEmpty()) out.append(marked(DiffOperation.DELETE, format.apply(oldText)));
      if (!newText.isEmpty()) out.append(marked(DiffOperation.INSERT, format.apply(newText)));
      return out.toString();
    }

    /**
     * Inline elements without visible text, so an inserted or deleted one is wrapped as a whole.
     */
    private static boolean isWrappedWhole(Name name) {
      return name == Name.IMAGE;
    }

    /**
     * Writes the output of the action inside a marker for the specified operation.
     *
     * <p>If a marker for another operation is open, it is closed first and reopened after.
     */
    private void wrap(DiffOperation op, Appendable out, IOAction action) throws IOException {
      DiffOperation previous = state.open;
      if (previous != DiffOperation.NONE) out.append(DiffMarkers.close(previous));
      out.append(DiffMarkers.open(op));
      state.open = op;
      try {
        action.run();
      } finally {
        state.open = previous;
      }
      out.append(DiffMarkers.close(op));
      if (previous != DiffOperation.NONE) out.append(DiffMarkers.open(previous));
    }

    /**
     * Returns the attribute value, falling back on the deleted value in diff mode.
     *
     * <p>A deleted element only has its attributes in the Diff-X delete namespace.
     */
    private @Nullable String attr(PSMLElement element, String name) {
      String value = element.getAttribute(name);
      if (value == null && this.diff) value = element.getDeletedAttribute(name);
      return value;
    }

    private String attrOrElse(PSMLElement element, String name, String defaultValue) {
      String value = attr(element, name);
      return value != null ? value : defaultValue;
    }

    private int attrOrElse(PSMLElement element, String name, int defaultValue) {
      String value = attr(element, name);
      if (value == null) return defaultValue;
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ex) {
        return defaultValue;
      }
    }

    /**
     * In diff mode, removes sentinel characters and escapes marker-like text from source text.
     */
    private String clean(@Nullable String text) {
      if (text == null || !this.diff) return text == null ? "" : text;
      return DiffMarkers.escape(stripSentinels(text));
    }

    /**
     * Removes sentinel characters from source text, reporting a warning the first time.
     */
    private String stripSentinels(String text) {
      if (!DiffMarkers.hasSentinels(text)) return text;
      if (!this.sentinelWarning) {
        this.collector.warn("Removed reserved characters U+E000 to U+E003 from the source text");
        this.sentinelWarning = true;
      }
      return DiffMarkers.strip(text);
    }

    private void serializeElement(PSMLElement element, Appendable out) throws IOException {
      Name name = element.getElement();
      state.push(name);

      switch (name) {
        case BLOCK:
          serializeBlock(element, out);
          break;

        case BLOCKXREF:
          serializeBlockXref(element, out);
          break;

        case BOLD:
          serializeBold(element, out);
          break;

        case BR:
          serializeBreak(out);
          break;

        case DOCUMENT:
          serializeDocument(element, out);
          break;

        case DOCUMENTINFO:
          serializeDocumentInfo(element, out);
          break;

        case FRAGMENT:
          serializeFragment(element, out);
          break;

        case HEADING:
          serializeHeading(element, out);
          break;

        case IMAGE:
          serializeImage(element, out);
          break;

        case ITALIC:
          serializeItalic(element, out);
          break;

        case ITEM:
          // Handled in list
          break;

        case LINK:
          serializeLink(element, out);
          break;

        case LIST:
          serializeList(element, out);
          break;

        case MEDIA_FRAGMENT:
          serializeMediaFragment(element, out);
          break;

        case MONOSPACE:
          serializeMonospace(element, out);
          break;

        case NLIST:
          serializeNlist(element, out);
          break;

        case PARA:
          serializePara(element, out);
          break;

        case PREFORMAT:
          serializePreformat(element, out);
          break;

        case PLACEHOLDER:
          serializePlaceholder(element, out);
          break;

        case PROPERTY:
          serializeProperty(element, out);
          break;

        case PROPERTIES_FRAGMENT:
          serializePropertiesFragment(element, out);
          break;

        case CAPTION:
        case CELL:
        case COL:
        case ROW:
          // Handled in table
          break;

        case TABLE:
          serializeTable(element, out);
          break;

        case METADATA:
        case PROPERTIES:
        case SECTION:
        case URI:
        case XREF_FRAGMENT:
          processChildElements(element, out);
          break;

        case SUB:
          serializeSub(element, out);
          break;

        case SUP:
          serializeSup(element, out);
          break;

        case TITLE:
          serializeTitle(element, out);
          break;

        case UNDERLINE:
          serializeUnderline(element, out);
          break;

        case TOC:
        case AUTHOR:
        case FRAGMENTINFO:
        case COMPARE:
        case COMPARETO:
        case VERSIONS:
        case REVERSEXREFS:
          // Ignore these elements: do nothing
          break;

        case XREF:
          serializeXref(element, out);
          break;

        default:
          processChildren(element, out);

      }
      state.pop();
    }

    private void serializeBlock(PSMLElement block, Appendable out) throws IOException {
      switch (options.block()) {
        case QUOTED:
          serializeBlockAsQuoted(block, out);
          break;
        case FENCED:
          serializeBlockAsFenced(block, out);
          break;
        default:
          serializeBlockAsLabelText(block, out);
          break;
      }
    }

    private void serializeBlockAsQuoted(PSMLElement block, Appendable out) throws IOException {
      String label = attr(block, "label");
      if (label != null) label = clean(label);
      out.append("> ");
      if (label != null) {
        out.append(this.options.designator().format(label)).append(": ");
      }
      StringBuilder buffer = new StringBuilder();
      processChildren(block, buffer);
      String text = String.join("\n> ", buffer.toString().split("\n"));
      out.append(text).append('\n');
    }

    private void serializeBlockAsFenced(PSMLElement block, Appendable out) throws IOException {
      String label = clean(attrOrElse(block, "label", ""));
      out.append("~~~").append(label);
      processChildren(block, out);
      out.append("~~~\n");
    }

    private void serializeBlockAsLabelText(PSMLElement block, Appendable out) throws IOException {
      String label = attr(block, "label");
      if (label != null) label = clean(label);
      if (label != null) {
        out.append(this.options.designator().format(label)).append(": ");
      }
      processChildren(block, out);
      out.append('\n');
    }

    private void serializeBlockXref(PSMLElement blockXref, Appendable out) throws IOException {
      List<PSMLElement> children = blockXref.getChildElements(Name.DOCUMENT, Name.FRAGMENT, Name.XREF_FRAGMENT, Name.MEDIA_FRAGMENT, Name.XREF_FRAGMENT);
      if (!children.isEmpty()) {
        PSMLElement child = children.get(0);
        if (child.getElement() == Name.DOCUMENT) {
          List<PSMLElement> sections = child.getChildElements(Name.SECTION);
          for (PSMLElement section : sections) {
            List<PSMLElement> fragments = section.getChildElements();
            for (PSMLElement fragment : fragments) {
              serialize(fragment, out);
            }
          }
        } else {
          serialize(child, out);
        }
      } else {
        serializeXref(blockXref, out);
      }
    }

    private void serializeBold(PSMLElement bold, Appendable out) throws IOException {
      MarkdownOutputOptions.BoldFormat format = this.options.bold();
      if (format == MarkdownOutputOptions.BoldFormat.IGNORE) {
        processChildren(bold, out);
      } else {
        String wrapper = format.format();
        out.append(wrapper);
        processChildren(bold, out);
        out.append(wrapper);
      }
    }

    private void serializeBreak(Appendable out) throws IOException {
      if (state.isDescendantOf(Name.TABLE)) {
        out.append("<br>");
      } else {
        out.append("\n");
      }
    }

    @SuppressWarnings("java:S3776")
    private void serializeDocument(PSMLElement document, Appendable out) throws IOException {
      // Include metadata as Yaml section at start of document
      if (this.options.metadata()) {
        String status = document.getAttribute("status");
        PSMLElement documentInfo = document.getFirstChildElement(Name.DOCUMENTINFO);
        PSMLElement metadata = document.getFirstChildElement(Name.METADATA);
        // Check if documentinfo or metadata first
        if (documentInfo != null || metadata != null) {
          out.append("---\n");
          if (documentInfo != null) {
            serialize(documentInfo, out);
          }
          if (status != null) {
            out.append("Status: ").append(status).append('\n');
          }
          if (metadata != null) {
            serialize(metadata, out);
          }
          out.append("---\n");
        }
      }
      // Iterate over the sections
      boolean firstSection = true;
      for (PSMLElement section : document.getChildElements(Name.SECTION)) {
        if (firstSection) firstSection = false;
        else out.append("\n---\n");
        processChildren(section, out);
      }
    }

    private void serializeDocumentInfo(PSMLElement documentInfo, Appendable out) throws IOException {
      PSMLElement uri = documentInfo.getFirstChildElement(Name.URI);
      if (uri != null) {
        String title = textOf(uri.getFirstChildElement(Name.DISPLAYTITLE));
        String description = textOf(uri.getFirstChildElement(Name.DESCRIPTION));
        String labels = textOf(uri.getFirstChildElement(Name.LABELS));
        state.host = uri.getAttributeOrElse("host", "");
        state.path = uri.getAttributeOrElse("path", "");
        if (!title.isEmpty()) {
          out.append("Title: ").append(title).append('\n');
        }
        if (!description.isEmpty()) {
          out.append("Description: ").append(description).append('\n');
        }
        if (!labels.isEmpty()) {
          boolean firstLabel = true;
          out.append("Labels: [");
          for (String label : labels.split(",")) {
            if (!firstLabel) out.append(", ");
            out.append(label);
            firstLabel = false;
          }
          out.append("]\n");
        }
      }
    }

    private void serializeFragment(PSMLElement fragment, Appendable out) throws IOException {
      processChildElements(fragment, out);
    }

    private void serializeHeading(PSMLElement heading, Appendable out) throws IOException {
      int level = attrOrElse(heading, "level", 1);
      String prefix = attr(heading, "prefix");
      out.append("\n");
      for (int i = 0; i < Math.min(level, 6); i++) {
        out.append('#');
      }
      out.append(' ');
      if (prefix != null) {
        out.append(clean(prefix)).append(' ');
      } else if ("true".equals(attr(heading, "numbered"))) {
        out.append(state.nextHeadingPrefix(level - 1));
      }
      processChildren(heading, out);
      out.append("\n");
    }

    private void serializeImage(PSMLElement image, Appendable out) throws IOException {
      String src = attrOrElse(image, "src", "");
      String alt = attr(image, "alt");
      if (alt != null) alt = clean(alt);
      if (options.designator() != MarkdownOutputOptions.DesignatorStyle.NONE) {
        out.append(options.designator().format(state.nextImage()));
        if (alt != null) {
          out.append(": ").append(alt);
        }
        out.append("\n");
      }
      if (alt == null) {
        alt = src.isEmpty() ? "" : src.substring(src.lastIndexOf('/') + 1);
      }
      switch (options.image()) {
        case LOCAL:
          String href = state.toRelativeLocalPath(src, collector);
          out.append("![").append(alt).append("](").append(href).append(")");
          break;
        case EXTERNAL:
          if (state.host.isEmpty() || state.path.isEmpty()) {
            collector.warn("Cannot generate external image URL without host and path information");
          }
          String url = state.toExternalUrl(src);
          out.append("![").append(alt).append("](").append(url).append(")");
          break;
        case DATA_URI:
          // Data URI images are not supported
          collector.warn("Data URI images are not currently supported");
          break;
        case IMG_TAG:
          String width = attrOrElse(image, "width", "");
          String height = attrOrElse(image, "height", "");
          out.append("<img src=\"").append(src).append('"');
          out.append(" alt=\"").append(alt).append('"');
          if (!width.isEmpty()) {
            out.append(" width=\"").append(width).append('"');
          }
          if (!height.isEmpty()) {
            out.append(" height=\"").append(height).append('"');
          }
          out.append(" />");
          break;
        default:
      }
    }

    private void serializeItalic(PSMLElement italic, Appendable out) throws IOException {
      MarkdownOutputOptions.ItalicFormat format = this.options.italic();
      if (format == MarkdownOutputOptions.ItalicFormat.IGNORE) {
        processChildren(italic, out);
      } else {
        char wrapper = format == MarkdownOutputOptions.ItalicFormat.UNDERSCORE ? '_' : '*';
        out.append(wrapper);
        processChildren(italic, out);
        out.append(wrapper);
      }
    }

    private void serializeLink(PSMLElement link, Appendable out) throws IOException {
      String text = inlineText(link);
      String url = attr(link, "href");
      out.append("[").append(text).append("](").append(url).append(")");
    }

    private void serializeList(PSMLElement list, Appendable out) throws IOException {
      List<PSMLElement> items = list.getChildElements(Name.ITEM);
      int level = state.listLevel();
      out.append('\n');
      for (PSMLElement item : items) {
        state.push(Name.ITEM);
        for (int i = 0; i < level; i++) {
          out.append("    ");
        }
        out.append("- ");
        // Note: items are serialized inline, multiple paragraphs and blocks are not supported
        processChildren(item, out);
        out.append('\n');
        state.pop();
      }
    }

    private void serializeMediaFragment(PSMLElement fragment, Appendable out) throws IOException {
      String mediatype = attr(fragment, "mediatype");
      String lang = mediatype != null ? toLang(mediatype) : "";
      if (this.diff) {
        serializeCodeBlockDiff(fragment, lang, out);
        return;
      }
      out.append("\n```");
      out.append(lang);
      String text = fragment.getText();
      if (!text.startsWith("\n")) out.append("\n");
      out.append(stripTrailingWhitespace(text)).append("\n```\n");
    }

    /**
     * Formats projected text as a fenced code block in diff mode, escaping literal marker tags.
     */
    private static String toFencedCode(String text, String lang) {
      text = DiffMarkers.escape(text);
      StringBuilder out = new StringBuilder("```").append(lang);
      if (!text.startsWith("\n")) out.append('\n');
      return out.append(stripTrailingWhitespace(text)).append("\n```").toString();
    }

    /**
     * Serializes a code block in diff mode.
     *
     * <p>Unchanged code is serialized as without diff, except that literal marker tags are
     * escaped. Changed code is serialized as the old
     * version in a fenced block inside {@code <del>} followed by the new version in a fenced
     * block inside {@code <ins>}, each marker on its own line. Code that only exists in one
     * version is wrapped once.
     */
    private void serializeCodeBlockDiff(PSMLElement element, String lang, Appendable out) throws IOException {
      List<Segment> segments = new ArrayList<>();
      collectSegments(element, state.inherited, segments);
      String oldText = projection(segments, DiffOperation.INSERT);
      String newText = projection(segments, DiffOperation.DELETE);
      out.append('\n');
      if (oldText.equals(newText)) {
        out.append(toFencedCode(newText, lang)).append('\n');
        return;
      }
      if (!oldText.isEmpty()) {
        out.append(marked(DiffOperation.DELETE, "\n" + toFencedCode(oldText, lang) + "\n")).append('\n');
      }
      if (!newText.isEmpty()) {
        out.append(marked(DiffOperation.INSERT, "\n" + toFencedCode(newText, lang) + "\n")).append('\n');
      }
    }

    /**
     * @return {@code true} if the element only contains text and Diff-X text elements containing text.
     */
    private static boolean hasOnlyDiffText(PSMLElement element) {
      for (PSMLNode node : element.getNodes()) {
        if (node instanceof PSMLElement) {
          PSMLElement child = (PSMLElement) node;
          if (!child.getElement().isDiff() || child.hasChildElements()) return false;
        }
      }
      return true;
    }

    /**
     * Returns the text of the element with diff markers, each piece of text converted separately.
     */
    private String convertedText(PSMLElement element, TextTransform conversion) {
      List<Segment> segments = new ArrayList<>();
      collectSegments(element, state.inherited, segments);
      return markedText(segments, conversion);
    }

    /**
     * @return {@code true} if some text in the element has a diff operation other than the open marker.
     */
    private boolean hasChangedText(PSMLElement element) {
      List<Segment> segments = new ArrayList<>();
      collectSegments(element, state.inherited, segments);
      for (Segment segment : segments) {
        if (segment.op != DiffOperation.NONE && segment.op != state.open && !segment.text.isEmpty()) return true;
      }
      return false;
    }

    private void serializeMonospace(PSMLElement monospace, Appendable out) throws IOException {
      if (this.diff && hasChangedText(monospace)) {
        out.append(codeChange(monospace, text -> "`" + DiffMarkers.escape(normalizeText(text)).replace("`", "\\`") + "`"));
        return;
      }
      out.append("`");
      processChildren(monospace, out);
      out.append("`");
    }

    private void serializeNlist(PSMLElement nlist, Appendable out) throws IOException {
      List<PSMLElement> items = nlist.getChildElements(Name.ITEM);
      int level = state.listLevel();
      out.append('\n');
      int start = nlist.getAttributeOrElse("start", 1);
      for (PSMLElement item : items) {
        state.push(Name.ITEM);
        for (int i = 0; i < level; i++) {
          out.append("    ");
        }
        out.append(Integer.toString(start++)).append(". ");
        processChildren(item, out);
        out.append('\n');
        state.pop();
      }
    }

    private void serializePara(PSMLElement para, Appendable out) throws IOException {
      String prefix = attr(para, "prefix");
      boolean inTableOrItem = state.isDescendantOf(Name.TABLE) || state.isDescendantOf(Name.ITEM);
      int indent = attrOrElse(para, "indent", 0);
      if (!inTableOrItem) out.append("\n");
      if (indent > 0) {
        for (int i = 0; i < Math.min(indent, 6); i++) {
          out.append("\u00a0\u00a0\u00a0\u00a0");
        }
      }
      if (prefix != null) {
        out.append(clean(prefix)).append(" ");
      } else if ("true".equals(attr(para, "numbered"))) {
        out.append(state.nextParaPrefix(indent));
      }
      processChildren(para, out);
      if (!inTableOrItem) out.append("\n");
    }

    private void serializePreformat(PSMLElement element, Appendable out) throws IOException {
      String role = attr(element, "role");
      String language = "";
      if (role != null && (role.startsWith("language-") || role.startsWith("lang-"))) {
        language = clean(role.substring(role.indexOf('-') + 1));
      }
      if (this.diff) {
        serializeCodeBlockDiff(element, language, out);
        return;
      }
      out.append("\n```");
      out.append(language);
      String text = element.getText();
      if (!text.startsWith("\n")) out.append("\n");
      out.append(stripTrailingWhitespace(text)).append("\n```\n");
    }

    private void serializePlaceholder(PSMLElement placeholder, Appendable out) throws IOException {
      String name = clean(attrOrElse(placeholder, "name", ""));
      String text = inlineText(placeholder);
      boolean empty = this.diff ? DiffMarkers.strip(text).isEmpty() : text.isEmpty();
      out.append("[[").append(empty ? marked(state.inherited, name) : text).append("]]");
    }

    private void serializeProperty(PSMLElement property, Appendable out) throws IOException {
      String name = attrOrElse(property, "name", "");
      String title = clean(attrOrElse(property, "title", name));
      String value = toPropertyValue(property);
      String label = normalizeText(title);
      if (this.diff) label = marked(state.inherited, label);
      if (state.isDescendantOf(Name.METADATA) || options.properties() == MarkdownOutputOptions.PropertiesFormat.VALUE_PAIRS) {
        out.append(label).append(": ").append(value).append("\n");
      } else {
        out.append("| ").append(label).append(" | ").append(value).append(" |\n");
      }
    }

    /**
     * Returns the value of an attribute-backed property in diff mode, reconstructing the old
     * and new values from the Diff-X attribute annotations.
     *
     * <p>A replaced value is written {@code <del>old</del><ins>new</ins>}, deletion first.
     *
     * @return the value or {@code null} if the property has no value attribute in either version.
     */
    private @Nullable String toPropertyValueDiff(PSMLElement property) {
      String newValue = property.getAttribute("value");
      String oldValue = property.getDeletedAttribute("value");
      if (newValue == null && oldValue == null) return null;
      String newText = newValue != null ? clean(normalizeText(newValue.replace("\n", "<br>"))) : "";
      String oldText = oldValue != null ? clean(normalizeText(oldValue.replace("\n", "<br>"))) : "";
      DiffOperation op = state.inherited;
      if (op == DiffOperation.DELETE) return marked(op, oldValue != null ? oldText : newText);
      if (op == DiffOperation.INSERT) return marked(op, newText);
      if (oldValue != null && !oldText.equals(newText)) {
        return marked(DiffOperation.DELETE, oldText) + marked(DiffOperation.INSERT, newText);
      }
      if (oldValue == null && property.isInsertedAttribute("value")) {
        return marked(DiffOperation.INSERT, newText);
      }
      return newText;
    }

    private String toPropertyValue(PSMLElement property) throws IOException {
      if (this.diff) {
        String value = toPropertyValueDiff(property);
        if (value != null) return value;
      }
      String value = property.getAttribute("value");
      // Single value
      if (value != null) {
        return normalizeText(value.replace("\n", "<br>"));
      }
      // Markdown
      PSMLElement markdown = property.getFirstChildElement(Name.MARKDOWN);
      if (markdown != null) {
        if (this.diff) return codeChange(markdown, text -> clean(text.replace("\n", "<br>")));
        return markdown.getText().replace("\n", "<br>");
      }
      // Multiple values
      List<PSMLElement> values = property.getChildElements(Name.VALUE);
      if (!values.isEmpty()) {
        return toMultipleValues(values);
      }
      // Xrefs
      List<PSMLElement> xrefs = property.getChildElements(Name.XREF);
      if (!xrefs.isEmpty()) {
        return toXrefList(xrefs);
      }
      // Could be some markup
      StringBuilder out = new StringBuilder();
      processChildren(property, out);
      return normalizeText(out.toString().replace("\n", "<br>"));
    }

    private String toMultipleValues(List<PSMLElement> values) {
      StringBuilder out = new StringBuilder("[");
      for (int i = 0; i < values.size(); i++) {
        if (i > 0) out.append(", ");
        PSMLElement v = values.get(i);
        if (this.diff) {
          out.append(DiffMarkers.escape(normalizeText(markedText(v, null))));
        } else {
          out.append(normalizeText(v.getText()));
        }
      }
      return out.append("]").toString();
    }

    private String toXrefList(List<PSMLElement> xrefs) throws IOException {
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < xrefs.size(); i++) {
        if (i > 0) out.append(", ");
        serialize(xrefs.get(i), out);
      }
      return out.toString();
    }

    private void serializePropertiesFragment(PSMLElement fragment, Appendable out) throws IOException {
      out.append('\n');
      if (options.properties() == MarkdownOutputOptions.PropertiesFormat.TABLE) {
        if (options.designator() != MarkdownOutputOptions.DesignatorStyle.NONE) {
          out.append(options.designator().format(state.nextProperties())).append(": ").append(fragment.getAttribute("id")).append('\n');
        }
        out.append("| Name | Value |\n");
        out.append("|---|---|\n");
      }
      for (PSMLElement property : fragment.getChildElements(Name.PROPERTY)) {
        // Through serialize so that the property's own diff operation applies
        serialize(property, out);
      }
    }

    private void serializeSup(PSMLElement element, Appendable out) throws IOException {
      if (element.isEmpty()) return;
      switch (options.superSub()) {
        case CARET_TILDE:
          out.append("^");
          processChildren(element, out);
          out.append("^");
          break;
        case HTML:
          out.append("<sup>");
          processChildren(element, out);
          out.append("</sup>");
          break;
        case UNICODE:
          if (this.diff && hasOnlyDiffText(element) && Superscripts.isReplaceable(element.getText())) {
            out.append(convertedText(element, Superscripts::toSuperscript));
          } else if (!element.hasChildElements() && Superscripts.isReplaceable(element.getText())) {
            out.append(Superscripts.toSuperscript(element.getText()));
          } else {
            collector.warn("Superscript text contains character for which there is no Unicode equivalent");
            processChildren(element, out);
          }
          break;
        case IGNORE: // default
        default:
          collector.warn("Superscripts are ignored in Markdown output format");
          processChildren(element, out);
      }
    }

    private void serializeSub(PSMLElement element, Appendable out) throws IOException {
      if (element.isEmpty()) return;
      switch (options.superSub()) {
        case CARET_TILDE:
          out.append("~");
          processChildren(element, out);
          out.append("~");
          break;
        case HTML:
          out.append("<sub>");
          processChildren(element, out);
          out.append("</sub>");
          break;
        case UNICODE:
          if (this.diff && hasOnlyDiffText(element) && Subscripts.isReplaceable(element.getText())) {
            out.append(convertedText(element, Subscripts::toSubscript));
          } else if (!element.hasChildElements() && Subscripts.isReplaceable(element.getText())) {
            out.append(Subscripts.toSubscript(element.getText()));
          } else {
            collector.warn("Subscript text contains character for which there is no Unicode equivalent");
            processChildren(element, out);
          }
          break;
        case IGNORE: // default
        default:
          collector.warn("Subscripts are ignored in Markdown output format");
          processChildren(element, out);
      }
    }

    private void serializeTable(PSMLElement table, Appendable out) throws IOException {
      MarkdownTable tableOut = new MarkdownTable(table, state.nextTable(), this.collector, this.diff ? this::markedText : null);
      tableOut.format(out, options);
    }

    private void serializeTitle(PSMLElement title, Appendable out) throws IOException {
      out.append("\n");
      out.append("## ");
      processChildren(title, out);
      out.append("\n");
    }

    private void serializeUnderline(PSMLElement element, Appendable out) throws IOException {
      if (element.isEmpty()) return;
      if (options.underline() == MarkdownOutputOptions.UnderlineFormat.HTML) {
        out.append("<u>");
        processChildren(element, out);
        out.append("</u>");
      } else {
        collector.warn("Underlines are ignored in Markdown output format");
        processChildren(element, out);
      }
    }

    private void serializeXref(PSMLElement link, Appendable out) throws IOException {
      String text = inlineText(link);
      String url = attrOrElse(link, "href", "");
      switch (options.xref()) {
        case EXTERNAL_LINK:
          String externalHref = state.toExternalUrl(url);
          out.append("[").append(text).append("](").append(externalHref).append(")");
          break;
        case LOCAL_LINK:
          String localHref = state.toRelativeLocalPath(url, this.collector);
          out.append("[").append(text).append("](").append(localHref).append(")");
          break;
        case BOLD_TEXT:
          out.append("**").append(text).append("**");
          break;
        default:
          out.append(text);
          break;
      }
    }

    private void processChildren(PSMLElement element, Appendable out) throws IOException {
      if (this.diff && state.inherited != DiffOperation.NONE && state.inherited != state.open) {
        processChildrenInRuns(element, out);
      } else {
        for (PSMLNode node : element.getNodes()) {
          processNode(node, out);
        }
      }
    }

    /**
     * Processes the children, wrapping each run of consecutive inline nodes in a marker for the
     * inherited operation. Child blocks are processed separately and mark their own content.
     */
    private void processChildrenInRuns(PSMLElement element, Appendable out) throws IOException {
      List<PSMLNode> nodes = element.getNodes();
      int i = 0;
      while (i < nodes.size()) {
        if (isInline(nodes.get(i))) {
          int j = i;
          while (j < nodes.size() && isInline(nodes.get(j))) j++;
          processInlineRun(nodes.subList(i, j), out);
          i = j;
        } else {
          processNode(nodes.get(i), out);
          i++;
        }
      }
    }

    /**
     * Processes a run of consecutive inline nodes, wrapping it in a marker for the inherited
     * operation unless it only contains white space.
     */
    private void processInlineRun(List<PSMLNode> run, Appendable out) throws IOException {
      if (isWhiteSpaceOnly(run)) {
        processNodes(run, out);
      } else {
        wrap(state.inherited, out, () -> processNodes(run, out));
      }
    }

    private void processNodes(List<PSMLNode> nodes, Appendable out) throws IOException {
      for (PSMLNode node : nodes) processNode(node, out);
    }

    private static boolean isInline(PSMLNode node) {
      return node instanceof PSMLText || ((PSMLElement) node).getElement().isInline();
    }

    /**
     * Removes trailing white space ({@code [ \t\n\x0B\f\r]}) without using a backtracking regex.
     */
    private static String stripTrailingWhitespace(String text) {
      int end = text.length();
      while (end > 0 && " \t\n\u000B\f\r".indexOf(text.charAt(end - 1)) >= 0) end--;
      return text.substring(0, end);
    }

    private static boolean isWhiteSpaceOnly(List<PSMLNode> run) {
      for (PSMLNode node : run) {
        if (!(node instanceof PSMLText) || !node.getText().trim().isEmpty()) return false;
      }
      return true;
    }

    @SuppressWarnings("java:S3776")
    private void processNode(PSMLNode node, Appendable out) throws IOException {
      if (node instanceof PSMLText) {
        String text = clean(normalizeText(node.getText()));
        if (state.isDescendantOf(Name.ITALIC) && this.options.italic() == MarkdownOutputOptions.ItalicFormat.ASTERISK
         || state.isDescendantOf(Name.BOLD) && this.options.bold() == MarkdownOutputOptions.BoldFormat.DOUBLE_ASTERISK) {
          text = text.replace("*", "\\*");
        }
        if (state.isDescendantOf(Name.BOLD) && this.options.bold() == MarkdownOutputOptions.BoldFormat.DOUBLE_UNDERSCORE
         || state.isDescendantOf(Name.ITALIC) && this.options.italic() == MarkdownOutputOptions.ItalicFormat.UNDERSCORE) {
          text = text.replace("_", "\\_");
        }
        if (state.isDescendantOf(Name.MONOSPACE)) {
          text = text.replace("`", "\\`");
        }
        out.append(text);
      } else {
        serialize((PSMLElement) node, out);
      }
    }

    private void processChildElements(PSMLElement element, Appendable out) throws IOException {
      for (PSMLElement child : element.getChildElements()) {
        serialize(child, out);
      }
    }

    private static String toLang(String mediatype) {
      switch (mediatype) {
        case "text/html":
          return "html";
        case "text/css":
          return "css";
        case "text/javascript":
          return "js";
        case "text/x-csrc":
          return "c";
        case "text/x-c++src":
          return "cpp";
        case "text/x-java":
          return "java";
        case "text/x-csharp":
          return "cs";
        case "text/x-groovy":
          return "groovy";
        case "text/x-ruby":
          return "rb";
        case "text/x-python":
          return "py";
        case "text/x-php":
          return "php";
        case "text/x-yaml":
          return "yaml";
        case "text/x-xml":
          return "xml";
        case "text/x-markdown":
          return "md";
        case "text/x-latex":
          return "latex";
        case "text/x-tex": // fall through
        case "application/x-tex":
          return "tex";
        default:
          return "";
      }
    }

    private String textOf(@Nullable PSMLElement element) {
      return element == null ? "" : clean(normalizeText(element.getText()));
    }

  }

  /**
   * A piece of text with the diff operation that applies to it.
   */
  private static final class Segment {
    private final DiffOperation op;
    private final String text;

    Segment(DiffOperation op, String text) {
      this.op = op;
      this.text = text;
    }
  }

  /**
   * Transforms a piece of text, for example to format one version of code-like content in its code syntax.
   */
  @FunctionalInterface
  private interface TextTransform {
    String apply(String text);
  }

  /**
   * An output action that may throw an I/O exception.
   */
  @FunctionalInterface
  private interface IOAction {
    void run() throws IOException;
  }

}

