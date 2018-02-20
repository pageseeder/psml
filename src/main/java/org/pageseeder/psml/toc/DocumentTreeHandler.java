/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.xml.BasicHandler;
import org.xml.sax.Attributes;

/**
 * Parse the document content to generate the document tree.
 *
 * The parser only considers the following elements as structural elements:
 * - headings
 * - section titles
 * - embed block references
 *
 * Markup within headings is ignored.
 */
public final class DocumentTreeHandler extends BasicHandler<DocumentTree> {

  /**
   * The heading level assigned to section titles.
   */
  private static final int SECTION_TITLE_LEVEL = 2;

  /**
   * Takes a list of elements and generate the tree.
   */
  private final TreeExpander _expander = new TreeExpander();

  /**
   * The last heading being processed
   */
  private @Nullable Heading currentHeading = null;

  /**
   * The content tree being built.
   */
  private final DocumentTree.Builder _tree;

  /**
   * Current fragment ID
   */
  private String fragment = "default";

  /**
   * We count the headings to use as index in TOC.
   */
  private int counter = 0;

  /**
   * We count the paras to use as index.
   */
  private int paracounter = 0;

  /**
   * The level to adjust headings with if updated by transclusion.
   */
  private int transclusionLevel = 0;

  /**
   * @param uri content tree to generate
   */
  public DocumentTreeHandler(long uri) {
    this._tree = new DocumentTree.Builder(uri);
  }

  @Override
  public void startElement(String element, Attributes attributes) {
    if (isElement("heading") || (isElement("title") && isParent("section"))) {
      startHeading(attributes);
    } else if (isElement("para") && ("true".equals(attributes.getValue("numbered")) ||
        (!Paragraph.NO_PREFIX.equals(attributes.getValue("prefix")) && attributes.getValue("prefix") != null))) {
      startPara(attributes);
    } else if (isAny("fragment", "xref-fragment", "properties-fragment", "media-fragment")) {
      startFragment(attributes);
    } else if ("blockxref".equals(element) && "embed".equals(attributes.getValue("type"))) {
      startEmbedRef(attributes);
    } else if ("blockxref".equals(element) && "transclude".equals(attributes.getValue("type"))) {
      this.transclusionLevel = getInt(attributes, "level", 0);
    } else if ("reversexref".equals(element) && hasAncestor("documentinfo") && !hasAncestor("blockxref")) {
      startReverseRef(attributes);
    } else if (isElement("displaytitle") && !hasAncestor("blockxref")) {
      newBuffer();
    } else if (isElement("labels") && !hasAncestor("blockxref")) {
      newBuffer();
    }
  }

  /**
   * Found `heading` element or `section/title` element,
   *
   * @param attributes The attributes
   */
  private void startHeading(Attributes attributes) {
    int level = this.transclusionLevel + (isParent("section")? SECTION_TITLE_LEVEL : getInt(attributes, "level"));
    String prefix = attributes.getValue("prefix");
    Heading heading = Heading.untitled(level, this.fragment, this.counter);
    if ("true".equals(attributes.getValue("numbered"))) {
      heading = heading.numbered(true);
    }
    if (prefix != null) {
      heading = heading.prefix(prefix);
    }

    newBuffer();

    this.currentHeading = heading;
    this.counter++;
  }

  /**
   * Found `para` element with `@numbered="true"`.
   *
   * @param attributes The attributes
   */
  private void startPara(Attributes attributes) {
    int level = getInt(attributes, "indent");
    String prefix = attributes.getValue("prefix");
    Paragraph para = new Paragraph(level, this.fragment, this.paracounter);
    if ("true".equals(attributes.getValue("numbered"))) {
      para = para.numbered(true);
    }
    if (prefix != null) {
      para = para.prefix(prefix);
    }

    this._expander.add(para);
    this.paracounter++;
  }

  /**
   * Record the ID pof the fragment
   *
   * @param attributes The attributes
   */
  private void startFragment(Attributes attributes) {
    // Only if not within a transclusion
    if (!hasAncestor("blockxref")) {
      this.fragment = getString(attributes, "id");
    }
  }

  /**
   * Found `reversexref` element
   *
   * @param attributes The attributes
   */
  private void startEmbedRef(Attributes attributes) {
    long uriid = getLong(attributes, "uriid", -1L).longValue();
    // We eliminate unresolved cross-references
    if (uriid > 0) {
      // The level is based on the part that contains it
      int partLevel = getBaseLevel()+1;
      int level = partLevel + getInt(attributes, "level", 0);
      String type = getString(attributes, "documenttype", Reference.DEFAULT_TYPE);
      String title = computeReferenceTitle(attributes);
      Reference reference = new Reference(level, title, uriid, type, attributes.getValue("frag"));
      this._expander.add(reference);
    }
  }

  /**
   * Found `reversexref` element
   *
   * @param attributes The attributes
   */
  private void startReverseRef(Attributes attributes) {
    String type = getString(attributes, "forwardtype", "none");
    Long ref = getLong(attributes, "uriid");
    // Only add embed references that haven't been added yet
    if ("embed".equals(type)) {
      this._tree.addReverseReferenceIfNew(ref);
    }
  }

  @Override
  public void endElement(String element) {
    if ("heading".equals(element) || (isElement("title") && isParent("section"))) {
      // Set the title of the current part (top of stack)
      Heading heading = this.currentHeading;
      if (heading != null) {
        String title = buffer(true);
        if (title != null) {
          heading = heading.title(title);
        }
        this._expander.add(heading);
      }

    } else if ("displaytitle".equals(element) && !hasAncestor("blockxref")) {
      String title = buffer(true);
      if (title != null) {
        this._tree.title(title);
      }

    } else if ("labels".equals(element) && !hasAncestor("blockxref")) {
      String labels = buffer(true);
      if (labels != null) {
        this._tree.labels(labels);
      }

    } else if ("blockxref".equals(element) && !hasAncestor("blockxref")) {
      this.transclusionLevel = 0;

    } else if ("document".equals(element) && !hasAncestor("blockxref")) {
      this._tree.parts(this._expander.parts());
      add(this._tree.build());
    }
  }

  /**
   * Compute the title based on the `blockxref` attributes
   *
   * @param attributes the attributes of the `blockxref` element
   *
   * @return The computed title
   */
  private String computeReferenceTitle(Attributes attributes) {
    String document = Objects.toString(attributes.getValue("urititle"));
    String display = Objects.toString(attributes.getValue("display"));
    if ("document+manual".equals(display)) {
      String manual = Objects.toString(attributes.getValue("title"));
      return document+manual;
    } else if ("document+fragment".equals(display)) {
      String fragment = Objects.toString(attributes.getValue("frag"));
      return document+fragment;
    } else if ("manual".equals(display)) {
      String manual = Objects.toString(attributes.getValue("title"));
      return manual;
    } else return document;
  }

  /**
   * @return the current part
   *
   * @throws IllegalStateException If the part is <code>null</code>
  */
  private int getBaseLevel() {
    Heading heading = this.currentHeading;
    return heading != null? heading.level() : 0;
  }

}
