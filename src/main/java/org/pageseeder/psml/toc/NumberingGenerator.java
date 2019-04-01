/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import org.pageseeder.psml.toc.FragmentNumbering.Prefix;

import java.util.*;

/**
 * Generates numbering for a publication.
 *
 * @author Philip Rutherford
 */
public final class NumberingGenerator {

  /**
   * The numbering config.
   */
  private PublicationNumbering numberConfig;

  /**
   * Map of current numbering levels keyed on blocklabel
   */
  private Map<String,ArrayDeque<Integer>> numberingLevels = new HashMap<>();

  /**
   * Constructor
   *
   * @param cfg  the numbering config, if null, no numbering is generated
   */
  public NumberingGenerator(PublicationNumbering cfg) {
    this.numberConfig = cfg;
    this.numberingLevels.put("", new ArrayDeque<>(9));
  }

  /**
   * Get the numbering config.
   *
   * @return the numbering config
   */
  public PublicationNumbering getPublicationNumbering() {
    return this.numberConfig;
  }

  /**
   * Generate and return numbering
   *
   * @param level         the level of the object
   * @param element       the name of the element being numbered (e.g. heading, para)
   * @param blocklabel    the parent block label name
   *
   * @return the numbering prefix
   */
  public Prefix generateNumbering(int level, String element, String blocklabel) {
    int found_level = (this.numberConfig != null) ? this.numberConfig.elementLevel(level, blocklabel, element) : 0;
    if (found_level != 0) {
      // if blocklabel not defined, use empty
      String label = this.numberConfig.getNumberFormat(found_level, blocklabel) == null ? "" : blocklabel;
      // add it to current levels
      this.addNewLevel(found_level, label);
      // compute canonical numbering
      String canonical = canonicalLabel(label);
      // compute prefix
      return this.numberConfig.getPrefix(canonical, label);
    }
    return null;
  }

  /**
   * Increment current numbering levels.
   *
   * @param level         the level to add to the list
   * @param blocklabel    the parent block label name
   */
  private void addNewLevel(int level, String blocklabel) {
    // if block defined and has no levels, then create a separate levels stack
    if (this.numberConfig.getNumberFormat(level, blocklabel) != null &&
        !this.numberingLevels.containsKey(blocklabel)) {
      ArrayDeque<Integer> blocklevels = this.numberingLevels.get("").clone();
      while (blocklevels.size() >= level) blocklevels.pop();
      this.numberingLevels.put(blocklabel, blocklevels);
    }
    Set<String> labels = this.numberingLevels.keySet();
    // for each stack of levels
    for (String label : labels) {
      boolean blockdefined = this.numberConfig.getNumberFormat(level, label) != null;
      // if block defined add to it's stack or if default block add to all stacks undefined for that level
      if ((blockdefined && label.equals(blocklabel)) || (!blockdefined && "".equals(blocklabel))) {
        Deque<Integer> levels = this.numberingLevels.get(label);
        if (levels.size() == level) {
          levels.push(levels.pop() + 1);
        } else if (levels.size() + 1 == level) {
          levels.push(1);
        } else {
          while (levels.size() > level) levels.pop();
          while (levels.size() < level) {
            // if skipped levels set to one and not a block stack push 1
            levels.push(this.numberConfig.getSkippedLevels() == PublicationNumbering.SkippedLevels.ONE &&
                levels.size() < level - 1 && "".equals(label) ? 1 : 0);
          }
          levels.push(levels.pop() + 1);
        }
      }
    }
  }

  /**
   * @param blocklabel    the parent block label name
   *
   * @return the canonical level according to the list of levels provided
   */
  private String canonicalLabel(String blocklabel) {
    Deque<Integer> levels = this.numberingLevels.get(blocklabel);
    // fall back on default numbering
    if (levels == null) levels = this.numberingLevels.get("");
    StringBuilder label = new StringBuilder();
    Iterator<Integer> leveli = levels.descendingIterator();
    while (leveli.hasNext()) {
      label.append(leveli.next()).append('.');
    }
    return label.toString();
  }

}
