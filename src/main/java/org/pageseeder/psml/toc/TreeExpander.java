package org.pageseeder.psml.toc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

public final class TreeExpander {

  /**
   * Stack of structural elements so that we can return to the previous level when a branch has been processed.
   */
  private final Deque<MutablePart> _parts = new ArrayDeque<>();

  public TreeExpander(String title) {
    this._parts.push(new MutablePart(new DocumentTitle(title)));
  }

  public TreeExpander() {
    this._parts.push(new MutablePart(DocumentTitle.UNTITLED));
  }

  /**
   * Add a new paragraph to the list (it should not affect heading structure)
   *
   * @param element The new element to add
   */
  public TreeExpander addParagraph(Paragraph element) {
    MutablePart current = this._parts.peek();
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
   */
  public TreeExpander add(Element element) {
    return add(element, element.level());
  }

  /**
   * Add a new element to the list.
   *
   * @param element      The new element to add
   * @param elementlevel The level for the element
   */
  public TreeExpander add(Element element, int elementlevel) {
    // level = size(parts) = level(current) + 1;
    int level = this._parts.size();
    MutablePart current = this._parts.peek();
    if (current == null) {
      current = new MutablePart(DocumentTitle.UNTITLED);
      this._parts.push(current);
    }

    // Insert phantom parts when we jumps levels (e.g. h1 -> h4)
    while (elementlevel > level) {
      Element phantom = Phantom.of(level++);
      MutablePart mutable = new MutablePart(phantom);
      current.add(mutable);
      this._parts.push(mutable);
      current = mutable;
    }

    // Remove other parts of deeper levels (e.g. h4 -> h1)
    while (elementlevel < level) {
      this._parts.pop();
      current = this._parts.peek();
      level--;
    }

    // Add the element to the current part
    MutablePart mutable = new MutablePart(element);
    if (current != null) {
      current.add(mutable);
    }
    this._parts.push(mutable);
    return this;
  }

  public List<Part<?>> parts() {
    MutablePart root = this._parts.peekLast();
    if (root == null) throw new IllegalStateException("No root!");
    Part<?> document = root.build();
    return document.parts();
  }

  /**
   * A mutable part so that we can assemble the tree
   */
  private static class MutablePart {

    private final Element _element;

    private final List<MutablePart> _parts = new ArrayList<>();

    public MutablePart(Element element) {
      this._element = element;
    }

    public void add(MutablePart part) {
      this._parts.add(part);
    }

    public Part<?> build() {
      @NonNull Part<?>[] parts = this._parts.stream().map(p -> p.build()).toArray(Part<?>[]::new);
      return new Part<>(this._element, parts);
    }

  }

}
