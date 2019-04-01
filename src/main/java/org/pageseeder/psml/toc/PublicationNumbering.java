/*
 * Copyright (c) 1999-2018 allette systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.toc.FragmentNumbering.Prefix;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Publication numbering configuration
 *
 * @author Philip Rutherford
 */
public final class PublicationNumbering {

  /**
   * An enumeration for numbering type
   *
   * @author Philip Rutherford
   */
  protected enum NumberType {

    /** decimal number **/
    DECIMAL,

    /** lower case letter **/
    LOWERALPHA,

    /** upper case letter **/
    UPPERALPHA,

    /** lower case Roman numeral **/
    LOWERROMAN,

    /** upper case Roman numeral **/
    UPPERROMAN;

    /**
     * Create the NumberType from a string.
     *
     * @param value the string value
     *
     * @return the type
     */
    public static NumberType fromString(String value) {
      for (NumberType n : values()) {
        if (n.name().toLowerCase().equals(value)) return n;
      }
      return DECIMAL;
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  /**
   * An enumeration for element names
   *
   * @author Philip Rutherford
   */
  public enum ElementName {

    /** heading element **/
    HEADING,

    /** para element **/
    PARA,

    /** apply to any elements **/
    ANY;

    /**
     * Create the ElementName from a string.
     *
     * @param value the string value
     *
     * @return the name
     */
    public static ElementName fromString(String value) {
      for (ElementName n : values()) {
        if (n.name().toLowerCase().equals(value)) return n;
      }
      return HEADING;
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  /**
   * An enumeration for numbering of skipped levels
   *
   * @author Philip Rutherford
   */
  public enum SkippedLevels {

    /** skipped levels will be numbered as 1 - e.g. 2.1.3 (default) **/
    ONE,

    /** skipped levels will be numbered as 0 - e.g. 2.0.3 **/
    ZERO,

    /** skipped levels will be stripped - e.g. 2.3 **/
    STRIP;

    /**
     * Create the SkippedLevels from a string.
     *
     * @param value the string value
     *
     * @return the type
     */
    public static SkippedLevels fromString(String value) {
      if ("1".equals(value)) return ONE;
      if ("0".equals(value)) return ZERO;
      if ("strip".equals(value)) return STRIP;
      return ONE;
    }
  }

  /** A pattern for all schemes */
  private static final Pattern SCHEME_PATTERN = Pattern.compile("\\[(.*?)([1-9])(.*?)\\]");
  /** A pattern for canonical labels */
  private static final Pattern CANONICAL_PATTERN = Pattern.compile("(\\d+)\\.");
  /** The lowercase alphabet, for convenience */
  private static final String[] LOWERCASE_ALPHABET = new String[] { "a", "b",
      "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
      "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
  /** The upperase alphabet, for convenience */
  private static final String[] UPPERCASE_ALPHABET = new String[] { "A", "B",
      "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
      "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
  /** The lowercase roman numerals, for convenience */
  private static final String[] LOWERCASE_ROMAN_ALPHABET = new String[] { "i",
      "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"};
  /** The uppercase roman numerals, for convenience */
  private static final String[] UPPERCASE_ROMAN_ALPHABET = new String[] { "I",
      "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

  /**
   * Numbering of skipped levels
   */
  private SkippedLevels skippedLevels = SkippedLevels.ONE;

  /**
   * Document label to apply numbering to
   */
  private String label = "";

  /**
   * Map of formats for numbering keyed on [level]-[blocklabel]
   */
  private final Map<String, String> formats = new HashMap<>();

  /**
   * Map of number types for numbering keyed on [level]-[blocklabel]
   */
  private final Map<String, NumberType> types = new HashMap<>();

  /**
   * Map of element names for numbering keyed on [level]-[blocklabel]
   */
  private final Map<String, ElementName> elements = new HashMap<>();


  /**
   * @param label the document label to set
   */
  public void setLabel(String label) {
    if (label == null) throw new IllegalArgumentException("Label can not be null");
    this.label = label ;
  }

  /**
   * @param skippedLevels the numbering for skipped levels to set
   */
  public void setSkippedLevels(SkippedLevels skippedLevels) {
    this.skippedLevels = skippedLevels;
  }

  /**
   * @return the document label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return the numbering for skipped levels
   */
  public SkippedLevels getSkippedLevels() {
    return this.skippedLevels;
  }

  /**
   * Add a new numbering format.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label (optional)
   * @param scheme     the scheme pattern
   */
  public void addNumberFormat(int level, @Nullable String blocklabel, String scheme) {
    if (blocklabel == null) blocklabel = "";
    this.formats.put(level + "-" + blocklabel, scheme);
  }

  /**
   * Add a new numbering type.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label (optional)
   * @param type       the scheme type
   */
  public void addNumberType(int level, @Nullable String blocklabel, String type) {
    if (blocklabel == null) blocklabel = "";
    this.types.put(level + "-" + blocklabel, NumberType.fromString(type));
  }

  /**
   * Add a new element name.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label (optional)
   * @param element    the scheme element
   */
  public void addElement(int level, @Nullable String blocklabel, String element) {
    if (blocklabel == null) blocklabel = "";
    this.elements.put(level + "-" + blocklabel, ElementName.fromString(element));
  }

  /**
   * Get a numbering format.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label name
   */
  public String getNumberFormat(int level, String blocklabel) {
    return this.formats.get(level + "-" + blocklabel);
  }

  /**
   * Get a numbering type. If none found for blocklabel returns type for no blocklabel.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label name
   */
  public NumberType getNumberType(int level, String blocklabel) {
    NumberType type = this.types.get(level + "-" + blocklabel);
    if (type == null) type = this.types.get(level + "-");
    return type;
  }

  /**
   * Finds the level that numbering for an element is defined at.
   * For non-empty blocklabel it ignores the given level and searches all levels.
   * If none found for blocklabel it, returns the result for no blocklabel.
   *
   * @param level      the level of the scheme
   * @param blocklabel the parent block label name
   * @param name       the element name
   *
   * @return level element was found at
   */
  public int elementLevel(int level, String blocklabel, String name) {
    String key = "";
    // search for blocklabel at any level
    if (!"".equals(blocklabel)) {
      Set<String> keys = this.elements.keySet();
      String suffix = "-" + blocklabel;
      for (String k : keys) {
        if (k.endsWith(suffix)) {
          if ("".equals(key)) {
            key = k;
          // if found twice it is invalid
          } else {
            key = "";
            break;
          }
        }
      }
    } else {
      key = level + "-" + blocklabel;
    }
    ElementName element = this.elements.get(key);
    if (element == null) {
      key = level + "-";
      element = this.elements.get(key);
    }
    int l = Integer.parseInt(key.substring(0, key.indexOf('-')));
    return (ElementName.ANY.equals(element) || (element != null && element.toString().equals(name))) ? l : 0;
  }

  /**
   * Compute the numbering prefix from the canonical label
   *
   * @param canonical   the canonical label
   * @param blocklabel  the parent blocklabel name
   *
   * @return the prefix
   */
  public Prefix getPrefix(String canonical, String blocklabel) {
    // make sure it always ends with dot
    String toParse = !canonical.matches("^.*\\.$") ? canonical + '.' : canonical;
    // find level
    int level = toParse.split("\\.").length;
    // compute prefix
    StringBuilder prefix = new StringBuilder();
    int lowest = buildPrefix(prefix, toParse, getNumberFormat(level, blocklabel), blocklabel);
    if (lowest == 1) return new Prefix(prefix.toString(), canonical, level, null);
    // compute parent number
    StringBuilder parentNumber = new StringBuilder();
    int i = toParse.lastIndexOf('.', toParse.length() - 2);
    int prevlevel = level;
    while (i != -1 && prevlevel > 1) {
      prevlevel--;
      // skip levels that are already included
      if (prevlevel >= lowest) continue;
      toParse = toParse.substring(0, i + 1);
      StringBuilder parent = new StringBuilder();
      lowest = buildPrefix(parent, toParse, getNumberFormat(prevlevel, ""), "");
      parentNumber.insert(0, parent);
      i = toParse.lastIndexOf('.', toParse.length() - 2);
    }
    return new Prefix(prefix.toString(), canonical, level, parentNumber.toString());
  }

  /**
   * Build the numbering prefix from the canonical label and the scheme.
   *
   * @param prefix     for appending the prefix
   * @param canonical  the canonical label
   * @param scheme     the scheme to apply
   * @param blocklabel the parent block label name
   *
   * @return the lowest level included in prefix
   */
  private int buildPrefix(StringBuilder prefix, String canonical, String scheme, String blocklabel) {
    // no scheme, return canonical value then
    if (scheme == null) {
      // no zeros or we don't strip them, return as is
      if (this.skippedLevels == SkippedLevels.STRIP) {
        prefix.append(canonical.replaceFirst("^(0\\.)+", "").replaceAll("(\\.0\\.)", "."));
      } else {
        prefix.append(canonical);
      }
      return 1;
    }
    // find the values for each level
    Matcher canonicalMatcher = CANONICAL_PATTERN.matcher(canonical);
    Map<Integer, Integer> levels = new HashMap<>();
    int currentLevel = 1;
    while (canonicalMatcher.find()) {
      levels.put(currentLevel++,
          Integer.parseInt(canonicalMatcher.group(1)));
    }
    // build prefix
    Matcher schemeMatcher = SCHEME_PATTERN.matcher(scheme);
    int lowest = currentLevel;
    while (schemeMatcher.find()) {
      int level = Integer.parseInt(schemeMatcher.group(2));
      if (level < lowest) lowest = level;
      Integer value = levels.get(level);
      // make sure level is good
      if (value == null) continue;
      if (value.intValue() == 0 && this.skippedLevels == SkippedLevels.STRIP) continue;
      // ok append it then
      prefix.append(schemeMatcher.group(1));
      prefix.append(numbering(value, getNumberType(level, blocklabel)));
      prefix.append(schemeMatcher.group(3));
    }
    return lowest;
  }

  /**
   * Compute the numbering for the value provided.
   *
   * @param value the canonical number
   * @param type the type of numbering to get (from the scheme)
   *
   * @return the numbering computed
   */
  protected static String numbering(int value, NumberType type) {
    if (value == 0 || type == NumberType.DECIMAL)  return String.valueOf(value);
    String[] numbers = null;
    if (type == NumberType.LOWERALPHA) numbers = LOWERCASE_ALPHABET;
    else if (type == NumberType.UPPERALPHA) numbers = UPPERCASE_ALPHABET;
    else if (type == NumberType.LOWERROMAN) numbers = LOWERCASE_ROMAN_ALPHABET;
    else if (type == NumberType.UPPERROMAN) numbers = UPPERCASE_ROMAN_ALPHABET;
    else return String.valueOf(value);
    StringBuilder number = new StringBuilder();
    while (value > 0) {
      int digit = value % numbers.length;
      if (digit == 0) digit = numbers.length;
      number.insert(0, numbers[digit - 1]);
      value -= digit;
      if (value > 0 && type != NumberType.LOWERROMAN && type != NumberType.UPPERROMAN) {
        value = value / numbers.length;
      }
    }
    return number.toString();
  }

}
