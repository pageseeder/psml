/*
 * Copyright (c) 2018 Allette Systems Pty Ltd
 */
package org.pageseeder.psml.diff;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.pageseeder.diffx.DiffException;
import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.action.OperationsBuffer;
import org.pageseeder.diffx.algorithm.*;
import org.pageseeder.diffx.api.DiffAlgorithm;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.config.DiffConfig;
import org.pageseeder.diffx.config.TextGranularity;
import org.pageseeder.diffx.config.WhiteSpaceProcessing;
import org.pageseeder.diffx.format.DefaultXMLDiffOutput;
import org.pageseeder.diffx.format.XMLDiffOutput;
import org.pageseeder.diffx.handler.CoalescingFilter;
import org.pageseeder.diffx.handler.PostXMLFixer;
import org.pageseeder.diffx.load.SAXLoader;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.xml.NamespaceSet;
import org.pageseeder.diffx.xml.Sequence;
import org.pageseeder.xmlwriter.UndeclaredNamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PageSeeder wrapper for DiffX.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 *
 * @since 0.3.7
 * @version 1.5.1
 */
public final class PSMLDiffer {

  /**
   * Logger for PageSeeder Diffing.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PSMLDiffer.class);

  /**
   * The configuration used for Diff-X.
   */
  private DiffConfig config;

  /**
   * Threshold to change the granularity of Diff-X.
   */
  private final int maxEvents;

  /**
   * Constructor.
   *
   * <p>Diff events are the number of elements/attributes/text in each fragment multiplied by each other.
   * When `maxEvents` is reached the diff will set the coarsest granularity (TEXT) and try again.
   * If events is still larger than `maxEvents` an exception is generated.
   *
   * <p>For reasonable performance, a maximum of 4,000,000 is recommended.
   *
   * @param maxEvents maximum allowed diff events
   */
  public PSMLDiffer(int maxEvents) {
    this.maxEvents = maxEvents;
    this.config = DiffConfig.getDefault()
        .granularity(TextGranularity.SPACE_WORD)
        .whitespace(WhiteSpaceProcessing.PRESERVE);
  }

  /**
   * Defines how Diff-X should process the white spaces (default is PRESERVE).
   *
   * @param whitespace how Diff-X should process the white spaces.
   */
  public void setWhiteSpaceProcessing(WhiteSpaceProcessing whitespace) {
    this.config = this.config.whitespace(whitespace);
  }

  /**
   * Defines the granularity of the text compare used by Diff-X (default is SPACE_WORD).
   *
   * @param granularity the granularity of the text compares used by Diff-X.
   */
  public void setGranularity(TextGranularity granularity) {
    this.config = this.config.granularity(granularity);
  }

  /**
   * Compares the two specified pieces of XML and prints the diff onto the given writer.
   *
   * @param xml1 The first XML reader to compare.
   * @param xml2 The first XML reader to compare.
   * @param out  Where the output goes
   *
   * @throws org.pageseeder.diffx.DiffException If a Diff-X exception occurs or if maxEvents is reached.
   * @throws IOException   If an I/O exception occurs.
   */
  public void diff(Reader xml1, Reader xml2, Writer out) throws org.pageseeder.diffx.DiffException, IOException {
    LOGGER.debug("Diff-X config: {} {}", this.config.granularity(), this.config.whitespace());
    if (LOGGER.isDebugEnabled()) {
      String source1 = toString(xml1);
      String source2 = toString(xml2);
      LOGGER.debug("XML Source B:\n"+source1);
      LOGGER.debug("XML Source A:\n"+source2);
      loadAndDiff(new StringReader(source2), new StringReader(source1), out);
    } else {
      loadAndDiff(xml2, xml1, out);
    }
  }

  private void loadAndDiff(Reader from, Reader to, Writer out) throws org.pageseeder.diffx.DiffException, IOException {
    // Load tokens from XML
    SAXLoader loader = new SAXLoader();
    loader.setConfig(this.config);
    Sequence seqB = loader.load(to);
    Sequence seqA = loader.load(from);
    LOGGER.debug("Sequence A: {} (granularity={})", seqA.size(), this.config.granularity());
    LOGGER.debug("Sequence B: {} (granularity={})", seqB.size(), this.config.granularity());

    // Diff sequences
    try {
      diff(seqA, seqB, out);
    } catch (DataLengthException ex) {
      throw new org.pageseeder.diffx.DiffException("There are over "+ex.getThreshold()+" points of comparison ("+ex.getSize()+") reducing the fragment size will allow the comparison to be calculated.");
    } catch (UndeclaredNamespaceException ex) {
      throw new DiffException(ex.getMessage(), ex);
    }
  }

  /**
   * Compares the two specified pieces of XML and prints the diff onto the given writer.
   *
   * @param from The original XML sequence.
   * @param to   The modified XML sequence.
   * @param out  Where the output goes.
   */
  private void diff(Sequence from, Sequence to, Writer out) throws DataLengthException {
    DefaultXMLDiffOutput output = new DefaultXMLDiffOutput(out);
    output.setWriteXMLDeclaration(false);
    NamespaceSet namespaces = NamespaceSet.merge(to.getNamespaces(), from.getNamespaces());
    output.setNamespaces(namespaces);

    diffWithFallback(from, to, output);
  }

  private static String toString(Reader input) throws IOException {
    StringWriter out = new StringWriter();
    char[] buffer = new char[1024];
    int n;
    while (-1 != (n = input.read(buffer))) {
      out.write(buffer, 0, n);
    }
    return out.toString();
  }

  /**
   * Similar to optimistic diff from diffx
   */
  private void diffWithFallback(Sequence from, Sequence to, XMLDiffOutput output) {
    // Try with Gasherbrum
    OperationsBuffer<XMLToken> buffer = new OperationsBuffer<>();
    boolean successful = diffGasherbrum(from, to, buffer);
    if (!successful) {
      LOGGER.debug("Gasherbrum diff failed! Falling back to matrix-based diff");
      buffer = new OperationsBuffer<>();
      diffMatrixXML(from, to, buffer, false);
    }
    // Apply the results from to the buffer
    buffer.applyTo(new CoalescingFilter(output));
  }

  /**
   * Fast diff uses myers' greedy algorithm with a post-process XML correction filter.
   *
   * @return true if successful; false otherwise.
   */
  private boolean diffGasherbrum(List<? extends XMLToken> from, List<? extends XMLToken> to, org.pageseeder.diffx.api.DiffHandler<XMLToken> handler) {
    GasherbrumIIIAlgorithm algorithm = new GasherbrumIIIAlgorithm(.5f);
    algorithm.diff(from, to, handler);
    return !algorithm.hasError();
  }

  /**
   * Computes the differences between two sequences of XML tokens using the matrix
   * diff algorithm. Handles cases where the diff computation exceeds a defined threshold
   * by coalescing the input sequences or throwing an exception if the threshold is still exceeded.
   *
   * @param from       The original list of XML tokens to compare.
   * @param to         The modified list of XML tokens to compare.
   * @param handler    The handler responsible for processing the diff output.
   * @param coalesced  Indicates whether the input sequences have already been coalesced
   *                   to a coarser granularity.
   */
  private void diffMatrixXML(List<? extends XMLToken> from, List<? extends XMLToken> to, DiffHandler<XMLToken> handler, boolean coalesced) {
    MatrixXMLAlgorithm algorithm = new MatrixXMLAlgorithm();
    algorithm.setThreshold(this.maxEvents);
    if (algorithm.isDiffComputable(from, to)) {
      handler.start();
      algorithm.diff(from, to, handler);
      handler.end();
    } else if (!coalesced) {
      LOGGER.debug("Coalescing content to");
      List<? extends XMLToken> a = CoalescingFilter.coalesce(from);
      List<? extends XMLToken> b = CoalescingFilter.coalesce(to);
      diffMatrixXML(a, b, handler, true);
    } else {
      throw new DataLengthException(from.size() * to.size(), this.maxEvents);
    }
  }

}
