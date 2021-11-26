/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.xml.BasicHandler;
import org.xml.sax.Attributes;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Objects;

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
   * Whether to store paragraph titles (turn off to save memory)
   */
  private boolean paraTitles = true;

  /**
   * The last heading being processed
   */
  private @Nullable Heading currentHeading = null;

  /**
   * The last reference being processed
   */
  private @Nullable Reference currentReference = null;

  /**
   * The last paragraph being processed
   */
  private @Nullable Paragraph currentParagraph = null;

  /**
   * Whether this is the first heading in fragment (not preceded by para)
   */
  private boolean firstHeading = false;

  /**
   * Whether to ignore elements (used for compare and metadata content).
   */
  private boolean ignore = false;

  /**
   * If larger than 0 then inside a media-fragment
   * **/
  private int inMediaFragment = 0;

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
   * Buffer for placeholder content
   */
  private @Nullable StringBuilder placeholderContent = null;

  /**
   * Last edited date (including transclusions)
   */
  private @Nullable OffsetDateTime lastEdited = null;

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

  /**
   * @param store  Whether to store paragraph titles (default true, turn off to save memory)
   */
  public void setParaTitles(boolean store) {
    this.paraTitles = store;
  }

  @Override
  public void startElement(String element, Attributes attributes) {
    if (isElement("media-fragment")) {
      this.inMediaFragment++;
    }
    if (this.inMediaFragment > 0) return;
    try {
      if (isElement("fragmentinfo") && attributes.getValue("structure-modified") != null) {
        OffsetDateTime modified = OffsetDateTime.parse(attributes.getValue("structure-modified"));
        if (this.lastEdited == null || this.lastEdited.isBefore(modified)) {
          this.lastEdited = modified;
        }
      } else if (isElement("locator") && attributes.getValue("modified") != null) {
        OffsetDateTime modified = OffsetDateTime.parse(attributes.getValue("modified"));
        if (this.lastEdited == null || this.lastEdited.isBefore(modified)) {
          this.lastEdited = modified;
        }
      }
    } catch (DateTimeParseException ex) {
    }
    if (this.ignore) return;
    if ("blockxref".equals(element)) {
      this._blockxrefs.push("transclude".equals(attributes.getValue("type")));
    }
    if (isElement("fragmentinfo") || isElement("metadata")) {
      this.ignore = true;
    } else if (isElement("document")) {
      startDocument(attributes);
    } else if (isElement("toc")) {
      this._expander.addLeaf(new Toc());
      // if not inside a transclusion reset heading level to 1 so front matter doesn't affect levels
      if (!hasAncestor("blockxref") && this.currentHeading != null) {
        this.currentHeading = Heading.untitled(1, DEFAULT_FRAGMENT, DEFAULT_FRAGMENT, 1);
      }
    } else if (isElement("heading") || (isElement("title") && isParent("section"))) {
      startHeading(attributes);
    } else if (isElement("property")) {
      startProperty(attributes);
    } else if (isElement("para")) {
      startPara(attributes);
    } else if (isAny("fragment", "xref-fragment", "properties-fragment")) {
      startFragment(attributes);
    } else if ("block".equals(element)) {
      startBlock(attributes);
    } else if ("blockxref".equals(element) &&
        ("embed".equals(attributes.getValue("type")) || "transclude".equals(attributes.getValue("type")))) {
      startReference(attributes);
    } else if ("reversexref".equals(element) && !hasAncestor("blockxref")) {
      startReverseRef(attributes);
    } else if (isElement("displaytitle") && !hasAncestor("blockxref")) {
      newBuffer();
    } else if (isElement("labels") && !hasAncestor("blockxref") && !hasAncestor("xref")) { // handle xref with type="math"
      newBuffer();
    } else if (isElement("placeholder")) {
      startPlaceholder(attributes);
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
    this.currentParagraph = para;
    newBuffer();
    this.firstHeading = false;
    this.counter++;
  }

  /**
   * Found `placeholder` element
   *
   * @param attributes The attributes
   */
  private void startPlaceholder(Attributes attributes) {
    // if placeholder not resolved or unresolved, then replace content with [my-property] so it can be resolved later
    String name = attributes.getValue("name");
    if (attributes.getValue("resolved") == null && attributes.getValue("unresolved") == null &&
        buffer() != null && name != null) {
      this.placeholderContent = new StringBuilder().append(buffer(true)).append("[").append(name).append("]");
    }
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
    if (!hasAncestor("blockxref") && !hasAncestor("xref")) { // xref with math type can be transcluded
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
      String documenttype = PSML_MEDIATYPE.equals(attributes.getValue("mediatype")) ?
          getString(attributes, "documenttype", Reference.DEFAULT_TYPE) : null;
      Reference.Type type = Reference.Type.fromString(attributes.getValue("type"));
      // get the fallback title - actual title is taken from XRef content if not empty
      String title = computeReferenceTitle(attributes);
      String display = attributes.getValue("display");
      Reference reference = new Reference(level, title, this.fragment, this._fragmentIDs.peek(),
          uriid, type, documenttype, attributes.getValue("frag"),
          display == null || "document".equals(display) ? Boolean.TRUE : Boolean.FALSE);
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
    // Only add embed references that haven't been added yet
    if ("embed".equals(type)) {
      this._tree.addReverseReferenceIfNew(ref);
    }
  }

  /**
   * Found `property` element
   *
   * @param attributes The attributes
   */
  private void startProperty(Attributes attributes) {
    String value = attributes.getValue("value");
    if (value != null) {
      if (this.firstHeading) {
        // set first property value as heading
        this._tree.putFragmentHeading(this.fragment, value);
        this.firstHeading = false;
      }
    }
  }

  @Override
  public void endElement(String element) {
    if ((isElement("fragmentinfo") || isElement("metadata")) && this.inMediaFragment == 0) {
      this.ignore = false;
    } else if (isElement("media-fragment")) {
      this.inMediaFragment--;
    }
    if (this.ignore || this.inMediaFragment > 0) return;
    if ("placeholder".equals(element) && this.placeholderContent != null) {
      newBuffer();
      append(this.placeholderContent.toString());
      this.placeholderContent = null;
    } else if ("heading".equals(element) || (isElement("title") && isParent("section"))) {
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

    } else if ("para".equals(element) && this.currentParagraph != null) {
      String title = buffer(true);
      Paragraph para = this.currentParagraph;
      // only store content for numbered paras to save memory
      if (title != null && para.numbered() && this.paraTitles) {
        // if no wrapping blocklabel truncate title to 40 chars
        if ("".equals(para.blocklabel()) && title.length() > 40) {
          title = title.substring(0, 40) + "...";
        // else truncate title to 100 chars to save memory
        } else if (title.length() > 100) {
          title = title.substring(0, 100) + "...";
        }
        para = para.title(title);
      }
      this._expander.addLeaf(para);
      this.currentParagraph = null;

    } else if ("displaytitle".equals(element) && !hasAncestor("blockxref")) {
      String title = buffer(true);
      if (title != null) {
        this._tree.title(title);
      }

    } else if ("labels".equals(element) && !hasAncestor("blockxref") && !hasAncestor("xref")) { // handle xref with type="math"
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
    } else if ("fragment".equals(element) || "xref-fragment".equals(element) || "properties-fragment".equals(element)) {
      this._fragmentIDs.pop();
    } else if ("document".equals(element) && !hasAncestor("blockxref")) {
      this._tree.lastedited(this.lastEdited);
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