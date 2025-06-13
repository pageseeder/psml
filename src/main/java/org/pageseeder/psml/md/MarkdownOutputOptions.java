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

  /**
   * Defines options for formatting superscript and subscript text in Markdown output.
   */
  public enum SuperSubFormat {

    /**
     * Superscripts and subscripts are represented using HTML tags (e.g.,
     * {@code <sup>}, {@code }<sub>}).
     */
    HTML,

    /**
     * Superscripts and subscripts are represented using caret (^) and tilde (~) notation.
     */
    CARET_TILDE,

    /**
     * Superscripts and subscripts are represented using their Unicode equivalent
     * characters to render formatted text.
     */
    UNICODE_EQUIVALENT,

    /**
     * Superscript and subscript formatting is ignored and not rendered.
     */
    IGNORE
  }

  /**
   * Specifies the underline formatting style for Markdown output generation.
   */
  public enum UnderlineFormat {

    /**
     * Underlines are represented using the HTML tag {@code <u>}).
     */
    HTML,

    /**
     * Underline formatting is ignored and not rendered.
     */
    IGNORE
  }

  /**
   * Defines the possible formats for rendering properties in the Markdown output.
   */
  public enum PropertiesFormat {

    /**
     * Renders properties as value-pairs {e.g. {@code Title: Value}.
     */
    VALUE_PAIRS,

    /**
     * Renders properties as a table with the "Title" and "Value" headers.
     */
    TABLE

  }

  private final boolean metadata;

  private final boolean captions;

  private final ImageFormat image;

  private final XrefFormat xref;

  private final BlockFormat block;

  private final SuperSubFormat superSub;

  private final UnderlineFormat underline;

  private final PropertiesFormat properties;

  private MarkdownOutputOptions(
      boolean metadata,
      boolean captions,
      ImageFormat image,
      XrefFormat xref,
      BlockFormat block,
      SuperSubFormat superSub,
      UnderlineFormat underline,
      PropertiesFormat properties) {
    this.metadata = metadata;
    this.captions = captions;
    this.image = Objects.requireNonNull(image);
    this.xref = Objects.requireNonNull(xref);
    this.block = Objects.requireNonNull(block);
    this.superSub = Objects.requireNonNull(superSub);
    this.underline = Objects.requireNonNull(underline);
    this.properties = Objects.requireNonNull(properties);
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
    return new MarkdownOutputOptions(true,
        false,
        ImageFormat.LOCAL,
        XrefFormat.BOLD_TEXT,
        BlockFormat.QUOTED,
        SuperSubFormat.IGNORE,
        UnderlineFormat.IGNORE,
        PropertiesFormat.TABLE
    );
  }

  /**
   * Determines whether metadata should be included in the Markdown output.
   *
   * @return true if metadata is included in the output, false otherwise.
   */
  public boolean metadata() {
    return metadata;
  }

  /**
   * Determines whether captions should be included as text in the Markdown output.
   *
   * @return true if captions is included in the output, false otherwise.
   */
  public boolean captions() {
    return captions;
  }

  /**
   * Retrieves the image format option for the Markdown output.
   *
   * @return the image format option used for processing images.
   */
  public ImageFormat image() {
    return image;
  }

  /**
   * Retrieves the current cross-reference (Xref) formatting option used for the Markdown output.
   *
   * @return the cross-reference formatting option (XrefFormat), which indicates how
   *         cross-references are rendered in the Markdown output.
   */
  public XrefFormat xref() {
    return xref;
  }

  /**
   * Retrieves the block formatting option for the Markdown output.
   *
   * @return the block formatting option (BlockFormat), which determines the rendering style
   *         for block label elements in the Markdown output.
   */
  public BlockFormat block() {
    return block;
  }

  /**
   * Retrieves the superscript and subscript formatting option for the Markdown output.
   *
   * @return the superscript and subscript formatting option (SuperSubFormat),
   *         which defines how superscript and subscript elements are represented
   *         in the Markdown output (e.g., HTML, caret/tilde, or ignored).
   */
  public SuperSubFormat superSub() {
    return this.superSub;
  }

  /**
   * Retrieves the underline formatting option for the Markdown output.
   *
   * @return the underline formatting option (UnderlineFormat), which determines
   *         how underline elements are rendered in the Markdown output (e.g., HTML or ignored).
   */
  public UnderlineFormat underline() {
    return this.underline;
  }

  /**
   * Retrieves the properties formatting option for the Markdown output.
   *
   * @return the properties formatting option (PropertiesFormat),
   *         which determines how properties are represented in the Markdown output.
   */
  public PropertiesFormat properties() {
    return this.properties;
  }

  /**
   * Sets whether metadata should be included in the Markdown output.
   *
   * @param include true to include metadata in the output, false to exclude it.
   */
  public MarkdownOutputOptions metadata(boolean include) {
    return new MarkdownOutputOptions(include, this.captions, this.image, this.xref, this.block, this.superSub, this.underline, this.properties);
  }

  /**
   * Sets whether table captions, properties, and images alt text should be included as a paragraph
   * before the table, properties, or images in the content.
   *
   * <p>For example, {@code **Image 2**: Network diagram}
   *
   * @param include true to include captions as text in the output, false to exclude it.
   */
  public MarkdownOutputOptions captions(boolean include) {
    return new MarkdownOutputOptions(this.metadata, include, this.image, this.xref, this.block, this.superSub, this.underline, this.properties);
  }

  /**
   * Sets the image formatting option for the Markdown output.
   *
   * @param format the image formatting option to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated image formatting setting.
   */
  public MarkdownOutputOptions image(ImageFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, format, this.xref, this.block, this.superSub, this.underline, this.properties);
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
  public MarkdownOutputOptions xref(XrefFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, this.image, format, this.block, this.superSub, this.underline, this.properties);
  }

  /**
   * Sets the block formatting option for the Markdown output.
   *
   * @param format the block formatting option to be applied, which determines the rendering style
   *               for block label elements in the Markdown output. Possible values include:
   *               QUOTED for quoted format, and FENCED for fenced format.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated block format.
   */
  public MarkdownOutputOptions block(BlockFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, this.image, this.xref, format, this.superSub, this.underline, this.properties);
  }

  /**
   * Sets the superscript and subscript formatting option for the Markdown output.
   *
   * @param format the superscript and subscript formatting to be applied. Possible values include:
   *               HTML for HTML tag-based formatting,
   *               CARET_TILDE for caret/tilde-based formatting, and
   *               IGNORE to skip rendering superscripts and subscripts.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated superscript
   *         and subscript formatting setting.
   */
  public MarkdownOutputOptions superSub(SuperSubFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, this.image, this.xref, this.block, format, this.underline, this.properties);
  }

  /**
   * Sets the underline formatting option for the Markdown output.
   *
   * @param format the underline formatting to be applied. Possible values include:
   *               HTML for HTML tag-based formatting, and
   *               IGNORE to skip rendering underlined elements.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated underline formatting setting.
   */
  public MarkdownOutputOptions underline(UnderlineFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, this.image, this.xref, this.block, this.superSub, format, this.properties);
  }

  /**
   * Sets the properties formatting option for the Markdown output.
   *
   * @param format the properties formatting option to be applied. Possible values include:
   *               VALUE_PAIRS for rendering properties as value pairs (e.g., Title: Value), and
   *               TABLE for rendering properties as a table with "Title" and "Value" headers.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated properties formatting setting.
   */
  public MarkdownOutputOptions properties(PropertiesFormat format) {
    return new MarkdownOutputOptions(this.metadata, this.captions, this.image, this.xref, this.block, this.superSub, this.underline, format);
  }

  @Override
  public String toString() {
    return "MarkdownOutputOptions{" +
        "metadata=" + metadata +
        ", captions=" + captions +
        ", image=" + image +
        ", xref=" + xref +
        ", block=" + block +
        ", superSub=" + superSub +
        ", underline=" + underline +
        ", properties=" + properties +
        '}';
  }
}
