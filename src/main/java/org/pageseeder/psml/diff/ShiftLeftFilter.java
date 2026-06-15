/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.handler.DiffFilter;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;
import org.pageseeder.psml.util.Beta;

import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A diff filter that corrects the output of a diff algorithm by shifting INS or DEL events to the
 * left when doing so produces a more structurally valid XML sequence.
 *
 * <p>Diff algorithms operating on repeated XML elements may assign changes lazily or eagerly to
 * similar patterns, producing event sequences where start and end element tags of an inserted or
 * deleted element are split across changed and matched events. Such sequences cannot be serialized
 * as well-formed XML.
 *
 * <p>For example, a diff algorithm may produce (where {@code +} denotes an INS event):
 * <pre>
 *   &lt;ul&gt; &lt;li&gt; +a +b +&lt;/li&gt; +&lt;li&gt; c &lt;/li&gt; &lt;/ul&gt;
 * </pre>
 * The {@code +&lt;/li&gt;} and {@code +&lt;li&gt;} events represent the element boundary of the
 * inserted item, but they are incorrectly attributed to the second (matched) {@code <li>} element.
 * This filter detects the opportunity to shift them left:
 * <pre>
 *   &lt;ul&gt; +&lt;li&gt; +a +b +&lt;/li&gt; &lt;li&gt; c &lt;/li&gt; &lt;/ul&gt;
 * </pre>
 * In the corrected sequence the inserted start and end tags form a balanced pair.
 *
 * <p>A shift is triggered when a MATCH token follows two or more consecutive non-MATCH (INS or
 * DEL) tokens, and the last changed token equals the last preceding MATCH token. The swap is
 * repeated moving left through the changed block until the remaining changed portion is already
 * balanced XML, or no matching token can be found. The total number of shifts applied is
 * accumulated and can be retrieved via {@link #getShifted()}.
 *
 * @see DiffFilter
 */
@Beta
public final class ShiftLeftFilter extends DiffFilter<XMLToken> {

  private final List<Operation<XMLToken>> operations = new ArrayList<>();

  private int lastOperatorCount = 0;

  private Operator lastOperator = Operator.MATCH;

  private int shifted = 0;

  public ShiftLeftFilter(DiffHandler<XMLToken> handler) {
    super(handler);
  }

  /**
   * Receives a single diff operation and buffers it for potential left-shift correction.
   *
   * <p>When a MATCH token is received after two or more consecutive non-MATCH tokens (INS or DEL),
   * the buffered operations are inspected for shift opportunities before being forwarded to the
   * target handler.
   *
   * @param operator the operator indicating the nature of the operation (MATCH, INS, or DEL)
   * @param token    the XML token being operated on
   */
  public void handle(Operator operator, XMLToken token) throws UncheckedIOException, IllegalStateException {
    // Shift left opportunity when we match after changes
    if (operator == Operator.MATCH && this.lastOperator != operator && this.lastOperatorCount > 1) {
      this.shifted += this.shiftOperations();
      this.flush();
    }

    // Keep track of consecutive operations
    if (this.lastOperator != operator) {
      this.lastOperatorCount =  0;
    }
    this.lastOperatorCount++;
    this.lastOperator = operator;

    // Always include
    this.operations.add(new Operation<>(operator, token));
  }

  @Override
  public void end() {
    this.flush();
    this.target.end();
  }

  /**
   * Returns the total number of token swaps performed by this filter across all shift operations.
   *
   * @return the number of shift operations applied.
   */
  public int getShifted() {
    return this.shifted;
  }

  /**
   * Attempts to shift the buffered changed operations left so that the changed block aligns with
   * a balanced XML subsequence.
   *
   * <p>For each position from the end of the changed block backwards, if the last changed token
   * equals the immediately preceding MATCH token, the two are swapped. Swapping stops when:
   * <ul>
   *   <li>the changed block is already balanced (no shift needed), or</li>
   *   <li>the tokens at the comparison positions no longer match, or</li>
   *   <li>there are no more MATCH tokens to compare against.</li>
   * </ul>
   *
   * @return the number of token swaps performed
   */
  private int shiftOperations() {
    int shift = 0;
    int p = this.operations.size()-1;
    Operation<XMLToken> lastChanged = this.operations.get(p);
    Operation<XMLToken> lastUnchanged = this.operations.get(p-this.lastOperatorCount);
    while (p >= 0 && lastUnchanged.operator() == Operator.MATCH && lastChanged.token().equals(lastUnchanged.token())) {
      if (isBalanced(this.operations, p-this.lastOperatorCount+1, p+1)) {
        return shift;
      }
      this.operations.set(p, new Operation<>(lastUnchanged.operator(), lastChanged.token()));
      this.operations.set(p-this.lastOperatorCount, new Operation<>(lastChanged.operator(), lastUnchanged.token()));

      p = p-1;
      shift += 1;
      if (p-this.lastOperatorCount < 0) break;
      lastChanged = this.operations.get(p);
      lastUnchanged = this.operations.get(p-this.lastOperatorCount);
    }
    return shift;
  }

  /**
   * Flushes all accumulated operations to the target handler, ensuring they are processed
   * and subsequently clears the list of operations.
   */
  private void flush() {
    for (Operation<XMLToken> operation : this.operations) {
      this.target.handle(operation.operator(), operation.token());
    }
    this.operations.clear();
  }

  /**
   * Checks if the XML tokens within the given range of operations are properly balanced.
   *
   * <p>The range is considered balanced if every start element has a matching end element
   * in the correct order and hierarchy irrespective of the operator.
   *
   * @param operations the list of operations containing XML tokens
   * @param from the starting index (inclusive) within the list to check
   * @param to the ending index (exclusive) within the list to check
   *
   * @return true if the XML tokens in the range are balanced, false otherwise
   */
  private static boolean isBalanced(List<Operation<XMLToken>> operations, int from, int to) {
    Deque<XMLToken> stack = new ArrayDeque<>();
    for (int i = from; i < to; i++) {
      XMLToken token = operations.get(i).token();
      if (token.getType() == XMLTokenType.START_ELEMENT) {
        stack.push(token);
      } else if (token.getType() == XMLTokenType.END_ELEMENT) {
        if (stack.isEmpty()) return false;
        if (!stack.pop().getName().equals(token.getName())) return false;
      }
    }
    return stack.isEmpty();
  }

}
