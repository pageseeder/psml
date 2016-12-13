/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pageseeder.psml.process.util.Files;
import org.pageseeder.psml.process.util.XMLUtils;
import org.xml.sax.Attributes;

/**
 * @author Jean-Baptiste Reure
 * @version 31/10/2012
 *
 */
public final class XRefTranscluder {

  /**
   * The max number of parents possible before throwing an error.
   */
  public static final int MAX_DEPTH = 50;

  /**
   * The number of parents possible before raising a warning.
   */
  public static final int WARNING_DEPTH = 10;

  /**
   * The parent handler.
   */
  private final PSMLProcessHandler parentHandler;

  /**
   * The list of parent files (when transcluding, to avoid looping).
   */
  private final Map<File, List<String>> parentFiles = new HashMap<File, List<String>>();

  /**
   * List of XRef types to transclude.
   */
  protected final List<String> xrefsTranscludeTypes = new ArrayList<String>();

  /**
   * If the xrefs in an xref-fragment are ignored.
   */
  protected boolean excludeXRefFragment = false;

  /**
   * If only the xrefs in an xref-fragment are included.
   */
  protected boolean onlyXRefFrament = false;

  /**
   * If images are included.
   */
  private boolean transcludeImages = false;

  /**
   * Whether or not any transclusion will happen.
   */
  private boolean isTranscluding = false;

  /**
   * @param dad the parent parser, used for cloning.
   */
  public XRefTranscluder(PSMLProcessHandler dad) {
    this.parentHandler = dad;
  }

  /**
   * @param xrefTypes the types of the xrefs to transclude.
   */
  public void addXRefsTypes(List<String> xrefTypes) {
    if (xrefTypes != null) {
      this.xrefsTranscludeTypes.addAll(xrefTypes);
      this.isTranscluding = true;
    }
  }

  /**
   * @param exclude If the xrefs in an xref-fragment are ignored.
   * @param only    If only the xrefs in an xref-fragment are included.
   */
  public void setXRefFragmentHandling(boolean exclude, boolean only) {
    this.excludeXRefFragment = exclude;
    this.onlyXRefFrament = only;
  }

  /**
   * @param transclude if images are transcluded
   */
  public void setTranscludeImages(boolean transclude) {
    this.transcludeImages = transclude;
  }

  /**
   * @return <code>true</code> if there could be any transclusions
   */
  public boolean isTranscluding() {
    return isTranscluding;
  }

  /**
   * @param parent   a parent file
   * @param fragment the source fragment
   */
  public void addParentFile(File parent, String fragment) {
    if (parent != null && fragment != null) {
      List<String> fragments = this.parentFiles.get(parent);
      if (fragments == null) fragments = new ArrayList<String>();
      // should not happen but jsut in case
      if (fragments.contains(fragment))
        throw new IllegalStateException("Should not be here: adding an existing fragment as a parent file");
      fragments.add(fragment);
      this.parentFiles.put(parent, fragments);
    }
  }

  /**
   * Check if the target represented by the href provided is found.
   *
   * @param href The path of the document to load
   *
   * @return <code>true</code> if the target document is not found
   */
  public boolean isNotFoundXRef(String href) {
    File target = findXRefTarget(href);
    return target == null || !target.exists() || !target.isFile();
  }

  /**
   * Resolve a transclusion by loading the content of the target specified
   * by the href attribute. If the fragment is not null and not "default",
   * only the content of that fragment is loaded.
   *
   * @param atts           The attributes on the XRef
   * @param inXrefFragment If this XRef is in an XRefFragment
   *
   * @return <code>true</code> if the XRef is transcluded, false otherwise
   *
   * @throws ProcessException if the target is invalid or could not be read
   */
  public boolean transcludeXRef(Attributes atts, boolean inXrefFragment, boolean image) throws ProcessException {
    // should transclude?
    if (!image &&
       ((inXrefFragment && this.excludeXRefFragment) ||
       (!inXrefFragment && this.onlyXRefFrament))) return false;
    String href = atts.getValue(image ? "src" : "href");
    String type = image ? "image" : atts.getValue("type");
    File target = findXRefTarget(href);
    boolean transclude = image ? this.transcludeImages : this.xrefsTranscludeTypes.contains(type);
    if (transclude) {
      // make sure it's valid
      if (target == null || !target.exists() ||!target.isFile())
        throw new XRefNotFoundException();
      // ensure PSML
      if (!target.getName().endsWith(".psml"))
        return false;
      // check for depth
      if (this.parentFiles.size() > MAX_DEPTH)
        throw new TooDeepException();
      if (this.parentFiles.size() > WARNING_DEPTH) {
        String src, tgt;
        try {
          src = this.parentHandler.getSourceFile().getCanonicalPath().substring(this.parentHandler.getPSMLRoot().getCanonicalPath().length()+1).replace(File.separatorChar, '/');
          tgt = target.getCanonicalPath().substring(this.parentHandler.getPSMLRoot().getCanonicalPath().length()+1).replace(File.separatorChar, '/');
        } catch (IOException e) {
          src = this.parentHandler.getSourceFile().getName();
          tgt = href;
        }
        this.parentHandler.getLogger().warn("Transclusion/embed depth is suspiciously high (> "+WARNING_DEPTH+") for XRef from "+src+" to "+tgt+".");
      }
      // loop?
      String fragment = image ? "default" : atts.getValue("frag");
      List<String> fragments = this.parentFiles.get(target);
      if (fragments != null && fragments.contains(fragment)) {
        throw new InfiniteLoopException();
      }
      this.parentHandler.getLogger().debug("Transcluding XRef to "+href);
      // clone handler
      String levelAtt = atts.getValue("level");
      int level = levelAtt == null || levelAtt.isEmpty() ? 0 : Integer.parseInt(levelAtt);
      PSMLProcessHandler handler = parentHandler.cloneForTransclusion(target, atts.getValue("uriid"), fragment, level, image);
      handler.getTranscluder().parentFiles.putAll(this.parentFiles);
      // parse now
      XMLUtils.parse(target, handler);
      return true;
    }
    return false;
  }

  /**
   * @param href the href attribute of the XRef.
   *
   * @return the target file object
   */
  public File findXRefTarget(String href) {
    if (href == null) return null;
    String path = href.replaceFirst("\\?(.*?)?$", ""); // remove fragment from href
    try {
      path = URLDecoder.decode(path, "utf-8");
    } catch (UnsupportedEncodingException ex) {
      this.parentHandler.getLogger().error(ex.getMessage(), ex);
    }
    String dadPath = this.parentHandler.getParentFolderRelativePath();
    // find target file
    File target;
    if (path.endsWith(".xml") || path.endsWith(".psml")) {
      // try psml
      target = new File(this.parentHandler.getPSMLRoot(), dadPath + '/' + path.replaceFirst("\\.xml$", ".psml"));
      // try binary xml?
      if (!target.exists())
        target = new File(this.parentHandler.getBinaryRepository(), dadPath + '/' + path);
    } else {
      target = new File(this.parentHandler.getBinaryRepository(), "META-INF/" + dadPath + '/' + path + ".psml");
    }
    return target;
  }

  /**
   * @param href the href attribute of the XRef.
   *
   * @return the relative path of the target file if it exists, <code>null</code> otherwise
   */
  public String findXRefRelativePath(String href) {
    if (href == null) return null;
    String path = href.replaceFirst("\\?(.*?)?$", ""); // remove fragment from href and decode
    try {
      path = URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
    String dadPath = this.parentHandler.getParentFolderRelativePath();
    // find target file
    if (path.endsWith(".xml") || path.endsWith(".psml")) {
      // try psml
      File target = new File(this.parentHandler.getPSMLRoot(), dadPath + '/' + path.replaceFirst("\\.xml$", ".psml"));
      if (target.exists()) return Files.computeRelativePath(target, this.parentHandler.getPSMLRoot());
      // try binary xml?
      target = new File(this.parentHandler.getBinaryRepository(), dadPath + '/' + path);
      if (target.exists()) return Files.computeRelativePath(target, this.parentHandler.getBinaryRepository());
    } else {
      File target = new File(this.parentHandler.getBinaryRepository(), dadPath + '/' + path);
      if (target.exists()) return Files.computeRelativePath(target, this.parentHandler.getBinaryRepository());
    }
    return null;
  }

  /**
   *
   * @author Jean-Baptiste Reure
   * @version 31/10/2012
   *
   */
  public static class XRefNotFoundException extends ProcessException {
    /** used for serialization */
    private static final long serialVersionUID = 1L;
  }

  /**
   *
   * @author Jean-Baptiste Reure
   * @version 31/10/2012
   *
   */
  public static class InfiniteLoopException extends ProcessException {
    /** used for serialization */
    private static final long serialVersionUID = 1L;
  }

  /**
   *
   * @author Jean-Baptiste Reure
   * @version 31/10/2012
   *
   */
  public static class TooDeepException extends ProcessException {
    /** used for serialization */
    private static final long serialVersionUID = 1L;
  }
}
