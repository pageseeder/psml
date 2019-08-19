/**
 * Copyright (c) 1999-2018 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process;

import org.pageseeder.psml.toc.*;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.util.*;

/**
 * Container for publication tree and fragment numbering.
 *
 * @author Philip Rutherford
 *
 */
public class NumberedTOCGenerator {

  /**
   * The tree for the TOC
   */
  private PublicationTree _publicationTree;

  /**
   * The numbering for the TOC
   */
  private FragmentNumbering _fragmentNumbering = null;

  /**
   * Trees to add to publication keyed on URIID
   */
  private Map<Long,DocumentTree> _addTrees = new HashMap<>();

  /**
   * Constructor
   *
   * @param tree            The tree for the TOC
   * @param numbering       The numbering for the TOC
   */
  public NumberedTOCGenerator(PublicationTree tree) {
    this._publicationTree = tree;
  }

  /**
   * @param numbering       The numbering for the publication
   */
  public void setFragmentNumbering(FragmentNumbering numbering) {
    this._fragmentNumbering = numbering;
  }

  /**
   * @return the publicationTree
   */
  public PublicationTree publicationTree() {
    return this._publicationTree;
  }

  /**
   * @return the fragmentNumbering
   */
  public FragmentNumbering fragmentNumbering() {
    return this._fragmentNumbering;
  }

  /**
   * Add a document tree to the publication
   *
   * @param tree  the tree to add
   */
  public void addTree(DocumentTree tree) {
    this._addTrees.put(tree.id(), tree);
  }

  /**
   * Update the publication with added trees.
   */
  public void updatePublication() {
    this._publicationTree = this._publicationTree.modify(
        Collections.emptyList(), this._addTrees, this._publicationTree.root().id());
  }

  /**
   * Serialize the publication as a TOC tree.
   *
   * @param xml           The XML writer
   *
   * @throws IOException If thrown by XML writer
   */
  public void toXML(XMLWriter xml) throws IOException {
    xml.openElement("toc-tree", true);
    DocumentTree root = this._publicationTree.root();
    if (root != null) {
      xml.attribute("title", root.title());
      Map<Long,Integer> doccount = new HashMap<>();
      toXML(xml, root.id(), 1, doccount, 1, new ArrayList<String>(), Reference.DEFAULT_FRAGMENT);
    }
    xml.closeElement();
  }

  /**
   * Serialize a tree as XML.
   *
   * @param xml       The XML writer
   * @param id        The ID of the tree to serialize.
   * @param level     The level that we are currently at
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   * @param fragment  The document fragment to serialize.
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level,
      Map<Long,Integer> doccount, Integer count, List<String> ancestors, String fragment) throws IOException {
    String key = id + "-" + fragment;
    if (ancestors.contains(key)) throw new IllegalStateException("XRef loop detected on URIID-fragment " + key);
    ancestors.add(key);
    DocumentTree current = this._publicationTree.tree(id);
    if (!Reference.DEFAULT_FRAGMENT.equals(fragment)) {
      current = current.singleFragmentTree(fragment);
    }
    for (Part<?> part : current.parts()) {
      toXML(xml, id, level, part, doccount, count, ancestors);
    }
    ancestors.remove(key);
  }

  /**
   * Serialize a part as XML.
   *
   * @param xml       The XML writer
   * @param id        The ID of the tree to output.
   * @param level     The level that we are currently at
   * @param part      The part to serialize
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level, Part<?> part,
      Map<Long,Integer> doccount, Integer count, List<String> ancestors) throws IOException {
    Element element = part.element();
    // ignore paragraphs, transclusion end and toc marker
    if (element instanceof Paragraph || element instanceof TransclusionEnd || element instanceof Toc) return;
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
      nextTree = this._publicationTree.tree(next);
      toNext = nextTree != null && Reference.Type.EMBED.equals(refType);
    }

    // Output the element
    Integer nextcount = null;
    if (toNext || Reference.Type.TRANSCLUDE.equals(refType)) {
      if (nextTree != null) {
        nextcount = doccount.get(next);
        nextcount = nextcount == null ? 1 : nextcount + 1;
        doccount.put(next, nextcount);
      }
      if (Reference.Type.EMBED.equals(refType)) {
        if (Reference.DEFAULT_FRAGMENT.equals(targetFragment)) {
          referenceToXML(xml, level, (Reference)element, next, nextcount, nextTree,
              !part.parts().isEmpty() || toNext);
        } else {
          // single embedded fragments can't be numbered
          partToXML(xml, level, !part.parts().isEmpty() || toNext);
        }
      }
    } else if (element instanceof Heading) {
      headingToXML(xml, level, (Heading)element, id, count, !part.parts().isEmpty());
    } else {
      partToXML(xml, level, !part.parts().isEmpty());
    }

    // Expand found reference
    if (toNext) {
      // Moving to the next tree (increase the level by 1 unless transclude)
      toXML(xml, next, level + 1, doccount, nextcount, ancestors, targetFragment);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, id, level+1, r, doccount, count, ancestors);
    }
    if (!Reference.Type.TRANSCLUDE.equals(refType)) {
      xml.closeElement();
    }
  }

  /**
   * Output a reference as XML (don't close element)
   *
   * @param xml       The XML writer
   * @param level     The level that we are currently at
   * @param children  Whether this element has children
   *
   * @throws IOException if problem writing XML
   */
  public void partToXML(XMLWriter xml, int level,boolean children) throws IOException {
    xml.openElement("toc-part", children);
    xml.attribute("level", level);
  }

  /**
   * Output a reference as XML (don't close element)
   *
   * @param xml       The XML writer
   * @param level     The level that we are currently at
   * @param ref       The reference to serialize
   * @param treeid    The ID of the target tree
   * @param count     No. of times ID has been used.
   * @param target    The target tree
   * @param children  Whether this element has children
   *
   * @throws IOException if problem writing XML
   */
  public void referenceToXML(XMLWriter xml, int level, Reference ref, long treeid, int count,
      DocumentTree target, boolean children) throws IOException {
    xml.openElement("toc-part", children);
    xml.attribute("level", level);
    // if display="document" use title from target document
    if (ref.displaydocument() != null && ref.displaydocument()) {
      xml.attribute("title", target.title());
    } else {
      xml.attribute("title", ref.title());
    }
    if (target.numbered() && this._fragmentNumbering != null) {
      Prefix pref = this._fragmentNumbering.getPrefix(treeid, count);
      if (pref != null) {
        xml.attribute("prefix", pref.value);
        xml.attribute("canonical", pref.canonical);
      }
    } else {
      if (!DocumentTree.NO_PREFIX.equals(target.prefix())) {
        xml.attribute("prefix", target.prefix());
      }
    }
    // only output idref if there is a corresponding heading
    if (!DocumentTree.NO_FRAGMENT.equals(target.titlefragment())) {
      xml.attribute("idref", treeid + "-" + count + "-" + target.titlefragment() + "-1");
    }
  }

  /**
   * Output a heading as XML (don't close element)
   *
   * @param xml       The XML writer
   * @param level     The level that we are currently at
   * @param head      The heading to serialize
   * @param treeid    The ID of the target tree
   * @param count     No. of times ID has been used.
   * @param children  Whether this element has children
   *
   * @throws IOException if problem writing XML
   */
  public void headingToXML(XMLWriter xml, int level, Heading head, long treeid, int count, boolean children) throws IOException {
    xml.openElement("toc-part", children);
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(head.title())) {
      xml.attribute("title", head.title());
    }
    if (head.numbered() && this._fragmentNumbering != null) {
      Prefix pref = this._fragmentNumbering.getTranscludedPrefix(treeid, count, head.fragment(), head.index());
      if (pref != null) {
        xml.attribute("prefix", pref.value);
        xml.attribute("canonical", pref.canonical);
      }
    } else {
      if (!Heading.NO_PREFIX.equals(head.prefix())) {
        xml.attribute("prefix", head.prefix());
      }
    }
    xml.attribute("idref", treeid + "-" + count + "-" + head.fragment() + "-" + head.index());
  }

}
