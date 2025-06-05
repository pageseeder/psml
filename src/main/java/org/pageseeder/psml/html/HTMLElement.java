package org.pageseeder.psml.html;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Represents an HTML element in a document, encapsulating its name, attributes, and child nodes.
 *
 * <p>This class allows the manipulation of an HTML element's attributes, child nodes, and content.
 * Instances of {@code HTMLElement} provide a convenient way to model and manipulate the structure
 * of an HTML document programmatically.
 *
 * <p>Implementation notes:
 * <ul>
 *   <li>The attribute map and node list are initialized lazily to optimize memory usage.</li>
 *   <li>Many methods return {@code this}, enabling a fluent API style to modify the element.</li>
 * </ul>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class HTMLElement implements HTMLNode {

  /**
   * An enumeration of possible element names in HTML.
   *
   * <p>Note: Not all HTML elements are model here, only elements which are relevant for this
   * library.
   */
  public enum Name {

    /**
     * &lt;a&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a">HTML Anchor element</a>
     */
    A(true),

    /**
     * &lt;abbr&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/abbr">HTML Abbreviation element</a>
     */
    ABBR(true),

    /**
     * &lt;address&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/address">HTML Address element</a>
     */
    ADDRESS(false),

    /**
     * &lt;article&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/article">HTML Article element</a>
     */
    ARTICLE(false),

    /**
    * &lt;aside&lt; element
    *
    * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/aside">HTML Aside element</a>
    */
    ASIDE(false),

    /**
     * &lt;audio&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/audio">HTML Audio element</a>
     */
    AUDIO(true),

    /**
     * &lt;b&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/b">HTML b element</a>
     */
    B(true),

    /**
     * &lt;blockquote&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/blockquote">HTML blockquote element</a>
     */
    BLOCKQUOTE(false),

    /**
     * &lt;body&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/body">HTML body element</a>
     */
    BODY(false),

    /**
     * &lt;br&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/br">HTML br element</a>
     */
    BR(true),

    /**
     * &lt;caption&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/caption">HTML caption element</a>
     */
    CAPTION(false),

    /**
     * &lt;cite&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/cite">HTML cite element</a>
     */
    CITE(true),

    /**
     * &lt;code&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/code">HTML code element</a>
     */
    CODE(true),

    /**
     * &lt;col&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/col">HTML col element</a>
     */
    COL(false),

    /**
     * &lt;colgroup&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/colgroup">HTML colgroup element</a>
     */
    COLGROUP(false),

    /**
     * &lt;dd&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dd">HTML dd element</a>
     */
    DD(false),

    /**
     * &lt;del&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/del">HTML del element</a>
     */
    DEL(true),

    /**
     * &lt;dfn&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dfn">HTML dfn element</a>
     */
    DFN(true),

    /**
     * &lt;div&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/div">HTML div element</a>
     */
    DIV(false),

    /**
     * &lt;dl&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dl">HTML dl element</a>
     */
    DL(false),

    /**
     * &lt;dt&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dt">HTML dt element</a>
     */
    DT(false),

    /**
     * &lt;em&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/em">HTML emphasis element</a>
     */
    EM(true),

    /**
     * &lt;figcaption&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/figcaption">HTML figcaption element</a>
     */
    FIGCAPTION(false),

    /**
     * &lt;figure&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/figure">HTML figure element</a>
     */
    FIGURE(false),

    /**
     * &lt;footer&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/footer">HTML footer element</a>
     */
    FOOTER(false),

    /**
     * &lt;h1&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h1">HTML h1 element</a>
     */
    H1(false),

    /**
     * &lt;h2&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h2">HTML h2 element</a>
     */
    H2(false),

    /**
     * &lt;h3&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h3">HTML h3 element</a>
     */
    H3(false),

    /**
     * &lt;h4&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h4">HTML h4 element</a>
     */
    H4(false),

    /**
     * &lt;h5&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h5">HTML h5 element</a>
     */
    H5(false),

    /**
     * &lt;h6&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/h6">HTML h6 element</a>
     */
    H6(false),

    /**
     * &lt;head&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/head">HTML head element</a>
     */
    HEAD(false),

    /**
     * &lt;header&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/header">HTML header element</a>
     */
    HEADER(false),

    /**
     * &lt;hr&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/hr">HTML hr element</a>
     */
    HR(false),

    /**
     * &lt;html&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/html">HTML html element</a>
     */
    HTML(false),

    /**
     * &lt;i&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/i">HTML i element</a>
     */
    I(true),

    /**
     * &lt;iframe&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/iframe">HTML iframe element</a>
     */
    IFRAME(false),

    /**
     * &lt;img&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">HTML img element</a>
     */
    IMG(true),

    /**
     * &lt;ins&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ins">HTML ins element</a>
     */
    INS(true),

    /**
     * &lt;kbd&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/kbd">HTML kbd element</a>
     */
    KBD(true),

    /**
     * &lt;label&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/label">HTML label element</a>
     */
    LABEL(true),

    /**
     * &lt;legend&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/legend">HTML legend element</a>
     */
    LEGEND(false),

    /**
     * &lt;li&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/li">HTML li element</a>
     */
    LI(false),

    /**
     * &lt;link&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/link">HTML link element</a>
     */
    LINK(true),

    /**
     * &lt;main&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/main">HTML main element</a>
     */
    MAIN(false),

    /**
     * &lt;mark&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/mark">HTML mark element</a>
     */
    MARK(true),

    /**
     * &lt;meta&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta">HTML meta element</a>
     */
    META(false),

    /**
     * &lt;nav&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/nav">HTML nav element</a>
     */
    NAV(false),

    /**
     * &lt;ol&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ol">HTML ol element</a>
     */
    OL(false),

    /**
     * &lt;p&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/p">HTML p element</a>
     */
    P(false),

    /**
     * &lt;pre&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/pre">HTML pre element</a>
     */
    PRE(false),

    /**
     * &lt;q&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/q">HTML q element</a>
     */
    Q(true),

    /**
     * &lt;s&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/s">HTML s element</a>
     */
    S(true),

    /**
     * &lt;samp&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/samp">HTML samp element</a>
     */
    SAMP(true),

    /**
     * &lt;section&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/section">HTML section element</a>
     */
    SECTION(false),

    /**
     * &lt;small&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/small">HTML small element</a>
     */
    SMALL(true),

    /**
     * &lt;source&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/source">HTML source element</a>
     */
    SOURCE(false),

    /**
     * &lt;span&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/span">HTML span element</a>
     */
    SPAN(true),

    /**
     * &lt;strong&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/strong">HTML strong element</a>
     */
    STRONG(true),

    /**
     * &lt;style&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/style">HTML style element</a>
     */
    STYLE(false),

    /**
     * &lt;sub&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sub">HTML sub element</a>
     */
    SUB(true),

    /**
     * &lt;summary&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/summary">HTML summary element</a>
     */
    SUMMARY(false),

    /**
     * &lt;sup&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sup">HTML sup element</a>
     */
    SUP(true),

    /**
     * &lt;table&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/table">HTML table element</a>
     */
    TABLE(false),

    /**
     * &lt;tbody&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tbody">HTML tbody element</a>
     */
    TBODY(false),

    /**
     * &lt;td&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/td">HTML td element</a>
     */
    TD(false),

    /**
     * &lt;tfoot&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tfoot">HTML tfoot element</a>
     */
    TFOOT(false),

    /**
     * &lt;th&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/th">HTML th element</a>
     */
    TH(false),

    /**
     * &lt;thead&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/thead">HTML thead element</a>
     */
    THEAD(false),

    /**
     * &lt;time&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/time">HTML time element</a>
     */
    TIME(true),

    /**
     * &lt;title&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/title">HTML title element</a>
     */
    TITLE(false),

    /**
     * &lt;tr&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/tr">HTML tr element</a>
     */
    TR(false),

    /**
     * &lt;u&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/u">HTML u element</a>
     */
    U(true),

    /**
     * &lt;ul&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ul">HTML ul element</a>
     */
    UL(true),

    /**
     * &lt;var&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/var">HTML var element</a>
     */
    VAR(true),

    /**
     * &lt;video&lt; element
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/video">HTML video element</a>
     */
    VIDEO(false),

    /**
     * This element is used for any unrecognized PSML element.
     */
    UNKNOWN(false);

    /**
     * Indicates that this element is an inline element.
     *
     * <p>It is defined as an inline element if it can have sibling text nodes
     * that are significant (i.e other than white spaces)
     */
    private final boolean inline;

    /**
     * Creates a new HTML
     *
     * @param inline whether it is an inline element
     */
    Name(boolean inline) {
      this.inline = inline;
    }

    /**
     * @return The element name.
     */
    public String element() {
      return name().toLowerCase();
    }

    /**
     * @return <code>true</code> if considered an inline element.
     */
    public boolean isInline() {
      return this.inline;
    }

    /**
     * Case insensitive match for element name.
     *
     * @param name The name of the element in any case.
     *
     * @return The corresponding instance,
     */
    public static Name forElement(String name) {
      for (Name element : values()) {
        if (element.name().equalsIgnoreCase(name)) return element;
      }
      return Name.UNKNOWN;
    }
  }

  /**
   *
   */
  private Name name = Name.UNKNOWN;

  private @Nullable Map<String, String> attributes;

  public @Nullable List<HTMLNode> nodes;

  public HTMLElement(String name) {
    this.name = Name.forElement(name);
  }

  public HTMLElement(Name name) {
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
  public HTMLElement setName(Name name) {
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
  public @Nullable String getAttribute(String name) {
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
  public HTMLElement setAttribute(String name, String value) {
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
  public HTMLElement setAttribute(String name, int value) {
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
  public HTMLElement setAttribute(String name, boolean value) {
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
   * @param node The node to add
   *
   * @return this element
   */
  public HTMLElement addNode(HTMLNode node) {
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
   * @return this element
   */
  public HTMLElement addNodes(List<? extends HTMLNode> nodes) {
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
   * @param nodes The nodes to add.
   *
   * @return this element
   */
  public HTMLElement addNodes(HTMLNode... nodes) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    Collections.addAll(this.nodes, nodes);
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
  public HTMLElement setText(String text) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>(1);
    } else {
      this.nodes.clear();
    }
    this.nodes.add(new HTMLText(text));
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
   * @return this element
   */
  public HTMLElement addText(String text) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.add(new HTMLText(text));
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
  public List<HTMLNode> getNodes() {
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
    return this.nodes == null || this.nodes.isEmpty();
  }

  @Override
  public String getText() {
    if (this.nodes == null) return "";
    StringBuilder out = new StringBuilder();
    for (HTMLNode node : this.nodes) {
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
      for (HTMLNode node : this.nodes) {
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
    for (HTMLNode node : this.nodes) {
      if (node instanceof HTMLText) return false;
    }
    return true;
  }
}
