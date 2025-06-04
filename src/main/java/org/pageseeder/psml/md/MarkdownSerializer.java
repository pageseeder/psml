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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLElement.Name;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;

public class MarkdownSerializer {

  public MarkdownSerializer() {
  }

  public void serialize(PSMLElement element, Writer out) throws IOException {
    Name name = element.getElement();

    switch (name) {
      case BLOCK:
        serializeBlock(element, out);
        break;

      case BLOCKXREF:
        // TODO
        break;

      case BOLD:
        serializeBold(element, out);
        break;

      case BR:
        out.write("\n");
        break;

      case DOCUMENT:
        serializeDocument(element, out);
        break;

      case FRAGMENT:
        // TODO
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

      case CAPTION:
      case CELL:
      case COL:
      case ROW:
        // Handled in table
        break;

      case TABLE:
        serializeTable(element, out);
        break;

      case XREF:
        // TODO
        break;

      case XREF_FRAGMENT:
        // TODO
        break;

      case SECTION:
        serializeSection(element, out);
        break;

      case TOC:
      case AUTHOR:
      case COMPARE:
      case COMPARETO:
      case MEDIA_FRAGMENT:
      case PROPERTIES_FRAGMENT:
        // Ignore these elements: do nothing
        break;

      default:
        processChildren(element, out);

    }

  }

  public void serializeDocument(PSMLElement document, Writer out) throws IOException {
    boolean first = true;
    for (PSMLElement section : document.getChildElements(Name.SECTION)) {
      if (first) first = false;
      else out.append("\n---\n");
      processChildren(section, out);
    }
  }

  public void serializeSection(PSMLElement section, Writer out) throws IOException {
    processChildren(section, out);
  }

  public void serializeFragment(PSMLElement section, Writer out) throws IOException {
    out.append('\n');
    processChildren(section, out);
    out.append('\n');
  }

  public void serializeList(PSMLElement list, Writer out) throws IOException {
    List<PSMLNode> items = list.getNodes();
    for (PSMLNode item : items) {
      out.append(" * ");
      processChildren((PSMLElement)item, out);
      out.append('\n');
    }
  }

  public void serializeNlist(PSMLElement nlist, Writer out) throws IOException {
    List<PSMLNode> items = nlist.getNodes();
    String start = nlist.getAttribute("start");
    int i = (start != null)? Integer.parseInt(start) : 1;
    for (PSMLNode item : items) {
      out.append(" ").append(Integer.toString(i++)).append(". ");
      processChildren((PSMLElement)item, out);
      out.append('\n');
    }
  }

  public void serializePreformat(PSMLElement element, Writer out) throws IOException {
    out.append("\n```");
    String language = element.getAttribute("role");
    if (language != null) {
      out.append(language);
    }
    out.append("\n");
    out.append(element.getText());
    out.append("\n```");
  }

  public void serializePara(PSMLElement para, Writer out) throws IOException {
    out.append("\n");
    processChildren(para, out);
    out.append("\n");
  }

  public void serializeBold(PSMLElement bold, Writer out) throws IOException {
    out.append("__");
    processChildren(bold, out);
    out.append("__");
  }

  public void serializeItalic(PSMLElement italic, Writer out) throws IOException {
    out.append("*");
    processChildren(italic, out);
    out.append("*");
  }

  public void serializeMonospace(PSMLElement monospace, Writer out) throws IOException {
    out.append("`");
    processChildren(monospace, out);
    out.append("`");
  }

  public void serializeLink(PSMLElement link, Writer out) throws IOException {
    String text = link.getText();
    String url = link.getAttribute("href");
    out.append("[").append(text).append("](").append(url).append(")");
  }

  public void serializeImage(PSMLElement image, Writer out) throws IOException {
    String alt = image.getAttribute("alt");
    String src = image.getAttribute("src");
    out.append("![").append(alt).append("](").append(src).append(")");
  }

  public void serializeBlock(PSMLElement block, Writer out) throws IOException {
    String label = block.getAttribute("label");
    if (label != null) {
      out.append(block.getAttribute("label").toUpperCase()).append(": ");
    }
    processChildren(block, out);
  }

  public void serializeHeading(PSMLElement heading, Writer out) throws IOException {
    String level = heading.getAttributeOrElse("level", "1");
    String prefix = heading.getAttribute("prefix");
    switch (level) {
      case "1": out.append("# "); break;
      case "2": out.append("## "); break;
      case "3": out.append("### "); break;
      case "4": out.append("#### "); break;
      case "5": out.append("##### "); break;
      case "6": out.append("###### "); break;
      default: // ignore
    }
    if (prefix != null) {
      out.append(prefix).append(" ");
    }
    processChildren(heading, out);
    out.append("\n");
  }

  public void serializeTable(PSMLElement table, Writer out) throws IOException {
    List<PSMLNode> nodes = table.getNodes();
    PSMLElement caption = table.getFirstChildElement(Name.CAPTION);
    if (caption != null) {
      out.append("**Table**: ").append(caption.getText()).append('\n');
    }
    // TODO Handle cols and alignment, and header row
    for (PSMLElement row : table.getChildElements(Name.ROW)) {
      out.append("| ");
      List<PSMLElement> cells = row.getChildElements(Name.CELL, Name.HCELL);
      for (PSMLElement cell : cells) {
        out.append(cell.getText()).append(" | ");
      }
      out.append('\n');
    }
  }

  public void serializeProperty(PSMLElement property, Writer out) throws IOException {
    // TODO xref and multiple values
    out.append("\n");
    out.append(property.getAttribute("name"));
    out.append(":");
    out.append(property.getAttribute("value"));
    out.append("\n");
  }

  public void processChildren(PSMLElement element, Writer out) throws IOException {
    for (PSMLNode node : element.getNodes()) {
      if (node instanceof PSMLText) {
        out.append(node.getText());
      } else {
        serialize((PSMLElement)node, out);
      }
    }
  }

}
