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
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
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
 *
 * @version 1.6.0
 * @since 1.0
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
    ANCHOR("anchor", true, "name"),

    /**
     * &lt;author&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-author.html">author element</a>
     */
    AUTHOR("author", "firstname", "id", "surname"),

    /**
     * &lt;block&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-block.html">block element</a>
     */
    BLOCK("block", "label"),

    /**
     * &lt;blockxref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-blockxref.html">blockxref element</a>
     */
    BLOCKXREF("blockxref", "display","docid","documenttype","external","frag","href","id","labels","level","mediatype","reverselink","reversetitle","reversetype","title","type","unresolved","uriid","urititle"),

    /**
     * &lt;bold&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-bold.html">bold element</a>
     */
    BOLD("bold", true),

    /**
     * &lt;br&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-br.html">br element</a>
     */
    BR("br", true),

    /**
     * &lt;caption&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-caption.html">caption element</a>
     */
    CAPTION("caption"),

    /**
     * &lt;cell&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-cell.html">cell element</a>
     */
    CELL("cell", "align", "colspan", "role", "rowspan", "valign", "width"),

    /**
     * &lt;col&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-col.html">col element</a>
     */
    COL("col", "align", "part", "role", "span", "width"),

    /**
     * &lt;compare&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-compare.html">compare element</a>
     */
    COMPARE("compare"),

    /**
     * &lt;compareto&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-compareto.html">compareto element</a>
     */
    COMPARETO("compareto", "date", "version", "docid"),

    /**
     * &lt;content&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-content.html">content element</a>
     */
    CONTENT("content"),

    /**
     * &lt;description&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-description.html">description element</a>
     */
    DESCRIPTION("description"),

    /**
     * &lt;diff&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-diff.html">diff element</a>
     */
    DIFF("diff"),

    /**
     * &lt;displaytitle&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-displaytitle.html">displaytitle element</a>
     */
    DISPLAYTITLE("displaytitle"),

    /**
     * &lt;document&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-document.html">document element</a>
     */
    DOCUMENT("document", "date", "edit", "id", "level", "lockstructure", "schemaversion", "status", "type", "version"),

    /**
     * &lt;documentinfo&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-documentinfo.html">documentinfo element</a>
     */
    DOCUMENTINFO("documentinfo"),

    /**
     * &lt;fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragment.html">fragment element</a>
     */
    FRAGMENT("fragment", "id", "type"),

    /**
     * &lt;fragment-ref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragment-ref.html">fragment-ref element</a>
     */
    FRAGMENT_REF("fragment-ref", "id"),

    /**
     * &lt;fragments&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragments.html">fragments element</a>
     */
    FRAGMENTS("overwrite"),

    /**
     * &lt;fragmentinfo&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fragmentinfo.html">fragmentinfo element</a>
     */
    FRAGMENTINFO("fragmentinfo"),

    /**
     * &lt;fullname&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-fullname.html">fullname element</a>
     */
    FULLNAME("fullname"),

    /**
     * &lt;hcell&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-hcell.html">hcell element</a>
     *
     * @deprecated Use cell instead
     */
    @Deprecated
    HCELL("hcell", "align", "alignment", "colspan", "role", "rowspan", "valign", "width"),

    /**
     * &lt;heading&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-heading.html">heading element</a>
     */
    HEADING("heading"),

    /**
     * &lt;image&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-image.html">image element</a>
     */
    IMAGE("image", true, "src", "docid", "uriid", "alt", "height", "unresolved", "width"),

    /**
     * &lt;inline&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-inline.html">inline element</a>
     */
    INLINE("inline", true, "label"),

    /**
     * &lt;italic&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-italic.html">italic element</a>
     */
    ITALIC("italic", true),

    /**
     * &lt;item&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-item.html">item element</a>
     */
    ITEM("item"),

    /**
     * &lt;item&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-item.html">item element</a>
     */
    LABELS("labels"),

    /**
     * &lt;link&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-link.html">link element</a>
     */
    LINK("link", true, "href", "role"),

    /**
     * &lt;list&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-list.html">list element</a>
     */
    LIST("list", "type", "role"),

    /**
     * &lt;locator&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-locator.html">locator element</a>
     */
    LOCATOR("locator", "editid", "fragment", "id", "modified"),

    /**
     * &lt;markdown&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-markdown.html">markdown element</a>
     */
    MARKDOWN("markdown"),

    /**
     * &lt;media-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-media-fragment.html">media-fragment element</a>
     */
    MEDIA_FRAGMENT("media-fragment", "id", "mediatype", "type"),

    /**
     * &lt;metadata&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-metadata.html">metadata element</a>
     */
    METADATA("metadata"),

    /**
     * &lt;monospace&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-monospace.html">monospace element</a>
     */
    MONOSPACE("monospace", true),

    /**
     * &lt;nlist&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-nlist.html">nlist element</a>
     */
    NLIST("nlist", "start", "type", "role"),

    /**
     * &lt;note&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-note.html">note element</a>
     */
    NOTE("note", "id", "modified", "title"),

    /**
     * &lt;notes&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-notes.html">notes element</a>
     */
    NOTES("notes"),

    /**
     * &lt;para&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-para.html">para element</a>
     */
    PARA("para", "indent", "numbered", "prefix"),

    /**
     * &lt;placeholder&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-placeholder.html">placeholder element</a>
     */
    PLACEHOLDER("name", true, "unresolved"),

    /**
     * &lt;preformat&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-preformat.html">preformat element</a>
     */
    PREFORMAT("preformat"),

    /**
     * &lt;properties&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-properties.html">properties element</a>
     */
    PROPERTIES("properties"),

    /**
     * &lt;properties-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-properties-fragment.html">properties-fragment element</a>
     */
    PROPERTIES_FRAGMENT("properties-fragment"),

    /**
     * &lt;property&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-property.html">property element</a>
     */
    PROPERTY("property", "count", "datatype", "name", "title", "value"),

    /**
     * &lt;publication&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-publication.html">publication element</a>
     */
    PUBLICATION("publication", "defaultgroupid", "hostid", "id", "rooturiid", "title", "type"),

    /**
     * &lt;reversexrefs&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-reversexrefs.html">reversexrefs element</a>
     */
    REVERSEXREF("reversexref", "docid","documenttype","forwardtitle","forwardtype","frag","href","id","labels","level","mediatype","title","type","uriid","urititle"),

    /**
     * &lt;reversexrefs&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-reversexrefs.html">reversexrefs element</a>
     */
    REVERSEXREFS("reversexrefs", "limitreached"),

    /**
     * &lt;row&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-row.html">row element</a>
     */
    ROW("row", "align", "part", "role"),

    /**
     * &lt;section&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-section.html">section element</a>
     */
    SECTION("section", "edit", "fragmenttype", "id", "lockstructure", "overwrite", "title"),

    /**
     * &lt;section-ref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-section-ref.html">section-ref element</a>
     */
    SECTION_REF("section-ref", "id", "title"),

    /**
     * &lt;structure&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-structure.html">structure element</a>
     */
    STRUCTURE("structure"),

    /**
     * &lt;sub&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-sub.html">sub element</a>
     */
    SUB("sub", true),

    /**
     * &lt;sup&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-sup.html">sup element</a>
     */
    SUP("sup", true),

    /**
     * &lt;table&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-table.html">table element</a>
     */
    TABLE("table", "height", "role", "summary", "width"),

    /**
     * &lt;title&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-title.html">title element</a>
     */
    TITLE("title"),

    /**
     * &lt;toc&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-toc.html">toc element</a>
     */
    TOC("toc"),

    /**
     * &lt;tocref&lt; element
     *
     * @deprecated Replaced by {@link #TOC_PART}
     */
    @Deprecated
    TOCREF("tocref", "level", "idref", "canonical", "prefix"),

    /**
     * &lt;toc-part&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-toc-part.html">toc-part element</a>
     */
    TOC_PART("toc-part", "level", "idref", "canonical", "prefix", "title"),

    /**
     * &lt;toc-ref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-toc-ref.html">toc-ref element</a>
     */
    TOC_REF("toc-ref"),

    /**
     * &lt;toc-tree&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-toc-tree.html">toc-tree element</a>
     */
    TOC_TREE("toc-tree", "title"),

    /**
     * &lt;underline&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-underline.html">underline element</a>
     */
    UNDERLINE("underline", true),

    /**
     * &lt;uri&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-uri.html">uri element</a>
     */
    URI("uri", "created", "decodedpath", "docid", "documenttype", "external", "folder", "host", "id", "mediatype", "modified", "path", "port", "scheme", "title"),

    /**
     * &lt;value&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-value.html">value element</a>
     */
    VALUE("value"),

    /**
     * &lt;version&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-version.html">version element</a>
     */
    VERSION("version", "created", "id", "name"),

    /**
     * &lt;versions&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-versions.html">versions element</a>
     */
    VERSIONS("versions"),

    /**
     * &lt;xref&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-xref.html">xref element</a>
     */
    XREF("xref", true, "display","docid","documenttype","external","frag","href","id","labels","level","mediatype","reverselink","reversetitle","reversetype","title","type","unresolved","uriid","urititle"),

    /**
     * &lt;xref-fragment&lt; element
     *
     * @see <a href="https://dev.pageseeder.com/api/psml/element_reference/element-xref-fragment.html">xref-fragment element</a>
     */
    XREF_FRAGMENT("xref-fragment", "id", "type"),

    /**
     * This element is used for any unrecognized PSML element.
     */
    UNKNOWN("unknown");

    /**
     * The actual element name.
     */
    private final String element;

    /**
     * Indicates that this element is an inline element.
     *
     * <p>It is defined as an inline element if it can have sibling text nodes
     * that are significant (i.e other than white spaces)
     */
    private final boolean inline;

    /**
     * The possible attributes on this element.
     */
    private final List<String> attributes;

    /**
     * Creates a new PSML name for the
     *
     * @param name       the name of the element
     * @param inline     whether it is an inline element
     * @param attributes an array of possible attribute names
     */
    Name(String name, boolean inline, String... attributes) {
      this.element = name;
      this.inline = inline;
      this.attributes = Arrays.asList(attributes);
    }

    /**
     * Creates a new PSML name for the
     *
     * @param name       the name of the element
     * @param attributes an array of possible attribute names
     */
    Name(String name, String... attributes) {
      this(name, false, attributes);
    }

    /**
     * @return The element name.
     */
    public String element() {
      return this.element;
    }

    /**
     * @return <code>true</code> if considered an inline element.
     */
    public boolean isInline() {
      return this.inline;
    }

    /**
     * @return The list of possible attribute names for this element.
     */
    public List<String> attributes() {
      return this.attributes;
    }

    public static Name forElement(String name) {
      for (Name element : values()) {
        if (element.element.equals(name)) return element;
      }
      return Name.UNKNOWN;
    }

  }

  /**
   * Represents the name of the PSML element.
   *
   * <p>The name serves as an identifier for the element, allowing interactions
   * such as retrieval, comparison, or updates. By default, this field is
   * initialized to {@code Name.Unknown}.
   */
  private Name name = Name.UNKNOWN;

  /**
   * A map of attributes associated with this element. Keys represent attribute
   * names and values represent their corresponding string values.
   *
   * <p>This map may be null if no attributes have been defined for the element.
   * It is typically initialized lazily when an attribute is added to the element.
   */
  private @Nullable Map<String, String> attributes;

  /**
   * The list of child nodes associated with this element.
   *
   * <p>Each child node implements the {@link PSMLNode} interface. The list can contain
   * various types of nodes, including element nodes and text nodes.
   *
   * <p>This field supports lazy initialization and may be {@code null} if no nodes
   * have been added to this element. Use methods like {@code addNode}, {@code addNodes},
   * or {@code setText} in the containing class to populate it.
   *
   * <p>Modifying this field directly outside provided methods is discouraged
   * to prevent inconsistent states.
   */
  public @Nullable List<PSMLNode> nodes;

  /**
   * Constructs a new PSMLElement with the specified name.
   *
   * @param name The name of this element. Must not be null.
   */
  public PSMLElement(Name name) {
    this.name = Objects.requireNonNull(name);
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
    this.name = Objects.requireNonNull(name);
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
   * Indicates whether the name of this element matches the specified name.
   *
   * @param names Names of any of the elements to match
   *
   * @return true if any of the names matches; false if none of them match
   */
  public boolean isAnyElement(Name... names) {
    for (Name element : names) {
      if (this.name == element) return true;
    }
    return false;
  }

  /**
   * Returns the attribute value for the specified attribute.
   *
   * @param name The name of the attribute.
   *
   * @return The corresponding value or <code>null</code>.
   */
  public @Nullable String getAttribute(String name) {
    if (this.attributes == null) return null;
    else return this.attributes.get(name);
  }

  /**
   * Returns the attribute value for the specified attribute.
   *
   * @param name The name of the attribute.
   * @param defaultValue The default value if the attribute is not specified
   *
   * @return The corresponding value or <code>null</code>.
   */
  public String getAttributeOrElse(String name, String defaultValue) {
    if (this.attributes == null) return defaultValue;
    String value = this.attributes.get(name);
    return value == null ? defaultValue : value;
  }

  /**
   * Returns the attribute value for the specified attribute.
   *
   * @param name The name of the attribute.
   * @param defaultValue The default value if the attribute is not specified
   *
   * @return The corresponding value or <code>null</code>.
   */
  public int getAttributeOrElse(String name, int defaultValue) {
    if (this.attributes == null) return defaultValue;
    String value = this.attributes.get(name);
    if (value == null) return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
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
   * @param node The name of the attribute.
   *
   * @return this element for easy chaining.
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
   * @param nodes The list of nodes to add
   *
   * @return this element for easy chaining.
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
   * @param nodes The array of nodes to add
   *
   * @return this element for easy chaining.
   */
  public PSMLElement addNodes(PSMLNode... nodes) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.addAll(Arrays.asList(nodes));
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
   * @return this element for easy chaining.
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
   * @param text The text to add to this element.
   *
   * @return this element for easy chaining.
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
   * Retrieves all child elements of this PSMLElement instance that are instances of PSMLElement.
   *
   * <p>The method filters the list of nodes associated with the element, extracting only those
   * that are of type PSMLElement.
   *
   * @return A list of child elements that are of type PSMLElement. If there are no such elements
   *         or the node list is uninitialized, an empty list is returned.
   */
  public List<PSMLElement> getChildElements() {
    if (this.nodes == null) return List.of();
    return this.nodes.stream()
        .filter(PSMLElement.class::isInstance)
        .map(PSMLElement.class::cast)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all child elements of this PSMLElement instance that match the specified name.
   *
   * <p>This method filters the list of nodes associated with the element, extracting only those
   * that are of type {@code PSMLElement} and whose name matches the specified {@code Name}.
   *
   * @param name The name to filter the child elements by. Must not be {@code null}.
   *
   * @return A list of child elements of type {@code PSMLElement} whose name matches the specified
   *         {@code Name}. If there are no matching elements or the node list is uninitialized,
   *         an empty list is returned.
   */
  public List<PSMLElement> getChildElements(Name name) {
    if (this.nodes == null) return List.of();
    return this.nodes.stream()
        .filter(PSMLElement.class::isInstance)
        .map(PSMLElement.class::cast)
        .filter(e -> e.isElement(name))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all child elements of this PSMLElement instance that match the specified names.
   *
   * This method filters the list of nodes associated with the element, extracting only those
   * that are of type {@code PSMLElement} and whose name matches any of the specified {@code Name}.
   *
   * @param names The array of names to filter the child elements by. Must not be {@code null}.
   * @return A list of child elements of type {@code PSMLElement} whose name matches any of the specified
   *         {@code Name}. If there are no matching elements or the node list is uninitialized,
   *         an empty list is returned.
   */
  public List<PSMLElement> getChildElements(Name... names) {
    if (this.nodes == null) return List.of();
    return this.nodes.stream()
        .filter(PSMLElement.class::isInstance)
        .map(PSMLElement.class::cast)
        .filter(e -> e.isAnyElement(names))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves the first child element of this PSMLElement instance that match the specified name.
   *
   * <p>This method filters the list of nodes associated with the element, extracting only those
   * that are of type {@code PSMLElement} and whose name matches the specified {@code Name}.
   *
   * @param name The name to filter the child elements by. Must not be {@code null}.
   *
   * @return A list of child elements of type {@code PSMLElement} whose name matches the specified
   *         {@code Name}. If there are no matching elements or the node list is uninitialized,
   *         an empty list is returned.
   */
  public @Nullable PSMLElement getFirstChildElement(Name name) {
    if (this.nodes == null) return null;
    for (PSMLNode node : this.nodes) {
      if (node instanceof PSMLElement && ((PSMLElement)node).isElement(name)) {
        return (PSMLElement)node;
      }
    }
    return null;
  }

  /**
   * Indicates whether the element has nay child node (attribute nodes are ignored)
   *
   * @return <code>true</code> is the node list is uninitialized or empty.
   */
  public boolean isEmpty() {
    return this.nodes == null || this.nodes.isEmpty();
  }

  /**
   * Checks if the current object contains any child elements.
   *
   * @return true if there is at least one child element of type PSMLElement, false otherwise.
   */
  public boolean hasChildElements() {
    if (this.nodes == null) return false;
    for (PSMLNode node : this.nodes) {
      if (node instanceof PSMLElement) return true;
    }
    return false;
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

  /**
   * Converts this PSMLElement object to its string representation in XML format.
   * The output is generated using an instance of XMLStringWriter.
   *
   * @return A string representation of this element in XML format.
   */
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
