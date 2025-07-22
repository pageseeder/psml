/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.impl.XMLStartElement;
import org.pageseeder.psml.util.Beta;

/**
 * A normalizer to replace the "hcell" elements by "cell" elements and including attribute
 * to indicate that the substitution took place.
 *
 * <p>For example:
 * <pre>{@code <hcell>Header</hcell>}</pre>
 *
 * <p>Becomes:
 * <pre>{@code <cell hcell="true">Header</cell>}</pre>
 *
 * <p>Normalising these changes allows the diff algorithm to focus on changes within the
 * cells and report the structural changes separately based on the "hcell" attribute.</p>
 *
 * @author Christophe Lauret
 *
 * @version 1.6.7
 * @since 1.6.7
 */
@Beta
public final class CellNormalizer extends ElementNormalizer {

  public static final StartElementToken HCELL_START = new XMLStartElement("hcell");
  public static final StartElementToken CELL_START = new XMLStartElement("cell");

  public CellNormalizer() {
    super(HCELL_START, CELL_START);
  }
}
