package org.pageseeder.psml.md;

/**
 * Configuration options for customizing how Markdown input content should be interpreted.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.6.0
 */
public final class MarkdownInputOptions {

  public static final MarkdownInputOptions DEFAULT_FRAGMENT
      = new MarkdownInputOptions(66, false, false);

  public static final MarkdownInputOptions DEFAULT_DOCUMENT
      = new MarkdownInputOptions(66, true, true);

  /**
   * Line below this value will insert a line break.
   */
  private final int lineBreakThreshold;

  /**
   * Indicates whether the parser should generate a document or a fragment.
   */
  private final boolean isDocumentMode;

  /**
   * Whether a new fragment should be created for every heading.
   */
  private final boolean newFragmentPerHeading;

  MarkdownInputOptions(int lineBreakThreshold, boolean isDocumentMode, boolean newFragmentPerHeading) {
    this.lineBreakThreshold = lineBreakThreshold;
    this.isDocumentMode = isDocumentMode;
    this.newFragmentPerHeading = newFragmentPerHeading;
  }

  /**
   * Returns the default configuration for parsing Markdown as a fragment.
   *
   * @return the default MarkdownInputOptions instance configured for fragment processing.
   */
  public static MarkdownInputOptions defaultFragmentOptions() {
    return DEFAULT_FRAGMENT;
  }

  /**
   * Returns the default configuration for parsing Markdown as a document.
   *
   * <p>This configuration enables document mode, allowing for headings
   * and other document-level features to be processed.
   *
   * @return the default MarkdownInputOptions instance configured for document processing.
   */
  public static MarkdownInputOptions defaultDocumentOptions() {
    return DEFAULT_DOCUMENT;
  }

  public boolean isFragment() {
    return !this.isDocumentMode;
  }

  public boolean isDocument() {
    return this.isDocumentMode;
  }

  public int getLineBreakThreshold() {
    return this.lineBreakThreshold;
  }

  public boolean isNewFragmentPerHeading() {
    return newFragmentPerHeading;
  }

  public MarkdownInputOptions enableDocumentMode() {
    return new MarkdownInputOptions(this.lineBreakThreshold, true, this.newFragmentPerHeading);
  }

  public MarkdownInputOptions disableDocumentMode() {
    return new MarkdownInputOptions(this.lineBreakThreshold, false, this.newFragmentPerHeading);
  }

  public MarkdownInputOptions lineBreakThreshold(int threshold) {
    return new MarkdownInputOptions(threshold, this.isDocumentMode, this.newFragmentPerHeading);
  }

  public MarkdownInputOptions newFragmentPerHeading(boolean enabled) {
    return new MarkdownInputOptions(this.lineBreakThreshold, this.isDocumentMode, enabled);
  }

}
