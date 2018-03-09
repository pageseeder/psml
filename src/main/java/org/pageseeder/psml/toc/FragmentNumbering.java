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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates fragment numbering for a publication.
 *
 * @author Philip Rutherford
 */
public final class FragmentNumbering implements Serializable {

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FragmentNumbering.class);

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
   * @param config       The publication config
   * @param unusedIds    Any tree IDs that are unreachable will be added to this list
   */
  public FragmentNumbering(PublicationTree pub, PublicationConfig config, List<Long> unusedIds) {
    Map<Long,Integer> doccount = new HashMap<>();
    DocumentTree root = pub.root();
    if (root != null) {
      processTree(pub, root.id(), 1, 1, config, getNumberingGenerator(config, null, root),
          doccount, 1, new ArrayList<Long>());
    }
    List<Long> allIds = new ArrayList<>(pub.ids());
    allIds.remove(root.id());
    allIds.removeAll(doccount.keySet());
    unusedIds.addAll(allIds);
  }

  /**
   * Get the new numbering generator for the document tree specified.
   * If labels haven't changed then returns the current numbering generator.
   *
   * @param config   the publication config
   * @param number   the current numbering generator
   * @param tree     the document tree
   *
   * @return
   */
  private NumberingGenerator getNumberingGenerator(PublicationConfig config,
      NumberingGenerator number, DocumentTree tree) {
    PublicationNumbering numbering = config == null ? null : config.getPublicationNumbering(tree.labels());
    if (numbering == null) {
      number = null;
    // if numbering config has changed then create new numbering generator
    } else if (number == null || !numbering.getLabel().equals(number.getPublicationNumbering().getLabel())) {
      number = new NumberingGenerator(numbering);
    }
    return number;
  }

  /**
   * Process numbering for a tree.
   *
   * @param pub       The publication tree
   * @param id        The ID of the tree to serialize.
   * @param level     The heading level that we are currently at
   * @param treelevel The level of the current tree
   * @param config    The publication config to get numbering config
   * @param number    The numbering generator (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processTree(PublicationTree pub, long id, int level, int treelevel, PublicationConfig config,
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
      processPart(pub, id, level, treelevel, part, config, number, doccount, count, ancestors);
    }
    ancestors.remove(id);
  }

  /**
   * Process numbering for a part.
   *
   * @param pub       The publication tree
   * @param id        The ID of the tree to process.
   * @param level     The level that we are currently at
   * @param treeLevel The level of the current tree
   * @param part      The part to process
   * @param config    The publication config to get numbering config
   * @param number    The numbering generator (optional)
   * @param doccount  Map of [uriid], [number of uses]
   * @param count     No. of times ID has been used.
   * @param ancestors List of the current ancestor tree IDs
   */
  private void processPart(PublicationTree pub, long id, int level, int treeLevel, Part<?> part, PublicationConfig config,
      @Nullable NumberingGenerator number, Map<Long,Integer> doccount, Integer count, List<Long> ancestors) {
    Element element = part.element();
    Long next = null;
    DocumentTree nextTree = null;
    Integer nextCount = null;
    NumberingGenerator nextNumber = number;
    int nextLevel = level + 1;
    int nextTreeLevel = treeLevel;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      // don't process embedded fragments
      if (Reference.DEFAULT_FRAGMENT.equals(ref.targetfragment())) {
        next = ref.uri();
        nextTree = pub.tree(next);
        // can only be numbered if the referenced tree exists
        if (nextTree != null) {
          nextNumber = getNumberingGenerator(config, nextNumber, nextTree);
          nextCount = doccount.get(next);
          nextCount = nextCount == null ? 1 : nextCount + 1;
          doccount.put(next, nextCount);
          if (PublicationConfig.LevelRelativeTo.DOCUMENT.equals(config.getXRefLevelRelativeTo())) {
            nextTreeLevel = treeLevel + ref.level();
            nextLevel = nextTreeLevel + 1;
          }
          // ref is always
          processReference(ref, nextLevel - 1, nextTree, nextNumber, nextCount);
        }
      }
    } else if (element instanceof Heading) {
      processHeading((Heading)element, level, id, number, count);
    } else if (element instanceof Paragraph) {
      int paralevel = PublicationConfig.LevelRelativeTo.DOCUMENT.equals(config.getParaLevelRelativeTo()) ?
          treeLevel + 1 : level;
      processParagraph((Paragraph)element, paralevel, id, number, count);
    }

    // Expand found reference
    if (nextTree != null) {
      // Moving to the next tree (use next level)
      processTree(pub, next, nextLevel, nextTreeLevel, config, nextNumber, doccount, nextCount, ancestors);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      processPart(pub, id, level + 1, treeLevel, r, config, number, doccount, count, ancestors);
    }
  }

  /**
   * Process numbering for a reference.
   *
   * @param ref       The reference element
   * @param level     The level that we are currently at
   * @param target    The target tree for the reference.
   * @param number    The numbering generator
   * @param count     No. of times target has been used.
   */
  public void processReference(Reference ref, int level, DocumentTree target, NumberingGenerator number, Integer count) {
    String p = target.prefix();
    Prefix pref;
    if (target.numbered() && number != null) {
      pref = number.generateNumbering(level, "heading", "");
    } else if (p == null) {
      return;
    } else {
      pref = new Prefix(p, null, level, null);
    }
    // always store prefix on default fragment
    this.numbering.put(target.id() + "-" + count + "-default", pref);
    if (NO_PREFIX.equals(p)) return;
    // store prefix on first heading fragment (must have index=1 for reference to have a prefix)
    this.numbering.put(target.id() + "-" + count + "-1-" + target.titlefragment(), pref);
  }

  /**
   * Process numbering for a heading.
   *
   * @param h         The heading element
   * @param level     The level that we are currently at
   * @param id        The ID of the tree containing the heading.
   * @param number    The numbering generator
   * @param count     No. of times tree ID has been used.
   */
  public void processHeading(Heading h, int level, long id, NumberingGenerator number, Integer count) {
    String p = h.prefix();
    Prefix pref;
    if (h.numbered() && number != null) {
      pref = number.generateNumbering(level, "heading", h.blocklabel());
    } else if (p == null || NO_PREFIX.equals(p)) {
      return;
    } else {
      pref = new Prefix(p, null, level, null);
    }
    // store prefix on fragment
    this.numbering.put(id + "-" + count + "-" + h.index() + "-" + h.fragment(), pref);
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
    Prefix pref;
    // adjust level minus 1 as level is already incremented
    int adjusted_level = level + para.level() - 1;
    if (para.numbered() && number != null) {
      pref = number.generateNumbering(adjusted_level, "para", para.blocklabel());
    } else if (p == null || NO_PREFIX.equals(p)) {
      return;
    } else {
      pref = new Prefix(p, null, adjusted_level, null);
    }
    // store prefix on fragment
    this.numbering.put(id + "-" + count + "-" + para.index() + "-" + para.fragment(), pref);
  }

  /**
   * Get prefix for a first heading in a document.
   *
   * @param uriid     the URI ID of the document
   * @param position  the document position (occurrence number) in the tree
   *
   * @return the prefix
   */
  public Prefix getPrefix(long uriid, int position) {
    Prefix pref = this.numbering.get(uriid + "-" + position + "-default");
    if (pref == null) {
      LOGGER.warn("Numbering not found for uriid: {}, position: {}, fragment default",
          uriid, position);
    }
    return pref;
  }

  /**
   * Get prefix for a heading/para in a fragment.
   *
   * @param uriid     the URI ID of the document
   * @param position  the document position (occurrence number) in the tree
   * @param fragment  the fragment ID
   * @param index     the heading/para number within the fragment
   *
   * @return the prefix
   */
  public Prefix getPrefix(long uriid, int position, String fragment, int index) {
    Prefix pref = this.numbering.get(uriid + "-" + position + "-" + index + "-" + fragment);
    if (pref == null) {
      LOGGER.warn("Numbering not found for uriid: {}, position: {}, fragment: {}, index: {}",
          uriid, position, fragment, index);
    }
    return pref;
  }

  /**
   * Get all prefixes as a map with key [uriid]-[position][~[index]][-[fragment]].
   *
   * @return  the unmodifiable map
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

    public final @Nullable String canonical;

    public final int level;

    public final @Nullable String parentNumber;

    /**
     * Constructor
     *
     * @param val        the prefix value
     * @param canonic    the canonical numbering
     * @param lvl        the heading/para level
     * @param parent     the parent number (optional)
     */
    public Prefix(String val, String canonic, int lvl, @Nullable String parent) {
      this.value = val;
      this.canonical = canonic;
      this.level = lvl;
      this.parentNumber = parent;
    }

    @Override
    public String toString() {
      return (this.parentNumber != null ? this.parentNumber + ">" : "") + this.value +
          " L" + this.level + (this.canonical != null ? " (" + this.canonical + ") " : "");
    }
  }
}
