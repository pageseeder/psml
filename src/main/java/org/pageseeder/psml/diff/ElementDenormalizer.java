/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.handler.DiffFilter;
import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;
import org.pageseeder.diffx.token.impl.XMLEndElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A class responsible for denormalizing XML elements as a result of a diff operation.
 *
 * <p>It ensures that elements replaced during normalization, such as "cell" to "hcell" or
 * "list" to "nlist", are properly substituted back to their original representations in
 * the final diff output.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.7
 * @since 1.6.7
 */
public final class ElementDenormalizer extends DiffFilter<XMLToken> implements org.pageseeder.diffx.api.DiffHandler<XMLToken> {

  public ElementDenormalizer(org.pageseeder.diffx.api.DiffHandler<XMLToken> handler) {
    super(handler);
  }

  private final List<Operation<XMLToken>> buffer = new ArrayList<>();

  private final Deque<StartElementToken> elementsToClose = new ArrayDeque<>();
  private final Deque<Integer> elementsToCloseLevel = new ArrayDeque<>();

  private int level = 0;

  @Override
  public void handle(Operator operator, XMLToken token) {
    // Adjust level
    if (token.getType() == XMLTokenType.START_ELEMENT) {
      level += 1;
    } else if (token.getType() == XMLTokenType.END_ELEMENT) {
      level -= 1;
      // Ensure that we close the elements that have been replaced
      if (!elementsToCloseLevel.isEmpty() && level == elementsToCloseLevel.peek()) {
        elementsToCloseLevel.pop();
        this.target.handle(operator, new XMLEndElement(elementsToClose.pop()));
        return;
      }
    }

    // If we encounter a cell or list start element, add it to the buffer for potential replacement
    if (token.equals(CellNormalizer.CELL_START) || token.equals(ListNormalizer.LIST_START)) {
      buffer.add(new Operation<>(operator, token));
      return;
    }

    // If we have a start element in the buffer, check for attributes that indicate normalization
    if (!buffer.isEmpty()) {
      if (token.getType() == XMLTokenType.ATTRIBUTE) {
        handleAttribute(operator, token);
        return;
      }
      // If we receive anything other than an attribute, we need to flush the buffer
      flushBuffer();
    }

    // Forward the current token to the target handler
    this.target.handle(operator, token);
  }

  @Override
  public void end() {
    flushBuffer();
    this.target.end();
  }

  /**
   * Handles the modification of attributes based on the operator and the type of elements
   * present in the buffer. Depending on specific conditions tied to attributes and element types,
   * certain tokens in the buffer may be replaced with normalized versions.
   *
   * <p>This method processes transformations for "hcell" and "nlist" attributes for certain
   * elements, updates the buffer accordingly, and tracks elements that require closure.
   * Finally, the given attribute is always added to the buffer.
   *
   * @param operator The operation type indicating whether the element was inserted, deleted, or matched.
   * @param attribute The XML attribute token being handled.
   */
  private void handleAttribute(Operator operator, XMLToken attribute) {
    // Only replace for inserted or matched elements
    if (operator == Operator.INS || operator == Operator.MATCH) {
      XMLToken element = buffer.get(0).token();

      // If we have a cell start element and find an "hcell" attribute with value "true"
      if (element.equals(CellNormalizer.CELL_START) &&
          "hcell".equals(attribute.getName()) && "true".equals(attribute.getValue())) {
        this.buffer.set(0, new Operation<>(buffer.get(0).operator(), CellNormalizer.HCELL_START));
        this.elementsToClose.push(CellNormalizer.HCELL_START);
        this.elementsToCloseLevel.push(this.level-1);
      }

      // If we have a list start element and find an "nlist" attribute with value "true"
      else if (element.equals(ListNormalizer.LIST_START) &&
          "nlist".equals(attribute.getName()) && "true".equals(attribute.getValue())) {
        // Replace LIST_START with NLIST_START in the buffer
        this.buffer.set(0, new Operation<>(buffer.get(0).operator(), ListNormalizer.NLIST_START));
        this.elementsToClose.push(ListNormalizer.NLIST_START);
        this.elementsToCloseLevel.push(this.level-1);
      }
    }

    // Add the attribute to the buffer
    this.buffer.add(new Operation<>(operator, attribute));
  }

  /**
   * Send the tokens to the target handler and clear the buffer
   */
  private void flushBuffer() {
    for (Operation<XMLToken> operation : buffer) {
      this.target.handle(operation.operator(), operation.token());
    }
    buffer.clear();
  }
}
