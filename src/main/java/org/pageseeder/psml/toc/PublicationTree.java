/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
   * List of trees that make up this tree.
   *
   * The first one is the root of the tree, the last is the content tree.
   */
  private final List<DocumentTree> _stack;

  /**
   * Creates a simple publication tree wrapping a document tree
   *
   * @param tree The document tree.
   */
  public PublicationTree(DocumentTree tree) {
    this._stack = Collections.singletonList(tree);
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
    List<DocumentTree> stack = new ArrayList<>(trunk._stack.size()+1);
    stack.add(parent);
    stack.addAll(trunk._stack);
    this._stack = Collections.unmodifiableList(stack);
  }

  /**
   * Creates a new tree appending existing publication tree with another document tree.
   *
   * <p>Note: the leaf should have at least one reference from the existing publication tree.
   *
   * @param trunk  The rest of the tree
   * @param leaf   The new leaf for the tree
   */
  private PublicationTree(PublicationTree trunk, DocumentTree leaf) {
    List<DocumentTree> stack = new ArrayList<>(trunk._stack.size()+1);
    stack.addAll(trunk._stack);
    stack.add(leaf);
    this._stack = Collections.unmodifiableList(stack);
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
    for (DocumentTree tree : this._stack) {
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
    for (Tree t : this._stack) {
      if (t.id() == id) return true;
    }
    return false;
  }

  /**
   * @return the root of this tree.
   */
  public DocumentTree root() {
    return this._stack.get(0);
  }

  /**
   * @return the top of this tree.
   */
  public DocumentTree leaf() {
    return this._stack.get(this._stack.size()-1);
  }

  /**
   * Create a new tree by adding the specified leaf.
   *
   * @param leaf The leaf to add
   *
   * @return The new tree
   */
  public PublicationTree leaf(DocumentTree leaf) {
    return new PublicationTree(this, leaf);
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
    for (DocumentTree t : this._stack) {
      hashCode = prime*hashCode + (t==null ? 0 : Long.hashCode(t.id()));
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
    xml.openElement("publication-tree", true);
    xml.attribute("id", Long.toString(id()));
    xml.attribute("title", title());
    xml.attribute("trees", this._stack.stream().map(t -> Long.toString(t.id())).collect(Collectors.joining(",")));
    if (this._stack.size() == 1) {
      xml.attribute("content", "true");
    }
    toXML(xml, 0, 1);
    xml.closeElement();
  }

  /**
   * Serialize the tree at index i as XML.
   *
   * @param xml   The XML writer
   * @param i     The index of the tree in our stack.
   * @param level The level that we are currently at
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, int i, int level) throws IOException {
    if (i == this._stack.size()-1) {
      // We've reached the content tree (the TOC of the visible content)
      //xml.attribute("content", "true");
      DocumentTree tree = leaf();
      for (Part<?> part : tree.parts()) {
        part.toXML(xml, level);
      }

    } else {
      // More trees to process
      DocumentTree current = this._stack.get(i);
      long next = this._stack.get(i+1).id();
      for (Part<?> part : current.parts()) {
        toXML(xml, i, level, part, next);
      }
    }
  }

  /**
   * Process the reference as XML.
   *
   * @param xml     The XML writer
   * @param level   The level that we are currently at
   * @param i       The index of the tree in our stack.
   * @param element The reference to serialize
   * @param next    The URI ID of the next tree
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, int i, int level, Part<?> part, long next) throws IOException {
    DocumentTree tree = this._stack.get(i);
    Element element = part.element();
    boolean toNext = (element instanceof Reference && ((Reference)element).uri() == next);
    xml.openElement("part", !part.parts().isEmpty() || toNext);
    xml.attribute("level", level);
    if (toNext && i == this._stack.size()-2) {
      xml.attribute("content", "true");
    } else if (element instanceof Heading) {
      xml.attribute("uri", Long.toString(tree.id()));
    }

    // Output the element
    element.toXML(xml, level);

    // Expand found reference
    if (toNext) {
      // Moving to the next tree (increase the level by 1)
      toXML(xml, i+1, level+1);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, i, level+1, r, next);
    }
    xml.closeElement();
  }

  /**
   * @return list of tree IDs
   */
  private List<Long> ids() {
    return this._stack.stream().map(t -> t.id()).collect(Collectors.toList());
  }

  @Override
  public void print(Appendable out) {
    // TODO
  }
}
