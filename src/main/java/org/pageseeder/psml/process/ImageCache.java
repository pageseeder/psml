/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.pageseeder.psml.process.config.Images.ImageSrc;
import org.pageseeder.psml.process.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Jean-Baptiste Reure
 * @version 23/10/2012
 *
 */
public final class ImageCache {

  /**
   * The folder where the metadata files are located.
   */
  private final File metaInfFolder;

  /**
   * The cache.
   */
  private final Map<String, String> cache = new HashMap<String, String>();

  /**
   * @param metainf The folder where the metadata files are located.
   */
  public ImageCache(File metainf) {
    this.metaInfFolder = metainf;
  }

  /**
   * Checks if an image is in the cache
   *
   * @param relativePath the image's relative path
   *
   * @return <code>true</code> if the image is in the cache
   */
  public boolean isCached(String relativePath) {
    return cache.containsKey(relativePath);
  }

  /**
   * Get the new path for the image defined by the relative path.
   *
   * @param relativePath the image's relative path
   *
   * @return the new path
   */
  public String getImageNewPath(String relativePath) {
    return cache.get(relativePath);
  }
  
  /**
   * Get the new path for the image defined by the relative path.
   *
   * @param relativePath the image's relative path
   * @param src          how to rewrite image src
   *
   * @return the new path
   *
   * @throws PageseederException if loading the metadata file failed
   */
  public String getImageNewPath(String relativePath, ImageSrc src) throws ProcessException {
    String newpath = cache.get(relativePath);
    if (newpath == null) {
      // ok find the metadata file
      File metadata = new File(this.metaInfFolder, relativePath+".psml");
      if (!metadata.exists() || !metadata.isFile())
        throw new ProcessException("Image metadata file not found "+relativePath);
      // load path from the metadata file
      MetadataFileHandler handler = new MetadataFileHandler();
      XMLUtils.parse(metadata, handler);
      if (src == ImageSrc.URIIDFOLDERS) {
        newpath = buildURIIDFoldersPath(handler.getUriID()) + handler.getUriID() + '.' + handler.getUriExtension();
      } else {
        newpath = handler.getUriID() + '.' + handler.getUriExtension();
      }
      cache.put(relativePath, newpath);
    }
    return newpath;
  }

  /**
   * Get the new path for the image defined by the relative path.
   * 
   * @param relativePath the image's relative path
   * @param src          how to rewrite image src
   * @param uriid        the image's URI ID
   * 
   * @return the new path
   */
  public String getImageNewPath(String relativePath, ImageSrc src, String uriid) {
    String newpath = cache.get(relativePath);
    if (newpath == null) {
      int lastDot = relativePath.lastIndexOf('.');
      String extension = lastDot != -1 ? relativePath.substring(lastDot) : "";
      if (src == ImageSrc.URIIDFOLDERS) {
        newpath = buildURIIDFoldersPath(uriid) + uriid + extension;
      } else {
        newpath = uriid + extension;
      }
      cache.put(relativePath, newpath);
    }
    return newpath;
  }

  
  /**
   * Build the path [uriid billions]/[uriid millions]/[uriid thousands]/
   * with leading zeros on folders and overflow on first folder e.g.
   * 
   *  - uriid 123 would be            000/000/000/
   *  - uriid 12345678 would be       000/012/345/
   *  - uriid 1234567890123 would be 1234/567/890/
   *  
   *  @param uriid the URI ID of the image
   *  
   *  @return the folder path
   */
  public static String buildURIIDFoldersPath(String uriid) {
    String overflow = "";
    int len = uriid.length();
    // add leading zeros if required
    if (len < 12) {
      uriid = "000000000000".substring(len) + uriid;
    } else {
      // add overflow digits
      overflow = uriid.substring(0, len - 12);
      // truncate to 12 digits
      uriid = uriid.substring(len - 12);
    }
    return overflow + uriid.substring(0, 3) + "/" + uriid.substring(3, 6) + "/" + uriid.substring(6, 9) + "/";
  }
  
  /**
   * Handler used to load URI details from an image metadata file.
   *
   * @author Jean-Baptiste Reure
   * @version 23/10/2012
   *
   */
  private class MetadataFileHandler extends DefaultHandler {
    /**
     * The URI ID.
     */
    private String uriID = null;

    /**
     * The URI extension, loaded from the URI path.
     */
    private String uriExtension = null;

    /**
     * Current state.
     */
    private boolean inDocInfo = false;

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
      if ("documentinfo".equals(qName)) this.inDocInfo = true;
      else if (this.inDocInfo && "uri".equals(qName)) {
        this.uriID = attributes.getValue("id");
        String path = attributes.getValue("path");
        if (path != null) {
          String[] parts = path.split("/");
          String last = parts[parts.length - 1];
          this.uriExtension = last.substring(last.lastIndexOf('.') + 1);
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("documentinfo".equals(qName)) this.inDocInfo = false;
    }

    /**
     * @return The URI extension, loaded from the URI path.
     */
    public String getUriExtension() {
      return this.uriExtension;
    }

    /**
     * @return The URI ID.
     */
    public String getUriID() {
      return this.uriID;
    }
  }
}
