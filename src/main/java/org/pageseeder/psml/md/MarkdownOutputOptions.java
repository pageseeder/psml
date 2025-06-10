package org.pageseeder.psml.md;

/**
 * Configuration options for customizing the output of Markdown content.
 *
 * <p>This class provides various toggles for including or excluding specific elements
 * in the Markdown output, such as metadata, image URLs, and cross-reference URLs.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public final class MarkdownOutputOptions {

  private boolean includeMetadata = true;

  private boolean includeImageUrl = false;

  private boolean includeXrefUrl = false;

  /**
   * Determines whether metadata should be included in the Markdown output.
   *
   * @return true if metadata is included in the output, false otherwise.
   */
  public boolean includeMetadata() {
    return includeMetadata;
  }

  /**
   * Sets whether metadata should be included in the Markdown output.
   *
   * @param includeMetadata true to include metadata in the output, false to exclude it.
   */
  public void setIncludeMetadata(boolean includeMetadata) {
    this.includeMetadata = includeMetadata;
  }

  public boolean includeImageUrl() {
    return includeImageUrl;
  }

  public void setIncludeImageUrl(boolean includeImageUrl) {
    this.includeImageUrl = includeImageUrl;
  }

  public void setIncludeXrefUrl(boolean includeXrefUrl) {
    this.includeXrefUrl = includeXrefUrl;
  }

  public boolean includeXrefUrl() {
    return includeXrefUrl;
  }

  // Possible options

  // Images:
  // - export image alongside md file as local
  // - embed as base64 (max size threshold)
  // - include url to PageSeeder?

  // Xrefs:
  // - as plain text
  // - as links to PageSeeder

  // Block xrefs
  // - as plain text
  // - as links to PageSeeder

  // Properties
  // - As tables
  // - As value-pairs

  // SuperScript/subscript
  // - As HTML (with <sup> <sub)
  // - As extended (with ^ and ~)
  // - Ignore

  // Underline
  // - As HTML (with <u>)
  // - with `~` (Bear)
  // - with `++` (Common)


}
