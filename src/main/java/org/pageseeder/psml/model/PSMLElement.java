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

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A PSML element.
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
    Anchor("anchor", "name"),

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
    Bold("bold"),

    /**
     * &lt;br&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-br.html">br element</a>
     */
    Br("br"),

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

    Document("document"),

    Element("element"),

    Fragment("fragment"),

    Heading("heading"),

    Image("image"),

    Inline("inline"),

    Italic("italic"),

    Item("item"),

    Link("link"),

    List("list"),

    MediaFragment("media-fragment"),

    Metadata("metadata"),

    Monospace("monospace"),

    Nlist("nlist"),

    Para("para"),

    Preformat("preformat"),

    Properties("properties"),

    PropertiesFragment("properties-fragment"),

    Property("property"),

    Row("row"),

    Section("section"),

    Sub("sub"),

    Sup("sup"),

    Table("table"),

    Toc("toc"),

    Underline("underline"),

    Xref("xref"),

    XrefFragment("xref-fragment"),

    XrefType("xref-type"),

    Unknown("unknown");


    /**
     * The actual element name.
     */
    private final String _element;

    /**
     * The possible attributes on this element.
     */
    private final List<String> _attributes;

    /**
     * Creates a new PSML name for the
     *
     * @param name       the name of the element
     * @param attributes an array of possible attribute names
     */
    private Name(String name, String... attributes) {
      this._element = name;
      this._attributes = Arrays.asList(attributes);
    }

    /**
     * @return The element name.
     */
    public String element() {
      return this._element;
    }

    /**
     * @return The list of possible attribute names for this element.
     */
    public List<String> attributes() {
      return this._attributes;
    }

  }

  /**
   *
   */
  private Name element = Name.Unknown;

  private Map<String, String> attributes;

  public List<PSMLNode> nodes;

  public PSMLElement(Name name) {
    this.element = name;
  }

  /**
   * @return the name of the element.
   */
  public Name getElement() {
    return this.element;
  }

  /**
   * Changes the name of this element.
   *
   * @param name The name of the element.
   */
  public void setElement(Name name) {
    this.element = name;
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
    return this.element == name;
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
   * <p>Implementation node: this method will initialize the attribute map
   * if necessary.
   *
   * @param name The name of the attribute.
   *
   * @return The corresponding value or <code>null</code>.
   */
  public void setAttribute(String name, String value) {
    if (this.attributes == null) {
      this.attributes = new HashMap<>(4);
    }
    this.attributes.put(name, value);
  }

  /**
   * Adds a child node to this element.
   *
   * <p>If the element already has child nodes, the specified node is appended
   * to the list of nodes.
   *
   * <p>Implementation node: this method will initialize the node list if
   * necessary.
   *
   * @param name The name of the attribute.
   *
   * @return The corresponding value or <code>null</code>.
   */
  public void addNode(PSMLNode node) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.add(node);
  }

  /**
   * Adds a list of child nodes to this element.
   *
   * <p>If the element already has child nodes, the specified nodes are
   * appended to the list of nodes.
   *
   * <p>Implementation node: this method will initialize the node list if
   * necessary.
   *
   * @param name The name of the attribute.
   *
   * @return The corresponding value or <code>null</code>.
   */
  public void addNodes(List<? extends PSMLNode> nodes) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.addAll(nodes);
  }

  /**
   * Returns the actual list of nodes in this element (not a copy)
   *
   * <p>Implementation node: this method will initialize the node list if
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

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    boolean hasChildren = hasOnlyElementAsChildren();
    xml.openElement(this.element.element(), hasChildren);
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
