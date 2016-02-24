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
      case Block:
        // TODO
        break;

      case Blockxref:
        // TODO
        break;

      case Bold:
        serializeBold(element, out);
        break;

      case Br:
        out.write("\n");

      case Caption:
        // TODO
        break;

      case Cell:
        // TODO
        break;

      case Col:
        // TODO
        break;

      case Fragment:
        // TODO
        break;

      case Heading:
        // TODO
        break;

      case Image:
        serializeImage(element, out);
        break;

      case Italic:
        serializeItalic(element, out);
        break;

      case Item:
        // Handled in list
        break;

      case Link:
        serializeLink(element, out);
        break;

      case List:
        serializeList(element, out);
        break;

      case Monospace:
        serializeMonospace(element, out);
        break;

      case Nlist:
        serializeNlist(element, out);
        break;

      case Para:
        serializePara(element, out);
        break;

      case Preformat:
        serializePreformat(element, out);
        break;

      case Property:
        serializeProperty(element, out);
        break;

      case Row:
        // TODO
        break;
      case Table:
        // TODO
        break;

      case Xref:
        // TODO
        break;

      case XrefFragment:
        // TODO
        break;

      case Toc:
      case Author:
      case Compare:
      case Compareto:
      case MediaFragment:
      case PropertiesFragment:
        // Ignore these elements: do nothing
        break;

      default:
        processChildren(element, out);

    }

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
