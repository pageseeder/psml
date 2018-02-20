package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A paragraph in the content.
 */
public final class Paragraph extends Element implements Serializable {

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
   * Full constructor for a paragraph.
   *
   * @param level    The level of this paragraph.
   * @param title    The title (or text) for the paragraph.
   * @param fragment The Fragment identifier where the paragraph was found.
   * @param index    The index of the paragraph in the current document.
   * @param numbered Whether the paragraph is auto-numbered
   * @param prefix   Any prefix given to this paragraph.
   */
  private Paragraph(int level, String title, String fragment, int index, boolean numbered, String prefix) {
    super(level, title);
    this._fragment = fragment;
    this._index = index;
    this._numbered = numbered;
    this._prefix = prefix;
  }

  /**
   * Constructor for a paragraph.
   *
   * @param level    The level of this paragraph.
   * @param fragment The Fragment identifier where the paragraph was found.
   * @param index    The index of the paragraph in the current document.
   */
  public Paragraph(int level, String fragment, int index) {
    this(level, NO_TITLE,  fragment, index, false, NO_PREFIX);
  }

  /**
   * Return a 0-based index of this paragraph in the document so that we can link to it.
   *
   * @return The index of the paragraph in the document.
   */
  public int index() {
    return this._index;
  }

  /**
   * @return Fragment ID that this paragraph is defined in
   */
  public String fragment() {
    return this._fragment;
  }

  /**
   * @return The full title of this paragraph including the prefix
   */
  public String getPrefixedTitle() {
    return !NO_PREFIX.equals(this._prefix)? this._prefix+" "+title() : title();
  }

  /**
   * @return The prefix given to this paragraph or empty if none.
   */
  public String prefix() {
    return this._prefix;
  }

  /**
   * @return Whether the paragraph is automatically numbered
   */
  public boolean numbered() {
    return this._numbered;
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified title
   *
   * @param title The different title
   *
   * @return A new paragraph instance unless the title is equal to the title of current paragraph.
   */
  public Paragraph title(String title) {
    if (title.equals(title())) return this;
    return new Paragraph(level(), title, this._fragment, this._index, this._numbered, this._prefix);
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new paragraph instance.
   */
  public Paragraph prefix(String prefix) {
    return new Paragraph(level(), title(), this._fragment, this._index, this._numbered, prefix);
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified numbered flag.
   *
   * @param numbered Whether the title is numbered
   *
   * @return A new paragraph instance unless the numbered flag is equal to the numbered flag of current paragraph.
   */
  public Paragraph numbered(boolean numbered) {
    if (this._numbered == numbered) return this;
    return new Paragraph(level(), title(), this._fragment, this._index, numbered, this._prefix);
  }

  /**
   *
   * @param delta The difference with the current level.
   *
   * @return a new paragraph with a adjusted level or this paragraph if the delta is 0.
   */
  @Override
 public Paragraph adjustLevel(int delta) {
   if (delta == 0) return this;
   return new Paragraph(level()+delta, title(), this._fragment, this._index, this._numbered, this._prefix);
 }

  @Override
  public void print(Appendable out) {
    try {
      for (int i=0; i < level(); i++) {
        out.append("-");
      }
      out.append(' ');
      if (!Paragraph.NO_PREFIX.equals(prefix())) {
        out.append(prefix()).append(' ');
      }
      out.append("para");
      out.append(" [").append(Integer.toString(this._index)).append(']');
      out.append(" @").append(this._fragment);
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, NumberingGenerator number) throws IOException {
    // Not included in TOC
  }

}
