/*
 * Copyright (c) 2018 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.pageseeder.diffx.DiffXException;
import org.pageseeder.diffx.algorithm.GuanoAlgorithm;
import org.pageseeder.diffx.config.DiffXConfig;
import org.pageseeder.diffx.config.TextGranularity;
import org.pageseeder.diffx.config.WhiteSpaceProcessing;
import org.pageseeder.diffx.format.SafeXMLFormatter;
import org.pageseeder.diffx.load.SAXRecorder;
import org.pageseeder.diffx.sequence.EventSequence;
import org.pageseeder.diffx.sequence.SequenceSlicer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * PageSeeder wrapper for DiffX.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 *
 * @since 0.3.7
 */
public final class PSMLDiffer {

  /**
   * Logger for PageSeeder Diffing.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLDiffer.class);

  /**
   * The default buffer size to use.
   */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

// class attributes ----------------------------------------------------------------------------

  /**
   * The configuration used for Diff-X.
   */
  private final DiffXConfig config = new DiffXConfig();

  /**
   * Threshold to change the granularity of Diff-X.
   */
  private final int maxEvents;

// constructors --------------------------------------------------------------------------------

  /**
   * Constructor.
   * Diff events are the number of elements/attributes/text in each fragment multiplied by each other.
   * When maxevents is reached the diff will set the coarsest granularity (TEXT) and try again.
   * If events is still larger than maxevents an exception is generated.
   * For reasonable performance maximum 4,000,000 is recommended.
   *
   * @param maxevents maximum allowed diff events
   */
  public PSMLDiffer(int maxevents) {
    this.maxEvents = maxevents;
    this.config.setGranularity(TextGranularity.WORD);
    this.config.setWhiteSpaceProcessing(WhiteSpaceProcessing.PRESERVE);
  }

// getters and setters -------------------------------------------------------------------------

  /**
   * Defines how the white spaces should be processed by Diff-X (default is PRESERVE).
   *
   * @param whitespace how the white spaces should be processed by Diff-X.
   */
  public void setWhiteSpaceProcessing(WhiteSpaceProcessing whitespace) {
    this.config.setWhiteSpaceProcessing(whitespace);
  }

  /**
   * Defines the granularity of the text compare used by Diff-X (default is WORD).
   *
   * @param granularity the granularity of the text compare used by Diff-X.
   */
  public void setGranularity(TextGranularity granularity) {
    this.config.setGranularity(granularity);
  }

  /**
   * Sets whether compare XML contains namspaces.
   *
   * @param aware <code>true</code> if contains namespaces;
   *                 <code>false</code> otherwise.
   */
  public void setNamespaceAware(boolean aware) {
    this.config.setNamespaceAware(aware);
  }

// methods -------------------------------------------------------------------------------------

  /**
   * Compares the two specified pieces of XML and prints the diff onto the given writer.
   *
   * @param xml1 The first XML reader to compare.
   * @param xml2 The first XML reader to compare.
   * @param out  Where the output goes
   *
   * @throws DiffXException Should a Diff-X exception occur or if maxevents is reached.
   * @throws IOException    Should an I/O exception occur.
   */
  public void diff(Reader xml1, Reader xml2, Writer out) throws DiffXException, IOException {

    // Records the events from the XML
    SAXRecorder recorder = new SAXRecorder();
    recorder.setConfig(this.config);
    LOGGER.debug("Diff-X config: {} {}", this.config.getGranularity(), this.config.getWhiteSpaceProcessing());

    StringWriter x1 = new StringWriter();
    StringWriter x2 = new StringWriter();
    copy(xml1, x1);
    copy(xml2, x2);

    EventSequence seq1 = recorder.process(new InputSource(new StringReader(x1.toString())));
    EventSequence seq2 = recorder.process(new InputSource(new StringReader(x2.toString())));
    LOGGER.debug("Sequence #1: {} (pre-slicing)", seq1.size());
    LOGGER.debug("Sequence #2: {} (pre-slicing)", seq2.size());

    // start slicing
    SequenceSlicer slicer = new SequenceSlicer(seq1, seq2);
    slicer.slice();
    LOGGER.debug("Sequence #1: {} (post-slicing, granularity={})", seq1.size(), this.config.getGranularity());
    LOGGER.debug("Sequence #2: {} (post-slicing, granularity={})", seq2.size(), this.config.getGranularity());

    // Check sequences lower than threshold
    long max = Long.valueOf(seq1.size()) * Long.valueOf(seq2.size());
    if (max > this.maxEvents) {
      LOGGER.debug("Threshold reached: {} > {}", max, this.maxEvents);

      // Let's try to change the granularity
      if (!TextGranularity.TEXT.equals(this.config.getGranularity())) {
        this.config.setGranularity(TextGranularity.TEXT);
        LOGGER.debug("Changed granularity to TEXT");
        seq1 = recorder.process(new InputSource(new StringReader(x1.toString())));
        seq2 = recorder.process(new InputSource(new StringReader(x2.toString())));
        slicer = new SequenceSlicer(seq1, seq2);
        slicer.slice();
        LOGGER.debug("Sequence #1: {} (post-slicing, granularity={})", seq1.size(), this.config.getGranularity());
        LOGGER.debug("Sequence #2: {} (post-slicing, granularity={})", seq2.size(), this.config.getGranularity());
        max = Long.valueOf(seq1.size()) * Long.valueOf(seq2.size());
      }
      // Let's check again
      if (max > this.maxEvents) {
        throw new DiffXException("Cannot compare contents with more than "+this.maxEvents+" events (found "+max+").");
      }
    }

    // Diff
    diff(seq1, seq2, slicer, out);
  }

  // Private helpers ------------------------------------------------------------------------------

  /**
   * Compares the two specified pieces of XML and prints the diff onto the given writer.
   *
   * @param seq1   The first XML sequence to compare.
   * @param seq2   The second XML sequence to compare.
   * @param slicer A slicer to avoid having to compare the same events.
   * @param out    Where the output goes.
   *
   * @throws DiffXException Should a Diff-X exception occur.
   * @throws IOException    Should an I/O exception occur.
   */
  private void diff(EventSequence seq1, EventSequence seq2, SequenceSlicer slicer, Writer out)
      throws DiffXException, IOException {
    SafeXMLFormatter formatter = new SafeXMLFormatter(out);
    // This prefix mapping is needed for DOM namespace prefix mapping
    seq2.mapPrefix("http://www.w3.org/XML/1998/namespace", "xml");
    formatter.declarePrefixMapping(seq1.getPrefixMapping());
    formatter.declarePrefixMapping(seq2.getPrefixMapping());
    formatter.setConfig(this.config);

    // Start formatting
    slicer.formatStart(formatter);

    // Do the diffing
    GuanoAlgorithm df = new GuanoAlgorithm(seq1, seq2);
    df.process(formatter);

    // append the remaining XML
    slicer.formatEnd(formatter);
  }

  /**
   * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
   *
   * @param input  the <code>Reader</code> to read from
   * @param output  the <code>Writer</code> to write to
   *
   * @throws IOException if read or write error occurs
   */
  public static void copy(Reader input, Writer output) throws IOException {
      char[] buffer = new char[DEFAULT_BUFFER_SIZE];
      int n = 0;
      while (-1 != (n = input.read(buffer))) {
          output.write(buffer, 0, n);
      }
  }

}
