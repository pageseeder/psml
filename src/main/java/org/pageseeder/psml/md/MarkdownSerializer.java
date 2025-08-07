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

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.model.PSMLElement;
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
 * @version 1.6.7
 * @since 1.0
 */
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
    instance.serialize(element, out);
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
    instance.serialize(element, out);
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

    private final int[] headingPrefix = new int[]{0, 0, 0, 0, 0, 0};

    private final int[] paraPrefix = new int[]{0, 0, 0, 0, 0};

    private String host = "";
    private String path = "";

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

    Instance(MarkdownOutputOptions options, DiagnosticCollector collector) {
      this.options = options;
      this.collector = Objects.requireNonNull(collector);
    }

    void serialize(PSMLElement element, Appendable out) throws IOException {
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
      String label = block.getAttribute("label");
      out.append("> ");
      if (label != null) {
        out.append("**").append(block.getAttribute("label")).append("**: ");
      }
      StringBuilder buffer = new StringBuilder();
      processChildren(block, buffer);
      String text = String.join("\n> ", buffer.toString().split("\n"));
      out.append(text).append('\n');
    }

    private void serializeBlockAsFenced(PSMLElement block, Appendable out) throws IOException {
      String label = block.getAttributeOrElse("label", "");
      out.append("~~~").append(label);
      processChildren(block, out);
      out.append("~~~\n");
    }

    private void serializeBlockAsLabelText(PSMLElement block, Appendable out) throws IOException {
      String label = block.getAttribute("label");
      if (label != null) {
        out.append("**").append(block.getAttribute("label")).append("**: ");
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
      out.append("__");
      processChildren(bold, out);
      out.append("__");
    }

    private void serializeBreak(Appendable out) throws IOException {
      if (state.isDescendantOf(Name.TABLE)) {
        out.append("<br>");
      } else {
        out.append("\n");
      }
    }

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
      int level = heading.getAttributeOrElse("level", 1);
      String prefix = heading.getAttribute("prefix");
      out.append("\n");
      for (int i = 0; i < Math.min(level, 6); i++) {
        out.append('#');
      }
      out.append(' ');
      if (prefix != null) {
        out.append(prefix).append(' ');
      } else if ("true".equals(heading.getAttribute("numbered"))) {
        out.append(state.nextHeadingPrefix(level - 1));
      }
      processChildren(heading, out);
      out.append("\n");
    }

    private void serializeImage(PSMLElement image, Appendable out) throws IOException {
      String src = image.getAttributeOrElse("src", "");
      String alt = image.getAttribute("alt");
      if (options.captions()) {
        out.append("**").append(state.nextImage()).append("**");
        if (alt != null) {
          out.append(": ").append(alt);
        }
        out.append("\n");
      }
      if (alt == null) {
        alt = !src.isEmpty() ? src.substring(src.lastIndexOf('/') + 1) : "";
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
          // TODO Not supported
          collector.warn("Data URI images are not currently supported");
          break;
        case IMG_TAG:
          String width = image.getAttribute("width");
          String height = image.getAttribute("height");
          out.append("<img src=\"").append(src).append('"');
          out.append(" alt=\"").append(alt).append('"');
          if (width != null && !width.isEmpty()) {
            out.append(" width=\"").append(width).append('"');
          }
          if (height != null && !height.isEmpty()) {
            out.append(" height=\"").append(height).append('"');
          }
          out.append(" />");
          break;
        default:
      }
    }

    private void serializeItalic(PSMLElement italic, Appendable out) throws IOException {
      out.append("*");
      processChildren(italic, out);
      out.append("*");
    }

    private void serializeLink(PSMLElement link, Appendable out) throws IOException {
      String text = normalizeText(link.getText());
      String url = link.getAttribute("href");
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
        out.append("* ");
        // TODO multiple paragraph, blocks
        processChildren(item, out);
        out.append('\n');
        state.pop();
      }
    }

    private void serializeMediaFragment(PSMLElement fragment, Appendable out) throws IOException {
      out.append("\n```");
      String mediatype = fragment.getAttribute("mediatype");
      if (mediatype != null) {
        out.append(toLang(mediatype));
      }
      String text = fragment.getText();
      if (!text.startsWith("\n")) out.append("\n");
      out.append(text.replaceAll("\\s+$", "")).append("\n```\n");
    }

    private void serializeMonospace(PSMLElement monospace, Appendable out) throws IOException {
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
      String prefix = para.getAttribute("prefix");
      boolean inTableOrItem = state.isDescendantOf(Name.TABLE) || state.isDescendantOf(Name.ITEM);
      int indent = para.getAttributeOrElse("indent", 0);
      if (!inTableOrItem) out.append("\n");
      if (indent > 0) {
        for (int i = 0; i < Math.min(indent, 6); i++) {
          out.append("\u00a0\u00a0\u00a0\u00a0");
        }
      }
      if (prefix != null) {
        out.append(prefix).append(" ");
      } else if ("true".equals(para.getAttribute("numbered"))) {
        out.append(state.nextParaPrefix(indent));
      }
      processChildren(para, out);
      if (!inTableOrItem) out.append("\n");
    }

    private void serializePreformat(PSMLElement element, Appendable out) throws IOException {
      out.append("\n```");
      String role = element.getAttribute("role");
      if (role != null && (role.startsWith("language-") || role.startsWith("lang-"))) {
        String language = role.substring(role.indexOf('-') + 1);
        out.append(language);
      }
      String text = element.getText();
      if (!text.startsWith("\n")) out.append("\n");
      out.append(text.replaceAll("\\s+$", "")).append("\n```\n");
    }

    private void serializePlaceholder(PSMLElement placeholder, Appendable out) throws IOException {
      String name = placeholder.getAttributeOrElse("name", "");
      String text = normalizeText(placeholder.getText());
      out.append("[[").append(text.isEmpty() ? name : text).append("]]");
    }

    private void serializeProperty(PSMLElement property, Appendable out) throws IOException {
      String name = property.getAttributeOrElse("name", "");
      String title = property.getAttributeOrElse("title", name);
      String value = toPropertyValue(property);
      if (state.isDescendantOf(Name.METADATA) || options.properties() == MarkdownOutputOptions.PropertiesFormat.VALUE_PAIRS) {
        out.append(normalizeText(title)).append(": ").append(value).append("\n");
      } else {
        out.append("| ").append(normalizeText(title)).append(" | ").append(value).append(" |\n");
      }
    }

    private String toPropertyValue(PSMLElement property) throws IOException {
      String value = property.getAttribute("value");
      // Single value
      if (value != null) {
        return normalizeText(value.replace("\n", "<br>"));
      }
      // Markdown
      PSMLElement markdown = property.getFirstChildElement(Name.MARKDOWN);
      if (markdown != null) {
        return markdown.getText().replace("\n", "<br>");
      }
      StringBuilder out = new StringBuilder();
      // Multiple values
      List<PSMLElement> values = property.getChildElements(Name.VALUE);
      if (!values.isEmpty()) {
        out.append("[");
        boolean first = true;
        for (PSMLElement v : values) {
          if (first) first = false;
          else out.append(", ");
          out.append(normalizeText(v.getText()));
        }
        out.append("]");
        return out.toString();
      }
      // Xrefs
      List<PSMLElement> xrefs = property.getChildElements(Name.XREF);
      if (!xrefs.isEmpty()) {
        boolean first = true;
        for (PSMLElement xref : xrefs) {
          if (first) first = false;
          else out.append(", ");
          serialize(xref, out);
        }
        return out.toString();
      }

      // Could be some markup
      processChildren(property, out);
      return normalizeText(out.toString().replace("\n", "<br>"));
    }

    private void serializePropertiesFragment(PSMLElement fragment, Appendable out) throws IOException {
      out.append('\n');
      if (options.properties() == MarkdownOutputOptions.PropertiesFormat.TABLE) {
        if (options.captions()) {
          out.append("**").append(state.nextProperties()).append("**: ").append(fragment.getAttribute("id")).append('\n');
        }
        out.append("| Name | Value |\n");
        out.append("|---|---|\n");
      }
      for (PSMLElement property : fragment.getChildElements(Name.PROPERTY)) {
        serializeProperty(property, out);
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
          if (!element.hasChildElements() && Superscripts.isReplaceable(element.getText())) {
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
          if (!element.hasChildElements() && Subscripts.isReplaceable(element.getText())) {
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
      MarkdownTable tableOut = new MarkdownTable(table, state.nextTable(), this.collector);
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
      String text = normalizeText(link.getText());
      String url = link.getAttributeOrElse("href", "");
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
      for (PSMLNode node : element.getNodes()) {
        if (node instanceof PSMLText) {
          String text = normalizeText(node.getText());
          if (state.isDescendantOf(Name.ITALIC)) {
            text = text.replace("*", "\\*");
          }
          if (state.isDescendantOf(Name.BOLD)) {
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

    private static String textOf(@Nullable PSMLElement element) {
      return element == null ? "" : normalizeText(element.getText());
    }

  }

}

