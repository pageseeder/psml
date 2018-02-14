/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
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
   * Map of [uriid]-[fragment], [prefix].
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
    processTree(pub, pub.root().id(), 1, number);
  }

  /**
   * Process numbering for a tree.
   *
   * @param pub     The publication tree
   * @param id      The ID of the tree to serialize.
   * @param level   The level that we are currently at
   * @param number  The numbering generator
   */
  private void processTree(PublicationTree pub, long id, int level, NumberingGenerator number) {
    DocumentTree current = pub.tree(id);
    for (Part<?> part : current.parts()) {
      processPart(pub, id, level, part, number);
    }
  }

  /**
   * Process numbering for a part.
   *
   * @param pub     The publication tree
   * @param level   The level that we are currently at
   * @param id      The ID of the tree to process.
   * @param part    The part to process
   * @param number  The numbering generator
   */
  private void processPart(PublicationTree pub, long id, int level, Part<?> part, NumberingGenerator number) {
    Element element = part.element();
    Long next = null;
    DocumentTree nextTree = null;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      next = ref.uri();
      nextTree = pub.tree(next);
      // can only be numbered if the referenced tree exists
      if (nextTree != null) {
        processReference(ref, level, nextTree, number);
      }
    } else if (element instanceof Heading) {
      processHeading((Heading)element, level, id, number);
    }

    // Expand found reference
    if (nextTree != null) {
      // Moving to the next tree (increase the level by 1)
      processTree(pub, next, level+1, number);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      processPart(pub, id, level+1, r, number);
    }
  }

  /**
   * Process numbering for a reference.
   *
   * @param ref      The reference element
   * @param level    The level that we are currently at
   * @param target   The target tree for the reference.
   * @param number   The numbering generator
   */
  public void processReference(Reference ref, int level, DocumentTree target, NumberingGenerator number) {
    String p = target.prefix();
    if (target.numbered()) {
      p = number.generateHeadingNumbering(level);
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = Long.toString(target.id()) + "-default";
    // if this is not the first reference to this document then ignore
    if (this.numbering.containsKey(key)) return;
    // store prefix on default fragment
    this.numbering.put(key, p);
    // store prefix on first heading fragment
    this.numbering.put(Long.toString(target.id()) + "-" + target.headingfragment(), p);
  }

  /**
   * Process numbering for a heading.
   *
   * @param h        The heading element
   * @param level    The level that we are currently at
   * @param id       The ID of the tree containing the heading.
   * @param number   The numbering generator
   */
  public void processHeading(Heading h, int level, long id, NumberingGenerator number) {
    String p = h.prefix();
    if (h.numbered()) {
      p = number.generateHeadingNumbering(level);
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = Long.toString(id) + "-" + h.fragment();
    // if this is not the first heading in fragment then ignore
    if (this.numbering.containsKey(key)) return;
    // store prefix on first heading fragment
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
   * Get all prefixes as a map with key [uriid]-[fragment].
   *
   * @return  the unmodifiable map
   *
   */
  public Map<String,String> getAllPrefixes() {
    return Collections.unmodifiableMap(this.numbering);
  }

}
