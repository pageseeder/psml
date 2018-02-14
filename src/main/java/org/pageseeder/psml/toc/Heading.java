package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A heading in the content.
 */
public final class Heading extends Element implements Serializable {

  /** Required for caching */
  private static final long serialVersionUID = 1L;

  /** When there is no prefix */
  public static final String NO_PREFIX = "";

  /** Fragment ID this part starts in */
  private final String _fragment;

  /** The location of this part in the content */
  private final int _index;

  /** Whether the title is numbered */
  private final boolean _numbered;

  /** Prefix of title if any */
  private final String _prefix;

  /**
   * Full constructor for a heading.
   *
   * @param level    The level of this heading.
   * @param title    The title (or text) for the heading.
   * @param fragment The Fragment identifier where the heading was found.
   * @param index    The index of the heading in the current document.
   * @param numbered Whether the heading is auto-numbered
   * @param prefix   Any prefix given to this heading.
   */
  private Heading(int level, String title, String fragment, int index, boolean numbered, String prefix) {
    super(level, title);
    this._fragment = fragment;
    this._index = index;
    this._numbered = numbered;
    this._prefix = prefix;
  }

  public Heading(int level, String title, String fragment, int index) {
    this(level, title,  fragment, index, false, NO_PREFIX);
  }

  /**
   * Create a new untitled heading.
   *
   * @param level    The level of this heading.
   * @param fragment The Fragment identifier where the heading was found.
   * @param index    The index of the heading in the current document.
   *
   * @return A new heading instance.
   */
  public static Heading untitled(int level, String fragment, int index) {
    return new Heading(level, NO_TITLE, fragment, index, false, NO_PREFIX);
  }

  /**
   * Return a 0-based index of this heading in the document so that we can link to it.
   *
   * @return The index of the heading in the document.
   */
  public int index() {
    return this._index;
  }

  /**
   * @return Fragment ID that this heading is defined in
   */
  public String fragment() {
    return this._fragment;
  }

  /**
   * @return The full title of this heading including the prefix
   */
  public String getPrefixedTitle() {
    return !NO_PREFIX.equals(this._prefix)? this._prefix+" "+title() : title();
  }

  /**
   * @return The prefix given to this heading or empty if none.
   */
  public String prefix() {
    return this._prefix;
  }

  /**
   * @return Whether the heading is automatically numbered
   */
  public boolean numbered() {
    return this._numbered;
  }

  /**
   * Create a new heading identical to this heading but with the specified title
   *
   * @param title The different title
   *
   * @return A new heading instance unless the title is equal to the title of current heading.
   */
  public Heading title(String title) {
    if (title.equals(title())) return this;
    return new Heading(level(), title, this._fragment, this._index, this._numbered, this._prefix);
  }

  /**
   * Create a new heading identical to this heading but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new heading instance.
   */
  public Heading prefix(String prefix) {
    return new Heading(level(), title(), this._fragment, this._index, this._numbered, prefix);
  }

  /**
   * Create a new heading identical to this heading but with the specified numbered flag.
   *
   * @param numbered Whether the title is numbered
   *
   * @return A new heading instance unless the numbered flag is equal to the numbered flag of current heading.
   */
  public Heading numbered(boolean numbered) {
    if (this._numbered == numbered) return this;
    return new Heading(level(), title(), this._fragment, this._index, numbered, this._prefix);
  }

  /**
   *
   * @param delta The difference with the current level.
   *
   * @return a new heading with a adjusted level or this heading if the delta is 0.
   */
  @Override
  public Heading adjustLevel(int delta) {
    if (delta == 0) return this;
    return new Heading(level()+delta, title(), this._fragment, this._index, this._numbered, this._prefix);
  }

  @Override
  public void print(Appendable out) {
    try {
      for (int i=0; i < level(); i++) {
        out.append("#");
      }
      out.append(' ');
      if (!Heading.NO_PREFIX.equals(prefix())) {
        out.append(prefix()).append(' ');
      }
      out.append(title());
      out.append(" [").append(Integer.toString(this._index)).append(']');
      out.append(" @").append(this._fragment);
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, NumberingGenerator number) throws IOException {
    xml.openElement("heading", false);
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", this._fragment);
    xml.attribute("index", this._index);
    if (this._numbered) {
      xml.attribute("numbered", "true");
    }
    if (this._numbered && number != null) {
      number.generateHeadingNumbering(level, xml);
    } else {
      if (!NO_PREFIX.equals(this._prefix)) {
        xml.attribute("prefix", this._prefix);
      }
    }
    xml.closeElement();
  }

  @Override
  public void attributes(XMLWriter xml, int level) throws IOException {
    xml.attribute("from", "heading");
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", this._fragment);
    xml.attribute("index", this._index);
    if (this._numbered) {
      xml.attribute("numbered", "true");
    }
    if (!NO_PREFIX.equals(this._prefix)) {
      xml.attribute("prefix", this._prefix);
    }
  }
}
