package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.jspecify.annotations.Nullable;
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
  private final int index;

  /** Whether the title is numbered */
  private final boolean numbered;

  /** Prefix of title if any */
  private final String prefix;

  /** Parent block label if any */
  private final String blocklabel;

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
    this.index = index;
    this.numbered = numbered;
    this.prefix = prefix;
    this.blocklabel = blocklabel;
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
    return this.index;
  }

  /**
   * @return The full title of this heading including the prefix
   */
  public String getPrefixedTitle() {
    return !NO_PREFIX.equals(this.prefix)? this.prefix +" "+title() : title();
  }

  /**
   * @return The prefix given to this heading or empty if none.
   */
  public String prefix() {
    return this.prefix;
  }

  /**
   * @return The blocklabel parent of this heading or empty if none.
   */
  public String blocklabel() {
    return this.blocklabel;
  }

  /**
   * @return Whether the heading is automatically numbered
   */
  public boolean numbered() {
    return this.numbered;
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
    return new Heading(level(), title, fragment(), originalFragment(), this.index, this.numbered, this.prefix, this.blocklabel);
  }

  /**
   * Create a new heading identical to this heading but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new heading instance.
   */
  public Heading prefix(String prefix) {
    return new Heading(level(), title(), fragment(), originalFragment(), this.index, this.numbered, prefix, this.blocklabel);
  }

  /**
   * Create a new heading identical to this heading but with the specified prefix.
   *
   * @param blocklabel The different blocklabel
   *
   * @return A new heading instance.
   */
  public Heading blocklabel(String blocklabel) {
    return new Heading(level(), title(), fragment(), originalFragment(), this.index, this.numbered, this.prefix, blocklabel);
  }

  /**
   * Create a new heading identical to this heading but with the specified numbered flag.
   *
   * @param numbered Whether the title is numbered
   *
   * @return A new heading instance unless the numbered flag is equal to the numbered flag of current heading.
   */
  public Heading numbered(boolean numbered) {
    if (this.numbered == numbered) return this;
    return new Heading(level(), title(), fragment(), originalFragment(), this.index, numbered, this.prefix, this.blocklabel);
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
      out.append(" [").append(Integer.toString(this.index)).append(']');
      out.append(" @").append(fragment());
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    xml.openElement("heading-ref", false);
    xml.attribute("level", this.level());
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", fragment());
    xml.attribute("index", this.index);
    if (this.numbered) {
      xml.attribute("numbered", "true");
    }
    if (this.numbered && number != null) {
      Prefix pref = number.getTranscludedPrefix(treeid, count, fragment(), this.index);
      if (pref != null) {
        xml.attribute("prefix", pref.value);
        xml.attribute("canonical", pref.canonical);
      }
    } else {
      if (!NO_PREFIX.equals(this.prefix)) {
        xml.attribute("prefix", this.prefix);
      }
    }
    xml.closeElement();
  }

}
