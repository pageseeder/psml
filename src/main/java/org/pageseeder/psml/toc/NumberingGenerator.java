/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.util.Stack;

import org.pageseeder.psml.process.NumberingConfig;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Generates numbering for a publication.
 *
 * @author Philip Rutherford
 */
public final class NumberingGenerator {

  /**
   * The numbering config.
   */
  private NumberingConfig numberConfig;

  /**
   * List of current heading levels, used for numbering.
   */
  private Stack<Integer> headingLevels = new Stack<>();

  /**
   * List of current para levels, used for numbering.
   */
  private Stack<Integer> paraLevels = new Stack<>();

  /**
   * Constructor
   *
   * @param cfg  the numbering config, if null, no numbering is generated
   */
  public NumberingGenerator(NumberingConfig cfg) {
    this.numberConfig = cfg;
  }

  /**
   * Add the canonical and prefix attributes for generated heading numbering.
   *
   * @param level         the level of the heading
   * @param xml           the XML writer where the attributes are written to
   *
   * @throws IOException           if the writing the attributes to the XML failed
   */
  public void generateHeadingNumbering(int level, XMLWriter xml)
      throws IOException {
    if (this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.headingLevels, level);
      // compute canonical label
      String canonical = canonicalLabel(this.headingLevels);
      // compute numbered label
      String label = this.numberConfig.getHeadingLabel(canonical);
      // add attributes to XML
      xml.attribute("canonical", canonical);
      xml.attribute("prefix", label);
    }
  }

  /**
   * Generate and return heading numbering
   *
   * @param level         the level of the heading
   *
   * @return the heading numbering
   */
  public String generateHeadingNumbering(int level) {
    if (this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.headingLevels, level);
      // restart paragraph numbering
      if (this.numberConfig.shouldRestartPara()) {
        this.paraLevels.clear();
      }
      // compute canonical label
      String canonical = canonicalLabel(this.headingLevels);
      // compute numbered label
      return this.numberConfig.getHeadingLabel(canonical);
    }
    return null;
  }

  /**
   * Generate and return paragraph numbering
   *
   * @param level         the level of the heading
   *
   * @return the paragraph numbering
   */
  public String generateParaNumbering(int level) {
    if (this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.paraLevels, level);
      // compute canonical label
      String canonical = canonicalLabel(this.paraLevels);
      // compute numbered label
      return this.numberConfig.getParaLabel(canonical, canonicalLabel(this.headingLevels));
    }
    return null;
  }

  /**
   * @param levels  list of current levels
   * @param level   the level to add to the list
   */
  private static void addNewLevel(Stack<Integer> levels, int level) {
    if (levels.size() == level) {
      levels.push(levels.pop() + 1);
    } else if (levels.size() + 1 == level) {
      levels.push(1);
    } else if (levels.size() > level) {
      levels.pop();
      addNewLevel(levels, level);
    } else if (levels.size() < level) {
      levels.push(0);
      addNewLevel(levels, level);
    }
  }

  /**
   * @param levels  list of current levels
   *
   * @return the canonical level according to the list of levels provided
   */
  private static String canonicalLabel(Stack<Integer> levels) {
    StringBuilder label = new StringBuilder();
    for (Integer level : levels) {
      label.append(level).append('.');
    }
    return label.toString();
  }

}
