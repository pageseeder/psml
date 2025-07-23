package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.util.ExtendedWhitespaceStripper;
import org.pageseeder.diffx.xml.SequenceProcessor;

import java.util.List;

/**
 * A preconfigured whitespace stripper for PSML.
 *
 * <p>This class initializes and configures an instance of
 * <code>ExtendedWhitespaceStripper</code> by setting rules for always ignoring or
 * potentially ignoring certain PSML element types during whitespace processing.
 *
 * <p>Whitespaces directly under the following elements are systematically stripped:
 * "row", "list", "nlist", "table", "fragment".
 *
 * <p>Whitespaces directly under the following elements are stripped if possible:
 * "cell", "item", "para", "block", "blockxref", "hcell"
 *
 * @author Christophe Lauret
 *
 * @since 1.6.7
 * @version 1.6.7
 */
public class PSMLWhiteSpaceStripper implements SequenceProcessor {

  /**
   * The whitespace stripper doing the work.
   */
  private final ExtendedWhitespaceStripper stripper;

  /**
   * Constructs a PSMLWhiteSpaceStripper with predefined configuration to handle
   * whitespace stripping for specific PSML elements.
   */
  public PSMLWhiteSpaceStripper() {
    this.stripper = new ExtendedWhitespaceStripper();
    stripper.setAlwaysIgnore("row", "list", "nlist", "table", "fragment");
    stripper.setMaybeIgnore("cell", "item", "para", "block", "blockxref", "hcell");
  }

  @Override
  public List<XMLToken> process(List<XMLToken> tokens) {
    return this.stripper.process(tokens);
  }
}
