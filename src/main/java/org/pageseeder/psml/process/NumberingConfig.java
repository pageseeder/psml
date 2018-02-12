/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pageseeder.psml.process.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configuration file for numbering schemes, options are:
 *    - strip-zeros:                      If enabled, '0' are stripped from a label for example if a heading had hierarchy '1.0.2.',
 *                                        it would be converted to '1.2.'.
 *                                        Allowed values are 'true' or 'false', default is 'true'.
 *    - prefix-para:                      If enabled, numbered paragraph are prefixed with the heading they belong to. For example,
 *                                        if there is para a.i. under the heading 2.1, it will be displayed as 2.1.a.i.
 *                                        Paragraph numbering restarts after each numbered heading.
 *                                        Allowed values are 'true' or 'false', default is 'false'.
 *    - para-schemes and heading-schemes: A list of patterns that the numbering should follow. The patterns are defined below.
 *                                        Each pattern corresponds to a level in the numbering hierarchy: for example
 *                                        for heading patterns, level 1 is for heading1 elements.
 *
 * Scheme Pattern:
 *    Each level in the pattern is defined by the picture [*(letter)(level)*] where:
 *      - (letter) is a symbol used to specify the numbering type, supported values are:
 *          - 1: numbering uses digits (1, 2, 3, ...)
 *          - a: numbering uses lowercase letters (a, b, c, ...)
 *          - A: numbering uses uppercase letters (A, B, C, ...)
 *          - i: numbering uses lowercase roman numerals (i, ii, iii, ...)
 *          - I: numbering uses uppercase roman numerals (I, II, III, ...)
 *      - (level) is a digit which defines the level of numbering. Currently levels 1 to 6 are supported by PageSeeder.
 *      - * is any other content.
 *    Here are valid examples:
 *      canonical value   scheme                     strip-zeros     resulting label
 *      1.1.3.1.          [11.][12.][13.][14.]       true            1.1.3.1.
 *      1.1.3.1.          [11.][13.][14.]            true            1.3.1.
 *      1.2.1.3.          [a1-][A2-][I3-][14.]       true            a-B-I-3.
 *      2.4               [11.][(i2).]               true            2.(iv).
 *      1.0.3             [11.][(i2).][a3]           true            1.c.
 *      1.0.3             [11.][(i2).][a3]           false           1.(0).c.
 * @author Jean-Baptiste Reure
 * @version 24/10/2012
 *
 */
public final class NumberingConfig {

  /** Use number to display a level */
  private static final String NUMERAL = "1";
  /** Use lowercase letter to display a level */
  private static final String LOWERCASE = "a";
  /** Use uppercase letter to display a level */
  private static final String UPPERCASE = "A";
  /** Use lowercase roman numeral to display a level */
  private static final String LOWERCASE_ROMAN = "i";
  /** Use uppercase roman numeral to display a level */
  private static final String UPPERCASE_ROMAN = "I";
  /** A pattern for all schemes */
  private static final Pattern SCHEME_PATTERN = Pattern.compile("\\[(.*?)("
      + NUMERAL + "|" + LOWERCASE + "|" + UPPERCASE + "|" + LOWERCASE_ROMAN
      + "|" + UPPERCASE_ROMAN + ")(1|2|3|4|5|6)(.*?)\\]");
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
      "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii",
      "xiii", "xiv", "xv", "xvi", "xvii", "xviii", "xix", "xx" };
  /** The uppercase roman numerals, for convenience */
  private static final String[] UPPERCASE_ROMAN_ALPHABET = new String[] { "I",
      "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII",
      "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX" };

  /**
   * If enabled, '0' are stripped from labels.
   */
  private boolean stripZeros = true;

  /**
   * If enabled, numbered paragraph are prefixed with the heading they belong
   * to.
   */
  private boolean prefixPara = false;

  /**
   * List of schemes for heading numbering
   */
  private final Map<Integer, String> headingSchemes = new HashMap<>();

  /**
   * List of schemes for heading numbering
   */
  private final Map<Integer, String> paraSchemes = new HashMap<>();

  /**
   * @param prefixpara the prefixPara to set
   */
  public void setPrefixPara(boolean prefixpara ) {
    this.prefixPara = prefixpara ;
  }

  /**
   * @param stripzeros the stripZeros to set
   */
  public void setStripZeros(boolean stripzeros) {
    this.stripZeros = stripzeros;
  }

  /**
   * @return the prefixPara
   */
  public boolean shouldPrefixPara() {
    return this.prefixPara;
  }

  /**
   * @return the stripZeros
   */
  public boolean shouldStripZeros() {
    return this.stripZeros;
  }

  /**
   * Add a new heading scheme.
   *
   * @param level   the level of the scheme
   * @param scheme  the scheme pattern
   */
  public void addHeadingScheme(int level, String scheme) {
    this.headingSchemes.put(level, scheme);
  }

  /**
   * Add a new para scheme.
   *
   * @param level   the level of the scheme
   * @param scheme  the scheme pattern
   */
  public void addParaScheme(int level, String scheme) {
    this.paraSchemes.put(level, scheme);
  }

  /**
   * Get the computed label for a heading.
   *
   * @param canonical the canonical label
   *
   * @return the computed label
   */
  public String getHeadingLabel(String canonical) {
    // make sure it always ends with dot
    String toParse = !canonical.matches("^.*\\.$") ? canonical + '.' : canonical;
    // find level
    int level = toParse.split("\\.").length;
    // compute label
    return getLabel(toParse, this.headingSchemes.get(level));
  }

  /**
   * Get the computed label for a para.
   *
   * @param paraCanonical     the canonical label of the paragraph
   * @param headingCanonical  the canonical label of the parent heading
   *
   * @return the computed label
   */
  public String getParaLabel(String paraCanonical, String headingCanonical) {
    // make sure it always ends with dot
    String toParse = !paraCanonical.endsWith(".") ? paraCanonical + '.' : paraCanonical;
    // find level
    int level = toParse.split("\\.").length;
    // compute label
    String paraLabel = getLabel(toParse, this.paraSchemes.get(level));
    if (!this.prefixPara) return paraLabel;
    // make sure it always ends with dot
    toParse = !headingCanonical.endsWith(".") ? headingCanonical + '.' : headingCanonical;
    // find level
    level = toParse.split("\\.").length;
    // compute label
    return getLabel(toParse, this.headingSchemes.get(level)) + paraLabel;
  }

  /**
   * Compute the label from the canonical label and the scheme.
   *
   * @param canonical the canonical label
   * @param scheme    the scheme to apply
   *
   * @return the new label
   */
  private String getLabel(String canonical, String scheme) {
    // no scheme, return canonical value then
    if (scheme == null) {
      // no zeros or we don't strip them, return as is
      if (this.stripZeros)
        return canonical.replaceFirst("^(0\\.)+", "").replaceAll("(\\.0\\.)", ".");
      return canonical;
    }
    // find the values for each level
    Matcher canonicalMatcher = CANONICAL_PATTERN.matcher(canonical);
    Map<String, Integer> levels = new HashMap<>();
    int currentLevel = 1;
    while (canonicalMatcher.find()) {
      levels.put(String.valueOf(currentLevel++),
          Integer.parseInt(canonicalMatcher.group(1)));
    }
    // build label now
    StringBuilder label = new StringBuilder();
    Matcher schemeMatcher = SCHEME_PATTERN.matcher(scheme);
    while (schemeMatcher.find()) {
      Integer value = levels.get(schemeMatcher.group(3));
      // make sure level is good
      if (value == null) continue;
      if (value.intValue() == 0 && this.stripZeros) continue;
      // ok append it then
      label.append(schemeMatcher.group(1));
      label.append(numbering(value, schemeMatcher.group(2)));
      label.append(schemeMatcher.group(4));
    }
    return label.toString();
  }

  /**
   * compute the numbering for the level provided.
   *
   * @param level the level
   * @param type the type of numbering to get (from the scheme)
   *
   * @return the numbering computed
   */
  private static String numbering(int level, String type) {
    if (level == 0 || NUMERAL.equals(type))  return String.valueOf(level);
    if (LOWERCASE.equals(type))              return LOWERCASE_ALPHABET[level-1];
    if (UPPERCASE.equals(type))              return UPPERCASE_ALPHABET[level-1];
    if (LOWERCASE_ROMAN.equals(type))        return LOWERCASE_ROMAN_ALPHABET[level-1];
    if (UPPERCASE_ROMAN.equals(type))        return UPPERCASE_ROMAN_ALPHABET[level-1];
    throw new IllegalArgumentException("Unknown numbering type: " + type);
  }

  /**
   * Parse the config file provided into a NumberingConfig object.
   *
   * @param configFile the file to parse
   *
   * @return the loaded config
   *
   * @throws PageseederException If invalid file or parsing the file failed
   */
  public static NumberingConfig loadNumberingConfigFile(File configFile) throws ProcessException {
    if (!configFile.exists() || !configFile.isFile())
      throw new ProcessException("Numbering config file not found: "+configFile.getAbsolutePath());
    try {
      return loadNumberingConfig(new FileInputStream(configFile));
    } catch (FileNotFoundException ex) {
      throw new ProcessException("Numbering config file not found: "+configFile.getAbsolutePath());
    }
  }

  /**
   * Parse the config file provided into a NumberingConfig object.
   *
   * @param in  the file input stream to parse
   *
   * @return the loaded config
   *
   * @throws PageseederException If invalid file or parsing the file failed
   */
  public static NumberingConfig loadNumberingConfig(InputStream in) throws ProcessException {
    NumberingConfigHandler handler = new NumberingConfigHandler();
    try {
      XMLUtils.parse(in, handler);
    } catch (ProcessException ex) {
      throw new ProcessException("Invalid numbering config file: "+ex.getMessage(), ex);
    }
    return handler.getConfig();
  }
/**
   * Parser for the numbering config file.
   *
   * @author Jean-Baptiste Reure
   * @version 24/10/2012
   *
   */
  private static class NumberingConfigHandler extends DefaultHandler {

    /** The config object to populate. */
    private NumberingConfig config = null;
    /** If zeros are stripped. */
    private boolean inStripZeros = false;
    /** If numered paras are prefixed by the heading's numbering. */
    private boolean inPrefixPara = false;
    /** Local state flag to know if the handler is in a scheme element. */
    private boolean inScheme = false;
    /** Local state flag to know if the handler is in a heading-schemes element. */
    private boolean inHeadingScheme = false;
    /** Local state flag to know if the handler is in a para-schemes element. */
    private boolean inParaScheme = false;
    /** Local variable storing the value of the level attribute. */
    private int currentLevel = -1;
    /** Local variable storing the text in an element. */
    private StringBuilder text = new StringBuilder();
    /**
     * @return the config
     */
    public NumberingConfig getConfig() {
      return this.config;
    }

    @Override
    public void startDocument() throws SAXException {
      this.config = new NumberingConfig();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("strip-zeros".equals(qName)) this.inStripZeros = true;
      else if ("prefix-para".equals(qName)) this.inPrefixPara = true;
      else if ("para-schemes".equals(qName)) this.inParaScheme = true;
      else if ("heading-schemes".equals(qName)) this.inHeadingScheme = true;
      else if ("scheme".equals(qName)) {
        this.inScheme = true;
        String lvl = attributes.getValue("level");
        if (lvl == null) throw new SAXException("Missing level attribute on a scheme element");
        try {
          this.currentLevel = Integer.parseInt(lvl);
        } catch (NumberFormatException e) {
          throw new SAXException("Invalid level attribute "+lvl+" on a scheme element");
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("strip-zeros".equals(qName)) {
        this.config.setStripZeros(Boolean.parseBoolean(this.text.toString()));
        this.text.setLength(0);
        this.inStripZeros = false;
      } else if ("prefix-para".equals(qName))  {
        this.config.setPrefixPara(Boolean.parseBoolean(this.text.toString()));
        this.text.setLength(0);
        this.inPrefixPara = false;
      } else if ("para-schemes".equals(qName)) this.inParaScheme = false;
      else if ("heading-schemes".equals(qName)) this.inHeadingScheme = false;
      else if ("scheme".equals(qName))  {
        if (this.inParaScheme)
          this.config.addParaScheme(this.currentLevel, this.text.toString());
        else if (this.inHeadingScheme)
          this.config.addHeadingScheme(this.currentLevel, this.text.toString());
        this.text.setLength(0);
        this.currentLevel = -1;
        this.inScheme = false;
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.inStripZeros || this.inPrefixPara || this.inScheme)
        this.text.append(ch, start, length);
    }
  }

}
