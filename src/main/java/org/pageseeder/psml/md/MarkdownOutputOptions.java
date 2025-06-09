package org.pageseeder.psml.md;

public class MarkdownOutputOptions {

  private boolean includeMetadata = true;

  private boolean includeImageUrl = false;

  private boolean includeXrefUrl = false;

  public boolean includeMetadata() {
    return includeMetadata;
  }

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
