/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /** When there is no first heading fragment stored */
  public static final String NO_FRAGMENT = "";

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
   * The document labels (comma separated)
   */
  private final String _labels;

  /**
   * List of URI ID of reverse cross-references to the document (not fragments).
   */
  private final List<Long> _reverse;

  /**
   * List of structural parts in this tree which may be a part or a reference.
   */
  private final List<Part<?>> _parts;

  /**
   * The fragment ID of first (title) heading
   */
  private final String _titlefragment;

  /**
   * Whether the title is numbered
   */
  private final boolean _numbered;

  /**
   * Prefix of title if any
   */
  private final String _prefix;

  /**
   * Map of fragment ID to the first heading in the fragment,
   * otherwise the first preceding heading or section title (within the current section).
   */
  private final Map<String,String> _fragmentheadings;


  /**
   * Map of fragment ID to the level of the fragment (level of closest preceding heading),
   * used for adjusting para indents.
   */
  private final Map<String,Integer> _fragmentlevels;

  /**
   * @param id                The URI ID of the document.
   * @param title             The title the document.
   * @param labels            The document labels
   * @param reverse           The list of reverse references.
   * @param titlefragment     The fragment ID of first (title) heading (only if numbered or prefixed)
   * @param numbered          Whether the heading is auto-numbered
   * @param prefix            Any prefix given to the title.
   * @param parts             The list of parts.
   * @param fragmentheadings  Map of fragment ID to the heading for the fragment
   * @param fragmentlevels    Map of fragment ID to the level of the fragment
   */
  private DocumentTree(long id, String title, String labels, List<Long> reverse, String titlefragment, boolean numbered,
      String prefix, List<Part<?>> parts, Map<String,String> fragmentheadings, Map<String,Integer> fragmentlevels) {
    this._id = id;
    this._title = title;
    this._labels = labels;
    this._reverse = Collections.unmodifiableList(reverse);
    this._parts = Collections.unmodifiableList(parts);
    this._level = computeActualLevel();
    this._titlefragment = titlefragment;
    this._numbered = numbered;
    this._prefix = prefix;
    this._fragmentheadings = Collections.unmodifiableMap(fragmentheadings);
    this._fragmentlevels = Collections.unmodifiableMap(fragmentlevels);
  }

  /**
   * @param id                The URI ID of the document.
   * @param title             The title the document.
   * @param labels            The document labels
   * @param reverse           The list of reverse references.
   * @param parts             The list of parts.
   * @param fragmentheadings  Map of fragment ID to the heading for the fragment
   * @param fragmentlevels    Map of fragment ID to the level of the fragment
   */
  public DocumentTree(long id, String title, String labels, List<Long> reverse,
      List<Part<?>> parts, Map<String,String> fragmentheadings, Map<String,Integer> fragmentlevels) {
    this(id, title, labels, reverse, NO_FRAGMENT, false, NO_PREFIX, parts, fragmentheadings, fragmentlevels);
  }

  @Override
  public long id() {
    return this._id;
  }

  @Override
  public String title() {
    return this._title;
  }

  public String labels() {
    return this._labels;
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
   * @return The fragment ID of first (title) heading (only if numbered or prefixed).
   */
  public String titlefragment() {
    return this._titlefragment;
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
   * @return Map of fragment ID to the heading for the fragment
   */
  public Map<String,String> fragmentheadings() {
    return Collections.unmodifiableMap(this._fragmentheadings);
  }


  /**
   * @return Map of fragment ID to the level of the fragment
   */
  public Map<String,Integer> fragmentlevels() {
    return Collections.unmodifiableMap(this._fragmentlevels);
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
   * List of URI ID of forward cross-references to documents and document fragments.
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
      Reference ref = (Reference)element;
      // don't add references already added
      if (!uris.contains(ref.uri())) {
        uris.add(ref.uri());
      }
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
   * Remove the parts from this tree that are not from the specified fragment.
   * Preserves hierarchy by changing non-fragment ancestor parts to phantoms
   * and removing the top phantoms.
   *
   * @param tree     The tree to process
   * @param fragment The fragment ID to preserve
   *
   * @return a new tree with other fragments removed
   */
  public DocumentTree singleFragmentTree(String fragment) {
    List<Part<?>> parts = removeOtherFragments(this.parts(), fragment, false);
    if (parts == null) parts = new ArrayList<>();
    DocumentTree tree = new DocumentTree(this._id, this._title, this._labels, this._reverse,
        NO_FRAGMENT, false, NO_PREFIX, parts, this._fragmentheadings, this._fragmentlevels);
    return removePhantomParts(tree);
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
    // handle first heading with level > 1
    normalized = removePhantomParts(normalized);
    // collapse first heading
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
    if (this.numbered()) {
      xml.attribute("numbered", "true");
    }
    if (!DocumentTree.NO_PREFIX.equals(this.prefix())) {
      xml.attribute("prefix", this.prefix());
    }
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
      normalized = new DocumentTree(tree._id, tree._title, tree._labels, tree._reverse, tree._titlefragment,
          tree._numbered, tree._prefix, unwrapped, tree._fragmentheadings, tree._fragmentlevels);
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
    if (parts.isEmpty()) return tree;
    Part<?> firstPart = parts.get(0);
    if (!(firstPart.element() instanceof Heading)) return tree;
    Heading firstHeading = (Heading)firstPart.element();
    // Title heading
    String headingTitle = firstHeading.title();
    String prefixedTitle = firstHeading.getPrefixedTitle();
    String documentTitle = tree._title;
    List<Part<?>> children = new ArrayList<>();
    // The first heading 1 matches the document title or always collapse
    if ((headingTitle.equals(documentTitle) || prefixedTitle.equals(documentTitle) || collapse == TitleCollapse.always)
        && collapse != TitleCollapse.never) {
      if (parts.size() > 1) {
        // if first heading has children add it as a phantom
        if (firstPart.size() > 0) {
          children.add(new Part<>(
              new Phantom(firstHeading.level(), firstHeading.fragment()), firstPart.parts()));
        }
        // Copy the other parts
        children.addAll(parts.subList(1, parts.size()));
      } else {
        // Copy the parts within that part
        for (Part<?> p : firstPart.parts()) {
          children.add(p.adjustLevel(-1));
        }
      }
    } else if (!firstHeading.numbered() && NO_PREFIX.equals(firstHeading.prefix())){
      return tree;
    } else {
      children.add(firstPart.element(firstHeading.numbered(false).prefix(NO_PREFIX)));
      if (parts.size() > 1) {
        // Copy the other parts
        children.addAll(parts.subList(1, parts.size()));
      }
    }
    // Always move the numbered and prefix from the first heading to the tree
    return new DocumentTree(tree._id, tree._title, tree._labels, tree._reverse, firstHeading.fragment(),
        firstHeading.numbered(), firstHeading.prefix(), children, tree._fragmentheadings, tree._fragmentlevels);
  }

  /**
   * Remove the parts from the tree that are not from the specified fragment.
   *
   * @param parts    The existing parts
   * @param fragment The fragment ID to preserve
   * @param found    Whether the fragment has been found
   *
   * @return the modified parts or <code>null</code> if fragment not found
   */
  public static List<Part<?>> removeOtherFragments(List<Part<?>> parts, String fragment, boolean found) {
    List<Part<?>> modified = new ArrayList<>();
    if (!parts.isEmpty()) {
      for (Part<?> part : parts) {
        Element el = part.element();
        if (fragment.equals(el.fragment())) {
          modified.add(new Part<>(el,
              removeOtherFragments(part.parts(), fragment, true)));
        } else if (!found) {
          List<Part<?>> branch = removeOtherFragments(part.parts(), fragment, false);
          // if found in branch change this element to a phantom
          if (branch != null) {
            modified.add(new Part<>(new Phantom(el.level(), el.fragment()), branch));
          }
        }
      }
    }
    return modified.isEmpty() && !found ? null : modified;
  }

  // Inner class
  // ----------------------------------------------------------------------------------------------

  /**
   * Builder for the content tree.
   */
  public static class Builder {

    /** URI ID */
    private long _id = -1;

    /** Title of the document. */
    private String title = "[untitled]";

    /** Document labels (comma separated). */
    private String labels = "";

    /** List of parts in this tree. */
    private final List<Part<?>> _parts = new ArrayList<>();

    /** List of URI ID of reverse cross-references. */
    private final List<Long> _reverse = new ArrayList<>();

    /**
     * Map of fragment ID to the first heading in the fragment,
     * otherwise the first preceding heading or section title (within the current section).
     */
    private final Map<String,String> _fragmentheadings = new HashMap<>();

    /**
     * Map of fragment ID to the level of the fragment (level of closest preceding heading),
     * used for adjusting para indents.
     */
    private final Map<String,Integer> _fragmentlevels = new HashMap<>();;

    /**
     * Creates a new builder for this content tree (id must be set before calling build).
     */
    public Builder() {
    }

    /**
     * Creates a new builder for this content tree.
     *
     * @param id The URI ID of the document.
     */
    public Builder(long id) {
      if (id < 0) throw new IllegalArgumentException("URI ID must be positive");
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

    public Builder id(long id) {
      if (id < 0) throw new IllegalArgumentException("URI ID must be positive");
      this._id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder labels(String labels) {
      this.labels = labels;
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

    public Builder putFragmentHeading(String fragment, String heading) {
      this._fragmentheadings.put(fragment, heading);
      return this;
    }

    public Builder putFragmentLevel(String fragment, int level) {
      this._fragmentlevels.put(fragment, level);
      return this;
    }

    public DocumentTree build() {
      if (this._id < 0) throw new IllegalStateException("URI ID must be set");
      // New lists to ensure the builder no longer affects built tree
      List<Part<?>> parts = new ArrayList<>(this._parts);
      List<Long> reverse = new ArrayList<>(this._reverse);
      Map<String,String> fragmentheadings = new HashMap<>(this._fragmentheadings);
      Map<String,Integer> fragmentlevels = new HashMap<>(this._fragmentlevels);
      return new DocumentTree(this._id, this.title, this.labels, reverse, parts, fragmentheadings, fragmentlevels);
    }

  }

}
