package org.pageseeder.psml.diff;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.action.OperationsBuffer;
import org.pageseeder.diffx.algorithm.MatrixXMLAlgorithm;
import org.pageseeder.diffx.api.DiffAlgorithm;
import org.pageseeder.diffx.api.DiffHandler;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.handler.XMLBalanceCheckFilter;
import org.pageseeder.diffx.handler.XMLEventBalancer;
import org.pageseeder.diffx.similarity.EditSimilarity;
import org.pageseeder.diffx.similarity.ElementSimilarity;
import org.pageseeder.diffx.similarity.SimilarityWagnerFischerAlgorithm;
import org.pageseeder.diffx.token.EndElementToken;
import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;
import org.pageseeder.diffx.token.impl.XMLElement;

import java.util.*;

/**
 * This algorithm class is designed to compare and compute differences between
 * lists of XMLToken objects while handling structural and hierarchical block elements.
 *
 * <p>It incorporates multiple algorithms and techniques, including the Myers greedy
 * algorithm for token matching and structural diffing.
 *
 * <p>This implementation supports a two-step diff strategy:
 * 1. Structural comparison using GToken lists for block-level grouping.
 * 2. Detailed comparison within block elements using nested algorithms.
 *
 * <p>The algorithm iteratively processes tokens, handling structural and textual
 * edits, and applies operations to a DiffHandler.
 *
 * <p>The class also includes mechanisms for comparing tokens based on similarity
 * thresholds and ensuring efficient handling of XML structures.
 *
 * @author Christophe Lauret
 *
 * @since 1.6.5
 * @version 1.6.5
 */
public final class GasherbrumVAlgorithm implements DiffAlgorithm<XMLToken> {

  private static final Set<String> DEFAULT_BLOCKS = Set.of(
      "heading", "item", "para", "preformat", "row"
  );

  /**
   * The default similarity threshold used to determine whether two tokens
   * are considered similar in the context of the diffing algorithm.
   */
  public static final float DEFAULT_SIMILARITY_THRESHOLD = 0.5f;

  /**
   * The default similarity threshold used to determine whether two tokens
   * are considered similar in the context of the diffing algorithm.
   */
  public static final double DEFAULT_LENGTH_BOOST_FACTOR = 0.1;

  /**
   * Defines the similarity threshold used to determine whether two tokens
   * are considered similar in the context of the diffing algorithm.
   *
   * <p>A token comparison that results in a similarity score greater than this
   * threshold is treated as a match. This is primarily used in the equality
   * checks of {@code GToken} instances.
   */
  private final float similarityThreshold;

  private final Set<String> blocks;

  public GasherbrumVAlgorithm() {
    this.similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    this.blocks = DEFAULT_BLOCKS;
  }

  public GasherbrumVAlgorithm(float similarityThreshold) {
    this.similarityThreshold = similarityThreshold;
    this.blocks = DEFAULT_BLOCKS;
  }

  public float getSimilarityThreshold() {
    return this.similarityThreshold;
  }

  public Set<String> getBlocks() {
    return this.blocks;
  }

  private boolean hasError = false;

  @Override
  public void diff(List<? extends XMLToken> from, List<? extends XMLToken> to, DiffHandler<XMLToken> handler) {
    List<XMLToken> gFrom = fold(from);
    List<XMLToken> gTo = fold(to);

    OperationsBuffer<XMLToken> buffer = diffBySimilarity(gFrom, gTo);

    // Diff within each structural block
    XMLBalanceCheckFilter checker = new XMLBalanceCheckFilter(handler);
    XMLEventBalancer balancer = new XMLEventBalancer(checker);
    ShiftLeftFilter shifter = new ShiftLeftFilter(balancer);
    shifter.start();
    diffAndUnfold(gFrom, gTo, buffer, shifter);
    shifter.end();
    if (!checker.isBalanced()) {
      this.hasError = true;
    }
  }

  public boolean hasError() {
    return hasError;
  }

  /**
   * Computes the differences between two lists of {@link XMLToken} objects using a similarity-aware
   * algorithm. The operation produces a sequence of edit operations that aim to transform the
   * source list into the target list as efficiently as possible, considering token similarity.
   *
   * @param from the source list of {@link XMLToken} objects to compare, must not be null
   * @param to the target list of {@link XMLToken} objects to compare, must not be null
   * @return an {@link OperationsBuffer} containing the computed edit operations to transform
   *         the source list into the target list
   */
  private OperationsBuffer<XMLToken> diffBySimilarity(List<XMLToken> from, List<XMLToken> to) {
    DiffAlgorithm<XMLToken> algorithm = new SimilarityWagnerFischerAlgorithm<>(
        new ElementSimilarity(new EditSimilarity<>(), DEFAULT_LENGTH_BOOST_FACTOR),
        this.similarityThreshold
    );
    OperationsBuffer<XMLToken> buffer = new OperationsBuffer<>();
    algorithm.diff(from, to, buffer);
    return buffer;
  }

  /**
   * Processes a sequence of edit operations to compute and handle the differences
   * between two lists of {@link XMLToken} objects. This method maps over each
   * operation, unfolding nested {@link XMLElement} tokens where necessary,
   * and invokes the provided {@link DiffHandler} to process each operation.
   *
   * @param from    The source list of {@link XMLToken} objects to compare, must not be null.
   * @param to      The target list of {@link XMLToken} objects to compare, must not be null.
   * @param path    The {@link OperationsBuffer} containing the sequence of edit operations
   *                to apply, must not be null.
   * @param handler The {@link DiffHandler} responsible for processing the differences,
   *                must not be null.
   */
  private void diffAndUnfold(List<XMLToken> from, List<XMLToken> to, OperationsBuffer<XMLToken> path, DiffHandler<XMLToken> handler) {
    int i = 0;
    int j = 0;
    for (Operation<XMLToken> operation : path.getOperations()) {
      Operator operator = operation.operator();
      XMLToken token = operation.token();
      if (token.getType() == XMLTokenType.ELEMENT) {
        if (operator == Operator.MATCH) {
          XMLElement fromElement = (XMLElement)from.get(i);
          XMLElement toElement = (XMLElement)to.get(j);
          diffElement(fromElement, toElement, operator, handler);
        } else {
          for (XMLToken t : ((XMLElement)token).tokens()) {
            handler.handle(operator, t);
          }
        }
      } else {
        handler.handle(operator, token);
      }
      if (operator == Operator.MATCH || operator == Operator.INS) { j++; }
      if (operator == Operator.MATCH || operator == Operator.DEL) { i++; }
    }
  }

  /**
   * Computes the difference between two {@link XMLElement} objects and handles the differences using
   * the provided {@link DiffHandler}. Depending on whether the elements have children containing
   * specific block tokens, it either processes the children recursively or computes differences at
   * the token level using a matrix-based algorithm.
   *
   * @param from      The source {@link XMLElement} to compare, must not be null.
   * @param to        The target {@link XMLElement} to compare, must not be null.
   * @param operator  The {@link Operator} defining the type of operation to be performed
   *                  (e.g., addition, deletion), must not be null.
   * @param handler   The {@link DiffHandler} used to process the computed differences, must not be null.
   */
  void diffElement(XMLElement from, XMLElement to, Operator operator, DiffHandler<XMLToken> handler) {
    boolean recurse = hasMultipleOrDifferentBlocks(from, to);
    if (recurse) {
      handler.handle(operator, from.getStart());
      diff(from.getContent(), to.getContent(), handler);
      handler.handle(operator, from.getEnd());
    } else {
      MatrixXMLAlgorithm matrix = new MatrixXMLAlgorithm();
      Operator op = getOp(from.getStart(), to.getStart());
      if (op != null) handler.handle(op, from.getStart());
      matrix.diff(from.getContent(), to.getContent(), handler);
      if (op != null) handler.handle(op, from.getEnd());
    }
  }

  private @Nullable Operator getOp(StartElementToken from, StartElementToken to) {
    boolean fromIsPseudo = from instanceof PseudoStartToken;
    boolean toIsPseudo = to instanceof PseudoStartToken;
    if (fromIsPseudo) return toIsPseudo ? null : Operator.INS;
    return toIsPseudo ? Operator.DEL : Operator.MATCH;
  }

  /**
   * Processes a list of {@link XMLToken} objects to produce a compacted output by folding
   * specific start and end element blocks into a single {@link XMLElement}.
   *
   * @param in The list of {@link XMLToken} objects to be processed.
   *           Must not be null but can be empty.
   * @return A list of folded {@link XMLToken} objects where specific start and end blocks
   *         defined in BLOCKS are collapsed into single {@link XMLElement} instances.
   */
  private List<XMLToken> fold(List<? extends XMLToken> in) {
    Deque<XMLToken> stack = new ArrayDeque<>();
    List<XMLToken> out = new ArrayList<>();
    List<XMLToken> children = null;
    for (XMLToken token : in) {
      if (children != null) {
        if (token.getType() == XMLTokenType.END_ELEMENT && isBlock(token)
            && stack.size() == 1 && stack.peek().getName().equals(token.getName())) {
          StartElementToken start = (StartElementToken) stack.pop();
          EndElementToken end = (EndElementToken) token;
          out.add(new XMLElement(start, end, children));
          children = null;
        } else {
          if (token.getType() == XMLTokenType.START_ELEMENT) {
            stack.push(token);
          } else if (token.getType() == XMLTokenType.END_ELEMENT) {
            stack.pop();
          }
          children.add(token);
        }
      } else {
        if (token.getType() == XMLTokenType.START_ELEMENT && isBlock(token)) {
          children = new ArrayList<>();
          stack.push(token);
        } else {
          out.add(token);
        }
      }
    }
    return out;
  }

  private boolean hasMultipleOrDifferentBlocks(XMLElement from, XMLElement to) {
    // No need to recurse if few tokens
    if (from.getContent().size() <= 2 || to.getContent().size() <= 2) return false;

    // Check for blocks in the `from`
    String fromBlock = findFirstBlock(from);
    if (fromBlock == null) return false;
    if (fromBlock.isEmpty()) return true;

    // Check for blocks in the `to`
    String toBlock = findFirstBlock(to);
    if (toBlock == null) return false;
    if (toBlock.isEmpty()) return true;

    // Only recurse if the block is different
    return !fromBlock.equals(toBlock);
  }

  private @Nullable String findFirstBlock(XMLElement element) {
    String firstBlock = null;
    for (XMLToken t : element.getContent()) {
      if (t.getType() == XMLTokenType.START_ELEMENT && isBlock(t)) {
        if (firstBlock == null) {
          firstBlock = t.getName();
        } else {
          return "";
        }
      }
    }
    return firstBlock;
  }

  /**
   * Determines whether the given {@link XMLToken} represents a block element.
   *
   * @param token The {@link XMLToken} to evaluate, must not be null.
   * @return {@code true} if the token is a block element, {@code false} otherwise.
   */
  private boolean isBlock(XMLToken token) {
    // We assume the default XML namespace URI ""
    return token.getNamespaceURI().isEmpty() && this.blocks.contains(token.getName());
  }

}
