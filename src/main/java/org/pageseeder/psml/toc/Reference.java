/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A embed block reference.
 */
public final class Reference extends Element implements Serializable {

  /** Required for caching */
  private static final long serialVersionUID = 2L;

  /** When there is no document type. */
  public static final String DEFAULT_TYPE = "default";

  /**
   * The URI ID of the target.
   */
  private final long _uri;

  /**
   * The document type of the target.
   */
  private final String _type;

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level The level.
   * @param title The title of the reference.
   * @param uri   The URI ID.
   * @param type  The document type of the target.
   */
  public Reference(int level, String title, Long uri, String type) {
    super(level, title);
    this._uri = uri;
    this._type = type;
  }

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level The level.
   * @param title The title of the reference.
   * @param uri   The URI ID.
   */
  public Reference(int level, String title, Long uri) {
    this(level, title, uri, DEFAULT_TYPE);
  }

  /**
   * @return The URI ID of the target.
   */
  public long uri() {
    return this._uri;
  }

  /**
   * @return The document type of the target.
   */
  public String type() {
    return this._type;
  }

  @Override
  public Reference adjustLevel(int delta) {
    if (delta == 0) return this;
    return new Reference(level()+delta, title(), this._uri, this._type);
  }

  @Override
  public void print(Appendable out) {
    try {
      for (int i=0; i < level(); i++) {
        out.append('>');
      }
      out.append(' ');
      out.append(title());
    } catch (IOException ex) {
      // Ignore
    }
  }

  @Override
  public void toXML(@NonNull XMLWriter xml, int level) throws IOException {
    xml.openElement("ref");
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("type", this._type);
    if (this._uri > 0) {
      xml.attribute("uri", Long.toString(this._uri));
    }
    xml.closeElement();
  }

  @Override
  public void attributes(XMLWriter xml, int level) throws IOException {
    xml.attribute("from", "reference");
    xml.attribute("level", level);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("type", this._type);
    if (this._uri > 0) {
      xml.attribute("uri", Long.toString(this._uri));
    }
  }
}