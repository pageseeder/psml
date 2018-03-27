/**
 * Copyright (c) 1999-2018 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.pageseeder.psml.toc.DocumentTree;
import org.pageseeder.psml.toc.DocumentTreeHandler;
import org.pageseeder.psml.toc.Element;
import org.pageseeder.psml.toc.FragmentNumbering;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.psml.toc.Heading;
import org.pageseeder.psml.toc.Paragraph;
import org.pageseeder.psml.toc.Part;
import org.pageseeder.psml.toc.PublicationTree;
import org.pageseeder.psml.toc.Reference;
import org.pageseeder.xmlwriter.XMLWriter;
import org.xml.sax.SAXException;

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
  private final PublicationTree _publicationTree;

  /**
   * The numbering for the TOC
   */
  private FragmentNumbering _fragmentNumbering = null;

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
      toXML(xml, root.id(), 1, doccount, 1, new ArrayList<Long>());
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
   *
   * @throws IOException If thrown by XML writer
   */
  private void toXML(XMLWriter xml, long id, int level,
      Map<Long,Integer> doccount, Integer count, List<Long> ancestors) throws IOException {
    if (ancestors.contains(id)) throw new IllegalStateException("XRef loop detected on URIID " + id);
    ancestors.add(id);
    DocumentTree current = this._publicationTree.tree(id);
    for (Part<?> part : current.parts()) {
      toXML(xml, id, level, part, doccount, count, ancestors);
    }
    ancestors.remove(id);
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
      Map<Long,Integer> doccount, Integer count, List<Long> ancestors) throws IOException {
    Element element = part.element();
    // ignore paragraphs
    if (element instanceof Paragraph) return;
    boolean toNext = false;
    Long next = null;
    DocumentTree nextTree = null;
    boolean embedded_fragment = false;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      embedded_fragment = !Reference.DEFAULT_FRAGMENT.equals(ref.targetfragment());
      next = ref.uri();
      nextTree = this._publicationTree.tree(next);
      toNext = nextTree != null && !embedded_fragment;
    }

    // Output the element
    Integer nextcount = null;
    if (embedded_fragment) {
      partToXML(xml, level, false);
    } else if (nextTree != null) {
      nextcount = doccount.get(next);
      nextcount = nextcount == null ? 1 : nextcount + 1;
      doccount.put(next, nextcount);
      referenceToXML(xml, level, (Reference)element, next, nextcount, nextTree,
            !part.parts().isEmpty() || toNext);
    } else if (element instanceof Heading) {
      headingToXML(xml, level, (Heading)element, id, count, !part.parts().isEmpty());
    } else {
      partToXML(xml, level, !part.parts().isEmpty());
    }

    // Expand found reference
    if (toNext && !embedded_fragment) {
      // Moving to the next tree (increase the level by 1)
      toXML(xml, next, level+1, doccount, nextcount, ancestors);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      toXML(xml, id, level+1, r, doccount, count, ancestors);
    }
    xml.closeElement();
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
    if (!Element.NO_TITLE.equals(ref.title())) {
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
      Prefix pref = this._fragmentNumbering.getPrefix(treeid, count, head.fragment(), head.index());
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

  /**
   * Parse a PSML file.
   *
   * @param file  the PSML file
   * @return the parsed document tree
   *
   * @throws SAXException           if problem parsing
   * @throws FileNotFoundException  if file doesn't exist
   */
  public static DocumentTree parse(File file) throws SAXException, FileNotFoundException {
    InputStream in = new FileInputStream(file);
    DocumentTree tree = null;
    try {
      DocumentTreeHandler handler = new DocumentTreeHandler();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(in, handler);
      tree = handler.get();
    } catch (ParserConfigurationException | IOException ex) {
      throw new SAXException(ex);
    }
    if (tree == null) throw new SAXException("Unable to generate tree instance from parse!");
    return tree;
  }

}
