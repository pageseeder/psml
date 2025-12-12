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
 * @version 1.6.10
 * @since 1.6.0
 */
public final class MarkdownOutputOptions {

  /**
   * Specifies the format used for PSML content marked as bold with the
   * {@code <bold>} element in the Markdown output.
   *
   * <p>For example:</p>
   * <pre>{@code This is <bold>bold text</bold>.}</pre>
   */
  public enum BoldFormat {

    /**
     * Format the content using a double asterisk.
     *
     * <p>For example:</p>
     * <pre>{@code This is **bold text**.}</pre>
     */
    DOUBLE_ASTERISK("**"),

    /**
     * Format the content using a double underscore.
     *
     * <p>For example:</p>
     * <pre>{@code This is __bold text__.}</pre>
     */
    DOUBLE_UNDERSCORE("__"),

    /**
     * Ignore the bold formatting and just output the content as-is.
     *
     * <p>For example:</p>
     * <pre>{@code This is bold text.}</pre>
     */
    IGNORE("");

    private final String format;

    BoldFormat(String format) {
      this.format = format;
    }

    String format() {
      return format;
    }
  }

  /**
   * Specifies the format used for PSML content marked as italic with the
   * {@code <italic>} element in the Markdown output.
   *
   * <p>For example:</p>
   * <pre>{@code This is an <bold>emphasis</bold>.}</pre>
   */
  public enum ItalicFormat {

    /**
     * Format the content using a single asterisk.
     *
     * <p>For example:</p>
     * <pre>{@code This is an *emphasis*.}</pre>
     */
    ASTERISK,

    /**
     * Format the content using a single underscore.
     *
     * <p>For example:</p>
     * <pre>{@code This is an _emphasis_.}</pre>
     */
    UNDERSCORE,

    /**
     * Ignore the bold formatting and just output the content as-is.
     *
     * <p>For example:</p>
     * <pre>{@code This is an emphasis.}</pre>
     */
    IGNORE;

  }

  /**
   * Specifies the format used for exporting images in the Markdown output.
   *
   * <p>This enumeration defines the various options for handling image references or embedding within
   * the generated Markdown content. Each format controls how the image is represented in the output.
   *
   * <p>for example:</p>
   * <pre>{@code
   * <image src="/common/images/sample.png" alt="My sample image" uriid="1234"
   *        height="150" width="300" labels="border,shadow,photo"/>
   * }</pre>
   */
  public enum ImageFormat {

    /**
     * As a local link
     *
     * <p>for example:</p>
     * <pre>{@code ![My sample image](images/sample.png) }</pre>
     */
    LOCAL,

    /**
     * As a link to PageSeeder.
     *
     * <p>for example:</p>
     * <pre>{@code ![My sample image](https://ps.example.org/ps/uri/1234.png) }</pre>
     */
    EXTERNAL,

    /**
     * Embedded as a Data URI
     */
    DATA_URI,

    /**
     * As an HTML image tag.
     *
     * <p>for example:</p>
     * <pre>{@code
     * <img src="/common/images/sample.png" alt="My sample image"
     *      height="150" width="300" />
     * }</pre>
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
     * Represents the bold text format in Markdown output, used for rendering text in a bold style
     * within cross-references (Xref).
     */
    BOLD_TEXT,

    /**
     * Represents the plain text format in Markdown output, used for cross-references (Xref)
     * where no hyperlink is applied, and only the text is rendered as-is.
     */
    TEXT
  }

  /**
   * Enumeration representing the formats for rendering block label elements
   * in Markdown output.
   *
   * <p>For example:</p>
   * <pre>{@code
   * <block label="warning">Do not attempt to read this document while eating.
   * Your focus may be compromised.</block>
   * }</pre>
   */
  public enum BlockFormat {

    /**
     * Formats a block label element using the quoted format and prefixed with
     * {@code '> **[label]**:'} in the Markdown output.
     *
     * <p>For example:</p>
     * <pre>{@code
     * > **Warning**: Do not attempt to read this document while eating.
     * > Your focus may be compromised.
     * }</pre>
     */
    QUOTED,

    /**
     * Formates a block label element using the fenced format using
     * {@code '~~~[label]'} around block content in the Markdown output.
     *
     * <p>For example:</p>
     * <pre>{@code
     * ~~~warning
     * Do not attempt to read this document while eating.
     * Your focus may be compromised.
     * ~~~
     * }</pre>
     */
    FENCED,

    /**
     * Formats a block label element using a prefixed format starting with
     * {@code '**[label]**:'} in the Markdown output.
     *
     * <p>For example:</p>
     * <pre>{@code
     * **Warning**: Do not attempt to read this document while eating.
     * Your focus may be compromised.
     * }</pre>
     */
    LABELED

  }

  /**
   * Defines options for formatting superscript and subscript text in Markdown output.
   *
   * <p>For example</p>
   * <pre>{@code x<sup>2</sup> or note<sup>2</sup>}</pre>
   */
  public enum SuperSubFormat {

    /**
     * Superscripts and subscripts are represented using HTML tags (e.g.,
     * {@code <sup>}, {@code <sub>}).
     *
     * <p>For example</p>
     * <pre>{@code x<sup>2</sup> or note<sup>2</sup>}</pre>
     */
    HTML,

    /**
     * Superscripts and subscripts are represented using caret (^) and tilde (~) notation.
     *
     * <p>For example</p>
     * <pre>{@code x^2^ or note~2~}</pre>
     */
    CARET_TILDE,

    /**
     * Superscripts and subscripts are represented using their Unicode equivalent
     * characters to render formatted text.
     *
     * <p>For example</p>
     * <pre>{@code x² or note₂}</pre>
     */
    UNICODE,

    /**
     * Superscript and subscript formatting is ignored and not rendered.
     *
     * <p>For example</p>
     * <pre>{@code x2 or note2}</pre>
     */
    IGNORE
  }

  /**
   * Specifies the underline formatting style for Markdown output generation.
   *
   * <p>For example</p>
   * <pre>{@code The <underline>formula</underline> is crucial.}</pre>
   */
  public enum UnderlineFormat {

    /**
     * Underlines are represented using the HTML tag {@code <u>}).
     *
     * <p>For example</p>
     * <pre>{@code The <u>formula</u> is crucial.}</pre>
     */
    HTML,

    /**
     * Underline formatting is ignored and not rendered.
     *
     * <p>For example</p>
     * <pre>{@code The formula is crucial.}</pre>
     */
    IGNORE
  }

  /**
   * Defines the possible formats for rendering properties in the Markdown output.
   *
   * <p>For example.</p>
   * <pre>{@code
   *   <properties-fragment id="bookinfo"
   *     <property title="Title"  name="title"  value="Echoes of the Forgotten Sky"/>
   *     <property title="Author" name="author" value="Dr. Elara Voss"/>
   *     <property title="Publication date" name="pubdate" datatype="date value="2023-04-05"/>
   *     <property title="Genre" name="gener" multiple="true">
   *       <value>Science Fiction</value>
   *       <value>Mystery</value>
   *     </property>
   *   </properties-fragment>
   * }</pre>
   *
   * <p>If metadata are enabled, the properties fragment will start with</p>
   * <pre>{@code **Properties 1**: bookinfo }</pre>
   */
  public enum PropertiesFormat {

    /**
     * Renders properties as value-pairs {e.g. {@code Title: Value}.
     *
     * <p>For example.</p>
     * <pre>{@code
     * Title: Echoes of the Forgotten Sky
     * Author: Dr. Elara Voss
     * Publication date: 2023-04-05
     * Genre: [Science Fiction, Mystery]
     * }</pre>
     */
    VALUE_PAIRS,

    /**
     * Renders properties as a table with the "Title" and "Value" headers.
     *
     * <p>For example.</p>
     * <pre>{@code
     * | Name | Value |
     * | ----- | ----- |
     * | Title | Echoes of the Forgotten Sky |
     * | Author |  Dr. Elara Voss |
     * | Publication date | 2023-04-05 |
     * | Genre | [Science Fiction, Mystery] |
     * }</pre>
     */
    TABLE

  }

  public enum TableFormat {

    /**
     * Formats the table with the minimum number of characters to be valid Markdown
     */
    COMPACT,

    /**
     * Formats the table so that the cell borders line up are easier to read, making the table
     * more visually appealing.
     */
    PRETTY,

    /**
     * Formats and normalize the table for AI systems, where cells spanning multiple rows
     * or columns are repeated to ensure maximum compatibility with AI systems.
     */
    NORMALIZED,

    /**
     * Formats the table as an HTML table, suitable for including in web pages or other
     * environments that support HTML markup.
     */
    HTML

  }

  /**
   * Specifies the style to use for designators for table captions, images, properties and block labels.
   *
   * <p>The designator can be: "Table", "Image", "Properties" or the label name.</p>
   */
  public enum DesignatorStyle {

    /**
     * Use a double asterisk.
     *
     * <p>For example:</p>
     * <pre>{@code **Table 1**: Sample table**}</pre>
     * or
     * <pre>{@code **Note:** this just an example}</pre>
     */
    DOUBLE_ASTERISK,

    /**
     * Use a single asterisk.
     *
     * <p>For example:</p>
     * <pre>{@code *Table 1*: Sample table}</pre>
     * or
     * <pre>{@code *Note:* this just an example}</pre>
     */
    ASTERISK,

    /**
     * Convert all characters of the designator to the upper case.
     *
     * <p>For example:</p>
     * <pre>{@code TABLE 1: Sample table}</pre>
     * or
     * <pre>{@code NOTE: this just an example}</pre>
     */
    UPPER_CASE,

    /**
     * Just use a capitalized word.
     *
     * <p>For example:</p>
     * <pre>{@code Table 1: Sample table}</pre>
     * or
     * <pre>{@code Note: this just an example}</pre>
     */
    PLAIN,

    /**
     * Don't output designators at all.
     */
    NONE;

    /**
     * Formats the designator using the specified style.
     *
     * @param designator The designator to format.
     * @return the formatted designator.
     */
    public String format(String designator) {
      switch (this) {
        case DOUBLE_ASTERISK: return "**" + designator + "**";
        case ASTERISK: return "*" + designator + "*";
        case UPPER_CASE: return designator.toUpperCase();
        case PLAIN: return designator.substring(0, 1).toUpperCase() + designator.substring(1);
        case NONE: return "";
      }
      return designator;
    }

  }

  private final boolean metadata;

  private final DesignatorStyle designator;

  private final BoldFormat bold;

  private final ItalicFormat italic;

  private final ImageFormat image;

  private final XrefFormat xref;

  private final BlockFormat block;

  private final SuperSubFormat superSub;

  private final UnderlineFormat underline;

  private final PropertiesFormat properties;

  private final TableFormat table;

  @SuppressWarnings("java:S107")
  private MarkdownOutputOptions(
      boolean metadata,
      DesignatorStyle designator,
      BoldFormat bold,
      ItalicFormat italic,
      ImageFormat image,
      XrefFormat xref,
      BlockFormat block,
      SuperSubFormat superSub,
      UnderlineFormat underline,
      PropertiesFormat properties,
      TableFormat table) {
    this.metadata = metadata;
    this.designator = designator;
    this.bold = Objects.requireNonNull(bold);
    this.italic = Objects.requireNonNull(italic);
    this.image = Objects.requireNonNull(image);
    this.xref = Objects.requireNonNull(xref);
    this.block = Objects.requireNonNull(block);
    this.superSub = Objects.requireNonNull(superSub);
    this.underline = Objects.requireNonNull(underline);
    this.properties = Objects.requireNonNull(properties);
    this.table = Objects.requireNonNull(table);
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
        DesignatorStyle.NONE,
        BoldFormat.DOUBLE_UNDERSCORE,
        ItalicFormat.ASTERISK,
        ImageFormat.LOCAL,
        XrefFormat.BOLD_TEXT,
        BlockFormat.QUOTED,
        SuperSubFormat.IGNORE,
        UnderlineFormat.IGNORE,
        PropertiesFormat.TABLE,
        TableFormat.PRETTY
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
   * @return true if captions are included in the output, false otherwise.
   *
   * @deprecated since 1.6.9, use designator() instead.
   */
  @Deprecated(forRemoval = true, since = "1.6.9")
  public boolean captions() {
    return DesignatorStyle.NONE != this.designator;
  }

  /**
   * Returns the style for designators
   *
   * @return The designator style.
   */
  public DesignatorStyle designator() {
    return designator;
  }

  /**
   * Returns the bold format option for the Markdown output.
   *
   * @return the bold format option used for processing bold text.
   */
  public BoldFormat bold() {
    return bold;
  }

  /**
   * Returns the italic format option for the Markdown output.
   *
   * @return the italic format option used for processing italic text.
   */
  public ItalicFormat italic() {
    return italic;
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
   * Retrieves the formatting option for properties in the Markdown output.
   *
   * @return the properties formatting option (PropertiesFormat),
   *         which determines how properties are represented in the Markdown output.
   */
  public PropertiesFormat properties() {
    return this.properties;
  }

  /**
   * Retrieves the table formatting option for the Markdown output.
   *
   * @return the table formatting option (TableFormat), which specifies how table elements
   *         are formatted in the Markdown output, such as compact or visually aligned styles.
   */
  public TableFormat table() {
    return this.table;
  }

  /**
   * Sets whether metadata should be included in the Markdown output.
   *
   * @param include true to include metadata in the output, false to exclude it.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated metadata formatting setting.
   */
  public MarkdownOutputOptions metadata(boolean include) {
    if (this.metadata == include) return this;
    return new MarkdownOutputOptions(include, this.designator, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
  }

  /**
   * Sets whether table captions, properties, and images alt text should be included as a paragraph
   * before the table, properties, or images in the content.
   *
   * <p>For example, {@code **Image 2**: Network diagram}
   *
   * @param include true to include captions as text in the output, false to exclude it.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated caption formatting setting.
   */
  public MarkdownOutputOptions captions(boolean include) {
    if (include) {
      if (this.designator != DesignatorStyle.NONE) return this;
      return new MarkdownOutputOptions(this.metadata, DesignatorStyle.DOUBLE_ASTERISK, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
    } else {
      if (this.designator == DesignatorStyle.NONE) return this;
      return new MarkdownOutputOptions(this.metadata, DesignatorStyle.NONE, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
    }
  }

  /**
   * Sets the style of table captions, properties, images alt text should be included as a paragraph
   * before the table, properties, or images in the content.
   *
   * <p>For example, {@code **Image 2**: Network diagram}
   *
   * @param style true to include captions as text in the output, false to exclude it.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated caption formatting setting.
   */
  public MarkdownOutputOptions designator(DesignatorStyle style) {
    if (this.designator == style) return this;
    return new MarkdownOutputOptions(this.metadata, style, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
  }

  /**
   * Sets the bold formatting option for the Markdown output.
   *
   * @param format the bold formatting to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated bold formatting setting.
   */
  public MarkdownOutputOptions bold(BoldFormat format) {
    if (this.bold == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, format, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
  }

  /**
   * Sets the italic formatting option for the Markdown output.
   *
   * @param format the italic formatting to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated italic formatting setting.
   */
  public MarkdownOutputOptions italic(ItalicFormat format) {
    if (this.italic == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, format, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
  }

  /**
   * Sets the image formatting option for the Markdown output.
   *
   * @param format the image formatting option to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated image formatting setting.
   */
  public MarkdownOutputOptions image(ImageFormat format) {
    if (this.image == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, format, this.xref, this.block, this.superSub, this.underline, this.properties, this.table);
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
    if (this.xref == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, format, this.block, this.superSub, this.underline, this.properties, this.table);
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
    if (this.block == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, this.xref, format, this.superSub, this.underline, this.properties, this.table);
  }

  /**
   * Sets the superscript and subscript formatting option for the Markdown output.
   *
   * @param format the superscript and subscript formatting to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated superscript
   *         and subscript formatting setting.
   */
  public MarkdownOutputOptions superSub(SuperSubFormat format) {
    if (this.superSub == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, this.xref, this.block, format, this.underline, this.properties, this.table);
  }

  /**
   * Sets the underline formatting option for the Markdown output.
   *
   * @param format the underline formatting to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated underline formatting setting.
   */
  public MarkdownOutputOptions underline(UnderlineFormat format) {
    if (this.underline == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, format, this.properties, this.table);
  }

  /**
   * Sets the properties formatting option for the Markdown output.
   *
   * @param format the properties formatting option to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated properties formatting setting.
   */
  public MarkdownOutputOptions properties(PropertiesFormat format) {
    if (this.properties == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, format, this.table);
  }

  /**
   * Configures the table formatting option for the Markdown output.
   *
   * @param format the table formatting option to be applied.
   * @return a new instance of {@code MarkdownOutputOptions} with the updated table formatting setting.
   */
  public MarkdownOutputOptions table(TableFormat format) {
    if (this.table == format) return this;
    return new MarkdownOutputOptions(this.metadata, this.designator, this.bold, this.italic, this.image, this.xref, this.block, this.superSub, this.underline, this.properties, format);
  }

  /**
   * Creates a new {@code Builder} instance based on the current {@code MarkdownOutputOptions}.
   *
   * @return a {@code Builder} instance initialized with the settings of the current {@code MarkdownOutputOptions}.
   */
  public Builder builder() {
    return Builder.from(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MarkdownOutputOptions that = (MarkdownOutputOptions) o;
    return metadata == that.metadata &&
        bold == that.bold &&
        italic == that.italic &&
        designator == that.designator &&
        image == that.image &&
        xref == that.xref &&
        block == that.block &&
        superSub == that.superSub &&
        underline == that.underline &&
        properties == that.properties &&
        table == that.table;
  }

  @Override
  public int hashCode() {
    return Objects.hash(metadata, designator, bold, italic, image, xref, block, superSub, underline, properties, table);
  }

  @Override
  public String toString() {
    return "MarkdownOutputOptions{" +
        "metadata=" + metadata +
        ", designator=" + designator +
        ", bold=" + bold +
        ", italic=" + italic +
        ", image=" + image +
        ", xref=" + xref +
        ", block=" + block +
        ", superSub=" + superSub +
        ", underline=" + underline +
        ", properties=" + properties +
        ", table=" + table +
        '}';
  }

  /**
   * Builder class for configuring and creating instances of {@code MarkdownOutputOptions}.
   *
   * <p>This class provides methods to customize various formatting options for Markdown output.
   *
   * <p>All customization methods return the builder instance itself to support method chaining.
   */
  public static class Builder {
    private boolean metadata = true;
    private DesignatorStyle designator = DesignatorStyle.NONE;
    private BoldFormat bold = BoldFormat.DOUBLE_UNDERSCORE;
    private ItalicFormat italic = ItalicFormat.ASTERISK;
    private ImageFormat image = ImageFormat.LOCAL;
    private XrefFormat xref = XrefFormat.BOLD_TEXT;
    private BlockFormat block = BlockFormat.QUOTED;
    private SuperSubFormat superSub = SuperSubFormat.IGNORE;
    private UnderlineFormat underline = UnderlineFormat.IGNORE;
    private PropertiesFormat properties = PropertiesFormat.TABLE;
    private TableFormat table = TableFormat.PRETTY;

    /**
     * Creates a new Builder with default options.
     */
    public Builder() {
      // Default values already set in field initialization
    }

    /**
     * Creates a new Builder with values copied from an existing options object.
     *
     * @param options The options to copy values from
     * @return A new builder initialized with the values from the provided options
     */
    public static Builder from(MarkdownOutputOptions options) {
      Builder builder = new Builder();
      builder.metadata = options.metadata();
      builder.designator = options.designator();
      builder.bold = options.bold();
      builder.italic = options.italic();
      builder.image = options.image();
      builder.xref = options.xref();
      builder.block = options.block();
      builder.superSub = options.superSub();
      builder.underline = options.underline();
      builder.properties = options.properties();
      builder.table = options.table();
      return builder;
    }

    /**
     * Sets whether metadata should be included in the Markdown output.
     *
     * @param include true to include metadata in the output, false to exclude it.
     * @return this builder instance
     */
    public Builder metadata(boolean include) {
      this.metadata = include;
      return this;
    }

    /**
     * Sets whether table captions, properties, and images alt text should be included as a paragraph
     * before the table, properties, or images in the content.
     *
     * <p>For example, {@code **Image 2**: Network diagram}
     *
     * @param include true to include captions as text in the output, false to exclude it.
     * @return this builder instance
     */
    public Builder captions(boolean include) {
      this.designator = DesignatorStyle.DOUBLE_ASTERISK;
      return this;
    }

    /**
     * Sets whether table captions, properties, images alt text should be included as a paragraph
     * before the table, properties, or images in the content.
     *
     * <p>For example, {@code **Image 2**: Network diagram}
     *
     * @param style the style to use for designators.
     * @return this builder instance
     */
    public Builder designator(DesignatorStyle style) {
      this.designator = style;
      return this;
    }

    /**
     * Sets the image formatting option for the Markdown output.
     *
     * @param format the image formatting option to be applied.
     * @return this builder instance
     */
    public Builder image(ImageFormat format) {
      this.image = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the format for cross-references (Xref) in the Markdown output.
     *
     * @param format the cross-reference formatting to be applied.
     * @return this builder instance
     */
    public Builder xref(XrefFormat format) {
      this.xref = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the block formatting option for the Markdown output.
     *
     * @param format the block formatting option to be applied.
     * @return this builder instance
     */
    public Builder block(BlockFormat format) {
      this.block = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the superscript and subscript formatting option for the Markdown output.
     *
     * @param format the superscript and subscript formatting to be applied.
     * @return this builder instance
     */
    public Builder superSub(SuperSubFormat format) {
      this.superSub = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the underline formatting option for the Markdown output.
     *
     * @param format the underline formatting to be applied.
     * @return this builder instance
     */
    public Builder underline(UnderlineFormat format) {
      this.underline = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the properties formatting option for the Markdown output.
     *
     * @param format the properties formatting option to be applied.
     * @return this builder instance
     */
    public Builder properties(PropertiesFormat format) {
      this.properties = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Configures the table formatting option for the Markdown output.
     *
     * @param format the table formatting option to be applied.
     * @return this builder instance
     */
    public Builder table(TableFormat format) {
      this.table = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the bold formatting option for the Markdown output.
     *
     * @param format the bold formatting option to be applied
     * @return this builder instance
     */
    public Builder bold(BoldFormat format) {
      this.bold = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Sets the italic formatting option for the Markdown output.
     *
     * @param format the italic formatting option to be applied
     * @return this builder instance
     */
    public Builder italic(ItalicFormat format) {
      this.italic = Objects.requireNonNull(format);
      return this;
    }

    /**
     * Builds a new {@code MarkdownOutputOptions} instance with the current builder configuration.
     *
     * @return a new immutable {@code MarkdownOutputOptions} instance
     */
    public MarkdownOutputOptions build() {
      return new MarkdownOutputOptions(
          metadata,
          designator,
          bold,
          italic,
          image,
          xref,
          block,
          superSub,
          underline,
          properties,
          table
      );
    }
  }

}
