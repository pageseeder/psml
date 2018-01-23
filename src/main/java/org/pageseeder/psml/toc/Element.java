/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * An element in PSML that can be used to define a TOC part.
 */
public abstract class Element implements Serializable, XMLWritable {

  /** As required for Serializable. */
  private static final long serialVersionUID = 1L;

  /**
   * When there is no title.
   */
  public static final String NO_TITLE = "";

  /** Level of this element */
  private final int _level;

  /** Text of the element */
  private final String _title;

  /**
   * Creates a new element with an empty title
   *
   * @param level The level.
   */
  public Element(int level) {
    this._level = level;
    this._title = NO_TITLE;
  }

  /**
   * Creates a new element with the specified title.
   *
   * @param level The level.
   * @param title The element title.
   *
   * @throws IllegalArgumentException If the level is less than zero.
   */
  public Element(int level, String title) {
    if (level < 0) throw new IllegalArgumentException("Level must be > 0 but was "+level);
    this._level = level;
    this._title = title;
  }

  /**
   * The level of this element, the level is always a positive integer.
   *
   * @return Level of this element
   */
  public final int level() {
    return this._level;
  }

  /**
   * The title of the element which may be an empty string if not title was defined.
   *
   * @return Text of the element
   */
  public final String title() {
    return this._title;
  }

  /**
   * Indicates whether the specified element has a title.
   *
   * @return true if the element has a title.
   */
  public final boolean hasTitle() {
    return !NO_TITLE.equals(this._title);
  }

  @Override
  public final String toString() {
    StringBuilder out = new StringBuilder();
    print(out);
    return out.toString();
  }

  /**
   *
   * @param delta The difference with the current level.
   *
   * @return a new element with the adjusted level of the same type.
   *
   * @throws IllegalArgumentException If the resulting level is equal to or less than zero.
   */
  public abstract Element adjustLevel(int delta);

  /**
   * Print a text representation of the structural element.
   *
   * @param out Where to print the structure
   *
   * @throws IOException If thrown by the appendable
   */
  public abstract void print(Appendable out);

  /**
   * Writes this element as XML.
   *
   * @param xml   The XML output.
   * @param level The level (if overridden)
   *
   * @throws IOException Should an I/O error occur
   */
  public abstract void toXML(XMLWriter xml, int level) throws IOException;

  /**
   * Prints the XML attributes associated with this element.
   *
   * @param xml   The XML output.
   * @param level The level (if overridden)
   *
   * @throws IOException Should an I/O error occur
   */
  public abstract void attributes(XMLWriter xml, int level) throws IOException;

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(xml, level());
  }

}
