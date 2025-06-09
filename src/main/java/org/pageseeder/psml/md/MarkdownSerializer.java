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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for serializing PSML content into Markdown format.
 *
 * @version 1.6.0
 * @since 1.0
 */
public class MarkdownSerializer {

  private enum CellAlignment {
    NONE, LEFT, CENTER, RIGHT
  }

  private enum CellStyle {
    NONE, BOLD, ITALIC
  }

  MarkdownOutputOptions options = new MarkdownOutputOptions();

  public void setOptions(MarkdownOutputOptions options) {
    this.options = options;
  }

  public MarkdownOutputOptions getOptions() {
    return options;
  }

  public void serialize(PSMLElement element, Appendable out) throws IOException {
    Instance instance = new Instance(this.options);
    instance.serialize(element, out);
  }

  private static String normalizeText(String text) {
    return text.replaceAll("\\s+", " ");
  }

  private static class State {

    private final Deque<Name> context = new ArrayDeque<>();

    private int imageCounter = 0;

    private int tableCounter = 0;

    private int propertiesCounter = 0;

    private final int[] headingPrefix = new int[]{0, 0, 0, 0, 0, 0};

    private final int[] paraPrefix = new int[]{0, 0, 0, 0, 0};

    private final List<String> warnings = new ArrayList<>();

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

    void addWarning(String warning) {
      this.warnings.add(warning);
    }

  }

  private static class Instance {

    private final MarkdownOutputOptions options;

    private final State state = new State();

    Instance(MarkdownOutputOptions options) {
      this.options = options;
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
          serializeBreak(element, out);
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
      String label = block.getAttribute("label");
      out.append("> ");
      if (label != null) {
        out.append("**").append(block.getAttribute("label")).append("**: ");
      }
      StringBuilder buffer = new StringBuilder();
      processChildren(block, buffer);
      String text = String.join("\n> ", buffer.toString().split("\n"));
      out.append(text).append("\n");
    }

    private void serializeBlockXref(PSMLElement link, Appendable out) throws IOException {
      String text = normalizeText(link.getText());
      if (options.includeXrefUrl()) {
        String url = link.getAttribute("href");
        out.append("[").append(text).append("](").append(url).append(")");
      } else {
        out.append("**").append(text).append("**");
      }
    }

    private void serializeBold(PSMLElement bold, Appendable out) throws IOException {
      out.append("__");
      processChildren(bold, out);
      out.append("__");
    }

    private void serializeBreak(PSMLElement br, Appendable out) throws IOException {
      if (state.isDescendantOf(Name.TABLE)) {
        out.append("<br>");
      } else {
        out.append("\n");
      }
    }

    private void serializeDocument(PSMLElement document, Appendable out) throws IOException {
      // Include metadata as Yaml section at start of document
      if (this.options.includeMetadata()) {
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
      // TODO option to include reference or anchor
//      out.append("<a name=\"").append(fragment.getAttribute("id")).append("\"></a>");
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
      String alt = image.getAttribute("alt");
      String src = image.getAttribute("src");
      out.append("**").append(state.nextImage()).append("**");
      if (alt != null) {
        // TODO See if other attribute could be used
        out.append(": ").append(alt);
      }
      out.append("\n");
      if (options.includeImageUrl()) {
        if (src != null && alt != null && !src.isEmpty() && !alt.isEmpty()) {
          out.append("![").append(alt).append("](").append(src).append(")");
        }
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
      for (PSMLElement item : items) {
        state.push(Name.ITEM);
        out.append(" * ");
        // TODO multiple paragraph, blocks and sublists
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
      int start = nlist.getAttributeOrElse("start", 1);
      for (PSMLElement item : items) {
        state.push(Name.ITEM);
        out.append(" ").append(Integer.toString(start++)).append(". ");
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

    private void serializeProperty(PSMLElement property, Appendable out) throws IOException {
      String name = property.getAttributeOrElse("name", "");
      String title = property.getAttributeOrElse("title", name);
      String value = property.getAttribute("value");
      if (state.isDescendantOf(Name.METADATA)) {
        // TODO xref and multiple values
        out.append(normalizeText(title)).append(": ");
        if (value != null) {
          out.append(value);
        } else {
          List<PSMLElement> values = property.getChildElements(Name.VALUE);
          List<PSMLElement> xrefs = property.getChildElements(Name.XREF);
          if (!values.isEmpty()) {
            out.append("[");
            boolean first = true;
            for (PSMLElement v : values) {
              if (first) first = false;
              else out.append(", ");
              out.append(v.getText());
            }
            out.append("]");
          } else if (!xrefs.isEmpty()) {
            out.append("[");
            boolean first = true;
            for (PSMLElement xref : xrefs) {
              if (first) first = false;
              else out.append(", ");
              out.append(xref.getText());
            }
            out.append("]");
          }
        }
        out.append("\n");
      } else if (state.isDescendantOf(Name.PROPERTIES_FRAGMENT)) {
        out.append("| ").append(normalizeText(title).trim()).append(" | ");
        if (value == null) {
          value = property.getChildElements(Name.VALUE, Name.XREF).stream()
              .map(PSMLElement::getText)
              .map(MarkdownSerializer::normalizeText)
              .collect(Collectors.joining(", "));
        }
        out.append(value);
        // TODO handle markup, markdown, and xref
        out.append(" |\n");
      }
    }

    private void serializePropertiesFragment(PSMLElement fragment, Appendable out) throws IOException {
      out.append('\n');
      out.append("**").append(state.nextProperties()).append("**: ").append(fragment.getAttribute("id")).append('\n');
      out.append("| Name | Value |\n");
      out.append("|---|---|\n");
      for (PSMLElement property : fragment.getChildElements(Name.PROPERTY)) {
        serializeProperty(property, out);
      }
    }

    private void serializeSup(PSMLElement element, Appendable out) throws IOException {
      state.addWarning("Superscripts are not supported in Markdown");
      processChildren(element, out);
    }

    private void serializeSub(PSMLElement element, Appendable out) throws IOException {
      state.addWarning("Subscripts are not supported in Markdown");
      processChildren(element, out);
    }

    private void serializeTable(PSMLElement table, Appendable out) throws IOException {
      PSMLElement caption = table.getFirstChildElement(Name.CAPTION);
      out.append("\n**").append(state.nextTable()).append("**");
      if (caption != null) {
        out.append(": ").append(caption.getText());
      }
      out.append("\n");
      List<PSMLElement> columns = table.getChildElements(Name.COL);
      List<CellAlignment> align = toCellAlignments(columns);
      List<CellStyle> styles = toCellStyles(columns);

      boolean firstRow = true;
      for (PSMLElement row : table.getChildElements(Name.ROW)) {
        List<PSMLElement> cells = row.getChildElements(Name.CELL, Name.HCELL);
        String attribute = row.getAttribute("part");
        CellStyle rowStyle = toCellStyle(attribute);
        out.append("| ");
        for (int i=0; i < cells.size(); i++) {
          PSMLElement cell = cells.get(i);
          CellStyle style = rowStyle;
          if (styles.size() > i) {
            style = styles.get(i);
          }
          String text = normalizeText(cell.getText().trim().replace("\n", "<br>"));
          if (style == CellStyle.BOLD) {
            out.append("**").append(text.replace("*", "\\*")).append("**");
          } else if (style == CellStyle.ITALIC) {
            out.append("*").append(text.replace("*", "\\*")).append("*");
          } else {
            out.append(text);
          }
          out.append(" | ");
        }
        out.append('\n');
        if (firstRow) {
          out.append("|");
          for (int i = 0; i < cells.size(); i++) {
            CellAlignment a = CellAlignment.NONE;
            if (align.size() > i) {
              a = align.get(i);
            }
            switch (a) {
              case LEFT:
                out.append(":---|");
                break;
              case CENTER:
                out.append(":---:|");
                break;
              case RIGHT:
                out.append("---:|");
                break;
              default:
                out.append("---|");
                break;
            }
          }
          out.append('\n');
          firstRow = false;
        }
      }
    }

    private void serializeTitle(PSMLElement title, Appendable out) throws IOException {
      out.append("\n");
      out.append("## ");
      processChildren(title, out);
      out.append("\n");
    }

    private void serializeUnderline(PSMLElement element, Appendable out) throws IOException {
      state.addWarning("Underlines are not supported in Markdown");
      processChildren(element, out);
    }


    private void serializeXref(PSMLElement link, Appendable out) throws IOException {
      String text = normalizeText(link.getText());
      if (options.includeXrefUrl()) {
        String url = link.getAttribute("href");
        out.append("[").append(text).append("](").append(url).append(")");
      } else {
        out.append("**").append(text).append("**");
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

    private static List<CellAlignment> toCellAlignments(List<PSMLElement> columns) {
      List<CellAlignment> alignments = new ArrayList<>();
      for (PSMLElement col : columns) {
        String attribute = col.getAttribute("align");
        CellAlignment alignment = CellAlignment.NONE;
        if (attribute != null) {
          switch (attribute) {
            case "left":
              alignment = CellAlignment.LEFT;
              break;
            case "center":
              alignment = CellAlignment.CENTER;
              break;
            case "right":
              alignment = CellAlignment.RIGHT;
              break;
            default:
          }
        }
        alignments.add(alignment);
      }
      return alignments;
    }

    private static List<CellStyle> toCellStyles(List<PSMLElement> columns) {
      List<CellStyle> styles = new ArrayList<>();
      for (PSMLElement col : columns) {
        String attribute = col.getAttribute("part");
        CellStyle style = toCellStyle(attribute);
        styles.add(style);
      }
      return styles;
    }

    private static CellStyle toCellStyle(@Nullable String attribute) {
      if (attribute == null) return CellStyle.NONE;
      if ("header".equals(attribute)) return CellStyle.BOLD;
      if ("footer".equals(attribute)) return CellStyle.ITALIC;
      return CellStyle.NONE;
    }

    private static String textOf(@Nullable PSMLElement element) {
      return element == null ? "" : normalizeText(element.getText());
    }
  }
}

