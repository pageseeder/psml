/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Represents the internal hierarchy of a document independently of its
 * context.
 *
 * <p>The internal hierarchy is constructed from the headings in the document.
 *
 * <p>This class effectively converts the linear structure of the document into a
 * tree structure based on heading levels
 *
 * <pre>{@code
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
 * }</pre>
 *
 * <p>Maintains a list of references for the specified document.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 *
 * @version 1.0
 * @since 1.0
 */
public final class DocumentTree implements Tree, Serializable, XMLWritable {

  /** As per requirement for Serializable. */
  private static final long serialVersionUID = 3L;

  /** When there is no title */
  public static final String NO_PREFIX = "";

  /** When there is no block label */
  public static final String NO_BLOCK_LABEL = "";

  /** When there is no first heading fragment stored */
  public static final String NO_FRAGMENT = "";

  /**
   * URI ID of the document.
   */
  private final long _id;

  /**
   * The actual level of this tree (i.e. the level of the first heading left in the tree after normalization).
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
   * Parent block label if any
   */
  private final String _blocklabel;

  /**
   * Document's last edited date (including transclusion edited dates)
   */
  private final OffsetDateTime _lastedited;

  /**
   * Document's path
   */
  private final String _path;

  /**
   * Map of fragment ID to the first heading in the fragment,
   * otherwise the first preceding heading or section title (within the current section).
   */
  private final Map<String,String> _fragmentheadings;

  /**
   * Whether the _fragmentheadings are unescaped XML (may be <code>null</code> for old cached trees)
   */
  private final Boolean _xmlheadings;

  /**
   * Map of fragment ID to the level of the fragment (level of closest preceding heading),
   * used for adjusting para indents.
   */
  private final Map<String,Integer> _fragmentlevels;

  /**
   * @param id                The URI ID of the document.
   * @param level             The level of the first part of the tree.
   * @param title             The title the document.
   * @param labels            The document labels
   * @param reverse           The list of reverse references.
   * @param titlefragment     The fragment ID of first (title) heading (only if numbered or prefixed)
   * @param numbered          Whether the heading is auto-numbered
   * @param prefix            Any prefix given to the title.
   * @param blocklabel        The parent block label (from first heading)
   * @param lastedited        The document's last edited date (including transclusion edited dates)
   * @param path              The document's path
   * @param parts             The list of parts.
   * @param fragmentheadings  Map of fragment ID to the heading for the fragment
   * @param fragmentlevels    Map of fragment ID to the level of the fragment
   */
  public DocumentTree(long id, int level, String title, String labels, List<Long> reverse, String titlefragment, boolean numbered,
      String prefix, String blocklabel, OffsetDateTime lastedited, String path,
      List<Part<?>> parts, Map<String,String> fragmentheadings, Map<String,Integer> fragmentlevels) {
    this._id = id;
    this._title = title;
    this._labels = labels;
    this._reverse = Collections.unmodifiableList(reverse);
    this._parts = Collections.unmodifiableList(parts);
    this._level = level;
    this._titlefragment = titlefragment;
    this._numbered = numbered;
    this._prefix = prefix;
    this._blocklabel = blocklabel;
    this._lastedited = lastedited;
    this._path = path;
    this._fragmentheadings = Collections.unmodifiableMap(fragmentheadings);
    this._xmlheadings = true;
    this._fragmentlevels = Collections.unmodifiableMap(fragmentlevels);
  }

  /**
   * @param id                The URI ID of the document.
   * @param title             The title the document.
   * @param labels            The document labels
   * @param lastedited        The document's last edited date (including transclusion edited dates)
   * @param path              The document's path
   * @param reverse           The list of reverse references.
   * @param parts             The list of parts.
   * @param fragmentheadings  Map of fragment ID to the heading for the fragment
   * @param fragmentlevels    Map of fragment ID to the level of the fragment
   */
  public DocumentTree(long id, String title, String labels, OffsetDateTime lastedited, String path, List<Long> reverse,
      List<Part<?>> parts, Map<String,String> fragmentheadings, Map<String,Integer> fragmentlevels) {
    this(id, calculateLevel(parts), title, labels, reverse,
        NO_FRAGMENT, false, NO_PREFIX, NO_BLOCK_LABEL, lastedited, path, parts, fragmentheadings, fragmentlevels);
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
   * @return The blocklabel parent of this heading or empty if none.
   */
  public String blocklabel() {
    return this._blocklabel;
  }

  /**
   * @return The document's last edited date (including transclusion edited dates).
   */
  public @Nullable OffsetDateTime lastedited() {
    return this._lastedited;
  }

  /**
   * @return The document's path.
   */
  public String path() {
    return this._path;
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
   * @return Whether the _fragmentheadings are unescaped XML
   */
  public boolean xmlheadings() {
    return this._xmlheadings == null ? false : this._xmlheadings;
  }

  /**
   * @return Map of fragment ID to the level of the fragment
   */
  public Map<String,Integer> fragmentlevels() {
    return Collections.unmodifiableMap(this._fragmentlevels);
  }

  /**
   * List of URI ID of reverse cross-references to the document and document fragments.
   */
  @Override
  public List<Long> listReverseReferences() {
    return this._reverse;
  }

  /**
   * List of URI ID of forward cross-references to documents and document fragments (including transclusions).
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
   * List of all forward references to documents and document fragments (including transclusions).
   * @return The corresponding references
   */
  public List<Reference> listReferences() {
    List<Reference> refs = new ArrayList<>();
    for (Part<?> c : this._parts) {
      collectReferences(c, refs);
    }
    return refs;
  }

  /**
   * Check if this tree contains a heading, embed reference or visible paras.
   *
   * @param config    The publication config for checking para visibility (optional)
   * @param fragment  The fragment for the heading or reference (optional)
   *
   * @return <code>true</code> if heading or reference found.
   */
  public boolean hasVisibleItems(@Nullable PublicationConfig config, @Nullable String fragment) {
    for (Part<?> c : this._parts) {
      if (hasVisibleItems(c, config, fragment)) return true;
    }
    return false;
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
   * Collect the list of references in this tree.
   *
   * @param part The part to collect from.
   * @param refs The references
   */
  private static void collectReferences(Part<?> part, List<Reference> refs) {
    Element element = part.element();
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      refs.add(ref);
    }
    for (Part<?> c : part.parts()) {
      collectReferences(c, refs);
    }
  }

  /**
   * Check if this part or it's children has a heading, embed reference or visible para.
   *
   * @param part      The part to check.
   * @param config    The publication config for checking para visibility (optional)
   * @param fragment  The fragment for the heading or reference (optional)
   *
   * @return <code>true</code> if heading or reference found.
   */
  private static boolean hasVisibleItems(Part<?> part, @Nullable PublicationConfig config, @Nullable String fragment) {
    Element element = part.element();
    if (((element instanceof Reference && Reference.Type.EMBED.equals(((Reference)element).type())) ||
          element instanceof Heading ||
         (element instanceof Paragraph && ((Paragraph)element).isVisible(config))) &&
        (fragment == null || fragment.equals(element.fragment()))) {
      return true;
    }
    for (Part<?> c : part.parts()) {
      if (hasVisibleItems(c, config, fragment)) return true;
    }
    return false;
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
   * @param fragment The fragment ID to preserve
   *
   * @return a new tree with other fragments removed
   */
  public DocumentTree singleFragmentTree(String fragment) {
    List<Part<?>> parts = removeOtherFragments(this.parts(), fragment, false);
    if (parts == null) parts = new ArrayList<>();
    DocumentTree tree = new DocumentTree(this._id, this._title, this._labels, this._lastedited,
            this._path, this._reverse, parts, this._fragmentheadings, this._fragmentlevels);
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
   * @param collapse how headings should be computed
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
        unwrapped.add(p);//.adjustLevel(-1));
      }
      normalized = new DocumentTree(tree._id,  calculateLevel(unwrapped),
          tree._title, tree._labels, tree._reverse, tree._titlefragment,
          tree._numbered, tree._prefix, tree._blocklabel, tree._lastedited,
          tree._path, unwrapped, tree._fragmentheadings, tree._fragmentlevels);
    }
    return normalized;
  }

  /**
   * Get the level of the first phantom or heading part in the list.
   *
   * @param parts  the list of parts
   *
   * @return the level
   */
  private static int calculateLevel(List<Part<?>> parts) {
    int level = 0;
    for (Part<?> part : parts) {
      Element el = part.element();
      if (el instanceof Phantom || el instanceof Heading) {
        return el.level();
      }
    }
    return level;
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
    // If the first heading 1 matches the document title or always collapse and only one part, then collapse
    if ((headingTitle.equals(documentTitle) || prefixedTitle.equals(documentTitle) || collapse == TitleCollapse.always)
        && collapse != TitleCollapse.never && parts.size() == 1) {
      // Copy the parts within that part
      List<Part<?>> children = new ArrayList<>();
      for (Part<?> p : firstPart.parts()) {
        children.add(p);//.adjustLevel(-1));
      }
      // Move the title, numbered and prefix from the first heading to the tree
      return new DocumentTree(tree._id, firstHeading.level() + 1, firstHeading.title(),
              tree._labels, tree._reverse, firstHeading.fragment(),
              firstHeading.numbered(), firstHeading.prefix(), firstHeading.blocklabel(),
              tree._lastedited, tree._path, children, tree._fragmentheadings, tree._fragmentlevels);
    }
    return tree;
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
  public static @Nullable List<Part<?>> removeOtherFragments(List<Part<?>> parts, String fragment, boolean found) {
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
            modified.add(new Part<>(new Phantom(el.level(), el.fragment(), el.originalFragment()), branch));
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
    private long id = -1;

    /** Title of the document. */
    private String title = "[untitled]";

    /** Document labels (comma separated). */
    private String labels = "";

    /** Document last edited date (including transclusions). */
    private @Nullable OffsetDateTime lastedited = null;

    /** Document URI path. */
    private String path = "";

    /** List of parts in this tree. */
    private final List<Part<?>> parts = new ArrayList<>();

    /** List of URI ID of reverse cross-references. */
    private final List<Long> reverse = new ArrayList<>();

    /**
     * Map of fragment ID to the first heading in the fragment,
     * otherwise the first preceding heading or section title (within the current section).
     */
    private final Map<String,String> fragmentheadings = new HashMap<>();

    /**
     * Map of fragment ID to the level of the fragment (level of closest preceding heading),
     * used for adjusting para indents.
     */
    private final Map<String,Integer> fragmentlevels = new HashMap<>();

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
      this.id = id;
    }

    /**
     * Creates a new builder for this content tree.
     *
     * @param id The URI ID of the document.
     * @param title The title of the document.
     */
    public Builder(long id, String title) {
      this(id);
      this.title(title);
    }

    public long id() {
      return this.id;
    }

    public Builder id(long id) {
      if (id < 0) throw new IllegalArgumentException("URI ID must be positive");
      this.id = id;
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

    public Builder lastedited(OffsetDateTime lastedited) {
      this.lastedited = lastedited;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder parts(List<Part<?>> parts) {
      // XXX Should only be Level 1
      this.parts.addAll(parts);
      return this;
    }

    public Builder part(Part<?> part) {
      // XXX Should only be Level 1
      this.parts.add(part);
      return this;
    }

    public Builder addReverseReference(Long ref) {
      this.reverse.add(ref);
      return this;
    }

    public Builder addReverseReferenceIfNew(Long ref) {
      if (!this.reverse.contains(ref)) {
        this.reverse.add(ref);
      }
      return this;
    }

    public Builder putFragmentHeading(String fragment, String heading) {
      this.fragmentheadings.put(fragment, heading);
      return this;
    }

    public Builder putFragmentLevel(String fragment, int level) {
      this.fragmentlevels.put(fragment, level);
      return this;
    }

    public DocumentTree build() {
      if (this.id < 0) throw new IllegalStateException("URI ID must be set");
      // New lists to ensure the builder no longer affects built tree
      List<Part<?>> parts = new ArrayList<>(this.parts);
      List<Long> reverse = new ArrayList<>(this.reverse);
      Map<String,String> fragmentheadings = new HashMap<>(this.fragmentheadings);
      Map<String,Integer> fragmentlevels = new HashMap<>(this.fragmentlevels);
      return new DocumentTree(this.id, this.title, this.labels, this.lastedited,
              this.path, reverse, parts, fragmentheadings, fragmentlevels);
    }

  }

}
