package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.AttributeToken;
import org.pageseeder.diffx.token.EndElementToken;
import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.XMLAttribute;
import org.pageseeder.diffx.token.impl.XMLEndElement;
import org.pageseeder.diffx.xml.SequenceProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for normalizers that transform specific XML elements during diff processing.
 *
 * <p>This class provides common functionality for normalizing elements by replacing source
 * elements with target elements and potentially adding attributes.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.7
 * @since 1.6.7
 */
class ElementNormalizer implements SequenceProcessor {

  /**
   * The start element token to be replaced.
   */
  protected final StartElementToken sourceStartToken;

  /**
   * The end element token to be replaced.
   */
  protected final EndElementToken sourceEndToken;

  /**
   * The start element token to replace with.
   */
  protected final StartElementToken targetStartToken;

  /**
   * The end element token to replace with.
   */
  protected final EndElementToken targetEndToken;

  /**
   * Attribute token to add to the target element to indicate the swap.
   */
  protected final AttributeToken flagAttribute;

  /**
   * Constructs an ElementNormalizer with the specified source and target elements.
   *
   * @param sourceStartToken The start element token to be replaced
   * @param targetStartToken The start element token to replace with
   */
  protected ElementNormalizer(StartElementToken sourceStartToken,
                              StartElementToken targetStartToken) {
    this.sourceStartToken = Objects.requireNonNull(sourceStartToken);
    this.sourceEndToken = new XMLEndElement(sourceStartToken);
    this.targetStartToken = Objects.requireNonNull(targetStartToken);
    this.targetEndToken = new XMLEndElement(targetStartToken);
    this.flagAttribute = new XMLAttribute(sourceStartToken.getName(), "true");
  }

  @Override
  public List<XMLToken> process(List<XMLToken> list) {
    if (!list.contains(sourceStartToken)) return list;
    List<XMLToken> result = new ArrayList<>(list.size() + 4);
    for (XMLToken token : list) {
      if (token.equals(sourceStartToken)) {
        result.add(targetStartToken);
        result.add(flagAttribute);
      } else if (token.equals(sourceEndToken)) {
        result.add(targetEndToken);
      } else {
        result.add(token);
      }
    }
    return result;
  }

}
