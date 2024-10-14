/*
 * Copyright (c) 2017-2018 Allette Systems
 */
package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.xmlwriter.XML;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * An immutable tree aggregating multiple trees together in order to generate a
 * deep table of contents.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 */
public final class PublicationTree implements Tree, Serializable, XMLWritable {

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(PublicationTree.class);

  /** As per requirement for Serializable */
  private static final long serialVersionUID = 4L;

  /**
   * Maximum number of reverse references to follow when serializing to XML
   */
  private static final int MAX_REVERSE_FOLLOW = 100;

  /**
   * Stores the state of the TOC as it is being serialized.
   *
   */
  private final class TOCState {

    /**
     * The ID of the content tree (leaf).
     */
    private long cid;

    /**
     * If not -1 output content tree only at this position (occurrence number) in the tree.
     */
    private int cposition;

    /**
     * The IDs of trees that cid is a descendant of (optional)
     */
    private @Nullable List<Long> trees;

    /**
     * The fragment numbering for the publication (optional)
     */
    private @Nullable FragmentNumbering number;

    /**
     * The config for the publication (optional)
     */
    private @Nullable PublicationConfig config;

    /**
     * Map of [uriid], [number of uses]
     */
    private Map<Long,Integer> doccount = new HashMap<>();

    /**
     * List of the current ancestor tree ID-fragment
     */
    private List<String> ancestors = new ArrayList<>();

    /**
     * Whether to output references to IDs not in this publication tree.
     */
    private boolean externalrefs;

    /**
     * Constructor
     *
     * @param cid           The ID of the content tree (leaf).
     * @param cposition     If not -1 output content tree only at this position (occurrence number) in the tree.
     * @param trees         The IDs of trees that cid is a descendant of (optional)
     * @param number        The fragment numbering for the publication (optional)
     * @param config        The config for the publication (optional)
     * @param externalrefs  Whether to output references to IDs not in this publication tree.
     */
    private TOCState(long cid, int cposition, @Nullable List<Long> trees, @Nullable FragmentNumbering number,
        @Nullable PublicationConfig config, boolean externalrefs) {
      this.cid = cid;
      this.cposition = cposition;
      this.trees = trees;
      this.number = number;
      this.config = config;
      this.externalrefs = externalrefs;
    }
  }

  /**
   * The ID of the root of the tree (-1 for blank tree).
   */
  private final long _rootid;

  /**
   * Map of trees that make up this tree.
   */
  private final Map<Long, DocumentTree> _map;

  /**
   * Map of transcluded Id to a list of it's parent Ids in this publication.
   * If list contains -1 then Id is also embedded.
   */
  private final Map<Long,List<Long>> _transclusions;

  /**
   * Creates a blank publication tree
   */
  public PublicationTree() {
    this._map = Collections.emptyMap();
    this._rootid = -1;
    this._transclusions = new HashMap<>();
  }

  /**
   * Creates a simple publication tree wrapping a document tree
   *
   * @param tree The document tree.
   */
  public PublicationTree(DocumentTree tree) {
    this._map = Collections.singletonMap(tree.id(), tree);
    this._rootid = tree.id();
    this._transclusions = new HashMap<>();
  }

  /**
   * Creates a new tree wrapping existing publication tree with another document tree.
   *
   * <p>Note: the parent tree should have at least one reference to the publication tree.
   *
   * @param parent The new root for the tree
   * @param trunk  The rest of the tree
   */
  private PublicationTree(DocumentTree parent, PublicationTree trunk) {
    Map<Long, DocumentTree> map = new HashMap<>(trunk._map);
    map.put(parent.id(), parent);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = parent.id();
    this._transclusions = new HashMap<>();
  }

  /**
   * Creates a new tree appending existing publication tree with another document tree.
   *
   * <p>Note: the tree should have at least one reference from the existing publication tree.
   *
   * @param trunk  The rest of the tree
   * @param tree   The new tree to add
   */
  private PublicationTree(PublicationTree trunk, DocumentTree tree) {
    Map<Long, DocumentTree> map = new HashMap<>(trunk._map);
    map.put(tree.id(), tree);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = trunk._rootid;
    this._transclusions = Collections.unmodifiableMap(trunk.transclusions());
  }

  /**
   * Creates a new publication from an existing publication by first removing trees with specified IDs,
   * then adding new trees provided.
   *
   * <p>Note: the trees should have at least one reference from the existing publication.
   *
   * @param pub         The existing publication
   * @param removeIds   The IDs of trees to remove
   * @param trees       The map of new document trees to add
   * @param rootid      The ID of the root tree for this publication
   */
  private PublicationTree(PublicationTree pub, List<Long> removeIds, Map<Long, DocumentTree> trees, long rootid) {
    if (pub.id() != rootid) {
      LOGGER.error("Changing publication root id from " + pub.id() + " to " + rootid);
    }
    Map<Long, DocumentTree> map = new HashMap<>(pub._map);
    for (Long id : removeIds) {
      if (id == rootid) {
        LOGGER.error("Attempt to remove publication root id " + pub.id() +
                ", removeIds " + removeIds + ", addIds " + trees.keySet());
      } else {
        map.remove(id);
      }
    }
    map.putAll(trees);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = rootid;
    this._transclusions = Collections.unmodifiableMap(pub.transclusions());
  }

  /**
   * Creates a new publication from an existing publication by first removing trees with specified IDs,
   * then adding new trees provided.
   *
   * <p>Note: the trees should have at least one reference from the existing publication.
   *
   * @param pub           The existing publication
   * @param removeIds     The IDs of trees to remove
   * @param trees         The map of new document trees to add
   * @param transclusions Map of transcluded Id to a list of it's parent Ids in this publication.
   *                      If list contains -1 then Id is also embedded.
   * @param rootid        The ID of the root tree for this publication
   */
  private PublicationTree(PublicationTree pub, List<Long> removeIds, Map<Long, DocumentTree> trees,
      Map<Long,List<Long>> transclusions, long rootid) {
    if (pub.id() != rootid) {
      LOGGER.error("Changing publication root id from " + pub.id() + " to " + rootid);
    }
    Map<Long, DocumentTree> map = new HashMap<>(pub._map);
    for (Long id : removeIds) {
      if (id == rootid) {
        LOGGER.error("Attempt to remove publication root id " + pub.id() +
                ", removeIds " + removeIds + ", addIds " + trees.keySet());
      } else {
        map.remove(id);
      }
    }
    map.putAll(trees);
    this._map = Collections.unmodifiableMap(map);
    this._rootid = rootid;
    this._transclusions = Collections.unmodifiableMap(transclusions);
  }

  /**
   * @return The URI ID of the root
   */
  @Override
  public long id() {
    return this._rootid;
  }

  /**
   * @return The list of reverse references from the root.
   */
  @Override
  public List<Long> listReverseReferences() {
    return root().listReverseReferences();
  }

  /**
   * @return The list of URI ID of all forward cross-references.
   */
  @Override
  public List<Long> listForwardReferences() {
    Set<Long> uris = new HashSet<>();
    for (DocumentTree tree : this._map.values()) {
      uris.addAll(tree.listForwardReferences());
    }
    return new ArrayList<>(uris);
  }

  /**
   * Get's the text of the first heading in the fragment,
   * otherwise the first preceding heading or section title (within the current section).
   *
   * @param treeid    the URI ID for the document tree
   * @param fragment  the fragment ID.
   *
   * @return the heading text which could include markup or <code>null</code> if none found
   */
  public @Nullable String getFragmentHeading(long treeid, String fragment) {
    DocumentTree t = tree(treeid);
    if (t == null) return null;
    String h = t.fragmentheadings().get(fragment);
    if (h == null) return null;
    if (t.xmlheadings()) {
      return h;
    } else {
      return XMLUtils.escape(h);
    }
  }

  /**
   * @return The title of the root.
   */
  @Override
  public String title() {
    return root().title();
  }

  /**
   * @return Indicates whether this publication contains the tree specified by its URI ID.
   */
  public boolean containsTree(long id) {
    return this._map.containsKey(id);
  }

  /**
   * @return the root of this tree.
   */
  public DocumentTree root() {
    return this._map.get(this._rootid);
  }


  /**
   * @return the map of transcluded Id to a list of it's parent Ids in this publication.
   *         If list contains -1 then Id is also embedded.
   */
  public Map<Long,List<Long>> transclusions() {
    if (this._transclusions == null) return new HashMap<>();
    return new HashMap<>(this._transclusions);
  }

  /**
   * @param id  the tree ID
   *
   * @return the a tree in the publication.
   */
  public DocumentTree tree(long id) {
    return this._map.get(id);
  }

  /**
   * Create a new publication tree by adding the specified document tree.
   *
   * @param tree The document tree to add
   *
   * @return The new publication tree
   */
  public PublicationTree add(DocumentTree tree) {
    return new PublicationTree(this, tree);
  }

  /**
   * Creates a new publication tree by first removing trees with the specified IDs,
   * then adding the new trees provided.
   *
   * <p>Note: the trees should have at least one reference from the existing publication.
   *
   * @param removeIds   The IDs of trees to remove
   * @param trees       The map of new document trees to add
   * @param rootid      The ID of the root tree for this publication
   */
  public PublicationTree modify(List<Long> removeIds, Map<Long, DocumentTree> trees, long rootid) {
    return new PublicationTree(this, removeIds, trees, rootid);
  }

  /**
   * Creates a new publication tree by first removing trees with the specified IDs,
   * then adding the new trees provided.
   *
   * <p>Note: the trees should have at least one reference from the existing publication.
   *
   * @param removeIds     The IDs of trees to remove
   * @param trees         The map of new document trees to add
   * @param transclusions Map of transcluded Id to a list of it's parent Ids in this publication.
   *                      If list contains -1 then Id is also embedded.
   * @param rootid        The ID of the root tree for this publication
   */
  public PublicationTree modify(List<Long> removeIds, Map<Long, DocumentTree> trees,
      Map<Long,List<Long>> transclusions, long rootid) {
    return new PublicationTree(this, removeIds, trees, transclusions, rootid);
  }

  /**
   * Create a new root tree by adding the specified root.
   *
   * @param root The root to add
   *
   * @return The new root tree
   */
  public PublicationTree root(DocumentTree root) {
    return new PublicationTree(root, this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int hashCode = 1;
    for (Long id : this._map.keySet()) {
      hashCode = prime*hashCode + Long.hashCode(id);
    }
    return hashCode;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    PublicationTree other = (PublicationTree)o;
    return !ids().equals(other.ids());
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(xml, -1, -1, null, null, true);
  }

  /**
   * Serialize the partial tree down to content ID.
   *
   * @param xml           The XML writer
   * @param cid           The ID of the content tree (leaf). If -1 output all.
   * @param cposition     If not -1 output content tree only at this position (occurrence number) in the tree.
   * @param number        The fragment numbering for the publication (optional)
   * @param externalrefs  Whether to output references to IDs not in this publication tree.
   *
   * @throws IOException If thrown by XML writer
   */
  public void toXML(XMLWriter xml, long cid, int cposition, @Nullable FragmentNumbering number, boolean externalrefs) throws IOException {
    toXML(xml, cid, cposition, number, null, externalrefs);
  }

  /**
   * Serialize the partial tree down to content ID.
   *
   * @param xml           The XML writer
   * @param cid           The ID of the content tree (leaf). If -1 output all.
   * @param cposition     If not -1 output content tree only at this position (occurrence number) in the tree.
   * @param number        The fragment numbering for the publication (optional)
   * @param config        The config for the publication (optional)
   * @param externalrefs  Whether to output references to IDs not in this publication tree.
   *
   * @throws IOException If thrown by XML writer
   */
  public void toXML(XMLWriter xml, long cid, int cposition, @Nullable FragmentNumbering number,
      @Nullable PublicationConfig config, boolean externalrefs) throws IOException {
    xml.openElement("publication-tree", true);
    DocumentTree root = tree(cposition == -1 ? this._rootid : cid);
    if (root != null) {
      xml.attribute("uriid", Long.toString(root.id()));
      xml.attribute("title", root.title());
      if (!"".equals(root.labels())) {
        xml.attribute("labels", root.labels());
      }
      if (root.lastedited() != null) {
        xml.attribute("last-edited", root.lastedited().format(DateTimeFormatter.ISO_DATE_TIME));
      }
      if (root.path() != null) {
        xml.attribute("path", root.path());
      }
      if (this._map.size() == 1 || cposition != -1) {
        xml.attribute("content", "true");
      }
      List<Long> trees = null;
      // Collect partial tree nodes
      if (cid != -1) {
        trees = new ArrayList<>();
        if (cposition != -1) {
          trees.add(cid);
        } else {
          collectReferences(cid, trees);
        }
      }
      toXML(xml, this._rootid, 1, 1, Reference.DEFAULT_FRAGMENT,
          new TOCState(cid, cposition, trees, number, config, externalrefs));
    }
    xml.closeElement();
  }

  /**
   * Collect all the ancestor references to a tree.
   *
   * @param id     the tree ID
   * @param trees  the list of ancestor IDs
   *
   * @return whether ID is embedded/transcluded in publication
   */
  private boolean collectReferences(long id, List<Long> trees) {
    if (trees.contains(id)) return true;
    int count = 0;
    DocumentTree t = tree(id);
    if (t != null) {
      for (Long ref : t.listReverseReferences()) {
        if (collectReferences(ref, trees)) count++;
        if (count >= MAX_REVERSE_FOLLOW) break;
      }
      trees.add(id);
      return true;
    } else if (this._transclusions != null) {
      List<Long> transcluded = this._transclusions.get(id);
      if (transcluded != null && !transcluded.isEmpty()) {
        for (Long ref : transcluded) {
          if (collectReferences(ref, trees)) count++;
          if (count >= MAX_REVERSE_FOLLOW) break;
        }
        trees.add(id);
        return true;
      }
    }
    return false;
  }

  /**
   * Serialize a tree as XML.
   *
   * @param xml         The XML writer
   * @param id          The ID of the tree to serialize.
   * @param level       The level that we are currently at
   * @param count       No. of times ID has been used.
   * @param fragment    The document fragment to serialize.
   * @param state       The current state of the TOC
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level, Integer count, String fragment, TOCState state) throws IOException {
    String key = id + "-" + fragment;
    if (state.ancestors.contains(key)) throw new IllegalStateException("XRef loop detected on URIID-fragment " + id);
    state.ancestors.add(key);
    DocumentTree current = tree(id);
    if (!Reference.DEFAULT_FRAGMENT.equals(fragment)) {
      current = current.singleFragmentTree(fragment);
    }
    for (Part<?> part : current.parts()) {
      toXML(xml, id, level, part, count, state);
    }
    state.ancestors.remove(key);
  }

  /**
   * Serialize a part as XML.
   *
   * @param xml         The XML writer
   * @param id          The ID of the tree to output.
   * @param level       The level that we are currently at
   * @param part        The part to serialize
   * @param count       No. of times ID has been used.
   * @param state       The current state of the TOC
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level, Part<?> part, Integer count, TOCState state) throws IOException {
    Element element = part.element();
    // ignore paragraphs
    if (element instanceof TransclusionEnd || element instanceof Toc) return;
    boolean output = (state.trees == null || state.trees.contains(id)) &&
        (state.cposition == -1 || state.cposition == count);
    boolean toNext = false;
    Long next = null;
    DocumentTree nextTree = null;
    String targetFragment = Reference.DEFAULT_FRAGMENT;
    Reference.Type refType = Reference.Type.EMBED;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      targetFragment = ref.targetfragment();
      refType = ref.type();
      next = ref.uri();
      nextTree = tree(next);
      toNext = nextTree != null && Reference.Type.EMBED.equals(refType);
    }
    if (output && !Reference.Type.TRANSCLUDE.equals(refType)) {
      if (element instanceof Paragraph) {
        Paragraph para = (Paragraph) element;
        if (para.isVisible(state.config)) {
          element.toXML(xml, level, state.number, id, count);
          return;
        } else {
          return;
        }
      } else {
        xml.openElement("part", !part.parts().isEmpty() ||
            (toNext && (state.trees == null || state.trees.contains(next) || state.cid == id)));
        xml.attribute("level", level);
        if (toNext && state.cid == next) {
          xml.attribute("content", "true");
        } else if (element instanceof Heading) {
          xml.attribute("uriid", Long.toString(id));
        }
      }
    }

    // Output the element
    Integer nextcount = null;
    if (nextTree != null || Reference.Type.TRANSCLUDE.equals(refType)) {
      nextcount = state.doccount.get(next);
      nextcount = nextcount == null ? 1 : nextcount + 1;
      state.doccount.put(next, nextcount);
      Reference ref = (Reference)element;
      if (Reference.Type.EMBED.equals(refType)) {
        if (Reference.DEFAULT_FRAGMENT.equals(targetFragment)) {
          if (output) ref.toXML(xml, level, state.number, next, nextcount, nextTree.title(),
              nextTree.numbered(), nextTree.prefix(), nextTree.hasVisibleItems(state.config, null),
              nextTree.labels(), nextTree.lastedited(), nextTree.path());
        } else {
          // single embedded fragments can't be numbered
          if (output) ref.toXML(xml, level, state.number, next, nextcount, ref.title(),
              false, DocumentTree.NO_PREFIX, nextTree.hasVisibleItems(state.config, targetFragment),
                  nextTree.labels(), nextTree.lastedited(), nextTree.path());
        }
      } else if (output) {
        xml.openElement("transclusion");
        xml.attribute("uriid", Long.toString(ref.uri()));
        xml.attribute("fragment", ref.targetfragment());
        xml.attribute("position", nextcount);
        xml.closeElement();
      }
    } else if (element instanceof Reference && !state.externalrefs) {
      // external reference not allowed so don't output XML
    } else {
      if (output) element.toXML(xml, level, state.number, id, count);
    }

    // Expand found reference
    if (toNext) {
      // Moving to the next tree (increase the level by 1)
      toXML(xml, next, level + 1, nextcount, targetFragment, state);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, id, level+1, r, count, state);
    }
    if (output && !Reference.Type.TRANSCLUDE.equals(refType)) xml.closeElement();
  }

  /**
   * @return list of tree IDs
   */
  public Set<Long> ids() {
    return this._map.keySet();
  }

  @Override
  public void print(Appendable out) {
    // TODO
  }
}
