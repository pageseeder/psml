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
package org.pageseeder.psml.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A PSML element.
 *
 * <p>Note: the setters and adders are chainable.
 *
 * @see <a href="https://dev.pageseeder.com/api/psml/element_reference.html">PSML element reference</a>
 *
 * @author Christophe Lauret
 */
public class PSMLElement implements PSMLNode {

  /**
   * An enumeration of possible element names in PSML.
   *
   * <p>Note: the capitalization rule is based on whether the element name
   * is hyphenated.
   */
  public enum Name {

    /**
     * &lt;anchor&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-anchor.html">anchor element</a>
     */
    Anchor("anchor", true, "name"),

    /**
     * &lt;author&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-author.html">author element</a>
     */
    Author("author", "firstname", "id", "surname"),

    /**
     * &lt;block&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-block.html">block element</a>
     */
    Block("block", "label"),

    /**
     * &lt;blockxref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-blockxref.html">blockxref element</a>
     */
    Blockxref("blockxref", "display","docid","documenttype","external","frag","href","id","labels","level","mediatype","reverselink","reversetitle","reversetype","title","type","unresolved","uriid","urititle"),

    /**
     * &lt;bold&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-bold.html">bold element</a>
     */
    Bold("bold", true),

    /**
     * &lt;br&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-br.html">br element</a>
     */
    Br("br", true),

    /**
     * &lt;caption&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-caption.html">caption element</a>
     */
    Caption("caption"),

    /**
     * &lt;cell&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-cell.html">cell element</a>
     */
    Cell("cell", "align", "colspan", "role", "rowspan", "valign", "width"),

    /**
     * &lt;col&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-col.html">col element</a>
     */
    Col("col", "align", "part", "role", "span", "width"),

    /**
     * &lt;compare&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-compare.html">compare element</a>
     */
    Compare("compare"),

    /**
     * &lt;compareto&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-compareto.html">compareto element</a>
     */
    Compareto("compareto", "date", "version", "docid"),

    /**
     * &lt;content&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-content.html">content element</a>
     */
    Content("content"),

    /**
     * &lt;description&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-description.html">description element</a>
     */
    Description("description"),

    /**
     * &lt;diff&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-diff.html">diff element</a>
     */
    Diff("diff"),

    /**
     * &lt;displaytitle&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-displaytitle.html">displaytitle element</a>
     */
    Displaytitle("displaytitle"),

    /**
     * &lt;document&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-document.html">document element</a>
     */
    Document("document", "date", "edit", "id", "level", "lockstructure", "schemaversion", "status", "type", "version"),

    /**
     * &lt;documentinfo&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-documentinfo.html">documentinfo element</a>
     */
    Documentinfo("documentinfo"),

    /**
     * &lt;fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragment.html">fragment element</a>
     */
    Fragment("fragment", "id", "type"),

    /**
     * &lt;fragmentinfo&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragmentinfo.html">fragmentinfo element</a>
     */
    Fragmentinfo("fragmentinfo"),

    /**
     * &lt;fullname&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fullname.html">fullname element</a>
     */
    Fullname("fullname"),

    /**
     * &lt;hcell&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-hcell.html">hcell element</a>
     *
     * @deprecated
     */
    Hcell("hcell", "align", "alignment", "colspan", "role", "rowspan", "valign", "width"),

    /**
     * &lt;heading&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-heading.html">heading element</a>
     */
    Heading("heading"),

    /**
     * &lt;image&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-image.html">image element</a>
     */
    Image("image", true, "src", "docid", "uriid", "alt", "height", "unresolved", "width"),

    /**
     * &lt;inline&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-inline.html">inline element</a>
     */
    Inline("inline", true, "label"),

    /**
     * &lt;italic&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-italic.html">italic element</a>
     */
    Italic("italic", true),

    /**
     * &lt;item&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-item.html">item element</a>
     */
    Item("item"),

    /**
     * &lt;item&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-item.html">item element</a>
     */
    Labels("labels"),

    /**
     * &lt;link&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-link.html">link element</a>
     */
    Link("link", true, "href", "role"),

    /**
     * &lt;list&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-list.html">list element</a>
     */
    List("list", "type", "role"),

    /**
     * &lt;locator&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-locator.html">locator element</a>
     */
    Locator("locator", "editid", "fragment", "id", "modified"),

    /**
     * &lt;markdown&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-markdown.html">markdown element</a>
     */
    Markdown("markdown"),

    /**
     * &lt;media-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-media-fragment.html">media-fragment element</a>
     */
    MediaFragment("media-fragment", "id", "mediatype", "type"),

    /**
     * &lt;metadata&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-metadata.html">metadata element</a>
     */
    Metadata("metadata"),

    /**
     * &lt;monospace&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-monospace.html">monospace element</a>
     */
    Monospace("monospace", true),

    /**
     * &lt;nlist&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-nlist.html">nlist element</a>
     */
    Nlist("nlist", "start", "type", "role"),

    /**
     * &lt;note&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-note.html">note element</a>
     */
    Note("note", "id", "modified", "title"),

    /**
     * &lt;notes&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-notes.html">notes element</a>
     */
    Notes("notes"),

    /**
     * &lt;para&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-para.html">para element</a>
     */
    Para("para", "indent", "numbered", "prefix"),

    /**
     * &lt;preformat&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-preformat.html">preformat element</a>
     */
    Preformat("preformat"),

    /**
     * &lt;properties&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-properties.html">properties element</a>
     */
    Properties("properties"),

    /**
     * &lt;properties-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-properties-fragment.html">properties-fragment element</a>
     */
    PropertiesFragment("properties-fragment"),

    /**
     * &lt;property&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-property.html">property element</a>
     */
    Property("property", "count", "datatype", "name", "title", "value"),

    /**
     * &lt;reversexrefs&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-reversexrefs.html">reversexrefs element</a>
     */
    Reversexref("reversexref", "docid","documenttype","forwardtitle","forwardtype","frag","href","id","labels","level","mediatype","title","type","uriid","urititle"),

    /**
     * &lt;reversexrefs&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-reversexrefs.html">reversexrefs element</a>
     */
    Reversexrefs("reversexrefs", "limitreached"),

    /**
     * &lt;row&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-row.html">row element</a>
     */
    Row("row", "align", "part", "role"),

    /**
     * &lt;section&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-section.html">section element</a>
     */
    Section("section", "edit", "fragmenttype", "id", "lockstructure", "overwrite", "title"),

    /**
     * &lt;sub&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-sub.html">sub element</a>
     */
    Sub("sub", true),

    /**
     * &lt;sup&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-sup.html">sup element</a>
     */
    Sup("sup", true),

    /**
     * &lt;table&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-table.html">table element</a>
     */
    Table("table", "height", "role", "summary", "width"),

    /**
     * &lt;title&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-title.html">title element</a>
     */
    Title("title"),

    /**
     * &lt;toc&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-toc.html">toc element</a>
     */
    Toc("toc"),

    /**
     * &lt;tocref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-tocref.html">tocref element</a>
     */
    Tocref("tocref", "level", "idref", "canonical", "prefix"),

    /**
     * &lt;underline&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-underline.html">underline element</a>
     */
    Underline("underline", true),

    /**
     * &lt;uri&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-uri.html">uri element</a>
     */
    Uri("uri", "created", "decodedpath", "docid", "documenttype", "external", "folder", "host", "id", "mediatype", "modified", "path", "port", "scheme", "title"),

    /**
     * &lt;value&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-value.html">value element</a>
     */
    Value("value"),

    /**
     * &lt;version&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-version.html">version element</a>
     */
    Version("version", "created", "id", "name"),

    /**
     * &lt;versions&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-versions.html">versions element</a>
     */
    Versions("versions"),

    /**
     * &lt;xref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-xref.html">xref element</a>
     */
    Xref("xref", true, "display","docid","documenttype","external","frag","href","id","labels","level","mediatype","reverselink","reversetitle","reversetype","title","type","unresolved","uriid","urititle"),

    /**
     * &lt;xref-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-xref-fragment.html">xref-fragment element</a>
     */
    XrefFragment("xref-fragment", "id", "type"),

    /**
     * This element is used for any unrecognized PSML element.
     */
    Unknown("unknown");

    /**
     * The actual element name.
     */
    private final String _element;

    /**
     * Indicates that this element is an inline element.
     *
     * <p>It is defined as an inline element if it can have sibling text nodes
     * that are significant (i.e other than white spaces)
     */
    private final boolean _inline;

    /**
     * The possible attributes on this element.
     */
    private final List<String> _attributes;

    /**
     * Creates a new PSML name for the
     *
     * @param name       the name of the element
     * @param inline     whether it is an inline element
     * @param attributes an array of possible attribute names
     */
    private Name(String name, boolean inline, String... attributes) {
      this._element = name;
      this._inline = inline;
      this._attributes = Arrays.asList(attributes);
    }

    /**
     * Creates a new PSML name for the
     *
     * @param name       the name of the element
     * @param attributes an array of possible attribute names
     */
    private Name(String name, String... attributes) {
      this(name, false, attributes);
    }

    /**
     * @return The element name.
     */
    public String element() {
      return this._element;
    }

    /**
     * @return <code>true</code> if considered an inline element.
     */
    public boolean isInline() {
      return this._inline;
    }

    /**
     * @return The list of possible attribute names for this element.
     */
    public List<String> attributes() {
      return this._attributes;
    }

    public static Name forElement(String name) {
      for (Name element : values()) {
        if (element._element.equals(name)) return element;
      }
      return Name.Unknown;
    }

  }

  /**
   *
   */
  private Name name = Name.Unknown;

  private Map<String, String> attributes;

  public List<PSMLNode> nodes;

  public PSMLElement(Name name) {
    this.name = name;
  }

  /**
   * @return the name of the element.
   */
  public Name getElement() {
    return this.name;
  }



  /**
   * Changes the name of this element.
   *
   * @param name The name of the element.
   *
   * @return this element
   */
  public PSMLElement setName(Name name) {
    this.name = name;
    return this;
  }

  /**
   * Indicates whether the name of this element matches the specified name.
   *
   * <p>A shorthand method for:
   * <pre>
   *  return this.element == name;
   * </pre>
   *
   * @param name the name to test against.
   *
   * @return <code>true</code> if and only if it is the same.
   */
  public boolean isElement(Name name) {
    return this.name == name;
  }

  /**
   * Returns the attribute value for the specified attribute.
   *
   * @param name The name of the attribute.
   *
   * @return The corresponding value or <code>null</code>.
   */
  public String getAttribute(String name) {
    if (this.attributes == null) return null;
    else return this.attributes.get(name);
  }

  /**
   * Sets the attribute value of the specified attribute.
   *
   * <p>Implementation note: this method will initialize the attribute map
   * if necessary.
   *
   * @param name  The name of the attribute.
   * @param value The string value of the attribute.
   *
   * @return this element
   */
  public PSMLElement setAttribute(String name, String value) {
    if (this.attributes == null) {
      this.attributes = new HashMap<>(4);
    }
    this.attributes.put(name, value);
    return this;
  }

  /**
   * Sets the attribute value of the specified attribute.
   *
   * <p>Implementation note: this method will initialize the attribute map
   * if necessary.
   *
   * @param name  The name of the attribute.
   * @param value The int value of the attribute.
   *
   * @return this element
   */
  public PSMLElement setAttribute(String name, int value) {
    setAttribute(name, Integer.toString(value));
    return this;
  }

  /**
   * Sets the attribute value of the specified attribute.
   *
   * <p>Implementation note: this method will initialize the attribute map
   * if necessary.
   *
   * @param name  The name of the attribute.
   * @param value The boolean value of the attribute.
   *
   * @return this element
   */
  public PSMLElement setAttribute(String name, boolean value) {
    setAttribute(name, Boolean.toString(value));
    return this;
  }

  /**
   * Adds a child node to this element.
   *
   * <p>If the element already has child nodes, the specified node is appended
   * to the list of nodes.
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @param name The name of the attribute.
   *
   * @return this element
   */
  public PSMLElement addNode(PSMLNode node) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.add(node);
    return this;
  }

  /**
   * Adds a list of child nodes to this element.
   *
   * <p>If the element already has child nodes, the specified nodes are
   * appended to the list of nodes.
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @param name The name of the attribute.
   *
   * @return this element
   */
  public PSMLElement addNodes(List<? extends PSMLNode> nodes) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.addAll(nodes);
    return this;
  }

  /**
   * Adds a list of child nodes to this element.
   *
   * <p>If the element already has child nodes, the specified nodes are
   * appended to the list of nodes.
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @param name The name of the attribute.
   *
   * @return this element
   */
  public PSMLElement addNodes(PSMLNode... nodes) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    for (PSMLNode node : nodes ){
      this.nodes.add(node);
    }
    return this;
  }

  /**
   * Set the text node for this element.
   *
   * <p>If the element already has child nodes, they are removed and replaced
   * with a single text node.
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @param text The text to set.
   *
   * @return this element
   */
  public PSMLElement setText(String text) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>(1);
    } else {
      this.nodes.clear();
    }
    this.nodes.add(new PSMLText(text));
    return this;
  }

  /**
   * Add a text node to this element.
   *
   * <p>If the element already has child nodes, a new text node is appended
   * to the list.
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @param name The text to add to this element.
   *
   * @return this element
   */
  public PSMLElement addText(String text) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.add(new PSMLText(text));
    return this;
  }

  /**
   * Returns the actual list of nodes in this element (not a copy)
   *
   * <p>Implementation note: this method will initialize the node list if
   * necessary.
   *
   * @return the actual list of nodes in this element.
   */
  public List<PSMLNode> getNodes() {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    return this.nodes;
  }

  /**
   * Indicates whether the element has nay child node (attribute nodes are ignored)
   *
   * @return <code>true</code> is the node list is uninitialized or empty.
   */
  public boolean isEmpty() {
    return this.nodes == null || this.nodes.size() == 0;
  }

  @Override
  public String getText() {
    if (this.nodes == null) return "";
    StringBuilder out = new StringBuilder();
    for (PSMLNode node : this.nodes) {
      out.append(node.getText());
    }
    return out.toString();
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    boolean hasChildren = !this.name.isInline() && hasOnlyElementAsChildren();
    xml.openElement(this.name.element(), hasChildren);
    // Se attributes if any
    if (this.attributes != null) {
      for (Entry<String, String> e : this.attributes.entrySet()) {
        xml.attribute(e.getKey(), e.getValue());
      }
    }
    // Process nodes if any
    if (this.nodes != null) {
      for (PSMLNode node : this.nodes) {
        node.toXML(xml);
      }
    }
    xml.closeElement();
  }

  @Override
  public String toString() {
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    try {
      toXML(xml);
    } catch (IOException ex) {
      // Will never happen
    }
    return xml.toString();
  }

  /**
   * @return <code>true</code> if and only if the this element has child nodes
   *         and they are not text nodes.
   */
  private boolean hasOnlyElementAsChildren() {
    if (this.nodes == null || this.nodes.isEmpty()) return false;
    for (PSMLNode node : this.nodes) {
      if (node instanceof PSMLText) return false;
    }
    return true;
  }

}
