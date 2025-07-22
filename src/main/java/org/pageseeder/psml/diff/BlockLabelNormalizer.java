/*
 * Copyright (c) 1999-2025 weborganic systems pty. ltd.
 */
package org.pageseeder.psml.diff;

import org.eclipse.jdt.annotation.Nullable;
import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.XMLTokenType;
import org.pageseeder.diffx.token.impl.XMLEndElement;
import org.pageseeder.diffx.token.impl.XMLStartElement;
import org.pageseeder.diffx.xml.SequenceProcessor;

import java.util.*;

/**
 * The BlockLabelNormalizer class processes a sequence of XML tokens to ensure that text nodes
 * directly within block elements are encapsulated in pseudo-paragraphs. This is particularly
 * useful when working with XML that has unstructured content within certain container or block
 * elements.
 *
 * <p>The class operates using a defined set of rules for what constitutes blocks, containers, and
 * pseudo-paragraph boundaries, which can be configured for specific formats such as HTML or PSML.
 *
 * <p>Instances of this class can be created using the static factory methods, {@code forHtml()}
 * and {@code forPsml()}, which provide preconfigured setups for these common formats.
 *
 * @author Christophe Lauret
 */
final class BlockLabelNormalizer implements SequenceProcessor {

  private final String para;

  private final Set<String> blocks;

  private final Set<String> containers;

  public static BlockLabelNormalizer forHtml() {
    return new BlockLabelNormalizer("p",
        Set.of("div"),
        Set.of("p", "ol", "ul", "div", "h1", "h2", "h3", "h4", "h5", "h6", "table", "pre"));
  }

  public static BlockLabelNormalizer forPsml() {
    return new BlockLabelNormalizer("para",
        Set.of("block"),
        Set.of("para", "list", "nlist", "block", "heading", "table", "preformat"));
  }

  BlockLabelNormalizer(String name, Set<String> blocks, Set<String> containers) {
    this.para = name;
    this.blocks = blocks;
    this.containers = containers;
  }

  @Override
  public List<XMLToken> process(List<XMLToken> tokens) {
    State state = new State(this.para, this.blocks, tokens.size());
    for (XMLToken token : tokens) {
      // If we find text directly under a block label, wrap it in a pseudo paragraph
      if (token.getType() == XMLTokenType.TEXT) {
        if (state.isBlock() && !token.isWhitespace()) {
          state.startPseudoPara();
        }
      // A start element
      } else if (token.getType() == XMLTokenType.START_ELEMENT) {
        // A container (list, block, etc.)
        if (isContainer(token)) {
          // Close any pseudo-para
          if (state.isPseudoPara()) {
            state.endPseudoPara();
          }
        // Any other element ensure it is wrapped in a pseudo para
        } else if (state.isBlock()) {
          state.startPseudoPara();
        }
        state.push(token);
      } else if (token.getType() == XMLTokenType.END_ELEMENT) {
        if (state.isBlock(token) && state.isPseudoPara()) {
          state.endPseudoPara();
        }
        state.pop();
      }
      state.target.add(token);
    }
    return state.target;
  }

  private boolean isContainer(@Nullable XMLToken token) {
    if (token == null) return false;
    return this.containers.contains(token.getName());
  }

  private static class State {

    final StartElementToken para;
    final Deque<XMLToken> context = new ArrayDeque<>();
    final ArrayList<XMLToken> target;
    final Set<String> blocks;

    public State(String para, Set<String> blocks, int size) {
      this.para = new XMLStartElement(para);
      this.blocks = blocks;
      this.target = new ArrayList<>(size);
    }

    boolean isBlock() {
      return isBlock(this.context.peek());
    }

    private boolean isBlock(@Nullable XMLToken token) {
      return token != null && this.blocks.contains(token.getName());
    }

    boolean isPseudoPara() {
      return isPseudoPara(this.context.peek());
    }

    private boolean isPseudoPara(@Nullable XMLToken token) {
      return token != null && this.para.getName().equals(token.getName()) && token instanceof PseudoStartToken;
    }

    void startPseudoPara() {
      StartElementToken start = new PseudoStartToken(this.para);
      target.add(start);
      context.push(start);
    }

    void endPseudoPara() {
      target.add(new PseudoEndToken(new XMLEndElement(para)));
      context.pop();
    }

    public void push(XMLToken token) {
      this.context.push(token);
    }

    public void pop() {
      this.context.pop();
    }

  }

}
