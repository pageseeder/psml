/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
   * Specifies a heading/para location within a publication.
   *
   */
  private final class Location {

    /**
     * Current URI ID
     */
    private long uriid;

    /**
     * Current URI position (instance number in publication)
     */
    private int position;

    /**
     * Current fragment
     */
    private String fragment = Element.NO_FRAGMENT;;

    /**
     * Index (instance number) of heading/para in fragment
     */
    private int index = 0;

    /**
     * Number of nested transclusions
     */
    private int transclusions = 0;

    /**
     * Constructor
     */
    private Location(long uriid, int position) {
      this.uriid = uriid;
      this.position = position;
    }
  }

  /**
   * Map of [uriid]-[position]-[fragment][-index]], [prefix]
   * where position is the number of times the document has been used (>=1)
   * and index is the index of the heading/para in the fragment (>=1).
   */
  private final Map<String,Prefix> numbering = new HashMap<>();


  /**
   * Numbering for transcluded content in context of parent fragment.
   *
   * Map of [uriid]-[position]-[fragment][-index]], [prefix]
   * where position is the number of times the document has been used (>=1)
   * and index is the index of the heading/para in the fragment (>=1).
   */
  private final Map<String,Prefix> transcludedNumbering = new HashMap<>();

  /**
   * Constructor
   *
   * @param pub              The publication tree
   * @param config           The publication config
   */
  public FragmentNumbering(PublicationTree pub, PublicationConfig config) {
    this(pub, config, new ArrayList<Long>(), new HashMap<Long,List<Long>>());
  }

  /**
   * Constructor returning unused IDs and transclusions.
   *
   * @param pub              The publication tree
   * @param config           The publication config
   * @param unusedIds        Any tree IDs that are unreachable will be added to this list (supply empty list)
   * @param transclusions    Map of transcluded Id to a list of it's parent Ids.
   *                         If list contains -1 then Id is also embedded (supply empty map).
   */
  public FragmentNumbering(PublicationTree pub, PublicationConfig config,
      List<Long> unusedIds, Map<Long,List<Long>> transclusions) {
    Map<Long,Integer> doccount = new HashMap<>();
    DocumentTree root = pub.root();
    if (root != null) {
      // store prefix on default fragment of root with level as an adjustment to the first heading in the document
      this.numbering.put(root.id() + "-1-default", new Prefix("", null, 2 - root.level(), null));
      // mark root as embeded
      addTransclusionParents(root.id(), -1, transclusions);
      processTree(pub, root.id(), 1, 1, config, getNumberingGenerator(config, null, root),
          doccount, 1, new ArrayList<String>(), Reference.DEFAULT_FRAGMENT, transclusions);
    }
    List<Long> allIds = new ArrayList<>(pub.ids());
    allIds.remove(root.id());
    allIds.removeAll(doccount.keySet());
    unusedIds.addAll(allIds);
    // remove IDs that are not transcluded from transclusions map
    Iterator<Entry<Long, List<Long>>> entryIt = transclusions.entrySet().iterator();
    while (entryIt.hasNext()) {
      Entry<Long, List<Long>> entry = entryIt.next();
      List<Long> value = entry.getValue();
      if (value.size() == 1 && value.get(0) == -1) {
        entryIt.remove();
      }
    }
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
   * @param pub           The publication tree
   * @param id            The ID of the tree to serialize.
   * @param level         The heading level that we are currently at
   * @param treelevel     The level of the current tree
   * @param config        The publication config to get numbering config
   * @param number        The numbering generator (optional)
   * @param doccount      Map of [uriid], [number of uses]
   * @param count         No. of times ID has been used.
   * @param ancestors     List of the current ancestor tree IDs
   * @param fragment      The document fragment to serialize
   * @param transclusions Map of transcluded Id to a list of it's parent Ids
   */
  private void processTree(PublicationTree pub, long id, int level, int treelevel, PublicationConfig config,
      @Nullable NumberingGenerator number, Map<Long,Integer> doccount, Integer count, List<String> ancestors,
      String fragment, Map<Long,List<Long>> transclusions) {
    String key = id + "-" + fragment;
    if (ancestors.contains(key)) throw new IllegalStateException("XRef loop detected on URIID " + id);
    ancestors.add(key);
    DocumentTree current = pub.tree(id);
    if (!Reference.DEFAULT_FRAGMENT.equals(fragment)) {
      current = current.singleFragmentTree(fragment);
    }
    PublicationNumbering numbering = config.getPublicationNumbering(current.labels());
    if (numbering == null) {
      number = null;
    // if numbering config has changed then create new numbering generator
    } else if (number == null || !numbering.getLabel().equals(number.getPublicationNumbering().getLabel())) {
      number = new NumberingGenerator(numbering);
    }
    Location location = new Location(id, count);
    for (Part<?> part : current.parts()) {
      processPart(pub, id, level, treelevel, part, config, number, doccount, count, ancestors, location, transclusions);
    }
    ancestors.remove(key);
  }

  /**
   * Process numbering for a part.
   *
   * @param pub           The publication tree
   * @param id            The ID of the tree to process.
   * @param level         The level that we are currently at
   * @param treeLevel     The level of the current tree
   * @param part          The part to process
   * @param config        The publication config to get numbering config
   * @param number        The numbering generator (optional)
   * @param doccount      Map of [uriid], [number of uses]
   * @param count         No. of times ID has been used.
   * @param ancestors     List of the current ancestor tree IDs
   * @param location      The original location for transcluded content
   * @param transclusions Map of transcluded Id to a list of it's parent Ids
   */
  private void processPart(PublicationTree pub, long id, int level, int treeLevel, Part<?> part, PublicationConfig config,
      @Nullable NumberingGenerator number, Map<Long,Integer> doccount, Integer count, List<String> ancestors,
      Location location, Map<Long,List<Long>> transclusions) {
    Element element = part.element();
    Long next = null;
    DocumentTree nextTree = null;
    Integer nextCount = 1;
    NumberingGenerator nextNumber = number;
    int nextLevel = level + 1;
    int nextTreeLevel = treeLevel;
    String targetFragment = Reference.DEFAULT_FRAGMENT;
    Reference.Type refType = Reference.Type.EMBED;
    if (element instanceof Reference) {
      Reference ref = (Reference)element;
      targetFragment = ref.targetfragment();
      refType = ref.type();
      next = ref.uri();
      nextTree = pub.tree(next);
      // can only be numbered if the referenced tree exists
      if (nextTree != null || Reference.Type.TRANSCLUDE.equals(refType)) {
        if (nextTree != null) {
          nextCount = doccount.get(next);
          nextCount = nextCount == null ? 1 : nextCount + 1;
          doccount.put(next, nextCount);
        }
        if (Reference.Type.EMBED.equals(refType)) {
          nextNumber = getNumberingGenerator(config, nextNumber, nextTree);
          if (PublicationConfig.LevelRelativeTo.DOCUMENT.equals(config.getXRefLevelRelativeTo())) {
            nextTreeLevel = treeLevel + ref.level();
            nextLevel = nextTreeLevel + 1;
          }
          // references to embedded single fragments are not numbered
          if (Reference.DEFAULT_FRAGMENT.equals(targetFragment)) {
            processReference(ref, nextLevel - 1, nextTree, nextNumber, nextCount);
          } else {
            // always store prefix on default fragment with level as an adjustment to the first heading in the document
            this.numbering.put(nextTree.id() + "-" + nextCount + "-default",
                new Prefix(DocumentTree.NO_PREFIX, null, nextLevel + 1 - nextTree.level(), null));
          }
          // add -1 to transclusion map
          addTransclusionParents(ref.uri(), -1, transclusions);
        } else {
          // ignore nested transclusion
          if (location.transclusions == 0) {
            location.uriid = ref.uri();
            location.position = nextCount;
            location.fragment = Element.NO_FRAGMENT;
            location.index = 0;
            // add to transclusion map
            addTransclusionParents(ref.uri(), id, transclusions);
          }
          location.transclusions++;
        }
      }
    } else if (element instanceof Heading) {
      processHeading((Heading)element, level, id, number, count, location);
    } else if (element instanceof Paragraph) {
      int paralevel = PublicationConfig.LevelRelativeTo.DOCUMENT.equals(config.getParaLevelRelativeTo()) ?
          treeLevel + 3 - pub.tree(id).level(): level; // adjust be tree level for collapse/phantom removal
      processParagraph((Paragraph)element, paralevel, id, number, count, location);
    } else if (element instanceof TransclusionEnd) {
      location.transclusions--;
      // reset location after top level transclusion
      if (location.transclusions == 0) {
        location.uriid = id;
        location.position = count;
        location.fragment = Element.NO_FRAGMENT;
        location.index = 0;
      }
    }

    // Expand found reference
    if (nextTree != null && Reference.Type.EMBED.equals(refType)) {
      // Moving to the next tree (use next level)
      processTree(pub, next, nextLevel, nextTreeLevel, config, nextNumber, doccount, nextCount, ancestors,
          targetFragment, transclusions);
    }

    // Process all child parts
    for (Part<?> r : part.parts()) {
      processPart(pub, id, level + 1, treeLevel, r, config, number, doccount, count, ancestors, location, transclusions);
    }
  }

  /**
   * Add transclusion parent for an ID.
   *
   * @param id             the document ID
   * @param parentid       the parent document ID
   * @param transclusions  the map of transclusion parents
   */
  private static void addTransclusionParents(long id, long parentid, Map<Long,List<Long>> transclusions) {
    List<Long> parents = transclusions.get(id);
    if (parents == null) {
      parents = new ArrayList<>();
      transclusions.put(id, parents);
    }
    // if not already in list add it
    if (!parents.contains(parentid)) {
      parents.add(parentid);
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
  public void processReference(Reference ref, int level, DocumentTree target, NumberingGenerator number, int count) {
    String p = target.prefix();
    Prefix pref = null;
    if (target.numbered() && number != null && Reference.DEFAULT_FRAGMENT.equals(ref.targetfragment())) {
      pref = number.generateNumbering(level, "heading", "");
    }
    if (pref == null) {
      pref = new Prefix(p, null, level, null);
    }
    // always store prefix on default fragment with level as an adjustment to the first heading in the document
    this.numbering.put(target.id() + "-" + count + "-default",
        new Prefix(pref.value, pref.canonical, level + 2 - target.level(), pref.parentNumber));
    if (NO_PREFIX.equals(pref.value)) return;
    // store prefix on first heading fragment (must have index=1 for reference to have a prefix)
    this.numbering.put(target.id() + "-" + count + "-" + target.titlefragment()+ "-1", pref);
    this.transcludedNumbering.put(target.id() + "-" + count + "-" + target.titlefragment()+ "-1", pref);
  }

  /**
   * Process numbering for a heading.
   *
   * @param h         The heading element
   * @param level     The level that we are currently at
   * @param id        The ID of the tree containing the heading.
   * @param number    The numbering generator
   * @param count     No. of times tree ID has been used.
   * @param location  The current original location for transcluded content
   */
  public void processHeading(Heading h, int level, long id, NumberingGenerator number, int count, Location location) {
    String p = h.prefix();
    Prefix pref = null;
    if (h.numbered() && number != null) {
      pref = number.generateNumbering(level, "heading", h.blocklabel());
    } else if (p != null && !NO_PREFIX.equals(p)) {
      pref = new Prefix(p, null, level, null);
    }
    updateLocation(h, location);
    if (pref == null) return;
    // store prefix on fragment
    this.transcludedNumbering.put(id + "-" + count + "-" + h.fragment() + "-" + h.index(), pref);
    // if not a nested transclusion then store it on original fragment
    if (location.transclusions <= 1) {
      this.numbering.put(location.uriid + "-" + location.position + "-" + location.fragment + "-" + location.index, pref);
    }
  }

  /**
   * Update the location for the current element.
   *
   * @param e        the current element
   * @param location the current location
   */
  private static void updateLocation(Element e, Location location) {
    // if not nested transclusion
    if (location.transclusions <= 1) {
      // if same fragment increment index
      if (location.fragment.equals(e.originalFragment())) {
        location.index++;
      // else reset for different fragment
      } else {
        location.fragment = e.originalFragment();
        location.index = 1;
      }
    }
  }

  /**
   * Process numbering for a paragraph.
   *
   * @param para      The paragraph element
   * @param level     The level that we are currently at
   * @param id        The ID of the tree containing the heading.
   * @param number    The numbering generator
   * @param count     No. of times tree ID has been used.
   * @param location  The current original location for transcluded content
   */
  public void processParagraph(Paragraph para, int level, long id, NumberingGenerator number, int count, Location location) {
    String p = para.prefix();
    Prefix pref = null;
    // adjust level minus 1 as level is already incremented
    int adjusted_level = level + para.level() - 1;
    if (para.numbered() && number != null) {
      // if block label set then use original paragraph level
      pref = number.generateNumbering(
          Paragraph.NO_BLOCK_LABEL.equals(para.blocklabel()) ? adjusted_level : para.level(), "para", para.blocklabel());
    } else if (p != null && !NO_PREFIX.equals(p)) {
      pref = new Prefix(p, null, adjusted_level, null);
    }
    updateLocation(para, location);
    if (pref == null) return;
    // store prefix on fragment
    this.transcludedNumbering.put(id + "-" + count + "-" + para.fragment() + "-" + para.index(), pref);
    // if not a nested transclusion then store it on orginal fragment
    if (location.transclusions <= 1) {
      this.numbering.put(location.uriid + "-" + location.position + "-" + location.fragment + "-" + location.index, pref);
    }
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
      LOGGER.debug("Numbering not found for uriid: {}, position: {}, fragment default",
          uriid, position);
    }
    return pref;
  }

  /**
   * Get prefix for a heading/para in a fragment (or first heading if fragment=default).
   *
   * @param uriid     the URI ID of the document
   * @param position  the document position (occurrence number) in the tree
   * @param fragment  the fragment ID
   * @param index     the heading/para number within the fragment
   *
   * @return the prefix
   */
  public Prefix getPrefix(long uriid, int position, String fragment, int index) {
    if ("default".equals(fragment)) {
      return getPrefix(uriid, position);
    }
    Prefix pref = this.numbering.get(uriid + "-" + position + "-" + fragment + "-" + index);
    if (pref == null) {
      LOGGER.debug("Numbering not found for uriid: {}, position: {}, fragment: {}, index: {}",
          uriid, position, fragment, index);
    }
    return pref;
  }

  /**
   * Get prefix for a heading/para in a fragment (for transcluded location where applicable).
   *
   * @param uriid     the URI ID of the document
   * @param position  the document position (occurrence number) in the tree
   * @param fragment  the fragment ID
   * @param index     the heading/para number within the fragment
   *
   * @return the prefix
   */
  public Prefix getTranscludedPrefix(long uriid, int position, String fragment, int index) {
    Prefix pref = this.transcludedNumbering.get(uriid + "-" + position + "-" + fragment + "-" + index);
    if (pref == null) {
      LOGGER.debug("Numbering not found for uriid: {}, position: {}, fragment: {}, index: {}",
          uriid, position, fragment, index);
    }
    return pref;
  }

  /**
   * Get all prefixes as a map with key [uriid]-[position]-[fragment][-index]].
   *
   * @return  the unmodifiable map
   *
   */
  public Map<String,Prefix> getAllPrefixes() {
    return Collections.unmodifiableMap(this.numbering);
  }

  /**
   * Get all prefixes (for transcluded location where applicable)
   * as a map with key [uriid]-[position]-[fragment][-index]].
   *
   * @return  the unmodifiable map
   *
   */
  public Map<String,Prefix> getAllTranscludedPrefixes() {
    return Collections.unmodifiableMap(this.transcludedNumbering);
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
     * @param canonic    the canonical numbering (optional)
     * @param lvl        the heading/para level
     * @param parent     the parent number (optional)
     */
    public Prefix(String val, @Nullable String canonic, int lvl, @Nullable String parent) {
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
