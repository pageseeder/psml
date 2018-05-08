/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.util.ArrayDeque;
import java.util.Deque;
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
   * The default fragment.
   */
  private static final String DEFAULT_FRAGMENT = "default";

  /**
   * The PSML mediatype
   */
  private static final String PSML_MEDIATYPE = "application/vnd.pageseeder.psml+xml";

  /**
   * Takes a list of elements and generate the tree.
   */
  private final TreeExpander _expander = new TreeExpander();

  /**
   * Stack of nested blockxrefs with true for each transclusion.
   */
  private final Deque<Boolean> _blockxrefs = new ArrayDeque<>();

  /**
   * Stack of nested original (untranscluded) fragment IDs.
   */
  private final Deque<String> _fragmentIDs = new ArrayDeque<>();

  /**
   * The last heading being processed
   */
  private @Nullable Heading currentHeading = null;

  /**
   * The last reference being processed
   */
  private @Nullable Reference currentReference = null;

  /**
   * Whether this is the first heading in fragment (not preceded by para)
   */
  private boolean firstHeading = false;

  /**
   * The content tree being built.
   */
  private final DocumentTree.Builder _tree;

  /**
   * Current fragment ID
   */
  private String fragment = DEFAULT_FRAGMENT;

  /**
   * The current block label being processed
   */
  private @Nullable String currentBlockLabel = null;

  /**
   * We count the headings/paras to use as index in TOC.
   */
  private int counter = 1;

  /**
   * We count the sections to use as index.
   */
  private int sectioncounter = 1;

  /**
   * The level to adjust headings with if updated by transclusion.
   */
  private int transclusionLevel = 0;

  /**
   * The current fragment level (level of last preceding heading)
   */
  private int fragmentLevel = 0;

  /**
   * Constructor (URI id set by handler)
   *
   */
  public DocumentTreeHandler() {
    this._tree = new DocumentTree.Builder();
    this._fragmentIDs.push(DEFAULT_FRAGMENT);
  }

  /**
   * Constructor
   *
   * @param uri content tree to generate
   */
  public DocumentTreeHandler(long uri) {
    this._tree = new DocumentTree.Builder(uri);
    this._fragmentIDs.push(DEFAULT_FRAGMENT);
  }

  @Override
  public void startElement(String element, Attributes attributes) {
    if ("blockxref".equals(element)) {
      this._blockxrefs.push("transclude".equals(attributes.getValue("type")));
    }
    if (isElement("document")) {
      startDocument(attributes);
    } else if (isElement("heading") || (isElement("title") && isParent("section"))) {
      startHeading(attributes);
    } else if (isElement("para")) {
      startPara(attributes);
    } else if (isAny("fragment", "xref-fragment", "properties-fragment", "media-fragment")) {
      startFragment(attributes);
    } else if ("block".equals(element)) {
      startBlock(attributes);
    } else if ("blockxref".equals(element) && PSML_MEDIATYPE.equals(attributes.getValue("mediatype")) &&
        ("embed".equals(attributes.getValue("type")) || "transclude".equals(attributes.getValue("type")))) {
      startReference(attributes);
    } else if ("reversexref".equals(element) && !hasAncestor("blockxref")) {
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
    Heading heading;
    if (isParent("section")) {
      this.fragmentLevel = 2;
      heading = Heading.untitled(this.transclusionLevel + SECTION_TITLE_LEVEL, DEFAULT_FRAGMENT, DEFAULT_FRAGMENT,
          this.sectioncounter);
      this.sectioncounter++;
    } else {
      int level = getInt(attributes, "level");
      this.fragmentLevel = getInt(attributes, "level");
      heading = Heading.untitled(this.transclusionLevel + level, this.fragment, this._fragmentIDs.peek(), this.counter);
      if ("true".equals(attributes.getValue("numbered"))) {
        heading = heading.numbered(true);
      }
      String prefix = attributes.getValue("prefix");
      if (prefix != null) {
        heading = heading.prefix(prefix);
      }
      if (isParent("block") && this.currentBlockLabel != null) {
        heading = heading.blocklabel(this.currentBlockLabel);
      }
      this.counter++;
    }

    newBuffer();
    this.currentHeading = heading;
  }

  /**
   * Found `para` element
   *
   * @param attributes The attributes
   */
  private void startPara(Attributes attributes) {
    String prefix = attributes.getValue("prefix");
    String numbered = attributes.getValue("numbered");
    if ("true".equals(numbered) || (!Paragraph.NO_PREFIX.equals(prefix) && prefix != null)) {
      Paragraph para = new Paragraph(getInt(attributes, "indent", 0), this.fragment, this._fragmentIDs.peek(), this.counter);
      if ("true".equals(numbered)) {
        para = para.numbered(true);
      }
      if (prefix != null) {
        para = para.prefix(prefix);
      }
      if (isParent("block") && this.currentBlockLabel != null) {
        para = para.blocklabel(this.currentBlockLabel);
      }
      this._expander.addLeaf(para);
    }
    this.firstHeading = false;
    this.counter++;
  }

  /**
   * Record the ID of the document
   *
   * @param attributes The attributes
   */
  private void startDocument(Attributes attributes) {
    // Only if not already set
    if (this._tree.id() < 0) {
      this._tree.id(getLong(attributes, "id"));
    }
  }

  /**
   * Record the ID of the fragment
   *
   * @param attributes The attributes
   */
  private void startFragment(Attributes attributes) {
    String fragment = getString(attributes, "id");
    this._fragmentIDs.push(fragment);
    // Only if not within a transclusion
    if (!hasAncestor("blockxref")) {
      this.fragment = fragment;
      this._tree.putFragmentLevel(this.fragment, this.fragmentLevel);
      this.counter = 1;
      this.firstHeading = true;
    }
  }

  /**
   * Record the label for a block
   *
   * @param attributes The attributes
   */
  private void startBlock(Attributes attributes) {
      this.currentBlockLabel = getString(attributes, "label");
  }

  /**
   * Found `blockxref` element
   *
   * @param attributes The attributes
   */
  private void startReference(Attributes attributes) {
    long uriid = getLong(attributes, "uriid", -1L).longValue();
    // We eliminate unresolved cross-references
    if (uriid > 0) {
      int level = getInt(attributes, "level", 0);
      String documenttype = getString(attributes, "documenttype", Reference.DEFAULT_TYPE);
      Reference.Type type = Reference.Type.fromString(attributes.getValue("type"));
      String title = computeReferenceTitle(attributes);
      Reference reference = new Reference(level, title, this.fragment, this._fragmentIDs.peek(),
          uriid, type, documenttype, attributes.getValue("frag"));
      if (Reference.Type.EMBED.equals(type)) {
        // use PageSeeder generated title
        newBuffer();
        this.currentReference = reference;
      } else {
        this._expander.addLeaf(reference);
        this.transclusionLevel = level;
      }
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
    // Only add embed/transclude references that haven't been added yet
    if ("embed".equals(type) || "transclude".equals(type)) {
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
          if (this.firstHeading) {
            this._tree.putFragmentHeading(this.fragment, title);
            this.firstHeading = false;
          }
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

    } else if ("block".equals(element)) {
      this.currentBlockLabel = null;

    } else if ("blockxref".equals(element)) {
      // Set the title of the current reference
      Reference reference = this.currentReference;
      if (reference != null) {
        String title = buffer(true);
        if (title != null) {
          reference = reference.title(title);
        }
        int partLevel = getBaseLevel()+1;
        int newlevel = partLevel + reference.level();
        this._expander.add(reference, newlevel);
        this.currentReference = null;
      }
      if (!hasAncestor("blockxref")) {
        this.transclusionLevel = 0;
      }
      if (this._blockxrefs.peek()) {
        this._expander.addLeaf(new TransclusionEnd(this.fragment, this._fragmentIDs.peek()));
      }
      this._blockxrefs.pop();
    } else if ("fragment".equals(element)) {
      this._fragmentIDs.pop();
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
