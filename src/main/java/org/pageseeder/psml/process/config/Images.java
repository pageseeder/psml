/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;

import org.jspecify.annotations.Nullable;

/**
 * This element provides options to control how images are managed and will
 * rewrite the path accordingly.
 *
 * <p>Used to represent the inner ANT element:
 *
 * <pre>
 * {@code <images
 *             src="[uriid|permalink|location]"
 *             location="[folder path]"
 *             embedmetadata="[true|false]"
 *             includes="[patterns]"
 *             excludes="[patterns]" />}
 * </pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>src:      Format of src attribute on {@code <image>} elements (i.e. uriid, uriidfolders
 *                 permalink or location).
 *                 - If uriid use [uriid].[ext]
 *                 - If uriidfolders use [uriid billions]/[uriid millions]/[uriid thousands]/[uriid].[ext]
 *                   with leading zeros on folders (e.g. uriid 12345 would be 000/000/012/12345.png)
 *                 - if permalink use [ps.site.prefix]/uri/[uriid].[ext]
 *                 - default location.</li>
 *   <li>location: Move all {@code <image>} src files to this folder path. If
 *                 src=uriid or permalink rename them to [uriid].[ext] otherwise use their path
 *                 relative to src (required if src=uriid).</li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @author Christophe Lauret
 *
 * @version 1.7.0
 * @since 0.5.0
 */
public class Images extends IncludeExcludeConfig {

  public enum ImageSrc {

    /**
     * If uriid, use [uriid].[ext].
     */
    URIID,

    /**
     * If uriidfolders use [uriid billions]/[uriid millions]/[uriid thousands]/[uriid].[ext]
     * with leading zeros on folders (e.g. uriid 12345 would be 000/000/012/12345.png)
     */
    URIIDFOLDERS,

    /**
     * If permalink use [ps.site.prefix]/uri/[uriid].[ext].
     */
    PERMALINK,

    /**
     * Default is location (@src attribute is not changed).
     */
    LOCATION,

    /**
     * If filename use [filename].
     */
    FILENAME,

    /**
     * If filenameencode use [url encoded filename].
     */
    FILENAMEENCODE;

    /**
     * Returns the image src corresponding to the given name.
     *
     * @param name The name of image src (uriid, uriidfolders, permalink or location).
     *
     * @return The corresponding image src.
     *
     * @throws IllegalArgumentException If the specified name does not match any of the predefined values.
     */
    public static ImageSrc forName(String name) {
      for (ImageSrc n : values()) {
        if (n.name().equalsIgnoreCase(name)) return n;
      }
      throw new IllegalArgumentException("Invalid image src attribute value: " + name);
    }

  }

  /**
   * List of include patterns
   */
  private ImageSrc src = ImageSrc.LOCATION;

  /**
   * List of exclude patterns
   */
  private @Nullable String location = null;

  /**
   * Site prefix, used for permalinks
   */
  private @Nullable String sitePrefix = null;

  /**
   * Whether or not to embed image metadata
   */
  private boolean embedMetadata = false;

  /**
   * @return the location
   */
  public @Nullable String getLocation() {
    return this.location;
  }

  /**
   * @return the src
   */
  public ImageSrc getSrc() {
    return this.src;
  }

  /**
   * @return the site prefix
   */
  public @Nullable String getSitePrefix() {
    return this.sitePrefix;
  }

  /**
   * @return the embed metadata flag
   */
  public boolean isMetadataEmbedded() {
    return this.embedMetadata;
  }

  /**
   * @param siteprefix the site prefix to set
   */
  public void setSitePrefix(String siteprefix) {
    this.sitePrefix = siteprefix;
  }

  /**
   * @param loc the location to set
   */
  public void setLocation(String loc) {
    this.location = loc;
  }

  /**
   * @param embed the embed metadata flag to set
   */
  public void setEmbedMetadata(boolean embed) {
    this.embedMetadata = embed;
  }

  /**
   * @param s  the image src to set.
   */
  public void setImageSrc(ImageSrc s) {
    this.src = s;
  }

  /**
   * @param s  the src to set (uriid, uriidfolders, permalink or location)
   */
  public void setSrc(String s) {
    this.src = ImageSrc.forName(s);
  }

}
