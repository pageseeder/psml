/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.util.Stack;

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
  private PublicationNumbering numberConfig;

  /**
   * List of current numbering levels.
   */
  private Stack<Integer> numberingLevels = new Stack<>();

  /**
   * Constructor
   *
   * @param cfg  the numbering config, if null, no numbering is generated
   */
  public NumberingGenerator(PublicationNumbering cfg) {
    this.numberConfig = cfg;
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
   * Add the canonical and prefix attributes for generated numbering.
   *
   * @param level         the level of the object
   * @param xml           the XML writer where the attributes are written to
   *
   * @throws IOException           if the writing the attributes to the XML failed
   */
  public void generateNumbering(int level, XMLWriter xml)
      throws IOException {
    if (this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.numberingLevels, level);
      // compute canonical label
      String canonical = canonicalLabel(this.numberingLevels);
      // compute numbered label
      String label = this.numberConfig.getHeadingLabel(canonical);
      // add attributes to XML
      xml.attribute("canonical", canonical);
      xml.attribute("prefix", label);
    }
  }

  /**
   * Generate and return numbering
   *
   * @param level         the level of the object
   *
   * @return the numbering
   */
  public String generateNumbering(int level) {
    if (this.numberConfig != null) {
      // add it to current levels
      addNewLevel(this.numberingLevels, level);
      // compute canonical label
      String canonical = canonicalLabel(this.numberingLevels);
      // compute numbered label
      return this.numberConfig.getHeadingLabel(canonical);
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
