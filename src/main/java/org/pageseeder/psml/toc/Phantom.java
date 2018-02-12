/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A phantom element used for when there was no actual element defined but
 * we still need one to maintain the integrity of the tree.
 *
 * <p>Parts defined by phantom elements are typically used when jumping heading levels
 * for example going from level 2 to 4.
 */
public final class Phantom extends Element implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static final Phantom P1 = new Phantom(1);
  private static final Phantom P2 = new Phantom(2);
  private static final Phantom P3 = new Phantom(3);
  private static final Phantom P4 = new Phantom(4);
  private static final Phantom P5 = new Phantom(5);
  private static final Phantom P6 = new Phantom(6);

  /**
   * Commonly used instances are cached.
   */
  private static final Map<Integer, Phantom> PHANTOMS = new HashMap<>();
  static {
    PHANTOMS.put(1, P1);
    PHANTOMS.put(2, P2);
    PHANTOMS.put(3, P3);
    PHANTOMS.put(4, P4);
    PHANTOMS.put(5, P5);
    PHANTOMS.put(6, P6);
  }

  /**
   * Keep this constructor private and use static instantiator instead.
   *
   * @param level The level of the phantom element.
   */
  private Phantom(int level) {
    super(level);
  }

  public static Phantom of(int level) {
    Phantom phantom = PHANTOMS.get(level);
    return phantom != null? phantom : new Phantom(level);
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
    return Phantom.of(level()+delta);
  }

  @Override
  public void toXML(XMLWriter xml, int level, NumberingGenerator number) throws IOException {
    xml.openElement("phantom", false);
    xml.attribute("level", level);
    xml.closeElement();
  }

  @Override
  public void attributes(XMLWriter xml, int level) throws IOException {
    xml.attribute("level", level);
  }
}
