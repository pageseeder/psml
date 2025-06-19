/*
 * Copyright (c) 1999-2012 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.process.util;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Collects XML parsing errors and warnings.
 *
 * @author Jean-Baptiste Reure
 * @version 1.7.9
 */
public final class XMLParserErrorHandler implements ErrorHandler {

  /**
   * Errors collected by error and fatal events.
   */
  private final List<String> errors = new ArrayList<>();

  /**
   * Warnings collected.
   */
  private final List<String> warnings = new ArrayList<>();

  /**
   * Handles a recoverable parsing error by adding a formatted error message
   * to the list of collected errors.
   *
   * @param exception The SAXParseException containing details of the parsing error.
   * @throws SAXException If a SAX error occurs while processing the exception.
   */
  public void error(SAXParseException exception) throws SAXException {
    this.errors.add("ERROR: "+toMessage(exception));
  }

  /**
   * Handles a fatal parsing error by adding a formatted fatal error message
   * to the list of collected errors.
   *
   * @param exception The SAXParseException containing details of the fatal parsing error.
   */
  public void fatalError(SAXParseException exception) {
    this.errors.add("FATAL: "+toMessage(exception));
  }

  /**
   * Handles a recoverable warning encountered during XML parsing by adding a formatted
   * warning message to the list of collected warnings.
   *
   * @param exception The SAXParseException containing details of the warning encountered during parsing.
   * @throws SAXException If a SAX error occurs while processing the exception.
   */
  public void warning(SAXParseException exception) throws SAXException {
    this.warnings.add("WARNING: "+toMessage(exception));
  }

  /**
   * Returns the warnings collected by this error handler.
   *
   * @return Returns the warnings collected by this error handler.
   */
  public List<String> getWarnings() {
    return this.warnings;
  }

  /**
   * @return <code>true</code> if there were errors collected.
   */
  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  /**
   * @return <code>true</code> if there were warnings collected.
   */
  public boolean hasWarnings() {
    return !this.warnings.isEmpty();
  }

  /**
   * Returns the errors collected by this error handler.
   *
   * @return Returns the errors collected by this error handler.
   */
  public List<String> getErrors() {
    return this.errors;
  }

  private String toMessage(SAXParseException exception) {
    return exception.getMessage()+" ["+exception.getLineNumber()+":"+exception.getColumnNumber()+"]";
  }
}
