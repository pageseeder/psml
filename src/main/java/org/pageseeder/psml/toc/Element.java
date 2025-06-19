/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;
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

  /**
   * When there is no fragment
   */
  public static final String NO_FRAGMENT = "";

  /** Level of this element */
  private final int level;

  /** Text of the element */
  private final String title;

  /** Fragment ID this element starts in */
  private final String fragment;

  /** The original (untranscluded) Fragment ID this element starts in */
  private final String originalFragment;

  /**
   * Creates a new element with an empty title and fragment
   *
   * @param level The level.
   */
  public Element(int level) {
    this.level = level;
    this.title = NO_TITLE;
    this.fragment = NO_FRAGMENT;
    this.originalFragment = NO_FRAGMENT;
  }

  /**
   * Creates a new element with the specified title.
   *
   * @param level            The level.
   * @param title            The element title.
   * @param fragment         The fragment ID this element starts in
   * @param originalFragment The original (untranscluded) Fragment ID this element starts in
   *
   * @throws IllegalArgumentException If the level is less than zero.
   */
  public Element(int level, String title, String fragment, String originalFragment) {
    if (level < 0) throw new IllegalArgumentException("Level must be > 0 but was "+level);
    this.level = level;
    this.title = title;
    this.fragment = fragment;
    this.originalFragment = originalFragment;
  }

  /**
   * The level of this element, the level is always a positive integer.
   *
   * @return Level of this element
   */
  public final int level() {
    return this.level;
  }

  /**
   * The title of the element which may be an empty string if not title was defined.
   *
   * @return Text of the element
   */
  public final String title() {
    return this.title;
  }

  /**
   * @return Fragment ID that this element starts in
   */
  public String fragment() {
    return this.fragment;
  }

  /**
   * @return The original (untranscluded) Fragment ID this element starts in
   */
  public String originalFragment() {
    return this.originalFragment;
  }

  /**
   * Indicates whether the specified element has a title.
   *
   * @return true if the element has a title.
   */
  public final boolean hasTitle() {
    return !NO_TITLE.equals(this.title);
  }

  @Override
  public final String toString() {
    StringBuilder out = new StringBuilder();
    print(out);
    return out.toString();
  }

  /**
   * Print a text representation of the structural element.
   *
   * @param out Where to print the structure
   */
  public abstract void print(Appendable out);

  /**
   * Writes this element as XML.
   *
   * @param xml      The XML output.
   * @param level    The level (if overridden)
   * @param number   The fragment numbering for the publication.
   * @param treeid   The ID of the current document tree.
   * @param count    The position (occurrence number) of the document in the publication.
   *
   * @throws IOException Should an I/O error occur
   */
  public abstract void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException;

  /**
   * Writes this element as XML.
   *
   * @param xml      The XML output.
   * @param level    The level (if overridden)
   * @param number   The fragment numbering for the publication.
   * @param treeid   The ID of the current document tree.
   * @param count    The position (occurrence number) of the document in the publication.
   * @param numbered Whether the heading is auto-numbered
   * @param prefix   Any prefix given to the title
   * @param children Whether the it has children
   *
   * @throws IOException Should an I/O error occur
   */
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count,
      boolean numbered, String prefix, boolean children) throws IOException {
    toXML(xml, level, number, treeid, count);
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(xml, level(), null, -1, -1);
  }

}
