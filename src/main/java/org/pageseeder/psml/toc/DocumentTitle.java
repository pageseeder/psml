/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * Represents the title of a document.
 *
 * <p>A `DocumentTitle` is an immutable data structure
 * that encapsulates the title of a document, providing the ability to render it as XML
 * or print its string representation.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 *
 * @version 1.0
 * @since 1.0
 */
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
    super(0, title, NO_FRAGMENT, NO_FRAGMENT);
  }

  @Override
  public void toXML(XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
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
