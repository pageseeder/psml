/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A phantom element used for when there was no actual element defined but
 * we still need one to maintain the integrity of the tree.
 *
 * <p>Parts defined by phantom elements are typically used when jumping heading levels
 * for example going from level 2 to 4.
 */
public final class Phantom extends Element implements Serializable {

  /** For serialization */
  private static final long serialVersionUID = 1L;

  /** When there is no fragment */
  public static final String NO_FRAGMENT = "";

  /** Fragment ID this part starts in */
  private final String _fragment;

  /**
   * Constructor
   *
   * @param level The level of the phantom element.
   */
  public Phantom(int level, String fragment) {
    super(level);
    this._fragment = fragment;
  }

  /**
   * @return Fragment ID that this phantom is defined in
   */
  public String fragment() {
    return this._fragment;
  }

  @Override
  public void print(Appendable out) {
    try {
      for (int i=0; i < level(); i++) {
        out.append("_");
      }
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public Phantom adjustLevel(int delta) {
    if (delta == 0) return this;
    return new Phantom(level()+delta, fragment());
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    // don't output phantom XML
  }

}
