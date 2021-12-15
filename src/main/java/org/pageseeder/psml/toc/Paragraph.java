package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;

/**
 * A paragraph in the content.
 */
public final class Paragraph extends Element implements Serializable {

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
   * Full constructor for a paragraph.
   *
   * @param level    The level of this paragraph.
   * @param title    The title (or text) for the paragraph.
   * @param fragment The Fragment identifier where the paragraph was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param index    The index of the paragraph in the current document.
   * @param numbered Whether the paragraph is auto-numbered
   * @param prefix   Any prefix given to this paragraph.
   * @param blocklabel Parent block label.
   */
  private Paragraph(int level, String title, String fragment, String originalfrag,
      int index, boolean numbered, String prefix, String blocklabel) {
    super(level, title, fragment, originalfrag);
    this._index = index;
    this._numbered = numbered;
    this._prefix = prefix;
    this._blocklabel = blocklabel;
  }

  /**
   * Constructor for a paragraph.
   *
   * @param level    The level of this paragraph.
   * @param fragment The Fragment identifier where the paragraph was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param index    The index of the paragraph in the current document.
   */
  public Paragraph(int level, String fragment, String originalfrag, int index) {
    this(level, NO_TITLE,  fragment, originalfrag, index, false, NO_PREFIX, NO_BLOCK_LABEL);
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
   * @return The blocklabel parent of this paragraph or empty if none.
   */
  public String blocklabel() {
    return this._blocklabel;
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
    return new Paragraph(level(), title, fragment(), originalFragment(), this._index, this._numbered, this._prefix, this._blocklabel);
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new paragraph instance.
   */
  public Paragraph prefix(String prefix) {
    return new Paragraph(level(), title(), fragment(), originalFragment(), this._index, this._numbered, prefix, this._blocklabel);
  }

  /**
   * Create a new paragraph identical to this heading but with the specified prefix.
   *
   * @param blocklabel The different blocklabel
   *
   * @return A new paragraph instance.
   */
  public Paragraph blocklabel(String blocklabel) {
    return new Paragraph(level(), title(), fragment(), originalFragment(), this._index, this._numbered, this._prefix, blocklabel);
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
    return new Paragraph(level(), title(), fragment(), originalFragment(), this._index, numbered, this._prefix, this._blocklabel);
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
      out.append(" @").append(fragment());
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    // don't output if not numbered and no prefix
    if (!this._numbered && (NO_PREFIX.equals(this._prefix) || this._prefix == null)) return;
    xml.openElement("para-ref", false);
    xml.attribute("level", this.level());
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", fragment());
    xml.attribute("index", this._index);
    if (this._numbered) {
      xml.attribute("numbered", "true");
    }
    if (!Paragraph.NO_BLOCK_LABEL.equals(this._blocklabel)) {
      xml.attribute("block-label", this._blocklabel);
    }
    if (this._numbered && number != null) {
      Prefix pref = number.getTranscludedPrefix(treeid, count, fragment(), this._index, true);
      if (pref != null) {
        xml.attribute("part-level", pref.level);
        // don't output undefined prefixes
        if (!"".equals(pref.value) || pref.canonical != null) {
          xml.attribute("prefix", pref.value);
          xml.attribute("canonical", pref.canonical);
        }
      }
    } else {
      if (!NO_PREFIX.equals(this._prefix)) {
        xml.attribute("prefix", this._prefix);
      }
    }
    xml.closeElement();
  }

}
