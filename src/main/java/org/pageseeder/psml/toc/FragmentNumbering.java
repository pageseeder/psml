/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pageseeder.psml.process.NumberingConfig;

/**
 * Generates fragment numbering for a publication.
 *
 * @author Philip Rutherford
 */
public final class FragmentNumbering implements Serializable {

  /** As per requirement for Serialization*/
  private static final long serialVersionUID = 20180213L;

  /** When there is no prefix */
  public static final String NO_PREFIX = "";

  /**
   * Map of [uriid]-[position][~[index]][-[fragment]], [prefix]
   * where position is the number of times the document has been used (>=1)
   * and index is the index of the heading/para in the document (>=0).
   */
  private final Map<String,String> numbering = new HashMap<>();

  /**
   * Constructor
   *
   * @param pub          The publication tree
   * @param numbering    The numbering config
   */
  public FragmentNumbering(PublicationTree pub, NumberingConfig numbering) {
    NumberingGenerator number = new NumberingGenerator(numbering);
    Map<Long,Integer> doccount = new HashMap<>();
    processTree(pub, pub.root().id(), 1, number, doccount, 1, new ArrayList<Long>());
  }

  /**
   * Process numbering for a tree.
   *
   * @param pub       The publication tree
   * @param id        The ID of the tree to serialize.
   * @param level     The level that we are currently at
   * @param number    The numbering generator
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processTree(PublicationTree pub, long id, int level, NumberingGenerator number,
      Map<Long,Integer> doccount, Integer count, List<Long> ancestors) {
    if (ancestors.contains(id)) throw new IllegalStateException("XRef loop detected on URIID " + id);
    ancestors.add(id);
    DocumentTree current = pub.tree(id);
    for (Part<?> part : current.parts()) {
      processPart(pub, id, level, part, number, doccount, count, ancestors);
    }
    ancestors.remove(id);
  }

  /**
   * Process numbering for a part.
   *
   * @param pub       The publication tree
   * @param level     The level that we are currently at
   * @param id        The ID of the tree to process.
   * @param part      The part to process
   * @param number    The numbering generator
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processPart(PublicationTree pub, long id, int level, Part<?> part, NumberingGenerator number,
      Map<Long,Integer> doccount, Integer count, List<Long> ancestors) {
    Element element = part.element();
    Long next = null;
    DocumentTree nextTree = null;
    Integer nextcount = null;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      next = ref.uri();
      nextTree = pub.tree(next);
      // can only be numbered if the referenced tree exists
      if (nextTree != null) {
        nextcount = doccount.get(next);
        nextcount = nextcount == null ? 1 : nextcount + 1;
        doccount.put(next, nextcount);
        processReference(ref, level, nextTree, number, nextcount);
      }
    } else if (element instanceof Heading) {
      processHeading((Heading)element, level, id, number, count);
    } else if (element instanceof Paragraph) {
      processParagraph((Paragraph)element, id, number, count);
    }

    // Expand found reference
    if (nextTree != null) {
      // Moving to the next tree (increase the level by 1)
      processTree(pub, next, level+1, number, doccount, nextcount, ancestors);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      processPart(pub, id, level+1, r, number, doccount, count, ancestors);
    }
  }

  /**
   * Process numbering for a reference.
   *
   * @param ref      The reference element
   * @param level    The level that we are currently at
   * @param target   The target tree for the reference.
   * @param number   The numbering generator
   * @param count    No. of times target has been used.
   */
  public void processReference(Reference ref, int level, DocumentTree target, NumberingGenerator number, Integer count) {
    String p = target.prefix();
    if (target.numbered()) {
      p = number.generateHeadingNumbering(level);
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    // store prefix on default fragment
    this.numbering.put(target.id() + "-" + count + "-default", p);
    // store prefix on first heading fragment
    this.numbering.put(target.id() + "-" + count + "-" + target.headingfragment(), p);
  }

  /**
   * Process numbering for a heading.
   *
   * @param h        The heading element
   * @param level    The level that we are currently at
   * @param id       The ID of the tree containing the heading.
   * @param number   The numbering generator
   */
  public void processHeading(Heading h, int level, long id, NumberingGenerator number, Integer count) {
    String p = h.prefix();
    if (h.numbered()) {
      p = number.generateHeadingNumbering(level);
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = id + "-" + count + "-" + h.fragment();
    // if this is not the first heading/para in fragment then add index
    if (this.numbering.containsKey(key)) {
      key = id + "-" + count + "~" + h.index() + "-" + h.fragment();
    };
    // store prefix on fragment
    this.numbering.put(key, p);
  }

  /**
   * Process numbering for a paragraph.
   *
   * @param para     The paragraph element
   * @param level    The level that we are currently at
   * @param id       The ID of the tree containing the heading.
   * @param number   The numbering generator
   */
  public void processParagraph(Paragraph para, long id, NumberingGenerator number, Integer count) {
    String p = para.prefix();
    if (para.numbered()) {
      p = number.generateParaNumbering(para.level());
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = id + "-" + count + "-" + para.fragment();
    // if this is not the first heading/para in fragment then add index
    if (this.numbering.containsKey(key)) {
      key = id + "-" + count + "~" + para.index() + "-" + para.fragment();
    }
    // store prefix on fragment
    this.numbering.put(key, p);
  }

  /**
   * Get prefix for the first heading/para in a fragment.
   *
   * @param uriid     the URI ID of the document
   * @param fragment  the fragment ID
   */
  public String getPrefix(String uriid, String fragment) {
    return this.numbering.get(uriid + "-" + fragment);
  }

  /**
   * Get all prefixes as a map with key [uriid]-[position][~[index]][-[fragment]].
   *
   * @return  the unmodifiable map
   *
   */
  public Map<String,String> getAllPrefixes() {
    return Collections.unmodifiableMap(this.numbering);
  }

}
