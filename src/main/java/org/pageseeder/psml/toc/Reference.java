/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A embed block reference.
 */
public final class Reference extends Element implements Serializable {

  /** Required for caching */
  private static final long serialVersionUID = 2L;

  /** When there is no document type. */
  public static final String DEFAULT_TYPE = "default";

  /** When there is no specific fragment. */
  public static final String DEFAULT_FRAGMENT = "default";

  /**
   * Fragment ID this part starts in
   */
  private final String _fragment;

  /**
   * The URI ID of the target.
   */
  private final long _uri;

  /**
   * The document type of the target.
   */
  private final String _type;

  /**
   * The target fragment ID.
   */
  private final String _targetfragment;

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level       The level.
   * @param title       The title of the reference.
   * @param fragment    The Fragment identifier where the heading was found.
   * @param uri         The URI ID.
   * @param type        The document type of the target.
   * @param targetfrag  The target fragment ID.
   */
  public Reference(int level, String title, String fragment, Long uri, String type, String targetfrag) {
    super(level, title);
    this._fragment = fragment;
    this._uri = uri;
    this._type = type;
    this._targetfragment = DEFAULT_FRAGMENT.equals(targetfrag) ? DEFAULT_FRAGMENT : targetfrag;
  }

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level      The level.
   * @param title      The title of the reference.
   * @param fragment   The Fragment identifier where the heading was found.
   * @param uri        The URI ID.
   */
  public Reference(int level, String fragment, String title, Long uri) {
    this(level, title, fragment, uri, DEFAULT_TYPE, DEFAULT_FRAGMENT);
  }

  /**
   * @return Fragment ID that this reference is defined in
   */
  public String fragment() {
    return this._fragment;
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

  /**
   * @return The target fragment ID.
   */
  public String targetfragment() {
    return this._targetfragment;
  }

  @Override
  public Reference adjustLevel(int delta) {
    // xref levels are not adjusted
    return this;
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
  public void toXML(@NonNull XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count) throws IOException {
    toXMLNoClose(xml, level);
    xml.closeElement();
  }

  public void toXMLNoClose(@NonNull XMLWriter xml, int level) throws IOException {
    xml.openElement("document-ref");
    xml.attribute("level", this.level());
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.attribute("documenttype", this._type);
    if (this._uri > 0) {
      xml.attribute("uriid", Long.toString(this._uri));
    }
    if (!DEFAULT_FRAGMENT.equals(this._targetfragment)) {
      xml.attribute("targetfragment", this._targetfragment);
    }
  }

  @Override
  public void toXML(@NonNull XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count, boolean numbered, String prefix) throws IOException {
    toXMLNoClose(xml, level);
    if (numbered) {
      xml.attribute("numbered", "true");
    }
    if (numbered && number != null) {
      Prefix pref = number.getPrefix(treeid, count);
      if (pref != null) {
        xml.attribute("prefix", pref.value);
        xml.attribute("canonical", pref.canonical);
      }
    } else {
      if (!DocumentTree.NO_PREFIX.equals(prefix)) {
        xml.attribute("prefix", prefix);
      }
    }
    xml.closeElement();
  }

}