/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.toc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.jdt.annotation.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Publication configuration file format:
 *
 * <pre>{@code
 * <publication-config>
 *   <numbering strip-zeros="true"  [label="xyz"]>
 *     <schemes>
 *       <scheme level="1" type="[decimal|upperalpha|loweralpha|upperroman|lowerroman]" format="[1]" />
 *       <scheme level="2" type="decimal" format="[1.][2]" />
 *       <scheme level="3" type="loweralpha" format="[(3)]" />
 *       <scheme level="4" type="lowerroman" format="[(4)]" />
 *       <scheme level="5" type="upperalpha" format="[(5)]" />
 *       <scheme level="6" type="upperroman" format="[(6)]" />
 *       <scheme level="7" type="loweralpha" format="[7]" />
 *       <scheme level="8" type="lowerroman" format="[8]" />
 *       <scheme level="9" type="upperalpha" format="[9]" />
 *     </schemes>
 *   </numbering>
 * </publication-config>
 * }</pre>
 *
 * Each level format is defined by the picture [*(level)*] where:
 * - (level) is a digit which defines the level of numbering. Currently levels 1 to 9 are supported by PageSeeder.
 * - * is any other content.
 *
 * @author Philip Rutherford
 */
public final class PublicationConfig {

  /**
   * List of numbering configs
   */
  private final List<PublicationNumbering> numberingConfigs = new ArrayList<>();

  /**
   * Return the first numbering config with one of the specified labels.
   * If none found return the first config with no label, otherwise <code>null<code>.
   *
   * @param labels a comma separated list of document labels
   *
   * @return the config (may be <code>null</code>)
   */
  public @Nullable PublicationNumbering getPublicationNumbering(String labels) {
    String[] ls = labels.split(",");
    PublicationNumbering def = null;
    for (PublicationNumbering config : this.numberingConfigs) {
      for (String label : ls) {
        if (label.equals(config.getLabel())) return config;
      }
      if ("".equals(config.getLabel()) && def == null) {
        def = config;
      }
    }
    return def;
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
  public static PublicationConfig loadPublicationConfigFile(File configFile) throws IOException {
    if (!configFile.exists() || !configFile.isFile())
      throw new IOException("Publication config file not found: "+configFile.getAbsolutePath());
    try {
      return loadPublicationConfig(new FileInputStream(configFile));
    } catch (FileNotFoundException ex) {
      throw new IOException("Publication config file not found: "+configFile.getAbsolutePath());
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
  public static PublicationConfig loadPublicationConfig(InputStream in) throws IOException {
    PublicationConfigHandler handler = new PublicationConfigHandler();
    parse(in, handler);
    return handler.getConfig();
  }

  /**
   * Parse the XML input using the handler provided.
   *
   * @param in       the XML input (will be closed!)
   * @param handler  the XML handler
   *
   * @throws IOException if the parsing failed
   */
  public static void parse(InputStream in, ContentHandler handler) throws IOException {
    try {
      // use the SAX parser factory to set features
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      // set not namespace aware
      factory.setNamespaceAware(false);
      // get reader
      XMLReader reader = factory.newSAXParser().getXMLReader();
      // set handlers
      reader.setContentHandler(handler);
      // parse
      try {
        reader.parse(new InputSource(in));
      } finally {
        in.close();
      }
    } catch (SAXException ex) {
      throw new IOException(ex.getMessage(), ex);
    } catch (ParserConfigurationException ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

  /**
   * Parser for the publication config file.
   *
   * @author Philip Rutherford
   *
   */
  private static class PublicationConfigHandler extends DefaultHandler {

    /** The config object to populate. */
    private PublicationConfig config = null;

    /** The numbering object to populate. */
    private PublicationNumbering numbering = null;

    /**
     * @return the config
     */
    public PublicationConfig getConfig() {
      return this.config;
    }

    @Override
    public void startDocument() throws SAXException {
      this.config = new PublicationConfig();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("numbering".equals(qName)) {
        this.numbering = new PublicationNumbering();
        this.numbering.setStripZeros("true".equals(attributes.getValue("strip-zeros")));
        String label = attributes.getValue("label");
        if (label != null) {
          this.numbering.setLabel(label);
        }
      } else if ("scheme".equals(qName) && this.numbering != null) {
        try {
          int level = Integer.parseInt(attributes.getValue("level"));
          if (level < 1 || level > 9) new SAXException("Invalid level: " + level);
          this.numbering.addNumberType(level, attributes.getValue("type"));
          this.numbering.addNumberFormat(level, attributes.getValue("format"));
        } catch (NumberFormatException ex) {
          throw new SAXException("Invalid level: " + attributes.getValue("level"));
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("numbering".equals(qName)) {
        this.config.numberingConfigs.add(this.numbering);
      }
    }

  }

}
