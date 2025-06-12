package org.pageseeder.psml.md;

import java.util.Objects;

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

  /**
   * Specifies the format used for exporting images in the Markdown output.
   *
   * <p>This enumeration defines the various options for handling image references or embedding within
   * the generated Markdown content. Each format controls how the image is represented in the output.
   */
  public enum ImageFormat {

    /**
     * As a local link
     */
    LOCAL,

    /**
     * As an link to PageSeeder
     */
    EXTERNAL,

    /**
     * Embedded as a Data URI
     */
    DATA_URI,

    /**
     * As an image tag
     */
    IMG_TAG,

    /**
     * Do not include the image
     */
    NONE
  }

  /**
   * Defines the formats for cross-references (Xref) in Markdown output.
   */
  public enum XrefFormat {

    /**
     * Represents an external hyperlink format in Markdown output, used for creating
     * cross-references (Xref) that point to external resources.
     */
    EXTERNAL_LINK,

    /**
     * Represents a local hyperlink format in Markdown output, used for creating
     * cross-references (Xref) that point to other locations within the same document.
     */
    LOCAL_LINK,

    /**
     * Represents bold text format in Markdown output, used for rendering text in a bold style
     * within cross-references (Xref).
     */
    BOLD_TEXT,

    /**
     * Represents plain text format in Markdown output, used for cross-references (Xref)
     * where no hyperlink is applied, and only the text is rendered as-is.
     */
    TEXT
  }

  /**
   * Enumeration representing the formats for rendering block label elements
   * in Markdown output.
   */
  public enum BlockFormat {

    /**
     * Represents a block label element rendered in a quoted format using
     * {@code '> **[label]**:'} in the Markdown output.
     */
    QUOTED,

    /**
     * Represents a block label element rendered in a fenced format using
     * {@code '~~~[label]'} in the Markdown output.
     */
    FENCED,

    /**
     * Represents a block label element rendered in a prefixed format in the Markdown output.
     * The specific rendering details for this format are not defined in the provided documentation.
     */
    LABELED_TEXT

  }

  private final boolean includeMetadata;

  private final ImageFormat imageFormat;

  private final XrefFormat xrefFormat;

  private final BlockFormat blockFormat;

  private MarkdownOutputOptions(boolean includeMetadata, ImageFormat image, XrefFormat xref, BlockFormat block) {
    this.includeMetadata = includeMetadata;
    this.imageFormat = Objects.requireNonNull(image);
    this.xrefFormat = Objects.requireNonNull(xref);
    this.blockFormat = Objects.requireNonNull(block);
  }

  /**
   * Provides the default configuration options for generating Markdown output.
   *
   * <p>The default options include:
   * - Metadata inclusion set to true
   * - Images are not included (ImageFormat.NONE)
   * - Cross-references are represented as plain text (XrefFormat.BOLD_TEXT)
   * - Block elements are formatted as quoted blocks (BlockFormat.QUOTED)
   *
   * @return the default {@code MarkdownOutputOptions} instance configured with standard settings.
   */
  public static MarkdownOutputOptions defaultOptions() {
    return new MarkdownOutputOptions(true, ImageFormat.NONE, XrefFormat.BOLD_TEXT, BlockFormat.QUOTED);
  }

  /**
   * Determines whether metadata should be included in the Markdown output.
   *
   * @return true if metadata is included in the output, false otherwise.
   */
  public boolean includeMetadata() {
    return includeMetadata;
  }

  /**
   * Retrieves the image format option for the Markdown output.
   *
   * @return the image format option used for processing images.
   */
  public ImageFormat imageFormat() {
    return imageFormat;
  }

  /**
   * Retrieves the current cross-reference (Xref) formatting option used for the Markdown output.
   *
   * @return the cross-reference formatting option (XrefFormat), which indicates how
   *         cross-references are rendered in the Markdown output.
   */
  public XrefFormat xrefFormat() {
    return xrefFormat;
  }

  /**
   * Retrieves the block formatting option for the Markdown output.
   *
   * @return the block formatting option (BlockFormat), which determines the rendering style
   *         for block label elements in the Markdown output.
   */
  public BlockFormat blockFormat() {
    return blockFormat;
  }

  /**
   * Sets whether metadata should be included in the Markdown output.
   *
   * @param includeMetadata true to include metadata in the output, false to exclude it.
   */
  public MarkdownOutputOptions includeMetadata(boolean includeMetadata) {
    return new MarkdownOutputOptions(includeMetadata, this.imageFormat, this.xrefFormat, this.blockFormat);
  }

  /**
   * Sets the image formatting option for the Markdown output.
   *
   * @param format the image formatting option to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated image formatting setting.
   */
  public MarkdownOutputOptions imageFormat(ImageFormat format) {
    return new MarkdownOutputOptions(this.includeMetadata, format, this.xrefFormat, this.blockFormat);
  }

  /**
   * Sets the format for cross-references (Xref) in the Markdown output.
   *
   * @param format the cross-reference formatting to be applied. Possible values include:
   *               EXTERNAL_LINK for external hyperlinks,
   *               LOCAL_LINK for local hyperlinks within the same document, and
   *               TEXT for plain text representation without hyperlinks.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated cross-reference format.
   */
  public MarkdownOutputOptions xrefFormat(XrefFormat format) {
    return new MarkdownOutputOptions(this.includeMetadata, this.imageFormat, format, this.blockFormat);
  }

  /**
   * Sets the block formatting option for the Markdown output.
   *
   * @param format the block formatting option to be applied, which determines the rendering style
   *               for block label elements in the Markdown output. Possible values include:
   *               QUOTED for quoted format, and FENCED for fenced format.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated block format.
   */
  public MarkdownOutputOptions blockFormat(BlockFormat format) {
    return new MarkdownOutputOptions(this.includeMetadata, this.imageFormat, this.xrefFormat, format);
  }

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
