package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.impl.XMLStartElement;
import org.pageseeder.diffx.xml.SequenceProcessor;

/**
 * A normalizer to replace the "nlist" elements by "list" elements and including attribute
 * to indicate that the substitution took place.
 *
 * <p>For example:
 * <pre>{@code <nlist>...</nlist>}</pre>
 *
 * <p>Becomes:
 * <pre>{@code <list nlist="true">...</list>}</pre>
 *
 * <p>Normalising these changes allows the diff algorithm to focus on changes within the
 * lists and report the structural changes separately based on the "nlist" attribute.</p>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.7
 * @since 1.6.7
 */
final class ListNormalizer extends ElementNormalizer implements SequenceProcessor {

  public static final StartElementToken NLIST_START = new XMLStartElement("nlist");

  public static final StartElementToken LIST_START = new XMLStartElement("list");

  public ListNormalizer() {
    super(NLIST_START, LIST_START);
  }

}
