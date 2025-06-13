/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.psml.process.math.TexConverter;
import org.pageseeder.psml.process.util.Files;
import org.pageseeder.psml.process.util.XMLUtils;
import org.pageseeder.psml.toc.DocumentTree;
import org.pageseeder.psml.toc.DocumentTreeHandler;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the transclusion of cross-references (xrefs) in documents.
 *
 * @author Jean-Baptiste Reure
 *
 * @version 1.6.0
 * @since 1.0
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
  private final Map<File, List<String>> parentFiles = new HashMap<>();

  /**
   * List of XRef types to transclude.
   */
  final List<String> xrefsTranscludeTypes = new ArrayList<>();

  /**
   * If the xrefs in an xref-fragment are ignored.
   */
  boolean excludeXRefFragment = false;

  /**
   * If only the xrefs in an xref-fragment are included.
   */
  boolean onlyXRefFrament = false;

  /**
   * If images are included.
   */
  private boolean transcludeImages = false;

  /**
   * If URL metadata is included in link elements.
   */
  private boolean transcludeLinks = false;

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
  public void addXRefsTypes(@Nullable List<String> xrefTypes) {
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
   * @param transclude if links are transcluded
   */
  public void setTranscludeLinks(boolean transclude) {
    this.transcludeLinks = transclude;
  }

  /**
   * @return <code>true</code> if there could be any transclusions
   */
  public boolean isTranscluding() {
    return this.isTranscluding;
  }

  /**
   * @param parent   a parent file
   * @param fragment the source fragment
   */
  public void addParentFile(@Nullable File parent, @Nullable String fragment) {
    if (parent != null && fragment != null) {
      List<String> fragments = this.parentFiles.get(parent);
      if (fragments == null) fragments = new ArrayList<>();
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
    File target = findXRefTarget(href, null, false);
    return target == null || !target.exists() || !target.isFile();
  }

  /**
   * Resolve a transclusion by loading the content of the target specified
   * by the href attribute. If the fragment is not null and not "default",
   * only the content of that fragment is loaded.
   *
   * @param atts             The attributes on the XRef
   * @param inXrefFragment   If this XRef is in an XRefFragment
   * @param image            If this is an image
   * @param link             If this is a link
   * @param inEmbedHierarchy If hierarchy has all embed XRefs
   * @param convertTex       If xrefs to .tex should be converted
   *
   * @return <code>true</code> if the XRef is transcluded, false otherwise
   *
   * @throws ProcessException if the target is invalid or could not be read
   */
  public boolean transcludeXRef(Attributes atts, boolean inXrefFragment, boolean image,
                                boolean link, boolean inEmbedHierarchy, boolean convertTex) throws ProcessException {
    // should transclude?
    if (!image && !link &&
       ((inXrefFragment && this.excludeXRefFragment) ||
       (!inXrefFragment && this.onlyXRefFrament))) return false;
    String href = atts.getValue(image ? "src" : "href");
    String type = image ? "image" : link ? "link" : atts.getValue("type");
    String uriid = atts.getValue("uriid");
    boolean transclude = image ? this.transcludeImages : link ? this.transcludeLinks : this.xrefsTranscludeTypes.contains(type);
    if (transclude && !"true".equals(atts.getValue("external")) &&
        !"true".equals(atts.getValue("unresolved")) && uriid != null) {
      File target = findXRefTarget(href, uriid, link);
      // make sure it's valid
      if (target == null || !target.exists() ||!target.isFile()) {
        throw new XRefNotFoundException();
      }
      boolean mathTarget = "math".equalsIgnoreCase(type) && "default".equals(atts.getValue("frag"));
      // ensure PSML or mathml for math xrefs
      if ((!mathTarget && !target.getName().endsWith(".psml")) ||
          (mathTarget  && !target.getName().endsWith(".mml") && !target.getName().endsWith(".mathml") && !(convertTex && target.getName().endsWith(".tex"))))
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
        this.parentHandler.getLogger().warn("Transclusion/embed depth is suspiciously high (> "+WARNING_DEPTH+") for XRef from {} to {}.", src, tgt);
      }
      // check for math xref
      if (mathTarget) {
        try {
          // read mathml target file and wrap it in media-fragment element
          this.parentHandler.write("<media-fragment id=\"media\" mediatype=\"application/mathml+xml\">");
          if (convertTex && target.getName().endsWith(".tex")) {
            try {
              this.parentHandler.write(TexConverter.convert(String.join("", java.nio.file.Files.readAllLines(target.toPath()))));
            } catch (IOException ex) {
              throw new ProcessException("Failed to read contents of file "+target.getName()+": "+ex.getMessage(), ex);
            } catch (IllegalArgumentException ex) {
              // Add filename for math conversion debugging
              throw new IllegalArgumentException("File " + target.getName() + ": " + ex.getMessage());
            }
          } else {
            this.parentHandler.writeFileContents(target);
          }
          this.parentHandler.write("</media-fragment>");
        } catch (SAXException ex) {
          throw new ProcessException("Failed to write contents of file "+target.getName()+": "+ex.getMessage(), ex);
        }
        return true;
      }
      // loop?
      String fragment = image || link ? "default" : atts.getValue("frag");
      List<String> fragments = this.parentFiles.get(target);
      if (fragments != null && fragments.contains(fragment)) {
        throw new InfiniteLoopException();
      }
      this.parentHandler.getLogger().debug("Transcluding XRef to {}", href);
      // clone handler
      NumberedTOCGenerator numberingAndTOC = this.parentHandler.getNumberedTOCGenerator();
      String levelAtt = atts.getValue("level");
      // use level if it exists and a transclude or no publication specified (for backward compatibility)
      int level = levelAtt != null && !levelAtt.isEmpty() &&
          ("transclude".equals(type) || numberingAndTOC == null) ?
          Integer.parseInt(levelAtt) : 0;
      PSMLProcessHandler handler = this.parentHandler.cloneForTransclusion(
          target, uriid, fragment, level, image,
          inEmbedHierarchy && "embed".equals(type), "transclude".equals(type),
          "alternate".equals(type) || this.parentHandler.inAlternateXRef());
      handler.getTranscluder().parentFiles.putAll(this.parentFiles);
      // parse now
      XMLUtils.parse(target, handler);
      // if publication then parse TOC
      if (numberingAndTOC != null) {
        // process transclusions
        XMLStringWriter out = new XMLStringWriter(NamespaceAware.No);
        TransclusionHandler thandler = new TransclusionHandler(out, "default", true, handler);
        XMLUtils.parse(target, thandler);
        // parse document tree
        DocumentTreeHandler tochandler = new DocumentTreeHandler();
        XMLUtils.parse(new InputSource(new StringReader(out.toString())), tochandler, null, null);
        DocumentTree tree = tochandler.get();
        if (tree != null) {
          tree = tree.normalize(this.parentHandler.getPublicationConfig().getTocTitleCollapse());
          numberingAndTOC.addTree(tree);
        }
      }
      return true;
    }
    return false;
  }

  /**
   * @param href the href attribute of the XRef.
   *
   * @return the target file object
   */
  public @Nullable File findXRefTarget(@Nullable String href, @Nullable String uriid, boolean link) {
    if (href == null) return null;
    String path = href.replaceFirst("\\?(.*?)?$", ""); // remove fragment from href
    path = URLDecoder.decode(path, StandardCharsets.UTF_8);
    String dadPath = this.parentHandler.cleanUpParentFolder();
    // Find the target file
    File target;
    if (link) {
      try {
        URL u = new URL(href);
        String scheme = u.getProtocol();
        int port = u.getPort();
        if (port == -1) port = "https".equals(scheme) ? 443 : "http".equals(scheme) ? 80 : -1;
        target = new File(this.parentHandler.getPSMLRoot(),
            "META-INF/_urls/" + scheme + '/' + u.getHost() + '/' + port + '/' + uriid + ".psml");
      } catch (MalformedURLException ex) {
        this.parentHandler.getLogger().error("Invalid link URL: " + ex.getMessage(), ex);
        target = null;
      }
    } else {
      if (path.endsWith(".psml")) {
        target = new File(this.parentHandler.getPSMLRoot(), dadPath + '/' + path);
      } else if (path.endsWith(".mml") || path.endsWith(".mathml") || path.endsWith(".tex")) {
        target = new File(this.parentHandler.getBinaryRepository(), dadPath + '/' + path);
      } else {
        target = new File(this.parentHandler.getPSMLRoot(), "META-INF/" + dadPath + '/' + path + ".psml");
      }
      try {
        // must use a canonical file as some parent folders may not exist under META-INF causing ".." to not resolve on Linux
        target = target.getCanonicalFile();
      } catch (IOException ex) {
        this.parentHandler.getLogger().error(ex.getMessage(), ex);
      }
    }
    return target;
  }

  /**
   * @param href the href attribute of the XRef.
   *
   * @return the relative path of the target file if it exists, <code>null</code> otherwise
   */
  public @Nullable String findXRefRelativePath(@Nullable String href) {
    if (href == null) return null;
    String path = href.replaceFirst("\\?(.*?)?$", ""); // remove fragment from href and decode
    path = URLDecoder.decode(path, StandardCharsets.UTF_8);
    String dadPath = this.parentHandler.getParentFolderRelativePath();
    // find target file
    if (path.endsWith(".psml")) {
      // try psml
      File target = new File(this.parentHandler.getPSMLRoot(), dadPath + '/' + path);
      if (!path.startsWith("/")) return Files.computeRelativePath(target, this.parentHandler.getPSMLRoot());
    } else {
      File target = new File(this.parentHandler.getBinaryRepository(), dadPath + '/' + path);
      if (!path.startsWith("/")) return Files.computeRelativePath(target, this.parentHandler.getBinaryRepository());
    }
    return null;
  }

  /**
   * Thrown to indicate that a cross-reference (XRef) could not be found during a processing operation.
   */
  public static class XRefNotFoundException extends ProcessException {

    /** used for serialization */
    private static final long serialVersionUID = 1L;

    XRefNotFoundException() {
      super("XRef not found.");
    }
  }

  /**
   * Indicates that an infinite loop has been detected during a process execution.
   */
  public static class InfiniteLoopException extends ProcessException {

    /** used for serialization */
    private static final long serialVersionUID = 1L;

    InfiniteLoopException() {
      super("Infinite loop detected.");
    }
  }

  /**
   * Exception thrown to indicate that the processing or traversal depth has exceeded
   * the allowed limit.
   */
  public static class TooDeepException extends ProcessException {

    /** used for serialization */
    private static final long serialVersionUID = 1L;

    TooDeepException() {
      super("Maximum processing depth ("+MAX_DEPTH+") exceeded.");
    }
  }
}
