/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.config;



/**
 * Provides finer control over the elements to strip.
 *
 * <p>Used to represent the inner ANT element:<p>
 * <pre>{@code<strip
 *             manifest="[true|false]"
 *             documentinfo="[all,docid,title,description,labels]"
 *             fragmentinfo="[all,labels]"
 *             xrefs="[all,docid,uriid,notfound,unresolved]" />}</pre>
 *
 * <p>Details are:</p>
 * <ul>
 *   <li>documentinfo: Comma separated list of items to strip (i.e. all, docid, title, description, labels)
 *                     all strips {@code<documentinfo>} elements - default none.</li>
 *   <li>fragmentinfo: Comma separated list of items to strip (i.e. all, labels)
 *                     all strips {@code<fragmentinfo>} elements - default none. </li>
 *   <li>manifest:     If 'true' deletes the META-INF/manifest.xml file - default is false. </li>
 *   <li>xrefs:        Comma separated list of items to strip (i.e. all, docid, uriid, notfound, unresolved)
 *                     all, notfound, unresolved strips {@code<xref>} element but leaves its content - default none. </li>
 * </ul>
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 *
 */
public class Strip {

  /**
   * Strip the documentinfo element
   */
  private boolean stripDocumentInfo = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripDocumentInfoDocID = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripDocumentInfoTitle = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripDocumentInfoDescription = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripDocumentInfoLabels = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripFragmentInfo = false;

  /**
   * Strip the documentinfo element
   */
  private boolean stripFragmentInfoLabels = false;

  /**
   * Whether or not to strip the manifest file
   */
  private boolean manifest = false;

  /**
   * Replace all the xrefs elements with their content.
   */
  private boolean stripAllXRefs = false;

  /**
   * Strip the docid attribute on xref elements.
   */
  private boolean stripXRefsDocID = false;

  /**
   * Strip the uriid attribute on xref elements.
   */
  private boolean stripXRefsURIID = false;

  /**
   * Replace the unresolved xrefs elements with their content.
   */
  private boolean stripUnresolvedXRefs = false;

  /**
   * Replace the notfound xrefs elements with their content.
   */
  private boolean stripNotFoundXRefs = false;

  /**
   * @param stripDocumentInfo the stripDocumentInfo to set
   */
  public void setStripDocumentInfo(boolean stripDocumentInfo) {
    this.stripDocumentInfo = stripDocumentInfo;
  }

  /**
   * @param stripDocumentInfoDocID the stripDocumentInfoDocID to set
   */
  public void setStripDocumentInfoDocID(boolean stripDocumentInfoDocID) {
    this.stripDocumentInfoDocID = stripDocumentInfoDocID;
  }

  /**
   * @param stripDocumentInfoTitle the stripDocumentInfoTitle to set
   */
  public void setStripDocumentInfoTitle(boolean stripDocumentInfoTitle) {
    this.stripDocumentInfoTitle = stripDocumentInfoTitle;
  }

  /**
   * @param stripDocumentInfoDescription the stripDocumentInfoDescription to set
   */
  public void setStripDocumentInfoDescription(boolean stripDocumentInfoDescription) {
    this.stripDocumentInfoDescription = stripDocumentInfoDescription;
  }

  /**
   * @param stripDocumentInfoLabels the stripDocumentInfoLabels to set
   */
  public void setStripDocumentInfoLabels(boolean stripDocumentInfoLabels) {
    this.stripDocumentInfoLabels = stripDocumentInfoLabels;
  }

  /**
   * @param stripFragmentInfo the stripFragmentInfo to set
   */
  public void setStripFragmentInfo(boolean stripFragmentInfo) {
    this.stripFragmentInfo = stripFragmentInfo;
  }

  /**
   * @param stripFragmentInfoLabels the stripFragmentInfoLabels to set
   */
  public void setStripFragmentInfoLabels(boolean stripFragmentInfoLabels) {
    this.stripFragmentInfoLabels = stripFragmentInfoLabels;
  }

  /**
   * @param stripAllXRefs the stripAllXRefs to set
   */
  public void setStripAllXRefs(boolean stripAllXRefs) {
    this.stripAllXRefs = stripAllXRefs;
  }

  /**
   * @param stripXRefsDocID the stripXRefsDocID to set
   */
  public void setStripXRefsDocID(boolean stripXRefsDocID) {
    this.stripXRefsDocID = stripXRefsDocID;
  }

  /**
   * @param stripXRefsURIID the stripXRefsURIID to set
   */
  public void setStripXRefsURIID(boolean stripXRefsURIID) {
    this.stripXRefsURIID = stripXRefsURIID;
  }

  /**
   * @param stripUnresolvedXRefs the stripUnresolvedXRefs to set
   */
  public void setStripUnresolvedXRefs(boolean stripUnresolvedXRefs) {
    this.stripUnresolvedXRefs = stripUnresolvedXRefs;
  }

  /**
   * @param stripNotFoundXRefs the stripNotFoundXRefs to set
   */
  public void setStripNotFoundXRefs(boolean stripNotFoundXRefs) {
    this.stripNotFoundXRefs = stripNotFoundXRefs;
  }

  /**
   * @param deleteManifest the manifest to set
   */
  public void setManifest(boolean deleteManifest) {
    this.manifest = deleteManifest;
  }

  /**
   * @return the manifest flag
   */
  public boolean stripManifest() {
    return this.manifest;
  }

  /**
   * @return the stripAllXRefs
   */
  public boolean stripAllXRefs() {
    return this.stripAllXRefs;
  }

  /**
   * @return the stripDocumentInfo
   */
  public boolean stripDocumentInfo() {
    return this.stripDocumentInfo;
  }

  /**
   * @return the stripDocumentInfoDescription
   */
  public boolean stripDocumentInfoDescription() {
    return this.stripDocumentInfoDescription;
  }

  /**
   * @return the stripDocumentInfoDocID
   */
  public boolean stripDocumentInfoDocID() {
    return this.stripDocumentInfoDocID;
  }

  /**
   * @return the stripDocumentInfoLabels
   */
  public boolean stripDocumentInfoLabels() {
    return this.stripDocumentInfoLabels;
  }

  /**
   * @return the stripDocumentInfoTitle
   */
  public boolean stripDocumentInfoTitle() {
    return this.stripDocumentInfoTitle;
  }

  /**
   * @return the stripFragmentInfo
   */
  public boolean stripFragmentInfo() {
    return this.stripFragmentInfo;
  }

  /**
   * @return the stripFragmentInfoLabels
   */
  public boolean stripFragmentInfoLabels() {
    return this.stripFragmentInfoLabels;
  }

  /**
   * @return the stripNotFoundXRefs
   */
  public boolean stripNotFoundXRefs() {
    return this.stripNotFoundXRefs;
  }

  /**
   * @return the stripUnresolvedXRefs
   */
  public boolean stripUnresolvedXRefs() {
    return this.stripUnresolvedXRefs;
  }

  /**
   * @return the stripXRefsDocID
   */
  public boolean stripXRefsDocID() {
    return this.stripXRefsDocID;
  }

  /**
   * @return the stripXRefsURIID
   */
  public boolean stripXRefsURIID() {
    return this.stripXRefsURIID;
  }

  /**
   * @return this strip cloned
   */
  public Strip cloneStrip() {
    Strip s = new Strip();
    s.manifest = this.manifest;
    s.stripAllXRefs = this.stripAllXRefs;
    s.stripDocumentInfo = this.stripDocumentInfo;
    s.stripDocumentInfoDescription = this.stripDocumentInfoDescription;
    s.stripDocumentInfoDocID = this.stripDocumentInfoDocID;
    s.stripDocumentInfoLabels = this.stripDocumentInfoLabels;
    s.stripDocumentInfoTitle = this.stripDocumentInfoTitle;
    s.stripFragmentInfo = this.stripFragmentInfo;
    s.stripFragmentInfoLabels = this.stripFragmentInfoLabels;
    s.stripNotFoundXRefs = this.stripNotFoundXRefs;
    s.stripUnresolvedXRefs = this.stripUnresolvedXRefs;
    s.stripXRefsDocID = this.stripXRefsDocID;
    s.stripXRefsURIID = this.stripXRefsURIID;
    return s;
  }
}
