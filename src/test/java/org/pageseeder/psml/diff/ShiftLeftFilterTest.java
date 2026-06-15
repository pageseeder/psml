package org.pageseeder.psml.diff;

import org.junit.jupiter.api.Test;
import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.action.OperationsBuffer;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.CharactersToken;
import org.pageseeder.diffx.token.impl.XMLEndElement;
import org.pageseeder.diffx.token.impl.XMLStartElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ShiftLeftFilter}.
 *
 * <p>Each test builds a sequence of diff operations using a compact string notation:
 * <ul>
 *   <li>{@code "+<tag>"} — INS start element</li>
 *   <li>{@code "-<tag>"} — DEL start element</li>
 *   <li>{@code "<tag>"}  — MATCH start element</li>
 *   <li>{@code "+</tag>"}, {@code "-</tag>"}, {@code "</tag>"} — end element variants</li>
 *   <li>{@code "+text"}, {@code "-text"}, {@code "text"} — character token variants</li>
 * </ul>
 */
class ShiftLeftFilterTest {

  // --- no-shift cases -------------------------------------------------------

  @Test
  void handle_allMatch_unchanged() {
    assertNoChange("<ul>", "<li>", "text", "</li>", "</ul>");
  }

  /**
   * A run of exactly one non-MATCH token does not satisfy the {@code lastOperatorCount > 1}
   * condition, so no shift is attempted.
   */
  @Test
  void handle_singleChangeBeforeMatch_noShift() {
    assertNoChange("<ul>", "<li>", "+<li>", "text", "</li>", "</ul>");
  }

  /**
   * When the last changed token does not equal the last preceding MATCH token, the while-loop
   * condition fails immediately and no swap is performed.
   */
  @Test
  void handle_nonMatchingLastToken_noShift() {
    assertNoChange("<ul>", "<li>", "+a", "+b", "c", "</li>", "</ul>");
  }

  /**
   * A changed block composed entirely of text tokens is structurally balanced (no elements).
   * {@code isBalanced} returns {@code true} on the first check, so the shift is short-circuited
   * even though the last text token equals a preceding MATCH text token.
   */
  @Test
  void handle_textOnlyChangedBlock_noShift() {
    assertNoChange("<para>", "hello", "+world", "+hello", "end");
  }

  // --- shift cases ----------------------------------------------------------

  /**
   * Canonical INS example: a diff algorithm lazily attributes the inserted {@code <li>} boundary
   * to the second (matched) list item.
   *
   * <pre>
   *   Input:    &lt;ul&gt; &lt;li&gt; +a +b +&lt;/li&gt; +&lt;li&gt; c &lt;/li&gt; &lt;/ul&gt;
   *   Expected: &lt;ul&gt; +&lt;li&gt; +a +b +&lt;/li&gt; &lt;li&gt; c &lt;/li&gt; &lt;/ul&gt;
   * </pre>
   */
  @Test
  void handle_insertedListItem_shiftsLeft() {
    List<Operation<XMLToken>> result = shiftLeft(
        "<ul>", "<li>", "+a", "+b", "+</li>", "+<li>", "c", "</li>", "</ul>"
    );
    List<Operation<XMLToken>> expected = toOperations(
        "<ul>", "+<li>", "+a", "+b", "+</li>", "<li>", "c", "</li>", "</ul>"
    );
    assertEquals(expected, result);
  }

  /** Same scenario as {@link #handle_insertedListItem_shiftsLeft} but with DEL events. */
  @Test
  void handle_deletedListItem_shiftsLeft() {
    List<Operation<XMLToken>> result = shiftLeft(
        "<ul>", "<li>", "-a", "-b", "-</li>", "-<li>", "c", "</li>", "</ul>"
    );
    List<Operation<XMLToken>> expected = toOperations(
        "<ul>", "-<li>", "-a", "-b", "-</li>", "<li>", "c", "</li>", "</ul>"
    );
    assertEquals(expected, result);
  }

  /**
   * Two independent INS blocks each get shifted left by one position.
   * The total {@link ShiftLeftFilter#getShifted()} count is two.
   */
  @Test
  void handle_twoInsertedListItems_eachShiftsLeft() {
    List<Operation<XMLToken>> result = shiftLeft(
        "<ul>",
        "<li>", "+a", "+</li>", "+<li>", "b", "</li>",
        "<li>", "+c", "+</li>", "+<li>", "d", "</li>",
        "</ul>"
    );
    List<Operation<XMLToken>> expected = toOperations(
        "<ul>",
        "+<li>", "+a", "+</li>", "<li>", "b", "</li>",
        "+<li>", "+c", "+</li>", "<li>", "d", "</li>",
        "</ul>"
    );
    assertEquals(expected, result);
  }

  // --- flushing behaviour ---------------------------------------------------

  /**
   * When the sequence ends with a non-MATCH block (no trailing MATCH to trigger an early flush),
   * {@link ShiftLeftFilter#end()} must still forward all buffered operations to the target.
   */
  @Test
  void end_flushesTrailingInsBlock() {
    assertNoChange("<para>", "+a", "+b");
  }

  // --- getShifted -----------------------------------------------------------

  @Test
  void getShifted_returnsZeroWhenNoShiftsOccur() {
    ShiftLeftFilter filter = applyFilter("<ul>", "<li>", "+a", "b", "</li>", "</ul>");
    assertEquals(0, filter.getShifted());
  }

  @Test
  void getShifted_returnsOneAfterSingleShift() {
    ShiftLeftFilter filter = applyFilter(
        "<ul>", "<li>", "+a", "+b", "+</li>", "+<li>", "c", "</li>", "</ul>"
    );
    assertEquals(1, filter.getShifted());
  }

  @Test
  void getShifted_returnsTwoAfterTwoIndependentShifts() {
    ShiftLeftFilter filter = applyFilter(
        "<ul>",
        "<li>", "+a", "+</li>", "+<li>", "b", "</li>",
        "<li>", "+c", "+</li>", "+<li>", "d", "</li>",
        "</ul>"
    );
    assertEquals(2, filter.getShifted());
  }

  // --- helpers --------------------------------------------------------------

  private static void assertNoChange(String... operations) {
    assertEquals(toOperations(operations), shiftLeft(operations));
  }

  /** Runs the filter over the given operations and returns the output as a list. */
  private static List<Operation<XMLToken>> shiftLeft(String... operations) {
    OperationsBuffer<XMLToken> out = new OperationsBuffer<>();
    ShiftLeftFilter filter = new ShiftLeftFilter(out);
    for (Operation<XMLToken> op : toOperations(operations)) {
      filter.handle(op.operator(), op.token());
    }
    filter.end();
    return out.getOperations();
  }

  /** Runs the filter and returns it so callers can inspect {@link ShiftLeftFilter#getShifted()}. */
  private static ShiftLeftFilter applyFilter(String... operations) {
    OperationsBuffer<XMLToken> out = new OperationsBuffer<>();
    ShiftLeftFilter filter = new ShiftLeftFilter(out);
    for (Operation<XMLToken> op : toOperations(operations)) {
      filter.handle(op.operator(), op.token());
    }
    filter.end();
    return filter;
  }

  /**
   * Converts compact string notation into a list of typed diff operations.
   *
   * <ul>
   *   <li>Leading {@code +} → INS, leading {@code -} → DEL, no prefix → MATCH.</li>
   *   <li>{@code <tag>} → {@link XMLStartElement}, {@code </tag>} → {@link XMLEndElement},
   *       anything else → {@link CharactersToken}.</li>
   * </ul>
   */
  private static List<Operation<XMLToken>> toOperations(String... items) {
    List<Operation<XMLToken>> result = new ArrayList<>(items.length);
    for (String item : items) {
      Operator operator = item.startsWith("-") ? Operator.DEL
          : item.startsWith("+") ? Operator.INS
          : Operator.MATCH;
      String tokenStr = item.replaceAll("^[+\\-]", "");
      XMLToken token;
      if (tokenStr.matches("</[a-zA-Z][a-zA-Z0-9]*>")) {
        token = new XMLEndElement(tokenStr.replaceAll("[^a-zA-Z0-9]", ""));
      } else if (tokenStr.matches("<[a-zA-Z][a-zA-Z0-9]*>")) {
        token = new XMLStartElement(tokenStr.replaceAll("[^a-zA-Z0-9]", ""));
      } else {
        token = new CharactersToken(tokenStr);
      }
      result.add(new Operation<>(operator, token));
    }
    return result;
  }
}
