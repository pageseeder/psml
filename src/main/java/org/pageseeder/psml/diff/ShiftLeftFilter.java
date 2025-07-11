package org.pageseeder.psml.diff;

import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.handler.DiffFilter;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;

import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class ShiftLeftFilter extends DiffFilter<XMLToken> {

  private final List<Operation<XMLToken>> operations = new ArrayList<>();

  private int lastOperatorCount = 0;

  private Operator lastOperator = Operator.MATCH;

  private int shifted = 0;

  public ShiftLeftFilter(DiffHandler<XMLToken> handler) {
    super(handler);
  }

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
   * Retrieves the value of the `shifted` field, indicating the number of shift operations
   * performed during processing by the filter.
   *
   * @return the number of shift operations applied.
   */
  public int getShifted() {
    return this.shifted;
  }

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
