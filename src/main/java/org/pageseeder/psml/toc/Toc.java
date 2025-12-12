/*
 * Copyright (c) 2019 Allette Systems
 */
package org.pageseeder.psml.toc;

import org.jspecify.annotations.Nullable;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;

/**
 * Used for table of contents location marker.
 *
 */
public final class Toc extends Element implements Serializable {

  /** For serialization */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   */
  public Toc() {
    super(0, NO_TITLE, NO_FRAGMENT, NO_FRAGMENT);
  }

  @Override
  public void print(Appendable out) {
    try {
      out.append("TOC");
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    // don't output XML
  }

}
