package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A heading in the content.
 */
public final class Heading extends Element implements Serializable {

  /** Required for caching */
  private static final long serialVersionUID = 1L;

  /** When there is no prefix */
  public static final String NO_PREFIX = "";

  /** When there is no block label */
  public static final String NO_BLOCK_LABEL = "";

  /** The location of this part in the content */
  private final int _index;

  /** Whether the title is numbered */
  private final boolean _numbered;

  /** Prefix of title if any */
  private final String _prefix;

  /** Parent block label if any */
  private final String _blocklabel;

  /**
   * Full constructor for a heading.
   *
   * @param level         The level of this heading.
   * @param title         The title (or text) for the heading.
   * @param fragment      The Fragment identifier where the heading was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param index         The index of the heading in the current document.
   * @param numbered      Whether the heading is auto-numbered
   * @param prefix        Any prefix given to this heading.
   * @param blocklabel    Parent block label.
   */
  private Heading(int level, String title, String fragment, String originalfrag,
      int index, boolean numbered, String prefix, String blocklabel) {
    super(level, title, fragment, originalfrag);
    this._index = index;
    this._numbered = numbered;
    this._prefix = prefix;
    this._blocklabel = blocklabel;
  }

  /**
   * Constructor for a heading.
   *
   * @param level         The level of this heading.
   * @param title         The title (or text) for the heading.
   * @param fragment      The Fragment identifier where the heading was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param index         The index of the heading in the current document.
   */
  public Heading(int level, String title, String fragment, String originalfrag, int index) {
    this(level, title,  fragment, originalfrag, index, false, NO_PREFIX, NO_BLOCK_LABEL);
  }

  /**
   * Create a new untitled heading.
   *
   * @param level         The level of this heading.
   * @param fragment      The Fragment identifier where the heading was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param index         The index of the heading in the current document.
   *
   * @return A new heading instance.
   */
  public static Heading untitled(int level, String fragment, String originalfrag, int index) {
    return new Heading(level, NO_TITLE, fragment, originalfrag, index, false, NO_PREFIX, NO_BLOCK_LABEL);
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
   * @return The blocklabel parent of this heading or empty if none.
   */
  public String blocklabel() {
    return this._blocklabel;
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
    return new Heading(level(), title, fragment(), originalFragment(), this._index, this._numbered, this._prefix, this._blocklabel);
  }

  /**
   * Create a new heading identical to this heading but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new heading instance.
   */
  public Heading prefix(String prefix) {
    return new Heading(level(), title(), fragment(), originalFragment(), this._index, this._numbered, prefix, this._blocklabel);
  }

  /**
   * Create a new heading identical to this heading but with the specified prefix.
   *
   * @param blocklabel The different blocklabel
   *
   * @return A new heading instance.
   */
  public Heading blocklabel(String blocklabel) {
    return new Heading(level(), title(), fragment(), originalFragment(), this._index, this._numbered, this._prefix, blocklabel);
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
    return new Heading(level(), title(), fragment(), originalFragment(), this._index, numbered, this._prefix, this._blocklabel);
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
    return new Heading(level()+delta, title(), fragment(), originalFragment(), this._index, this._numbered, this._prefix, this._blocklabel);
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
      out.append(" @").append(fragment());
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    xml.openElement("heading-ref", false);
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", fragment());
    xml.attribute("index", this._index);
    if (this._numbered) {
      xml.attribute("numbered", "true");
    }
    if (this._numbered && number != null) {
      Prefix pref = number.getTranscludedPrefix(treeid, count, fragment(), this._index);
      if (pref != null) {
        xml.attribute("prefix", pref.value);
        xml.attribute("canonical", pref.canonical);
      }
    } else {
      if (!NO_PREFIX.equals(this._prefix)) {
        xml.attribute("prefix", this._prefix);
      }
    }
    xml.closeElement();
  }

}
