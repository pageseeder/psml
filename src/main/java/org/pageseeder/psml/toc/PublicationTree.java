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
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * An immutable tree aggregating multiple trees together in order to generate a
 * deep table of contents.
 *
 * @author Christophe Lauret
 */
public final class PublicationTree implements Tree, Serializable, XMLWritable {

  /** As per requirement for Serializable */
  private static final long serialVersionUID = 4L;

  /**
   * The ID of the root of the tree.
   */
  private final long _rootid;

  /**
   * Map of trees that make up this tree.
   */
  private final Map<Long, DocumentTree> _map;

  /**
   * Creates a simple publication tree wrapping a document tree
   *
   * @param tree The document tree.
   */
  public PublicationTree(DocumentTree tree) {
    this._map = Collections.singletonMap(tree.id(), tree);
    this._rootid = tree.id();
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
  }

  /**
   * @return The URI ID of the root
   */
  @Override
  public long id() {
    return root().id();
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
    List<Long> uris = new ArrayList<>();
    for (DocumentTree tree : this._map.values()) {
      uris.addAll(tree.listForwardReferences());
    }
    return uris;
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
   * @id     the tree ID
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
    toXML(xml, -1);
  }

  /**
   * Serialize the partial tree down to content ID.
   *
   * @param xml   The XML writer
   * @param cid   The ID of the content tree (leaf).
   *
   * @throws IOException If thrown by XML writer
   */
  public void toXML(XMLWriter xml, long cid) throws IOException {
    xml.openElement("publication-tree", true);
    xml.attribute("id", Long.toString(id()));
    xml.attribute("title", title());
    xml.attribute("trees", this._map.keySet().stream().map(t -> Long.toString(t)).collect(Collectors.joining(",")));
    if (this._map.size() == 1) {
      xml.attribute("content", "true");
    }
    List<Long> trees = new ArrayList<>();
    // Collect partial tree nodes
    if (cid != -1) {
      collectReferences(tree(cid), trees);
    }
    toXML(xml, this._rootid, 1, cid, trees);
    xml.closeElement();
  }

  /**
   * Add
   * @param t
   * @param trees
   */
  private void collectReferences(DocumentTree t, List<Long> trees) {
    if (t == null || trees.contains(t.id())) return;
    trees.add(t.id());
    for (Long ref : t.listReverseReferences()) {
      collectReferences(tree(ref), trees);
    };
  }

  /**
   * Serialize the tree with ID i as XML.
   *
   * @param xml   The XML writer
   * @param i     The ID of the tree to output.
   * @param level The level that we are currently at
   * @param cid   The ID of the content tree (leaf).
   * @param trees The IDs of trees that cid is a decendant of.
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long i, int level, long cid, List<Long> trees) throws IOException {
    if (i == cid) {
      // We've reached the content tree (the TOC of the visible content)
      //xml.attribute("content", "true");
      DocumentTree tree = tree(cid);
      for (Part<?> part : tree.parts()) {
        part.toXML(xml, level);
      }

    } else {
      // More trees to process
      DocumentTree current = tree(i);
      for (Part<?> part : current.parts()) {
        toXML(xml, i, level, part, cid, trees);
      }
    }
  }

  /**
   * Process the reference as XML.
   *
   * @param xml     The XML writer
   * @param level   The level that we are currently at
   * @param i       The ID of the tree to output.
   * @param element The reference to serialize
   * @param cid     The ID of the content tree (leaf).
   * @param trees The IDs of trees that cid is a decendant of.
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long i, int level, Part<?> part, long cid, List<Long> trees) throws IOException {
    DocumentTree tree = tree(i);
    Element element = part.element();
    boolean toNext = false;
    Long next = null;
    DocumentTree nextTree = null;
    if (element instanceof Reference) {
      next = ((Reference)element).uri();
      nextTree = tree(next);
      toNext = nextTree != null && (cid == -1 || trees.contains(next));
    }
    xml.openElement("part", !part.parts().isEmpty() || toNext);
    xml.attribute("level", level);
    if (toNext && cid == next) {
      xml.attribute("content", "true");
    } else if (element instanceof Heading) {
      xml.attribute("uri", Long.toString(tree.id()));
    }

    // Output the element
    if (nextTree != null) {
      element.toXML(xml, level, nextTree.numbered(), nextTree.prefix());
    } else {
      element.toXML(xml, level);
    }

    // Expand found reference
    if (toNext) {
      // Moving to the next tree (increase the level by 1)
      toXML(xml, next, level+1, cid, trees);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, i, level+1, r, cid, trees);
    }
    xml.closeElement();
  }

  /**
   * @return list of tree IDs
   */
  private Set<Long> ids() {
    return this._map.keySet();
  }

  @Override
  public void print(Appendable out) {
    // TODO
  }
}
