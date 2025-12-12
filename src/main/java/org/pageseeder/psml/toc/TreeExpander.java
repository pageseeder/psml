package org.pageseeder.psml.toc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 *
 *
 * @author Phlilip Rutherford
 * @author Christophe Lauret
 *
 * @version 1.6.9
 * @since 1.0.0
 */
public final class TreeExpander {

  /**
   * Stack of structural elements so that we can return to the previous level when a branch has been processed.
   */
  private final Deque<MutablePart> parts = new ArrayDeque<>();

  public TreeExpander(String title) {
    this.parts.push(new MutablePart(new DocumentTitle(title)));
  }

  public TreeExpander() {
    this.parts.push(new MutablePart(DocumentTitle.UNTITLED));
  }

  /**
   * Add a new leaf to the list (it should not affect the heading structure).
   * Used for paragraphs end transclusion start/end.
   *
   * @param element The new element to add
   *
   * @return This tree expander instance for method chaining
   */
  public TreeExpander addLeaf(Element element) {
    MutablePart current = this.parts.peek();
    // Add the element to the current part
    MutablePart mutable = new MutablePart(element);
    if (current != null) {
      current.add(mutable);
    }
    return this;
  }

  /**
   * Add a new element to the list.
   *
   * @param element The new element to add
   *
   * @return This tree expander instance for method chaining
   */
  public TreeExpander add(Element element) {
    return add(element, element.level());
  }

  /**
   * Add a new element to the list.
   *
   * @param element      The new element to add
   * @param elementLevel The level for the element
   *
   * @return This tree expander instance for method chaining
   */
  public TreeExpander add(Element element, int elementLevel) {
    // level = size of parts = level of current + 1
    int level = this.parts.size();
    MutablePart current = this.parts.peek();
    if (current == null) {
      current = new MutablePart(DocumentTitle.UNTITLED);
      this.parts.push(current);
    }

    // Insert phantom parts when we jumps levels (e.g. h1 -> h4)
    while (elementLevel > level) {
      Element phantom = new Phantom(level++, element.fragment(), element.originalFragment());
      MutablePart mutable = new MutablePart(phantom);
      current.add(mutable);
      this.parts.push(mutable);
      current = mutable;
    }

    // Remove other parts of deeper levels (e.g. h4 -> h1)
    while (elementLevel < level) {
      this.parts.pop();
      current = this.parts.peek();
      level--;
    }

    // Add the element to the current part
    MutablePart mutable = new MutablePart(element);
    if (current != null) {
      current.add(mutable);
    }
    this.parts.push(mutable);
    return this;
  }

  @SuppressWarnings("java:S1452")
  public List<Part<? extends Element>> parts() {
    MutablePart root = this.parts.peekLast();
    if (root == null) throw new IllegalStateException("No root!");
    Part<?> document = root.build();
    return document.parts();
  }

  /**
   * A mutable part so that we can assemble the tree
   */
  private static class MutablePart {

    private final Element element;

    private final List<MutablePart> parts = new ArrayList<>();

    public MutablePart(Element element) {
      this.element = element;
    }

    public void add(MutablePart part) {
      this.parts.add(part);
    }

    public Part<Element> build() {
      Part<?>[] buildParts = this.parts.stream().map(MutablePart::build).toArray(Part<?>[]::new);
      return new Part<>(this.element, buildParts);
    }

  }

}
