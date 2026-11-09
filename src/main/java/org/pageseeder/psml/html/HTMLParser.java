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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jspecify.annotations.Nullable;
import org.pageseeder.psml.html.HTMLElement.Name;
import org.pageseeder.psml.model.PSMLElement;
import org.pageseeder.psml.model.PSMLNode;
import org.pageseeder.psml.model.PSMLText;
import org.pageseeder.psml.spi.Parser;
import org.pageseeder.psml.xml.XML;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implementation of the Parser for HTML content, specifically for XHTML.
 *
 * <p>This class is designed to parse XHTML content and generate corresponding PSML elements.
 * It specifies the mediatype it supports as "application/xhtml+xml".
 *
 * <p>The content must be well-formed XML; elements may be in the XHTML namespace or in no
 * namespace. Elements in other namespaces (such as SVG or MathML) are ignored.
 *
 * <p>The structure of the resulting PSML depends on the root element:
 * <ul>
 *   <li>{@code html}, {@code body}, {@code main} and {@code article} produce a PSML
 *       {@code document}: each {@code section} becomes a PSML section, and any nested
 *       {@code section} becomes a fragment;</li>
 *   <li>any other element produces a single PSML {@code fragment}.</li>
 * </ul>
 *
 * <p>This is the reverse of the {@link HTMLSerializer}, so that PSML serialized as HTML can be
 * parsed back, and of {@link org.pageseeder.psml.md.MarkdownParser#parseToHTML(Reader)}.
 *
 * @author Christophe Lauret
 *
 * @version 1.8.6
 * @since 1.0
 */
public class HTMLParser extends Parser {

  /**
   * The XHTML namespace URI.
   */
  public static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

  /**
   * Lazily loaded SAX parser factory.
   */
  private @Nullable SAXParserFactory factory = null;

  @Override
  public String getMediatype() {
    return "application/xhtml+xml";
  }

  @Override
  public PSMLElement parse(Reader reader) throws IOException {
    HTMLElement html = load(reader);
    return toPSML(html);
  }

  /**
   * Loads the XHTML content as an HTML tree.
   *
   * @param reader The reader containing the XHTML to parse
   *
   * @return The root HTML element
   *
   * @throws IOException If an error occurs while reading or if the content is not well-formed XML.
   */
  public HTMLElement load(Reader reader) throws IOException {
    if (this.factory == null) {
      try {
        SAXParserFactory f = XML.newSAXParserFactory();
        f.setNamespaceAware(true);
        this.factory = f;
      } catch (SAXException | ParserConfigurationException ex) {
        throw new IOException(ex);
      }
    }
    Handler handler = new Handler();
    try {
      SAXParser parser = this.factory.newSAXParser();
      parser.parse(new InputSource(reader), handler);
    } catch (SAXException | ParserConfigurationException ex) {
      throw new IOException(ex);
    }
    HTMLElement root = handler.root;
    if (root == null) throw new IOException("No HTML element found");
    return root;
  }

  /**
   * Converts the specified HTML element to PSML.
   *
   * @param html The HTML element to convert
   *
   * @return The corresponding PSML element: a document or a fragment
   */
  public PSMLElement toPSML(HTMLElement html) {
    return new Converter().convert(html);
  }

  /**
   * SAX handler building an HTML tree.
   */
  private static final class Handler extends DefaultHandler {

    private final List<HTMLElement> stack = new ArrayList<>();

    private final StringBuilder text = new StringBuilder();

    private @Nullable HTMLElement root = null;

    /**
     * Depth inside an ignored element (foreign namespace, script, etc.)
     */
    private int ignore = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (this.ignore > 0 || isIgnored(uri, localName)) {
        this.ignore++;
        return;
      }
      commitText();
      HTMLElement element = new HTMLElement(localName);
      for (int i = 0; i < attributes.getLength(); i++) {
        String ns = attributes.getURI(i);
        if (ns == null || ns.isEmpty()) {
          element.setAttribute(attributes.getLocalName(i), attributes.getValue(i));
        }
      }
      if (!this.stack.isEmpty()) {
        this.stack.get(this.stack.size() - 1).addNode(element);
      } else if (this.root == null) {
        this.root = element;
      }
      this.stack.add(element);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (this.ignore > 0) {
        this.ignore--;
        return;
      }
      commitText();
      this.stack.remove(this.stack.size() - 1);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      if (this.ignore == 0) this.text.append(ch, start, length);
    }

    private void commitText() {
      if (this.text.length() > 0) {
        if (!this.stack.isEmpty()) {
          this.stack.get(this.stack.size() - 1).addNode(new HTMLText(this.text.toString()));
        }
        this.text.setLength(0);
      }
    }

    private static boolean isIgnored(@Nullable String uri, String localName) {
      if (uri != null && !uri.isEmpty() && !XHTML_NAMESPACE.equals(uri)) return true;
      switch (localName.toLowerCase(Locale.ROOT)) {
        case "script":
        case "noscript":
        case "template":
        case "style":
          return true;
        default:
          return false;
      }
    }
  }

  /**
   * Converts an HTML tree into PSML.
   */
  private static final class Converter {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:.*");

    private int sectionCount = 0;

    private int fragmentCount = 0;

    PSMLElement convert(HTMLElement root) {
      HTMLElement html = root;
      if (html.isElement(Name.HTML)) {
        HTMLElement body = firstChild(html, Name.BODY);
        html = body != null ? body : new HTMLElement(Name.BODY);
      }
      if (html.isAnyElement(Name.BODY, Name.MAIN, Name.ARTICLE)) {
        PSMLElement document = new PSMLElement(PSMLElement.Name.DOCUMENT);
        document.setAttribute("level", "portable");
        List<HTMLNode> loose = new ArrayList<>();
        sections(html, document, loose);
        flushLooseSection(loose, document);
        return document;
      }
      if (isContainer(html)) {
        return fragment(html);
      }
      // Any other element is considered to be the content of a fragment
      return fragment(null, List.of(html));
    }

    // Structure
    // ---------------------------------------------------------------------------------------------

    /**
     * Collects the sections in the container, content outside sections is collected in the
     * loose list and wrapped in a section.
     */
    private void sections(HTMLElement container, PSMLElement document, List<HTMLNode> loose) {
      for (HTMLNode node : container.getNodes()) {
        if (node instanceof HTMLElement) {
          HTMLElement element = (HTMLElement) node;
          if (element.isElement(Name.SECTION)) {
            flushLooseSection(loose, document);
            document.addNode(section(element));
            continue;
          } else if (isTransparentStructure(element) && containsSection(element)) {
            sections(element, document, loose);
            continue;
          }
        }
        loose.add(node);
      }
    }

    private void flushLooseSection(List<HTMLNode> loose, PSMLElement document) {
      if (hasContent(loose)) {
        PSMLElement section = new PSMLElement(PSMLElement.Name.SECTION);
        section.setAttribute("id", nextSectionId());
        section.addNode(fragment(null, loose));
        document.addNode(section);
      }
      loose.clear();
    }

    private PSMLElement section(HTMLElement html) {
      PSMLElement section = new PSMLElement(PSMLElement.Name.SECTION);
      String id = html.getAttribute("id");
      section.setAttribute("id", id != null && !id.isEmpty() ? id : nextSectionId());
      if (containsSection(html)) {
        List<HTMLNode> loose = new ArrayList<>();
        fragments(html, section, loose);
        flushLooseFragment(loose, section);
      } else {
        section.addNode(fragment(null, html.getNodes()));
      }
      return section;
    }

    private void fragments(HTMLElement container, PSMLElement section, List<HTMLNode> loose) {
      for (HTMLNode node : container.getNodes()) {
        if (node instanceof HTMLElement) {
          HTMLElement element = (HTMLElement) node;
          if (element.isElement(Name.SECTION)) {
            flushLooseFragment(loose, section);
            section.addNode(fragment(element));
            continue;
          } else if (isTransparentStructure(element) && containsSection(element)) {
            fragments(element, section, loose);
            continue;
          }
        }
        loose.add(node);
      }
    }

    private void flushLooseFragment(List<HTMLNode> loose, PSMLElement section) {
      if (hasContent(loose)) {
        section.addNode(fragment(null, loose));
      }
      loose.clear();
    }

    /**
     * Converts the content of the specified element as a fragment.
     */
    private PSMLElement fragment(HTMLElement html) {
      String mediatype = html.getAttribute("data-mediatype");
      if (mediatype != null && !mediatype.isEmpty()) {
        PSMLElement fragment = new PSMLElement(PSMLElement.Name.MEDIA_FRAGMENT);
        String id = html.getAttribute("id");
        fragment.setAttribute("id", id != null && !id.isEmpty() ? id : nextFragmentId());
        String type = html.getAttribute("data-type");
        if (type != null && !type.isEmpty()) fragment.setAttribute("type", type);
        fragment.setAttribute("mediatype", mediatype);
        HTMLElement pre = firstDescendant(html, Name.PRE);
        String text = (pre != null ? pre : html).getText();
        if (!text.isEmpty()) fragment.setText(text);
        return fragment;
      }
      return fragment(html.getAttribute("id"), html.getNodes());
    }

    private PSMLElement fragment(@Nullable String id, List<HTMLNode> nodes) {
      String fragmentId = id != null && !id.isEmpty() ? id : nextFragmentId();
      HTMLElement dl = soleElement(nodes, Name.DL);
      if (dl != null) {
        return propertiesFragment(fragmentId, dl);
      }
      PSMLElement fragment = new PSMLElement(PSMLElement.Name.FRAGMENT);
      fragment.setAttribute("id", fragmentId);
      blocks(nodes, fragment, true);
      return fragment;
    }

    private PSMLElement propertiesFragment(String id, HTMLElement dl) {
      PSMLElement fragment = new PSMLElement(PSMLElement.Name.PROPERTIES_FRAGMENT);
      fragment.setAttribute("id", id);
      PSMLElement property = null;
      List<String> values = new ArrayList<>();
      for (HTMLElement child : childElements(dl)) {
        if (child.isElement(Name.DT)) {
          if (property != null) setPropertyValues(property, values);
          String title = normalize(child.getText()).trim();
          String name = child.getAttribute("data-name");
          if (name == null || name.isEmpty()) {
            name = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
          }
          property = new PSMLElement(PSMLElement.Name.PROPERTY);
          property.setAttribute("name", name);
          property.setAttribute("title", title);
          fragment.addNode(property);
        } else if (child.isElement(Name.DD) && property != null) {
          values.add(normalize(child.getText()).trim());
        }
      }
      if (property != null) setPropertyValues(property, values);
      return fragment;
    }

    private static void setPropertyValues(PSMLElement property, List<String> values) {
      if (values.size() == 1) {
        property.setAttribute("value", values.get(0));
      } else if (values.size() > 1) {
        property.setAttribute("multiple", "true");
        for (String v : values) {
          property.addNode(new PSMLElement(PSMLElement.Name.VALUE).setText(v));
        }
      }
      values.clear();
    }

    // Blocks
    // ---------------------------------------------------------------------------------------------

    /**
     * Converts a list of nodes in a context where block-level elements are expected.
     *
     * @param nodes    The HTML nodes to convert
     * @param target   The PSML element receiving the converted nodes
     * @param wrapRuns Whether consecutive inline content should be wrapped in a paragraph
     */
    private void blocks(List<HTMLNode> nodes, PSMLElement target, boolean wrapRuns) {
      List<PSMLNode> run = new ArrayList<>();
      for (HTMLNode node : nodes) {
        if (node instanceof HTMLElement && isBlock((HTMLElement) node)) {
          flushRun(run, target, wrapRuns);
          block((HTMLElement) node, target);
        } else {
          inline(node, run);
        }
      }
      flushRun(run, target, wrapRuns);
    }

    private static void flushRun(List<PSMLNode> run, PSMLElement target, boolean wrap) {
      trim(run);
      if (!run.isEmpty()) {
        if (wrap) {
          target.addNode(new PSMLElement(PSMLElement.Name.PARA).addNodes(run));
        } else {
          target.addNodes(run);
        }
      }
      run.clear();
    }

    private void block(HTMLElement html, PSMLElement target) {
      switch (html.getElement()) {
        case P:
          target.addNode(prefixed(html, new PSMLElement(PSMLElement.Name.PARA)));
          break;
        case H1:
        case H2:
        case H3:
        case H4:
        case H5:
        case H6:
          PSMLElement heading = new PSMLElement(PSMLElement.Name.HEADING);
          heading.setAttribute("level", html.getElement().name().substring(1));
          target.addNode(prefixed(html, heading));
          break;
        case UL:
          target.addNode(list(html, withRole(new PSMLElement(PSMLElement.Name.LIST), html)));
          break;
        case OL:
          target.addNode(nlist(html));
          break;
        case DL:
          definitionList(html, target);
          break;
        case PRE:
          target.addNode(preformat(html));
          break;
        case BLOCKQUOTE:
          PSMLElement quoted = new PSMLElement(PSMLElement.Name.BLOCK).setAttribute("label", "quoted");
          blocks(html.getNodes(), quoted, true);
          target.addNode(quoted);
          break;
        case DIV:
          div(html, target);
          break;
        case TABLE:
          target.addNode(table(html));
          break;
        case FIGCAPTION:
          PSMLElement caption = new PSMLElement(PSMLElement.Name.PARA);
          target.addNode(inlines(html.getNodes(), caption));
          break;
        case HR:
        case HEAD:
        case META:
        case TITLE:
        case STYLE:
        case SOURCE:
        case IFRAME:
        case VIDEO:
        case COL:
        case COLGROUP:
          // Ignored
          break;
        default:
          // Transparent: section, article, aside, header, footer, nav, figure, etc.
          blocks(html.getNodes(), target, true);
      }
    }

    private PSMLElement prefixed(HTMLElement html, PSMLElement element) {
      List<HTMLNode> nodes = new ArrayList<>(html.getNodes());
      // A leading span with the 'prefix' class is the prefix of the heading or paragraph
      for (int i = 0; i < nodes.size(); i++) {
        HTMLNode node = nodes.get(i);
        if (node instanceof HTMLElement) {
          HTMLElement span = (HTMLElement) node;
          if (span.isElement(Name.SPAN) && hasClass(span, "prefix")) {
            element.setAttribute("prefix", normalize(span.getText()).trim());
            nodes.remove(i);
          }
          break;
        } else if (!isWhitespace(node.getText())) {
          break;
        }
      }
      String indent = classValue(html, "indent-");
      if (indent != null && element.isElement(PSMLElement.Name.PARA)) {
        element.setAttribute("indent", indent);
      }
      return inlines(nodes, element);
    }

    private PSMLElement list(HTMLElement html, PSMLElement list) {
      for (HTMLElement child : childElements(html)) {
        if (child.isElement(Name.LI)) {
          PSMLElement item = new PSMLElement(PSMLElement.Name.ITEM);
          blocks(child.getNodes(), item, false);
          list.addNode(item);
        }
      }
      return list;
    }

    private PSMLElement nlist(HTMLElement html) {
      PSMLElement nlist = withRole(new PSMLElement(PSMLElement.Name.NLIST), html);
      String start = html.getAttribute("start");
      if (start != null && !start.isEmpty()) nlist.setAttribute("start", start);
      String type = toListType(html.getAttribute("type"));
      if (type != null) nlist.setAttribute("type", type);
      return list(html, nlist);
    }

    private void definitionList(HTMLElement dl, PSMLElement target) {
      for (HTMLElement child : childElements(dl)) {
        if (child.isElement(Name.DT)) {
          PSMLElement bold = inlines(child.getNodes(), new PSMLElement(PSMLElement.Name.BOLD));
          target.addNode(new PSMLElement(PSMLElement.Name.PARA).addNode(bold));
        } else if (child.isElement(Name.DD)) {
          blocks(child.getNodes(), target, true);
        }
      }
    }

    private static PSMLElement preformat(HTMLElement pre) {
      PSMLElement preformat = new PSMLElement(PSMLElement.Name.PREFORMAT);
      String language = null;
      List<HTMLElement> children = childElements(pre);
      if (children.size() == 1 && children.get(0).isElement(Name.CODE)) {
        HTMLElement code = children.get(0);
        language = classValue(code, "lang-");
        if (language == null) language = classValue(code, "language-");
      }
      if (language != null) preformat.setAttribute("role", "lang-" + language);
      String text = pre.getText();
      if (!text.isEmpty()) preformat.setText(text);
      return preformat;
    }

    private void div(HTMLElement div, PSMLElement target) {
      if (hasClass(div, "blockxref")) {
        target.addNode(blockxref(div));
        return;
      }
      String label = div.getAttribute("data-label");
      if (label == null || label.isEmpty()) label = div.getAttribute("label");
      if (label == null || label.isEmpty()) label = classValue(div, "label-");
      if (label != null && !label.isEmpty()) {
        PSMLElement block = new PSMLElement(PSMLElement.Name.BLOCK).setAttribute("label", label);
        blocks(div.getNodes(), block, false);
        target.addNode(block);
      } else {
        blocks(div.getNodes(), target, true);
      }
    }

    private static PSMLElement blockxref(HTMLElement div) {
      PSMLElement blockxref = new PSMLElement(PSMLElement.Name.BLOCKXREF);
      HTMLElement a = firstDescendant(div, Name.A);
      String text = normalize((a != null ? a : div).getText()).trim();
      String href = a != null ? a.getAttribute("href") : null;
      setXrefAttributes(blockxref, href != null ? href : "", text);
      blockxref.setAttribute("type", "embed");
      if (!text.isEmpty()) blockxref.setText(text);
      return blockxref;
    }

    private PSMLElement table(HTMLElement html) {
      PSMLElement table = withRole(new PSMLElement(PSMLElement.Name.TABLE), html);
      copy(html, table, "summary", "width", "height");
      boolean first = true;
      for (HTMLElement child : childElements(html)) {
        switch (child.getElement()) {
          case CAPTION:
            table.addNode(inlines(child.getNodes(), new PSMLElement(PSMLElement.Name.CAPTION)));
            break;
          case COLGROUP:
            for (HTMLElement col : childElements(child)) {
              if (col.isElement(Name.COL)) table.addNode(col(col));
            }
            break;
          case COL:
            table.addNode(col(child));
            break;
          case THEAD:
          case TBODY:
          case TFOOT:
            String part = child.isElement(Name.THEAD) ? "header" : child.isElement(Name.TFOOT) ? "footer" : null;
            for (HTMLElement tr : childElements(child)) {
              if (tr.isElement(Name.TR)) {
                table.addNode(row(tr, part != null ? part : first && isHeaderRow(tr) ? "header" : null));
                first = false;
              }
            }
            break;
          case TR:
            table.addNode(row(child, first && isHeaderRow(child) ? "header" : null));
            first = false;
            break;
          default:
        }
      }
      return table;
    }

    private static PSMLElement col(HTMLElement html) {
      PSMLElement col = withRole(new PSMLElement(PSMLElement.Name.COL), html);
      copy(html, col, "align", "span", "width");
      return col;
    }

    private PSMLElement row(HTMLElement tr, @Nullable String part) {
      PSMLElement row = withRole(new PSMLElement(PSMLElement.Name.ROW), tr);
      if (part != null) row.setAttribute("part", part);
      copy(tr, row, "align");
      boolean header = "header".equals(part);
      for (HTMLElement td : childElements(tr)) {
        if (td.isElement(Name.TD) || td.isElement(Name.TH)) {
          boolean hcell = td.isElement(Name.TH) && !header;
          PSMLElement cell = withRole(new PSMLElement(hcell ? PSMLElement.Name.HCELL : PSMLElement.Name.CELL), td);
          copy(td, cell, "align", "valign", "colspan", "rowspan", "width");
          blocks(td.getNodes(), cell, false);
          row.addNode(cell);
        }
      }
      return row;
    }

    private static boolean isHeaderRow(HTMLElement tr) {
      List<HTMLElement> cells = childElements(tr);
      if (cells.isEmpty()) return false;
      for (HTMLElement cell : cells) {
        if (!cell.isElement(Name.TH)) return false;
      }
      return true;
    }

    // Inline
    // ---------------------------------------------------------------------------------------------

    /**
     * Converts the nodes as inline content of the specified element and trims the result.
     */
    private PSMLElement inlines(List<HTMLNode> nodes, PSMLElement element) {
      List<PSMLNode> out = new ArrayList<>();
      for (HTMLNode node : nodes) {
        inline(node, out);
      }
      trim(out);
      return element.addNodes(out);
    }

    /**
     * Converts the nodes as inline content of the specified element without trimming.
     */
    private PSMLElement inlineChildren(HTMLElement html, PSMLElement element) {
      List<PSMLNode> out = element.getNodes();
      for (HTMLNode node : html.getNodes()) {
        inline(node, out);
      }
      return element;
    }

    private void inline(HTMLNode node, List<PSMLNode> out) {
      if (node instanceof HTMLText) {
        String text = normalize(node.getText());
        if (!text.isEmpty()) out.add(new PSMLText(text));
        return;
      }
      if (!(node instanceof HTMLElement)) return;
      HTMLElement html = (HTMLElement) node;
      switch (html.getElement()) {
        case STRONG:
        case B:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.BOLD)));
          break;
        case EM:
        case I:
        case CITE:
        case DFN:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.ITALIC)));
          break;
        case U:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.UNDERLINE)));
          break;
        case CODE:
        case KBD:
        case SAMP:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.MONOSPACE)));
          break;
        case SUB:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.SUB)));
          break;
        case SUP:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.SUP)));
          break;
        case BR:
          out.add(new PSMLElement(PSMLElement.Name.BR));
          break;
        case INS:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.DIFF_INS)));
          break;
        case DEL:
          out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.DIFF_DEL)));
          break;
        case A:
          out.add(anchor(html));
          break;
        case IMG:
          out.add(image(html));
          break;
        case SPAN:
          String label = classValue(html, "label-");
          if (label != null) {
            out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.INLINE).setAttribute("label", label)));
          } else {
            for (HTMLNode n : html.getNodes()) inline(n, out);
          }
          break;
        case VAR:
          String name = html.getAttribute("data-name");
          if (name != null && !name.isEmpty()) {
            PSMLElement placeholder = new PSMLElement(PSMLElement.Name.PLACEHOLDER).setAttribute("name", name);
            String text = normalize(html.getText());
            if (!text.equals(name)) inlineChildren(html, placeholder);
            out.add(placeholder);
          } else {
            out.add(inlineChildren(html, new PSMLElement(PSMLElement.Name.ITALIC)));
          }
          break;
        case HEAD:
        case META:
        case TITLE:
        case STYLE:
        case LINK:
        case SOURCE:
        case IFRAME:
        case VIDEO:
        case AUDIO:
          // Ignored
          break;
        default:
          // Transparent
          for (HTMLNode n : html.getNodes()) inline(n, out);
      }
    }

    private PSMLElement anchor(HTMLElement a) {
      String href = a.getAttribute("href");
      if (href == null || href.isEmpty()) {
        String name = a.getAttribute("id");
        if (name == null || name.isEmpty()) name = a.getAttribute("name");
        PSMLElement anchor = new PSMLElement(PSMLElement.Name.ANCHOR);
        if (name != null) anchor.setAttribute("name", name);
        return inlineChildren(a, anchor);
      }
      if (SCHEME.matcher(href).matches() || href.startsWith("#") || href.startsWith("//")) {
        return inlineChildren(a, new PSMLElement(PSMLElement.Name.LINK).setAttribute("href", href));
      }
      PSMLElement xref = new PSMLElement(PSMLElement.Name.XREF);
      setXrefAttributes(xref, href, normalize(a.getText()).trim());
      return inlineChildren(a, xref);
    }

    private static void setXrefAttributes(PSMLElement xref, String ref, String title) {
      int hash = ref.indexOf('#');
      String url = hash < 0 ? ref : ref.substring(0, hash);
      String fragment = hash < 0 ? "default" : ref.substring(hash + 1);
      // Use the PageSeeder defaults for manual links
      xref.setAttribute("display", "manual");
      xref.setAttribute("reverselink", "true");
      xref.setAttribute("title", title);
      xref.setAttribute("href", url);
      xref.setAttribute("frag", fragment);
    }

    private static PSMLElement image(HTMLElement img) {
      PSMLElement image = new PSMLElement(PSMLElement.Name.IMAGE);
      String alt = img.getAttribute("alt");
      if (alt != null) image.setAttribute("alt", alt);
      image.setAttribute("src", img.getAttribute("src") != null ? img.getAttribute("src") : "");
      copy(img, image, "width", "height");
      return image;
    }

    // Utilities
    // ---------------------------------------------------------------------------------------------

    private String nextSectionId() {
      return "section-" + (++this.sectionCount);
    }

    private String nextFragmentId() {
      return Integer.toString(++this.fragmentCount);
    }

    /**
     * Collapses sequences of whitespace into a single space.
     */
    private static String normalize(String text) {
      return WHITESPACE.matcher(text).replaceAll(" ");
    }

    private static boolean isWhitespace(String text) {
      return text.trim().isEmpty();
    }

    /**
     * Removes leading and trailing whitespace from inline content.
     */
    private static void trim(List<PSMLNode> nodes) {
      trimStart(nodes);
      trimEnd(nodes);
    }

    private static boolean trimStart(List<PSMLNode> nodes) {
      while (!nodes.isEmpty()) {
        PSMLNode first = nodes.get(0);
        if (first instanceof PSMLText) {
          String text = stripLeading(first.getText());
          if (!text.isEmpty()) {
            ((PSMLText) first).setText(text);
            return true;
          }
          nodes.remove(0);
        } else if (first instanceof PSMLElement && isTrimmable((PSMLElement) first)) {
          if (trimStart(((PSMLElement) first).getNodes())) return true;
          return false;
        } else {
          return true;
        }
      }
      return false;
    }

    private static boolean trimEnd(List<PSMLNode> nodes) {
      while (!nodes.isEmpty()) {
        int last = nodes.size() - 1;
        PSMLNode node = nodes.get(last);
        if (node instanceof PSMLText) {
          String text = stripTrailing(node.getText());
          if (!text.isEmpty()) {
            ((PSMLText) node).setText(text);
            return true;
          }
          nodes.remove(last);
        } else if (node instanceof PSMLElement && isTrimmable((PSMLElement) node)) {
          if (trimEnd(((PSMLElement) node).getNodes())) return true;
          return false;
        } else {
          return true;
        }
      }
      return false;
    }

    /**
     * Inline elements with text content which whitespace can be trimmed.
     */
    private static boolean isTrimmable(PSMLElement element) {
      return element.getElement().isInline()
          && !element.isAnyElement(PSMLElement.Name.BR, PSMLElement.Name.IMAGE, PSMLElement.Name.MONOSPACE);
    }

    private static String stripLeading(String text) {
      int i = 0;
      while (i < text.length() && Character.isWhitespace(text.charAt(i))) i++;
      return text.substring(i);
    }

    private static String stripTrailing(String text) {
      int i = text.length();
      while (i > 0 && Character.isWhitespace(text.charAt(i - 1))) i--;
      return text.substring(0, i);
    }

    private static boolean isBlock(HTMLElement element) {
      Name name = element.getElement();
      if (name == Name.UNKNOWN) {
        for (HTMLElement child : childElements(element)) {
          if (isBlock(child)) return true;
        }
        return false;
      }
      return !name.isInline();
    }

    private static boolean isTransparentStructure(HTMLElement element) {
      return element.isAnyElement(Name.ARTICLE, Name.MAIN, Name.BODY, Name.HEADER, Name.FOOTER, Name.DIV, Name.UNKNOWN)
          && classValue(element, "label-") == null && !hasClass(element, "blockxref");
    }

    /**
     * Indicates whether the element is a generic container which content can be used as a fragment.
     */
    private static boolean isContainer(HTMLElement element) {
      if (element.isElement(Name.SECTION)) return true;
      if (element.isAnyElement(Name.DIV, Name.ASIDE, Name.HEADER, Name.FOOTER, Name.NAV, Name.UNKNOWN)) {
        return classValue(element, "label-") == null && !hasClass(element, "blockxref")
            && element.getAttribute("label") == null && element.getAttribute("data-label") == null;
      }
      return false;
    }

    private static boolean containsSection(HTMLElement element) {
      for (HTMLElement child : childElements(element)) {
        if (child.isElement(Name.SECTION)) return true;
        if (isTransparentStructure(child) && containsSection(child)) return true;
      }
      return false;
    }

    private static boolean hasContent(List<HTMLNode> nodes) {
      for (HTMLNode node : nodes) {
        if (node instanceof HTMLElement) {
          if (!((HTMLElement) node).isElement(Name.HR)) return true;
        } else if (!isWhitespace(node.getText())) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns the element if it is the only significant node in the list and it matches the name.
     */
    private static @Nullable HTMLElement soleElement(List<HTMLNode> nodes, Name name) {
      HTMLElement found = null;
      for (HTMLNode node : nodes) {
        if (node instanceof HTMLElement) {
          if (found != null || !((HTMLElement) node).isElement(name)) return null;
          found = (HTMLElement) node;
        } else if (!isWhitespace(node.getText())) {
          return null;
        }
      }
      return found;
    }

    private static List<HTMLElement> childElements(HTMLElement element) {
      List<HTMLElement> children = new ArrayList<>();
      for (HTMLNode node : element.getNodes()) {
        if (node instanceof HTMLElement) children.add((HTMLElement) node);
      }
      return children;
    }

    private static @Nullable HTMLElement firstChild(HTMLElement element, Name name) {
      for (HTMLElement child : childElements(element)) {
        if (child.isElement(name)) return child;
      }
      return null;
    }

    private static @Nullable HTMLElement firstDescendant(HTMLElement element, Name name) {
      for (HTMLElement child : childElements(element)) {
        if (child.isElement(name)) return child;
        HTMLElement found = firstDescendant(child, name);
        if (found != null) return found;
      }
      return null;
    }

    private static boolean hasClass(HTMLElement element, String className) {
      String classes = element.getAttribute("class");
      if (classes == null) return false;
      for (String c : WHITESPACE.split(classes.trim())) {
        if (c.equals(className)) return true;
      }
      return false;
    }

    /**
     * Returns the remainder of the first class starting with the specified prefix.
     */
    private static @Nullable String classValue(HTMLElement element, String prefix) {
      String classes = element.getAttribute("class");
      if (classes == null) return null;
      for (String c : WHITESPACE.split(classes.trim())) {
        if (c.startsWith(prefix) && c.length() > prefix.length()) return c.substring(prefix.length());
      }
      return null;
    }

    private static PSMLElement withRole(PSMLElement to, HTMLElement from) {
      String role = from.getAttribute("class");
      if (role != null && !role.trim().isEmpty()) to.setAttribute("role", role.trim());
      return to;
    }

    private static void copy(HTMLElement from, PSMLElement to, String... attributes) {
      for (String name : attributes) {
        String value = from.getAttribute(name);
        if (value != null && !value.isEmpty()) to.setAttribute(name, value);
      }
    }

    private static @Nullable String toListType(@Nullable String type) {
      if (type == null) return null;
      switch (type) {
        case "a": return "loweralpha";
        case "A": return "upperalpha";
        case "i": return "lowerroman";
        case "I": return "upperroman";
        default: return null;
      }
    }
  }

}
