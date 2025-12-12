/*
 * Copyright (c) 2018 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.jspecify.annotations.Nullable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Used to mark the end of some transcluded content.
 *
 */
public final class TransclusionEnd extends Element implements Serializable {

  /** For serialization */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   *
   * @param fragment The fragment for the element.
   * @param originalfrag  The original (untranscluded) fragment.
   */
  public TransclusionEnd(String fragment, String originalfrag) {
    super(0, NO_TITLE, fragment, originalfrag);
  }

  @Override
  public void print(Appendable out) {
    try {
      out.append("TE");
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    // don't output phantom XML
  }

}
