/*
 * Copyright (c) 2016 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWritable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Represents a part of a single document defined by a heading and including any content up to
 * the next heading at the same level.
 *
 * <p>The level of part is set by the heading that defines it.
 *
 * <p>The sub-parts, that is the list of part within a part, has defined by headings which levels
 * is 1 greater than the current level.
 *
 * <p>When a level is missing, a phantom part may be created to bridge the gap between parts of
 * different levels.
 */
public final class Part<T extends Element> implements Serializable, XMLWritable {

  /** Required for caching */
  private static final long serialVersionUID = 6L;

  /**
   * Element defining this part.
   */
  private final T element;

  /**
   * List of sub-parts in this part
   */
  private final List<Part<?>> parts;

  /**
   * Create an empty part starting with the specified element.
   *
   * @param element The element starting this part
   */
  public Part(T element) {
    this(element, Collections.emptyList());
  }

  /**
   * Create a part starting with the specified heading and including the specified sub-parts.
   *
   * @param element The element starting this part
   * @param parts   The sub-parts
   */
  public Part(T element, @NonNull Part<?>... parts) {
    this.element = element;
    this.parts = Arrays.asList(parts);
  }

  /**
   * Create a part starting with the specified heading and including the specified sub-parts.
   *
   * @param element The element starting this part
   * @param parts   The sub-parts
   */
  public Part(T element, List<Part<?>> parts) {
    this.element = element;
    this.parts = parts;
  }

  /**
   * Create a new part identical to this part but with the specified element.
   *
   * @param element the element for the new part
   *
   * @return A new part instance
   */
  public Part<T> element(Element element) {
    return new Part<>((T)element, this.parts);
  }

  /**
   * @return The element at the origin of this part.
   */
  public @NonNull T element() {
    return this.element;
  }

  /**
   * @return The level of the structure
   */
  public int level() {
    return this.element.level();
  }

  /**
   * @return The title of the part
   */
  public String title() {
    return this.element.title();
  }

  /**
   * Indicates whether the specified part has a title.
   *
   * @return <code>true</code> if the part's defining element has a title;
   *         <code>false</code> otherwise.
   */
  public boolean hasTitle() {
    return this.element.hasTitle();
  }

  /**
   * @return The number of sub-parts.
   */
  public int size() {
    return this.parts.size();
  }

  /**
   * Returns the list of sub-parts in the part.
   *
   * @return the list of sub-parts in the part.
   */
  public List<Part<?>> parts() {
    return this.parts;
  }

  /**
   * Indicates whether the level of this part is consistent with the level of
   * its defining element and each sub-part.
   *
   * @return <code>true</code> if the levels are consistent;
   *         <code>false</code> otherwise.
   */
  public boolean isLevelConsistent() {
    return isLevelConsistent(this.element.level());
  }

  /**
   * Indicates whether the level of this part is consistent with the level of
   * its defining element and each sub-part.
   *
   * @param level The expected level
   *
   * @return <code>true</code> if the levels are consistent;
   *         <code>false</code> otherwise.
   */
  protected final boolean isLevelConsistent(int level) {
    if (this.element.level() != level) return false;
    for (Part<?> p: this.parts) {
      if (!p.isLevelConsistent(level+1)) return false;
    }
    return true;
  }

  /**
   * Find the reference matching the URI in the specified part.
   *
   * @param uri The URI ID for this reference
   *
   * @return the reference in this tree.
   */
  public static @Nullable Part<Reference> find(Part<?> part, long uri) {
    Element element = part.element();
    // Found it!
    if (element instanceof Reference && ((Reference)element).uri() == uri) return (Part<Reference>)part;
    // Look for sub-parts
    for (Part<?> p : part.parts()) {
      Part<Reference> found = find(p, uri);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Print a text representation of the structural element.
   *
   * @param out Where to print the structure
   */
  public void print(Appendable out) {
    try {
      element().print(out);
      out.append('\n');
      for (Part<?> part : parts()) {
        part.print(out);
      }
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml) throws IOException {
    toXML(xml, level(), null, -1, -1);
  }

  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    xml.openElement("part", !parts().isEmpty());
    xml.attribute("level", level);
    element().toXML(xml, level, number, treeid, count);
    for (Part<?> p : parts()) {
      p.toXML(xml, level+1, number, treeid, count);
    }
    xml.closeElement();
  }

}

