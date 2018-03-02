/*
 * Copyright (c) 2017 Allette Systems
 */
package org.pageseeder.psml.toc;

/**
 * Specifies how title headings should be computed.
 */
public enum TitleCollapse {

  /**
   * Assumes that the first heading in a document is always the title heading.
   */
  always,

  /**
   * Assumes that the first heading in a document is the title heading only if it
   * matches the document title.
   */
  auto,

  /**
   * There are no title headings, all heading must be preserved.
   */
  never;

  /**
   * Create the title collapse from a string.
   *
   * @param value the string value
   *
   * @return the type
   */
  public static TitleCollapse fromString(String value) {
    for (TitleCollapse n : values()) {
      if (n.name().equals(value)) return n;
    }
    return always;
  }

}
