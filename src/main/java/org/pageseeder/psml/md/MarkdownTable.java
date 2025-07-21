package org.pageseeder.psml.md;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.util.DiagnosticCollector;
import org.pageseeder.xmlwriter.XML;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Markdown table, responsible for formatting and writing table content in Markdown format.
 *
 * <p>This class extracts and processes structure and style information from a table element to produce
 * formatted Markdown output. It also provides support for different formats such as compact, pretty-printed,
 * or basic normalized representation.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.7
 * @since 1.0
 */
class MarkdownTable {

  private enum CellAlignment {
    NONE, LEFT, CENTER, RIGHT
  }

  private enum CellStyle {
    NONE, BOLD, ITALIC
  }

  private static final int MAX_COLUMN_WIDTH = 32;

  private static final int MIN_COLUMN_WIDTH = 3;

  private static final int MAX_ROWS_PRETTY_SCAN = 20;


  private final PSMLElement table;

  /**
   * The of the table "Table 1", "Table 2", etc... used for the caption.
   */
  private final String name;

  /**
   * A diagnostic collector used to manage and track warnings and errors related to table processing.
   */
  private final DiagnosticCollector collector;

  /**
   * Represents the total number of columns in the Markdown table.
   */
  private final int columnCount;

  /**
   * Represents the alignment of each column in the Markdown table.
   */
  private final CellAlignment[] columnAlignments;

  /**
   * Represents the styles of each column in the Markdown table.
   */
  private final CellStyle[] columnStyles;

  MarkdownTable(PSMLElement table, String name, DiagnosticCollector collector) {
    this.table = Objects.requireNonNull(table);
    this.name = name;
    this.collector = collector;
    this.columnCount = countColumns(table.getFirstChildElement(PSMLElement.Name.ROW));
    this.columnAlignments = toColumnAlignments(this.table.getChildElements(PSMLElement.Name.COL), this.columnCount);
    this.columnStyles = toColumnStyles(this.table.getChildElements(PSMLElement.Name.COL), this.columnCount);
    if (isValidTable(table)) {
      this.collector.error("This table is invalid");
    }
  }

  private boolean isValidTable(PSMLElement table) {
    if (!PSMLElement.Name.TABLE.equals(table.getElement())) return false;
    if (table.getFirstChildElement(PSMLElement.Name.ROW) == null) return false;
    return true;
  }


  /**
   * Counts the number of columns in a specified table row by iterating over all
   * cell elements and summing their colspan attributes. If the row is null, the
   * method returns 0.
   *
   * @param row the table row represented as a {@code PSMLElement}, or null
   * @return the total number of columns in the row; 0 if {@code row} is null
   */
  private static int countColumns(@Nullable PSMLElement row) {
    if (row == null) return 0;
    int count = 0;
    for (PSMLElement cell : row.getChildElements(PSMLElement.Name.CELL, PSMLElement.Name.HCELL)) {
      count += cell.getAttributeOrElse("colspan", 1);
    }
    return count;
  }

  /**
   * Computes the cell alignments for a given list of column elements based on their "align" attribute.
   * If the alignment attribute is not present or unrecognized, the alignment defaults to NONE.
   *
   * @param columns a list of {@code PSMLElement} representing the columns, each containing an "align" attribute
   * @param columnCount the total number of columns in the table
   * @return an array of {@code CellAlignment} values representing the alignment for each column
   */
  private static CellAlignment[] toColumnAlignments(List<PSMLElement> columns, int columnCount) {
    CellAlignment[] alignments = new CellAlignment[columnCount];
    Arrays.fill(alignments, CellAlignment.NONE);
    int i = 0;
    for (PSMLElement col : columns) {
      String attribute = col.getAttribute("align");
      CellAlignment alignment = toCellAlignment(attribute);
      alignments[i] = alignment;
      i++;
      if (i >= alignments.length) {
        break;
      }
    }
    return alignments;
  }

  /**
   * Converts a list of column elements with defined "part" attributes into an array of {@code CellStyle} objects.
   * Each column's style is determined based on the "part" attribute. If no "part" attribute is specified or
   * the style cannot be determined, the column defaults to {@code CellStyle.NONE}.
   *
   * @param columns a list of {@code PSMLElement} representing the table columns, each containing a "part" attribute
   * @param columnCount the total number of columns in the table, specifying the size of the resulting styles array
   * @return an array of {@code CellStyle} values, where each index corresponds to the style of a specific column
   */
  private static CellStyle[] toColumnStyles(List<PSMLElement> columns, int columnCount) {
    CellStyle[] styles = new CellStyle[columnCount];
    Arrays.fill(styles, CellStyle.NONE);
    int i = 0;
    for (PSMLElement col : columns) {
      String attribute = col.getAttribute("part");
      CellStyle style = toCellStyle(attribute);
      styles[i] = style;
      i++;
      if (i >= styles.length) break;
    }
    return styles;
  }

  public void format(Appendable out, MarkdownOutputOptions options) throws IOException {
    if (options.captions() && options.table() != MarkdownOutputOptions.TableFormat.HTML) {
      formatCaption(out);
    }
    if (options.table() == MarkdownOutputOptions.TableFormat.COMPACT) {
      formatCompact(out);
    } else if (options.table() == MarkdownOutputOptions.TableFormat.PRETTY) {
      formatPretty(out);
    } else if (options.table() == MarkdownOutputOptions.TableFormat.HTML) {
      formatHtml(out);
    } else if (options.table() == MarkdownOutputOptions.TableFormat.NORMALIZED) {
      formatNormalized(out);
    } else {
      this.collector.warn("Unrecognised table format: "+options.table()+" - using compact format instead.");
      formatCompact(out);
    }
  }

  /**
   * Formats the caption of the table and writes it to the specified {@code Appendable}.
   *
   * @param out the {@code Appendable} object to which the formatted caption is written
   * @throws IOException if an I/O error occurs while appending to the {@code Appendable}
   */
  public void formatCaption(Appendable out) throws IOException {
    out.append("\n*").append(name);
    PSMLElement captionElement = table.getFirstChildElement(PSMLElement.Name.CAPTION);
    if (captionElement != null) {
      out.append(": ").append(captionElement.getText());
    }
    out.append("*\n\n");
  }

  /**
   * Formats the table content into a compact Markdown representation and writes it to the specified
   * {@code Appendable} object.
   *
   * <p>Each row of the table is written sequentially, with the first row
   * being treated as the header. A line separating the header from the content is also added.
   *
   * @param out the {@code Appendable} object to which the compact markdown output is written
   * @throws IOException if an I/O error occurs while writing to the {@code Appendable}
   */
  public void formatCompact(Appendable out) throws IOException {
    List<PSMLElement> rows = table.getChildElements(PSMLElement.Name.ROW);
    int rowIndex = 0;
    for (PSMLElement row : rows) {
      String[] cells = toCells(row, rowIndex, false);
      formatCompactRow(out, cells);
      if (rowIndex == 0) {
        formatCompactHeaderLine(out);
      }
      rowIndex++;
    }
    out.append('\n');
  }

  /**
   * Formats the table into a well-aligned, human-readable Markdown representation and writes the result
   * to the specified {@code Appendable} object.
   *
   * <p>The method computes column widths based on the content of the first 20 rows and adjusts
   * formatting accordingly. Separates the header with a dividing line.
   *
   * @param out the {@code Appendable} object to which the pretty-formatted table is written
   * @throws IOException if an I/O error occurs while writing to the {@code Appendable}
   */
  private void formatPretty(Appendable out) throws IOException {
    List<PSMLElement> rows = table.getChildElements(PSMLElement.Name.ROW);
    final int[] columnWidths = new int[this.columnCount];
    Arrays.fill(columnWidths, MIN_COLUMN_WIDTH);
    // Computes the column widths
    int rowIndex = 0;
    for (PSMLElement row : rows) {
      String[] cells = toCells(row, rowIndex, false);
      for (int i=0; i < cells.length; i++) {
        columnWidths[i] = Math.max(columnWidths[i], Math.min(MAX_COLUMN_WIDTH, cells[i].length()+2));
      }
      rowIndex++;
      // Only scan the first 20 rows to compute the width
      if (rowIndex > MAX_ROWS_PRETTY_SCAN) break;
    }
    // Process
    rowIndex = 0;
    for (PSMLElement row : rows) {
      String[] cells = toCells(row, rowIndex, false);
      formatPrettyRow(out, cells, columnWidths);
      if (rowIndex == 0) {
        formatPrettyHeaderLine(out, columnWidths);
      }
      rowIndex++;
    }
    out.append('\n');
  }

  /**
   * Formats the table into a normalized Markdown representation and writes it to the specified
   * {@code Appendable} object. Each row of the table is processed sequentially, with the first
   * row treated as the header. A header line is added to separate the header from the table content.
   *
   * @param out the {@code Appendable} object to which the normalized Markdown output is written
   * @throws IOException if an I/O error occurs while writing to the {@code Appendable}
   */
  public void formatNormalized(Appendable out) throws IOException {
    List<PSMLElement> rows = table.getChildElements(PSMLElement.Name.ROW);
    int rowIndex = 0;
    for (PSMLElement row : rows) {
      String[] cells = toCells(row, rowIndex, true);
      formatCompactRow(out, cells);
      if (rowIndex == 0) {
        formatCompactHeaderLine(out);
      }
      rowIndex++;
    }
    out.append('\n');
  }

  /**
   * Formats the table represented by the object into an HTML representation and writes it
   * to the specified {@code Appendable} object.
   *
   * <p>The method handles the rendering of rows, cells, headers, and captions, maintaining
   * proper alignment and span attributes as defined in the underlying table structure.
   *
   * @param out the {@code Appendable} object to which the HTML-formatted table is written
   * @throws IOException if an I/O error occurs while writing to the {@code Appendable}
   */
  public void formatHtml(Appendable out) throws IOException {
    out.append("<table>\n");
    // Caption
    out.append("  <caption>").append(this.name);
    PSMLElement caption = table.getFirstChildElement(PSMLElement.Name.CAPTION);
    if (caption != null) {
      out.append(": ").append(XML.escape(caption.getText()));
    }
    out.append("</caption>\n");
    for (PSMLElement row : table.getChildElements(PSMLElement.Name.ROW)) {
      boolean isHeaderRow = "header".equals(row.getAttribute("part"));
      out.append("  <tr>\n");
      List<PSMLElement> cells = row.getChildElements(PSMLElement.Name.CELL, PSMLElement.Name.HCELL);
      int span = 0;
      for (int i=0; i < cells.size(); i++) {
        PSMLElement td = cells.get(i);
        String elementName = isHeaderRow || td.getElement() == PSMLElement.Name.HCELL ? "th" : "td";
        out.append("    <").append(elementName);
        // Compute alignment before spans
        if (this.columnAlignments[i+span] != CellAlignment.NONE) {
          out.append(" style=\"text-align:").append(this.columnAlignments[i].toString().toLowerCase()).append(";\"");
        }
        if (td.getAttribute("colspan") != null) {
          out.append(" colspan=\"").append(td.getAttribute("colspan")).append('"');
          span += td.getAttributeOrElse("colspan", 1) - 1;
        }
        if (td.getAttribute("rowspan") != null) {
          out.append(" rowspan=\"").append(td.getAttribute("rowspan")).append('"');
        }
        out.append('>').append(XML.escape(td.getText().trim())).append("</").append(elementName).append(">\n");
      }
      out.append("  </tr>\n");
    }
    out.append("</table>\n");
  }

  private void formatCompactRow(Appendable out, String[] cells) throws IOException {
    boolean firstCell = true;
    for (String cell : cells) {
      if (firstCell) {
        firstCell = false;
        out.append("| ");
      } else out.append(' ');
      out.append(cell).append(" |");
    }
    out.append('\n');
  }

  private void formatCompactHeaderLine(Appendable out) throws IOException {
    boolean firstCell = true;
    for (int i = 0; i < this.columnCount; i++) {
      if (firstCell) {
        out.append('|');
        firstCell = false;
      }
      CellAlignment align = this.columnAlignments[i];
      out.append(headerLine(align, MIN_COLUMN_WIDTH)).append('|');
    }
    out.append('\n');
  }

  private void formatPrettyRow(Appendable out, String[] cells, int[] columnWidths) throws IOException {
    boolean firstCell = true;
    for (int i=0; i < cells.length; i++) {
      if (firstCell) {
        firstCell = false;
        out.append("| ");
      } else out.append(' ');
      String cell = cells[i];
      CellAlignment align = this.columnAlignments[i];
      int pad = Math.max(columnWidths[i] - cell.length() - 2, 0);
      if (align == CellAlignment.RIGHT) {
        out.append(" ".repeat(pad));
        out.append(cell);
      } else {
        out.append(cell);
        out.append(" ".repeat(pad));
      }
      out.append(" |");
    }
    out.append('\n');
  }

  private void formatPrettyHeaderLine(Appendable out, int[] columnWidths) throws IOException {
    boolean firstCell = true;
    for (int i = 0; i < this.columnCount; i++) {
      if (firstCell) {
        out.append('|');
        firstCell = false;
      }
      CellAlignment align = this.columnAlignments[i];
      out.append(headerLine(align, columnWidths[i])).append('|');
    }
    out.append('\n');
  }

  private String[] toCells(PSMLElement row, int rowIndex, boolean normalize) {
    String[] cells = new String[this.columnCount];
    CellStyle rowStyle = toCellStyle(row.getAttribute("part"));
    int i = 0;
    for (PSMLElement cell : row.getChildElements(PSMLElement.Name.CELL, PSMLElement.Name.HCELL)) {
      // Compute text
      CellStyle style = rowStyle != CellStyle.NONE ? rowStyle : columnStyles[i];
      cells[i] = toText(cell, rowIndex, style);

      // Deal with spans
      int rowspan = cell.getAttributeOrElse("rowspan", 1);
      if (rowspan > 1) {
        this.collector.warn("Table cell "+(i+1)+" at row "+(rowIndex+1)+" has rowspan attribute, which is not supported in Markdown.");
      }
      int colspan = cell.getAttributeOrElse("colspan", 1);
      if (colspan > 1) {
        if (!normalize) {
          this.collector.warn("Table cell "+(i+1)+" at row "+(rowIndex+1)+" has colspan attribute, which is not supported in Markdown.");
        }
        for (int j=1; j < colspan; j++) {
          cells[i+j] = normalize ? cells[i] : "";
        }
      }

      i += colspan;
      if (i >= cells.length) break;
    }
    return cells;
  }

  private static String toText(PSMLElement cell, int rowIndex, CellStyle style) {
    String text = MarkdownSerializer.normalizeText(cell.getText().trim().replace("\n", "<br>"));
    if (style == CellStyle.BOLD && rowIndex > 0) {
      text = "**"+text.replace("*", "\\*")+"**";
    } else if (style == CellStyle.ITALIC) {
      text = "*"+text.replace("*", "\\*")+"*";
    }
    return text;
  }

  private static String headerLine(CellAlignment alignment, int width) {
    switch (alignment) {
      case LEFT: return ":"+"-".repeat(width-1);
      case CENTER: return ":"+"-".repeat(width-2)+":";
      case RIGHT: return "-".repeat(width-1)+":";
      default: return "-".repeat(width);
    }
  }

  private static CellAlignment toCellAlignment(@Nullable String align) {
    if (align == null) return CellAlignment.NONE;
    switch (align) {
      case "left":return CellAlignment.LEFT;
      case "center": return CellAlignment.CENTER;
      case "right": return CellAlignment.RIGHT;
      default: return CellAlignment.NONE;
    }
  }

  private static CellStyle toCellStyle(@Nullable String part) {
    if (part == null) return CellStyle.NONE;
    if ("header".equals(part)) return CellStyle.BOLD;
    if ("footer".equals(part)) return CellStyle.ITALIC;
    return CellStyle.NONE;
  }

}