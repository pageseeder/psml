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
  private final int index;

  /** Whether the title is numbered */
  private final boolean numbered;

  /** Prefix of title if any */
  private final String prefix;

  /** Parent block label if any */
  private final String blocklabel;

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
    this.index = index;
    this.numbered = numbered;
    this.prefix = prefix;
    this.blocklabel = blocklabel;
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
    return this.index;
  }

  /**
   * Whether this para is visible in a TOC with the specified config.
   *
   * @param config the publication config
   *
   * @return <code>true</code> if visible
   */
  public boolean isVisible(PublicationConfig config) {
    return config != null && (config.getTocParaIndents().indexOf(this.level() + ",") != -1 ||
        (!"".equals(this.blocklabel()) && config.getTocBlockLabels().indexOf(this.blocklabel() + ",") != -1));
  }

  /**
   * @return The full title of this paragraph including the prefix
   */
  public String getPrefixedTitle() {
    return !NO_PREFIX.equals(this.prefix)? this.prefix +" "+title() : title();
  }

  /**
   * @return The prefix given to this paragraph or empty if none.
   */
  public String prefix() {
    return this.prefix;
  }

  /**
   * @return The blocklabel parent of this paragraph or empty if none.
   */
  public String blocklabel() {
    return this.blocklabel;
  }

  /**
   * @return Whether the paragraph is automatically numbered
   */
  public boolean numbered() {
    return this.numbered;
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
    return new Paragraph(level(), title, fragment(), originalFragment(), this.index, this.numbered, this.prefix, this.blocklabel);
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified prefix.
   *
   * @param prefix The different prefix
   *
   * @return A new paragraph instance.
   */
  public Paragraph prefix(String prefix) {
    return new Paragraph(level(), title(), fragment(), originalFragment(), this.index, this.numbered, prefix, this.blocklabel);
  }

  /**
   * Create a new paragraph identical to this heading but with the specified prefix.
   *
   * @param blocklabel The different blocklabel
   *
   * @return A new paragraph instance.
   */
  public Paragraph blocklabel(String blocklabel) {
    return new Paragraph(level(), title(), fragment(), originalFragment(), this.index, this.numbered, this.prefix, blocklabel);
  }

  /**
   * Create a new paragraph identical to this paragraph but with the specified numbered flag.
   *
   * @param numbered Whether the title is numbered
   *
   * @return A new paragraph instance unless the numbered flag is equal to the numbered flag of current paragraph.
   */
  public Paragraph numbered(boolean numbered) {
    if (this.numbered == numbered) return this;
    return new Paragraph(level(), title(), fragment(), originalFragment(), this.index, numbered, this.prefix, this.blocklabel);
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
      out.append(" [").append(Integer.toString(this.index)).append(']');
      out.append(" @").append(fragment());
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    // don't output if not numbered and no prefix
    if (!this.numbered && (NO_PREFIX.equals(this.prefix) || this.prefix == null)) return;
    xml.openElement("para-ref", false);
    xml.attribute("level", this.level());
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("fragment", fragment());
    xml.attribute("index", this.index);
    if (this.numbered) {
      xml.attribute("numbered", "true");
    }
    if (!Paragraph.NO_BLOCK_LABEL.equals(this.blocklabel)) {
      xml.attribute("block-label", this.blocklabel);
    }
    if (number != null) {
      Prefix pref = number.getTranscludedPrefix(treeid, count, fragment(), this.index, true);
      if (pref != null) {
        xml.attribute("part-level", pref.level);
      }
      if (this.numbered) {
        if (pref != null) {
          // don't output undefined prefixes
          if (!"".equals(pref.value) || pref.canonical != null) {
            xml.attribute("prefix", pref.value);
            xml.attribute("canonical", pref.canonical);
          }
        }
      } else {
        if (!NO_PREFIX.equals(this.prefix)) {
          xml.attribute("prefix", this.prefix);
        }
      }
    }
    xml.closeElement();
  }

}
