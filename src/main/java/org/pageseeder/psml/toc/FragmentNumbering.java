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

import org.eclipse.jdt.annotation.Nullable;

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
  private final Map<String,Prefix> numbering = new HashMap<>();

  /**
   * Constructor
   *
   * @param pub          The publication tree
   * @param numbering    The numbering config
   */
  public FragmentNumbering(PublicationTree pub, PublicationConfig config) {
    Map<Long,Integer> doccount = new HashMap<>();
    DocumentTree root = pub.root();
    processTree(pub, root.id(), 1, config, pub.getNumberingGenerator(config, null, root), doccount, 1, new ArrayList<Long>());
  }

  /**
   * Process numbering for a tree.
   *
   * @param pub       The publication tree
   * @param id        The ID of the tree to serialize.
   * @param level     The level that we are currently at
   * @param config    The publication config to get numbering config
   * @param number    The numbering generator (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processTree(PublicationTree pub, long id, int level, PublicationConfig config,
      @Nullable NumberingGenerator number, Map<Long,Integer> doccount, Integer count, List<Long> ancestors) {
    if (ancestors.contains(id)) throw new IllegalStateException("XRef loop detected on URIID " + id);
    ancestors.add(id);
    DocumentTree current = pub.tree(id);
    PublicationNumbering numbering = config.getPublicationNumbering(current.labels());
    if (numbering == null) {
      number = null;
    // if numbering config has changed then create new numbering generator
    } else if (number == null || !numbering.getLabel().equals(number.getPublicationNumbering().getLabel())) {
      number = new NumberingGenerator(numbering);
    }
    for (Part<?> part : current.parts()) {
      processPart(pub, id, level, part, config, number, doccount, count, ancestors);
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
   * @param config    The publication config to get numbering config
   * @param number    The numbering generator (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processPart(PublicationTree pub, long id, int level, Part<?> part, PublicationConfig config,
      @Nullable NumberingGenerator number, Map<Long,Integer> doccount, Integer count, List<Long> ancestors) {
    Element element = part.element();
    Long next = null;
    DocumentTree nextTree = null;
    Integer nextcount = null;
    NumberingGenerator nextNumber = number;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      // don't process embedded fragments
      if (Reference.DEFAULT_FRAGMENT.equals(ref.targetfragment())) {
        next = ref.uri();
        nextTree = pub.tree(next);
        // can only be numbered if the referenced tree exists
        if (nextTree != null) {
          nextNumber = pub.getNumberingGenerator(config, nextNumber, nextTree);
          nextcount = doccount.get(next);
          nextcount = nextcount == null ? 1 : nextcount + 1;
          doccount.put(next, nextcount);
          processReference(ref, level, nextTree, nextNumber, nextcount);
        }
      }
    } else if (element instanceof Heading) {
      processHeading((Heading)element, level, id, number, count);
    } else if (element instanceof Paragraph) {
      processParagraph((Paragraph)element, level, id, number, count);
    }

    // Expand found reference
    if (nextTree != null) {
      // Moving to the next tree (increase the level by 1)
      processTree(pub, next, level+1, config, nextNumber, doccount, nextcount, ancestors);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      processPart(pub, id, level+1, r, config, number, doccount, count, ancestors);
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
    String parent = null;
    if (target.numbered() && number != null) {
      Prefix pref = number.generateNumbering(level);
      p = pref.value;
      parent = pref.parentNumber;
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    // store prefix on default fragment
    this.numbering.put(target.id() + "-" + count + "-default", new Prefix(p, parent));
    // store prefix on first heading fragment
    this.numbering.put(target.id() + "-" + count + "-" + target.headingfragment(), new Prefix(p, parent));
  }

  /**
   * Process numbering for a heading.
   *
   * @param h        The heading element
   * @param level    The level that we are currently at
   * @param id       The ID of the tree containing the heading.
   * @param number   The numbering generator
   * @param count    No. of times tree ID has been used.
   */
  public void processHeading(Heading h, int level, long id, NumberingGenerator number, Integer count) {
    String p = h.prefix();
    String parent = null;
    if (h.numbered() && number != null) {
      Prefix pref = number.generateNumbering(level);
      p = pref.value;
      parent = pref.parentNumber;
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = id + "-" + count + "-" + h.fragment();
    // if this is not the first heading/para in fragment then add index
    if (this.numbering.containsKey(key)) {
      key = id + "-" + count + "~" + h.index() + "-" + h.fragment();
    };
    // store prefix on fragment
    this.numbering.put(key, new Prefix(p, parent));
  }

  /**
   * Process numbering for a paragraph.
   *
   * @param para     The paragraph element
   * @param level    The level that we are currently at
   * @param id       The ID of the tree containing the heading.
   * @param number   The numbering generator
   * @param count    No. of times tree ID has been used.
   */
  public void processParagraph(Paragraph para, int level, long id, NumberingGenerator number, Integer count) {
    String p = para.prefix();
    String parent = null;
    if (para.numbered() && number != null) {
      Prefix pref = number.generateNumbering(level + para.level());
      p = pref.value;
      parent = pref.parentNumber;
    }
    if (p == null || NO_PREFIX.equals(p)) return;
    String key = id + "-" + count + "-" + para.fragment();
    // if this is not the first heading/para in fragment then add index
    if (this.numbering.containsKey(key)) {
      key = id + "-" + count + "~" + para.index() + "-" + para.fragment();
    }
    // store prefix on fragment
    this.numbering.put(key, new Prefix(p, parent));
  }

  /**
   * Get prefix (and parent number if it exists) for the first heading/para in a fragment.
   *
   * @param uriid     the URI ID of the document
   * @param fragment  the fragment ID
   *
   * @return an array [0]=prefix, [1]=parent number(optional)
   */
  public Prefix getPrefix(String uriid, String fragment) {
    return this.numbering.get(uriid + "-" + fragment);
  }

  /**
   * Get all prefixes (and parent number if it exists) as a map with key [uriid]-[position][~[index]][-[fragment]].
   *
   * @return  the unmodifiable map an array [0]=prefix, [1]=parent number(optional)
   *
   */
  public Map<String,Prefix> getAllPrefixes() {
    return Collections.unmodifiableMap(this.numbering);
  }

  /**
   * Container for prefix (and parent number if it exists) for the first heading/para in a fragment.
   *
   * @author Philip Rutherford
   *
   */
  public static final class Prefix implements Serializable {

    /**
     * For serialisation
     */
    private static final long serialVersionUID = 8704232243442685176L;

    public final String value;

    public final @Nullable String parentNumber;

    /**
     * Constructor
     *
     * @param val    the prefix value
     * @param parent the parent number (optional)
     */
    public Prefix(String val, @Nullable String parent) {
      this.value = val;
      this.parentNumber = parent;
    }

    @Override
    public String toString() {
      return (this.parentNumber != null ? this.parentNumber + ">" : "") + this.value;
    }
  }
}
