/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Represents the internal hierarchy of a document independently of its
 * context.
 *
 * The internal hierarchy is constructed from the headings in the document.
 *
 * This class effectively converts the linear structure of the document into a
 * tree structure based on heading levels
 *
 * <pre>
 *
 * Title                    Title                            Title
 *                          +-------------------------+       |
 * 1. Part 1         -->    | Part 1               L1 |  -->  +-+- Part 1
 *                          | +---------------------+ |       | |
 * 1.1. Sub-part 1          | | Sub-part 1       L2 | |       | +--- Sub-part 1
 *                          | +---------------------+ |       | |
 * 1.2. Sub-part 2          | | Sub-part 2       L2 | |       | +-+- Sub-part 2
 *                          | | +-----------------+ | |       |   |
 * 1.2.1 Example A          | | | Example A    L3 | | |       |   +--- Example A
 *                          | | +-----------------+ | |       |   |
 * 1.2.2 Example B          | | | Example B    L3 | | |       |   +--- Example B
 *                          | | +-----------------+ | |       |
 *                          | +---------------------+ |       |
 *                          +-------------------------+       |
 * 2. Part2                 | Part 2               L1 |       +--- Part 2
 * ...
 * </pre>
 *
 * Maintains a list of references for the specified document.
 */
public final class DocumentTree implements Tree, Serializable, XMLWritable {

  /** As per requirement for Serializable. */
  private static final long serialVersionUID = 3L;

  /** When there is no title */
  public static final String NO_PREFIX = "";

  /**
   * URI ID of the document.
   */
  private final long _id;

  /**
   * The actual level of this tree, it should always be 1 for a normalized tree.
   */
  private final transient int _level;

  /**
   * Title of the document.
   */
  private final String _title;

  /**
   * List of URI ID of reverse cross-references to the document (not fragments).
   */
  private final List<Long> _reverse;

  /**
   * List of structural parts in this tree which may be a part or a reference.
   */
  private final List<Part<?>> _parts;

  /** Whether the title is numbered */
  private final boolean _numbered;

  /** Prefix of title if any */
  private final String _prefix;

  /**
   * @param id       The URI ID of the document.
   * @param title    The title the document.
   * @param reverse  The list of reverse references.
   * @param numbered Whether the heading is auto-numbered
   * @param prefix   Any prefix given to the title.
   * @param parts    The list of parts.
   */
  private DocumentTree(long id, String title, List<Long> reverse, boolean numbered, String prefix, List<Part<?>> parts) {
    this._id = id;
    this._title = title;
    this._reverse = Collections.unmodifiableList(reverse);
    this._parts = Collections.unmodifiableList(parts);
    this._level = computeActualLevel();
    this._numbered = numbered;
    this._prefix = prefix;
  }

  /**
   * @param id      The URI ID of the document.
   * @param title   The title the document.
   * @param reverse The list of reverse references.
   * @param parts   The list of parts.
   */
  public DocumentTree(long id, String title, List<Long> reverse, List<Part<?>> parts) {
    this(id, title,  reverse, false, NO_PREFIX, parts);
  }

  @Override
  public long id() {
    return this._id;
  }

  @Override
  public String title() {
    return this._title;
  }

  public int level() {
    return this._level;
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
   * Create a new tree identical to this tree but with the specified title prefix.
   *
   * @param prefix The different title prefix
   *
   * @return A new tree instance.
   */
  public DocumentTree prefix(String prefix) {
    return new DocumentTree(this._id, this._title, this._reverse, this._numbered, prefix, this._parts);
  }

  /**
   * Create a new tree identical to this tree but with the specified numbered flag.
   *
   * @param numbered Whether the title is numbered
   *
   * @return A new tree instance.
   */
  public DocumentTree numbered(boolean numbered) {
    if (this._numbered == numbered) return this;
    return new DocumentTree(this._id, this._title, this._reverse, numbered, this._prefix, this._parts);
  }

  /**
   * Compute the actual level of the document.
   */
  private int computeActualLevel() {
    for (Part<?> p : this._parts) {
      int level = computeActualLevel(p);
      if (level > 0) return level;
    }
    return 0;
  }

  /**
   * Compute the actual level of the part.
   *
   * @param part The part to collect from.
   */
  private static int computeActualLevel(Part<?> part) {
    Element element = part.element();
    if (element.hasTitle()) return element.level();
    for (Part<?> p : part.parts()) {
      int level = computeActualLevel(p);
      if (level > 0) return level;
    }
    return 0;
  }

  /**
   * List of URI ID of reverse cross-references to the document (not fragments).
   */
  @Override
  public List<Long> listReverseReferences() {
    return this._reverse;
  }

  /**
   * List of URI ID of forward cross-references to the document (not fragments).
   */
  @Override
  public List<Long> listForwardReferences() {
    List<Long> uris = new ArrayList<>();
    for (Part<?> c : this._parts) {
      collectReferencesIds(c, uris);
    }
    return uris;
  }

  /**
   * Collect the list of URI IDs from the references in this tree.
   *
   * @param part The part to collect from.
   * @param uris The URIs
   */
  private static void collectReferencesIds(Part<?> part, List<Long> uris) {
    Element element = part.element();
    if (element instanceof Reference) {
      uris.add(((Reference)element).uri());
    }
    for (Part<?> c : part.parts()) {
      collectReferencesIds(c, uris);
    }
  }

  /**
   * Indicates whether the document has any reverse reference.
   */
  @Override
  public boolean isReferenced() {
    return !this._reverse.isEmpty();
  }

  /**
   * @return an unmodifiable list of the structural parts in the tree.
   */
  public List<Part<?>> parts() {
    return this._parts;
  }

  /**
   * Indicates whether this  tree is empty of not.
   *
   * @return <code>true</code> if there are no forward-block references;
   *         <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return this._parts.isEmpty();
  }

  /**
   * Normalize the content tree and return a new content tree.
   *
   * <p>This methods checks normalization is required by comparing the title
   * of the document with the first heading in the document (with and without
   * prefix).
   *
   * <p>If they match, the first heading is removed and other headings adjusted according.
   *
   * <p>If they don't, this content tree is returned.
   *
   * @return a normalized version of this content tree.
   */
  public DocumentTree normalize(TitleCollapse collapse) {
    DocumentTree normalized = this;
    normalized = removePhantomParts(normalized);
    normalized = removeTitleHeading(normalized, collapse);
    return normalized;
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    String reverse = toReverseReferencesString(",");
    xml.openElement("document-tree", this._parts.size() > 0);
    xml.attribute("id", Long.toString(this._id));
    xml.attribute("level", this._level);
    xml.attribute("title", this._title);
    if (reverse.length() > 0) {
      xml.attribute("reverse-references", reverse);
    }
    for (Part<?> p : this._parts) {
      p.toXML(xml);
    }
    xml.closeElement();
  }

  /**
   * Find the reference in this tree.
   *
   * @param uri The URI ID for this reference
   *
   * @return the reference in this tree.

  public @Nullable Reference find(long uri) {
    if (uri <= 0) throw new IllegalArgumentException("URI must be > 0");
    for (Part<?> part : parts()) {
      Reference found = Reference.find(part, uri);
      if (found != null) return found;
    }
    return null;
  }
   */

  @Override
  public @NonNull String toString() {
    return "DocumentTree("+this._id+","+this._title+")";
  }

  @Override
  public void print(Appendable out) {
    try {
      out.append(Long.toString(this._id)).append(':').append(this._title).append('\n');
      for (Part<?> p : this._parts) {
        p.print(out);
      }
    } catch (IOException ex) {
      // Ignore
    }
  }

  // Static helpers
  // ----------------------------------------------------------------------------------------------

  /**
   * Remove the phantom parts from the tree
   *
   * @param tree The tree to process
   * @return a new tree or the same tree if unmodified
   */
  public static DocumentTree removePhantomParts(DocumentTree tree) {
    DocumentTree normalized = tree;
    // TODO optimize
    while (normalized.parts().size() == 1 && !normalized.parts().get(0).hasTitle()) {
      List<Part<?>> unwrapped = new ArrayList<>();
      for (Part<?> p : normalized.parts().get(0).parts()) {
        unwrapped.add(p.adjustLevel(-1));
      }
      normalized = new DocumentTree(tree._id, tree._title, tree._reverse, unwrapped);
    }
    return normalized;
  }

  /**
   * Remove the title heading
   *
   * @param tree     The tree to process
   * @param collapse collapse configuration
   *
   * @return a new tree or the same tree if unmodified
   */
  public static DocumentTree removeTitleHeading(DocumentTree tree, TitleCollapse collapse) {
    List<Part<?>> parts = tree.parts();
    // No normalization
    if (parts.isEmpty() || collapse == TitleCollapse.never) return tree;
    Part<?> firstPart = parts.get(0);
    if (!(firstPart.element() instanceof Heading)) return tree;
    Heading firstHeading = (Heading)firstPart.element();
    DocumentTree normalized = tree;
    // Title heading
    String headingTitle = firstHeading.title();
    String prefixedTitle = firstHeading.getPrefixedTitle();
    String documentTitle = tree._title;
    // The first heading 1 matches the document title and all other headings appear under it
    if (headingTitle.equals(documentTitle) || prefixedTitle.equals(documentTitle) || collapse == TitleCollapse.always) {
      List<Part<?>> children = new ArrayList<>();
      if (parts.size() > 1) {
        // Copy the other parts
        children.addAll(parts.subList(1, parts.size()));
      } else {
        // Copy the parts within that part
        for (Part<?> p : firstPart.parts()) {
          children.add(p.adjustLevel(-1));
        }
      }
      normalized = new DocumentTree(tree._id, tree._title, tree._reverse, children)
          .numbered(firstHeading.numbered()).prefix(firstHeading.prefix());
    }
    return normalized;
  }


  // Inner class
  // ----------------------------------------------------------------------------------------------

  /**
   * Builder for the content tree.
   */
  public static class Builder {

    /** URI ID */
    private final long _id;

    /** Title of the document. */
    private String title = "[untitled]";

    /** List of parts in this tree. */
    private final List<Part<?>> _parts = new ArrayList<>();

    /** List of URI ID of reverse cross-references. */
    private final List<Long> _reverse = new ArrayList<>();

    /**
     * Creates a new builder for this content tree.
     *
     * @param id The URI ID of the document.
     */
    public Builder(long id) {
      if (id <= 0) throw new IllegalArgumentException("URI ID must be positive");
      this._id = id;
    }

    /**
     * Creates a new builder for this content tree.
     *
     * @param id The URI ID of the document.
     */
    public Builder(long id, String title) {
      this(id);
      this.title(title);
    }

    public long id() {
      return this._id;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder parts(List<Part<?>> parts) {
      // XXX Should only be Level 1
      this._parts.addAll(parts);
      return this;
    }

    public Builder part(Part<?> part) {
      // XXX Should only be Level 1
      this._parts.add(part);
      return this;
    }

    public Builder addReverseReference(Long ref) {
      this._reverse.add(ref);
      return this;
    }

    public Builder addReverseReferenceIfNew(Long ref) {
      if (!this._reverse.contains(ref)) {
        this._reverse.add(ref);
      }
      return this;
    }

    public DocumentTree build() {
      // New lists to ensure the builder no longer affects built tree
      List<Part<?>> parts = new ArrayList<>(this._parts);
      List<Long> reverse = new ArrayList<>(this._reverse);
      return new DocumentTree(this._id, this.title, reverse, parts);
    }

  }

}
