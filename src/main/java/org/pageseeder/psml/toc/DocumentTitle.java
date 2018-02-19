/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;

import org.pageseeder.xmlwriter.XMLWriter;

public final class DocumentTitle extends Element {

  /** As er requirement for Serialization*/
  private static final long serialVersionUID = 2L;

  /**
   * A document title without a title.
   */
  public static final DocumentTitle UNTITLED = new DocumentTitle();

  /** Create an untitled document. */
  private DocumentTitle() {
    super(0);
  }

  /**
   * Create an untitled document.
   *
   * @param title The title for the document.
   */
  public DocumentTitle(String title) {
    super(0, title);
  }

  /**
   * @throws UnsupportedOperationException Adjusting the level of the document is not supported.
   */
  @Override
  public DocumentTitle adjustLevel(int delta) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toXML(XMLWriter xml, int level, NumberingGenerator number) throws IOException {
    xml.openElement("title", false);
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.closeElement();
  }

  @Override
  public void print(Appendable out) {
    try {
      out.append(title());
    } catch (IOException ex) {
      // Ignore
    }
  }

}
