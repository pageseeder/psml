/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;
import org.pageseeder.xmlwriter.XMLWriter;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A embed block reference.
 */
public final class Reference extends Element implements Serializable {

  /**
   * An enumeration for XRef types (only embed and transclude supported in TOC)
   *
   * @author Philip Rutherford
   */
  public enum Type {

    /** embed **/
    EMBED,

    /** transclude **/
    TRANSCLUDE;

    /**
     * Create the XRef type from a string.
     *
     * @param value the string value
     *
     * @return the type
     */
    public static Type fromString(String value) {
      for (Type n : values()) {
        if (n.name().toLowerCase().equals(value)) return n;
      }
      return EMBED;
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  /** Required for caching */
  private static final long serialVersionUID = 2L;

  /** When there is no document type. */
  public static final String DEFAULT_TYPE = "default";

  /** When there is no specific fragment. */
  public static final String DEFAULT_FRAGMENT = "default";

  /**
   * The URI ID of the target.
   */
  private final long _uri;

  /**
   * The XRef type.
   */
  private final Type _type;

  /**
   * The document type of the target.
   */
  private final String _documenttype;

  /**
   * The target fragment ID.
   */
  private final String _targetfragment;

  /**
   * Whether the XRef has display="document"
   */
  private final Boolean _displaydocument;

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level           The level.
   * @param title           The title of the reference.
   * @param fragment        The Fragment identifier where the reference was found.
   * @param originalfrag    The original (untranscluded) fragment.
   * @param uri             The URI ID.
   * @param type            The XRef type.
   * @param documenttype    The document type of the target.
   * @param targetfrag      The target fragment ID.
   * @param displaydocument Whether the XRef has display="document"
   */
  public Reference(int level, String title, String fragment, String originalfrag,
      Long uri, Type type, @Nullable String documenttype, String targetfrag, Boolean displaydocument) {
    super(level, title, fragment, originalfrag);
    this._uri = uri;
    this._type = type;
    this._documenttype = documenttype;
    this._targetfragment = DEFAULT_FRAGMENT.equals(targetfrag) ? DEFAULT_FRAGMENT : targetfrag;
    this._displaydocument = displaydocument;
  }

  /**
   * Creates a new reference at the specified level for a given URI.
   *
   * @param level      The level.
   * @param title      The title of the reference.
   * @param fragment   The Fragment identifier where the reference was found.
   * @param originalfrag  The original (untranscluded) fragment.
   * @param uri        The URI ID.
   */
  public Reference(int level, String title, String fragment, String originalfrag, Long uri) {
    this(level, title, fragment, originalfrag, uri, Type.EMBED, DEFAULT_TYPE, DEFAULT_FRAGMENT, Boolean.TRUE);
  }

  /**
   * @return The URI ID of the target.
   */
  public long uri() {
    return this._uri;
  }

  /**
   * @return The XRef type.
   */
  public Type type() {
    return this._type;
  }

  /**
   * @return The document type of the target.
   */
  public String documenttype() {
    return this._documenttype;
  }

  /**
   * @return Whether the XRef has display="document".
   */
  public Boolean displaydocument() {
    return this._displaydocument;
  }

  /**
   * @return The target fragment ID.
   */
  public String targetfragment() {
    return this._targetfragment;
  }

  /**
   * Create a new reference identical to this reference but with the specified title
   *
   * @param title The different title
   *
   * @return A new reference instance unless the title is equal to the title of current reference.
   */
  public Reference title(String title) {
    if (title.equals(title())) return this;
    return new Reference(level(), title, fragment(), originalFragment(), this._uri, this._type,
        this._documenttype, this._targetfragment, this._displaydocument);
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
    toXMLNoClose(xml, level, count);
    if (!Element.NO_TITLE.equals(title())) {
      xml.attribute("title", title());
    }
    xml.closeElement();
  }

  public void toXMLNoClose(@NonNull XMLWriter xml, int level, int count) throws IOException {
    xml.openElement("document-ref");
    xml.attribute("level", this.level());
    if (this._documenttype != null) {
      xml.attribute("documenttype", this._documenttype);
    }
    if (this._uri > 0) {
      xml.attribute("uriid", Long.toString(this._uri));
    }
    if (!DEFAULT_FRAGMENT.equals(this._targetfragment)) {
      xml.attribute("targetfragment", this._targetfragment);
    }
    xml.attribute("position", count);
  }

  @Override
  public void toXML(@NonNull XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count,
      boolean numbered, String prefix, boolean children) throws IOException {
    toXML(xml, level, number, treeid, count, title(), numbered, prefix, children, "", null);
  }

  public void toXML(@NonNull XMLWriter xml, int level, @Nullable FragmentNumbering number, long treeid, int count,
                    String title, boolean numbered, String prefix, boolean children, String labels,
                    OffsetDateTime lastedited) throws IOException {
    toXMLNoClose(xml, level, count);
    // if display="document" use title from target document
    if (this._displaydocument != null && this._displaydocument) {
      xml.attribute("title", title);
    } else {
      xml.attribute("title", title());
    }
    if (numbered) {
      xml.attribute("numbered", "true");
    }
    if (children) {
      xml.attribute("children", "true");
    }
    if (!"".equals(labels)) {
      xml.attribute("labels", labels);
    }
    if (lastedited != null) {
      xml.attribute("last-edited", lastedited.format(DateTimeFormatter.ISO_DATE_TIME));
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