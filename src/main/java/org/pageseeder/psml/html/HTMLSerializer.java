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
package org.pageseeder.psml.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;
import org.pageseeder.xmlwriter.XMLWriter;
import org.pageseeder.xmlwriter.XMLWriterImpl;

/**
 * The HTMLSerializer class is responsible for serializing PSMLElement objects
 * into their HTML representation.
 *
 * <p>This class provides functionality to output the serialized HTML to a
 * specified Writer object.
 *
 * <p>The mapping follows the conventions used by
 * {@link org.pageseeder.psml.md.MarkdownParser#parseToHTML(java.io.Reader)} so that
 * the output can be parsed back with the {@link HTMLParser}:
 * <ul>
 *   <li>{@code document} becomes {@code article}, sections and fragments become nested
 *       {@code section} elements;</li>
 *   <li>{@code heading} becomes {@code h1}-{@code h6}, {@code para} becomes {@code p};</li>
 *   <li>{@code block} with a label becomes a {@code div} with a {@code label-*} class
 *       ({@code blockquote} for "quoted" blocks), {@code inline} becomes a {@code span};</li>
 *   <li>{@code list}/{@code nlist}/{@code item} become {@code ul}/{@code ol}/{@code li};</li>
 *   <li>{@code preformat} becomes {@code pre}, with a {@code code} element when a language is specified;</li>
 *   <li>{@code xref} and {@code link} become {@code a}, {@code image} becomes {@code img};</li>
 *   <li>Diff markup {@code diff:ins}/{@code diff:del} become {@code ins}/{@code del}.</li>
 * </ul>
 *
 * <p>Document and fragment metadata (document info, fragment info, properties used as metadata,
 * versions, etc.) is not serialized.
 *
 * @author Christophe Lauret
 *
 * @version 1.8.6
 * @since 1.0
 */
public class HTMLSerializer {

  /**
   * Serializes the specified PSML element as HTML to the specified writer.
   *
   * @param element The PSML element to serialize
   * @param out     The writer receiving the HTML
   *
   * @throws IOException If thrown by the writer
   */
  public void serialize(PSMLElement element, Writer out) throws IOException {
    XMLWriter xml = new XMLWriterImpl(out);
    for (HTMLNode node : toHTMLNodes(element)) {
      node.toXML(xml);
    }
    xml.flush();
  }

  /**
   * Converts the specified PSML element to HTML.
   *
   * <p>If the PSML element does not map to a single HTML element (for example, if it is an
   * element that is not serialized or an element whose content is unwrapped), the resulting
   * nodes are wrapped in a {@code div} element.
   *
   * @param element The PSML element to convert
   *
   * @return The corresponding HTML element
   */
  public HTMLElement toHTML(PSMLElement element) {
    List<HTMLNode> nodes = toHTMLNodes(element);
    if (nodes.size() == 1 && nodes.get(0) instanceof HTMLElement) {
      return (HTMLElement) nodes.get(0);
    }
    return new HTMLElement(Name.DIV).addNodes(nodes);
  }

  /**
   * Converts the specified PSML element to a list of HTML nodes.
   *
   * @param element The PSML element to convert
   *
   * @return The corresponding HTML nodes
   */
  private List<HTMLNode> toHTMLNodes(PSMLElement element) {
    List<HTMLNode> nodes = new ArrayList<>(1);
    new Converter().convert(element, nodes);
    return nodes;
  }

  /**
   * Converts a PSML tree into an HTML tree.
   */
  private static final class Converter {

    /**
     * Converts the PSML node and appends the resulting nodes to the list.
     */
    void convert(PSMLNode node, List<HTMLNode> out) {
      if (node instanceof PSMLElement) {
        convertElement((PSMLElement) node, out);
      } else if (node instanceof PSMLText) {
        String text = node.getText();
        if (!text.isEmpty()) out.add(new HTMLText(text));
      }
    }

    /**
     * Converts the child nodes of the PSML element and adds them to the HTML element.
     */
    HTMLElement children(PSMLElement from, HTMLElement to) {
      List<HTMLNode> nodes = new ArrayList<>();
      for (PSMLNode n : from.getNodes()) {
        convert(n, nodes);
      }
      to.addNodes(nodes);
      return to;
    }

    /**
     * Converts the child nodes of the PSML element directly into the output list.
     */
    void unwrap(PSMLElement from, List<HTMLNode> out) {
      for (PSMLNode n : from.getNodes()) {
        convert(n, out);
      }
    }

    private void convertElement(PSMLElement element, List<HTMLNode> out) {
      switch (element.getElement()) {
        // Structure
        case DOCUMENT:
          out.add(children(element, new HTMLElement(Name.ARTICLE)));
          break;
        case SECTION:
        case FRAGMENT:
        case XREF_FRAGMENT:
          out.add(children(element, withId(new HTMLElement(Name.SECTION), element)));
          break;
        case PROPERTIES_FRAGMENT:
          out.add(propertiesFragment(element));
          break;
        case MEDIA_FRAGMENT:
          out.add(mediaFragment(element));
          break;
        case TITLE:
          out.add(children(element, new HTMLElement(Name.H2)));
          break;

        // Blocks
        case HEADING:
          out.add(heading(element));
          break;
        case PARA:
          out.add(prefixed(element, new HTMLElement(Name.P)));
          break;
        case BLOCK:
          out.add(block(element));
          break;
        case PREFORMAT:
          out.add(preformat(element));
          break;
        case LIST:
          out.add(children(element, withRole(new HTMLElement(Name.UL), element)));
          break;
        case NLIST:
          out.add(nlist(element));
          break;
        case ITEM:
          out.add(children(element, new HTMLElement(Name.LI)));
          break;
        case TABLE:
          out.add(table(element));
          break;
        case BLOCKXREF:
          out.add(blockxref(element));
          break;

        // Inline
        case BOLD:
          out.add(children(element, new HTMLElement(Name.STRONG)));
          break;
        case ITALIC:
          out.add(children(element, new HTMLElement(Name.EM)));
          break;
        case UNDERLINE:
          out.add(children(element, new HTMLElement(Name.U)));
          break;
        case MONOSPACE:
          out.add(children(element, new HTMLElement(Name.CODE)));
          break;
        case SUB:
          out.add(children(element, new HTMLElement(Name.SUB)));
          break;
        case SUP:
          out.add(children(element, new HTMLElement(Name.SUP)));
          break;
        case BR:
          out.add(new HTMLElement(Name.BR));
          break;
        case INLINE:
          out.add(inline(element));
          break;
        case LINK:
          out.add(link(element));
          break;
        case XREF:
          out.add(xref(element));
          break;
        case IMAGE:
          out.add(image(element));
          break;
        case ANCHOR:
          out.add(anchor(element));
          break;
        case PLACEHOLDER:
          out.add(placeholder(element));
          break;
        case DIFF_INS:
          out.add(children(element, new HTMLElement(Name.INS)));
          break;
        case DIFF_DEL:
          out.add(children(element, new HTMLElement(Name.DEL)));
          break;

        // Metadata and other elements which are not serialized
        case DOCUMENTINFO:
        case FRAGMENTINFO:
        case METADATA:
        case TOC:
        case VERSIONS:
        case REVERSEXREFS:
        case LOCATOR:
        case NOTES:
        case LABELS:
        case COMPARE:
        case DIFF:
          break;

        // Anything else is unwrapped
        default:
          unwrap(element, out);
      }
    }

    private HTMLElement heading(PSMLElement heading) {
      int level = heading.getAttributeOrElse("level", 1);
      Name name;
      switch (Math.max(1, Math.min(level, 6))) {
        case 2: name = Name.H2; break;
        case 3: name = Name.H3; break;
        case 4: name = Name.H4; break;
        case 5: name = Name.H5; break;
        case 6: name = Name.H6; break;
        default: name = Name.H1;
      }
      return prefixed(heading, new HTMLElement(name));
    }

    /**
     * Adds the prefix of headings and paragraphs as a span with class 'prefix'.
     */
    private HTMLElement prefixed(PSMLElement from, HTMLElement to) {
      String prefix = from.getAttribute("prefix");
      if (prefix != null && !prefix.isEmpty()) {
        to.addNode(new HTMLElement(Name.SPAN).setAttribute("class", "prefix").setText(prefix));
        to.addText(" ");
      }
      String indent = from.getAttribute("indent");
      if (indent != null && !indent.isEmpty() && !"0".equals(indent)) {
        to.setAttribute("class", "indent-" + indent);
      }
      return children(from, to);
    }

    private HTMLElement block(PSMLElement block) {
      String label = block.getAttribute("label");
      if ("quoted".equals(label)) {
        return children(block, new HTMLElement(Name.BLOCKQUOTE));
      }
      HTMLElement div = new HTMLElement(Name.DIV);
      if (label != null && !label.isEmpty()) {
        div.setAttribute("class", "label-" + label);
      }
      return children(block, div);
    }

    private HTMLElement inline(PSMLElement inline) {
      HTMLElement span = new HTMLElement(Name.SPAN);
      String label = inline.getAttribute("label");
      if (label != null && !label.isEmpty()) {
        span.setAttribute("class", "label-" + label);
      }
      return children(inline, span);
    }

    private HTMLElement preformat(PSMLElement preformat) {
      HTMLElement pre = new HTMLElement(Name.PRE);
      String language = toLanguage(preformat.getAttribute("role"));
      if (language != null) {
        HTMLElement code = new HTMLElement(Name.CODE).setAttribute("class", "lang-" + language);
        return pre.addNode(children(preformat, code));
      }
      return children(preformat, pre);
    }

    private HTMLElement mediaFragment(PSMLElement fragment) {
      HTMLElement section = withId(new HTMLElement(Name.SECTION), fragment);
      String type = fragment.getAttribute("type");
      if (type != null && !type.isEmpty()) section.setAttribute("data-type", type);
      HTMLElement pre = new HTMLElement(Name.PRE);
      HTMLElement code = new HTMLElement(Name.CODE);
      String mediatype = fragment.getAttribute("mediatype");
      if (mediatype != null && !mediatype.isEmpty()) {
        String subtype = mediatype.substring(mediatype.indexOf('/') + 1);
        int plus = subtype.lastIndexOf('+');
        if (plus >= 0) subtype = subtype.substring(plus + 1);
        code.setAttribute("class", "lang-" + subtype.toLowerCase(Locale.ROOT));
        section.setAttribute("data-mediatype", mediatype);
      }
      return section.addNode(pre.addNode(children(fragment, code)));
    }

    private HTMLElement nlist(PSMLElement nlist) {
      HTMLElement ol = withRole(new HTMLElement(Name.OL), nlist);
      String start = nlist.getAttribute("start");
      if (start != null && !start.isEmpty() && !"1".equals(start)) {
        ol.setAttribute("start", start);
      }
      String type = toListType(nlist.getAttribute("type"));
      if (type != null) ol.setAttribute("type", type);
      return children(nlist, ol);
    }

    private HTMLElement table(PSMLElement table) {
      HTMLElement out = withRole(new HTMLElement(Name.TABLE), table);
      copy(table, out, "summary", "width", "height");
      PSMLElement caption = table.getFirstChildElement(PSMLElement.Name.CAPTION);
      if (caption != null) {
        out.addNode(children(caption, new HTMLElement(Name.CAPTION)));
      }
      List<PSMLElement> cols = table.getChildElements(PSMLElement.Name.COL);
      if (!cols.isEmpty()) {
        HTMLElement colgroup = new HTMLElement(Name.COLGROUP);
        for (PSMLElement col : cols) {
          HTMLElement c = withRole(new HTMLElement(Name.COL), col);
          copy(col, c, "align", "span", "width");
          colgroup.addNode(c);
        }
        out.addNode(colgroup);
      }
      HTMLElement thead = new HTMLElement(Name.THEAD);
      HTMLElement tbody = new HTMLElement(Name.TBODY);
      HTMLElement tfoot = new HTMLElement(Name.TFOOT);
      for (PSMLElement row : table.getChildElements(PSMLElement.Name.ROW)) {
        String part = row.getAttributeOrElse("part", "body");
        boolean header = "header".equals(part);
        HTMLElement tr = withRole(new HTMLElement(Name.TR), row);
        copy(row, tr, "align");
        for (PSMLElement cell : row.getChildElements(PSMLElement.Name.CELL, PSMLElement.Name.HCELL)) {
          boolean th = header || cell.isElement(PSMLElement.Name.HCELL);
          HTMLElement c = withRole(new HTMLElement(th ? Name.TH : Name.TD), cell);
          copy(cell, c, "align", "valign", "colspan", "rowspan", "width");
          tr.addNode(children(cell, c));
        }
        if (header) thead.addNode(tr);
        else if ("footer".equals(part)) tfoot.addNode(tr);
        else tbody.addNode(tr);
      }
      if (!thead.isEmpty()) out.addNode(thead);
      if (!tbody.isEmpty()) out.addNode(tbody);
      if (!tfoot.isEmpty()) out.addNode(tfoot);
      return out;
    }

    private HTMLElement blockxref(PSMLElement blockxref) {
      HTMLElement div = new HTMLElement(Name.DIV).setAttribute("class", "blockxref");
      List<PSMLElement> content = blockxref.getChildElements(PSMLElement.Name.DOCUMENT, PSMLElement.Name.FRAGMENT,
          PSMLElement.Name.XREF_FRAGMENT, PSMLElement.Name.PROPERTIES_FRAGMENT, PSMLElement.Name.MEDIA_FRAGMENT);
      if (content.isEmpty()) {
        HTMLElement a = new HTMLElement(Name.A).setAttribute("href", toHref(blockxref));
        a.setText(xrefText(blockxref));
        div.addNode(a);
      } else {
        // Transcluded content
        List<HTMLNode> nodes = new ArrayList<>();
        for (PSMLElement c : content) convert(c, nodes);
        div.addNodes(nodes);
      }
      return div;
    }

    private HTMLElement link(PSMLElement link) {
      HTMLElement a = new HTMLElement(Name.A);
      String href = link.getAttribute("href");
      if (href != null) a.setAttribute("href", href);
      return children(link, a);
    }

    private HTMLElement xref(PSMLElement xref) {
      HTMLElement a = new HTMLElement(Name.A).setAttribute("href", toHref(xref));
      if (xref.isEmpty()) {
        a.setText(xrefText(xref));
        return a;
      }
      return children(xref, a);
    }

    private HTMLElement image(PSMLElement image) {
      HTMLElement img = new HTMLElement(Name.IMG);
      img.setAttribute("src", image.getAttributeOrElse("src", ""));
      img.setAttribute("alt", image.getAttributeOrElse("alt", ""));
      copy(image, img, "width", "height");
      return img;
    }

    private HTMLElement anchor(PSMLElement anchor) {
      HTMLElement a = new HTMLElement(Name.A);
      String name = anchor.getAttribute("name");
      if (name != null) a.setAttribute("id", name);
      return children(anchor, a);
    }

    private HTMLElement placeholder(PSMLElement placeholder) {
      HTMLElement var = new HTMLElement(Name.VAR);
      String name = placeholder.getAttributeOrElse("name", "");
      if (!name.isEmpty()) var.setAttribute("data-name", name);
      if (placeholder.isEmpty()) {
        return var.setText(name);
      }
      return children(placeholder, var);
    }

    private HTMLElement propertiesFragment(PSMLElement fragment) {
      HTMLElement section = withId(new HTMLElement(Name.SECTION), fragment);
      HTMLElement dl = new HTMLElement(Name.DL);
      for (PSMLElement property : fragment.getChildElements(PSMLElement.Name.PROPERTY)) {
        String name = property.getAttributeOrElse("name", "");
        HTMLElement dt = new HTMLElement(Name.DT).setText(property.getAttributeOrElse("title", name));
        if (!name.isEmpty()) dt.setAttribute("data-name", name);
        dl.addNode(dt);
        String value = property.getAttribute("value");
        if (value != null) {
          dl.addNode(new HTMLElement(Name.DD).setText(value));
        }
        for (PSMLElement v : property.getChildElements()) {
          if (v.isElement(PSMLElement.Name.VALUE)) {
            dl.addNode(new HTMLElement(Name.DD).setText(v.getText()));
          } else {
            List<HTMLNode> nodes = new ArrayList<>();
            convert(v, nodes);
            dl.addNode(new HTMLElement(Name.DD).addNodes(nodes));
          }
        }
      }
      return section.addNode(dl);
    }

    private static HTMLElement withId(HTMLElement to, PSMLElement from) {
      String id = from.getAttribute("id");
      if (id != null && !id.isEmpty()) to.setAttribute("id", id);
      return to;
    }

    private static HTMLElement withRole(HTMLElement to, PSMLElement from) {
      String role = from.getAttribute("role");
      if (role != null && !role.isEmpty()) to.setAttribute("class", role);
      return to;
    }

    private static void copy(PSMLElement from, HTMLElement to, String... attributes) {
      for (String name : attributes) {
        String value = from.getAttribute(name);
        if (value != null && !value.isEmpty()) to.setAttribute(name, value);
      }
    }

    private static String toHref(PSMLElement xref) {
      String href = xref.getAttributeOrElse("href", "");
      String frag = xref.getAttribute("frag");
      if (frag != null && !frag.isEmpty() && !"default".equals(frag) && href.indexOf('#') < 0) {
        href = href + '#' + frag;
      }
      return href;
    }

    private static String xrefText(PSMLElement xref) {
      String text = xref.getText();
      if (!text.isEmpty()) return text;
      String title = xref.getAttribute("title");
      if (title != null && !title.isEmpty()) return title;
      String urititle = xref.getAttribute("urititle");
      if (urititle != null && !urititle.isEmpty()) return urititle;
      return xref.getAttributeOrElse("href", "");
    }

    private static @Nullable String toLanguage(@Nullable String role) {
      if (role == null) return null;
      if (role.startsWith("lang-")) return role.substring(5);
      if (role.startsWith("language-")) return role.substring(9);
      return null;
    }

    private static @Nullable String toListType(@Nullable String type) {
      if (type == null) return null;
      switch (type) {
        case "loweralpha": return "a";
        case "upperalpha": return "A";
        case "lowerroman": return "i";
        case "upperroman": return "I";
        default: return null;
      }
    }
  }

}
