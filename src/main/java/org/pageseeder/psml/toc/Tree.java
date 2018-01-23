package org.pageseeder.psml.toc;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.pageseeder.xmlwriter.XMLWritable;

public interface Tree extends XMLWritable, Serializable {

  /**
   * @return The URI ID of the document.
   */
  long id();

  /**
   * @return the title of the document.
   */
  String title();

  /**
   * @return an unmodifiable list of reverse references
   */
  List<Long> listReverseReferences();

  /**
   * @return an unmodifiable list of reverse references
   */
  List<Long> listForwardReferences();

  /**
   * Print a text representation of the structural element.
   *
   * @param out Where to print the structure
   *
   * @throws IOException If thrown by the appendable
   */
  void print(Appendable out);

  /**
   * @return <code>true</code> if the tree has any reverse reference.
   */
  default boolean isReferenced() {
    return !listReverseReferences().isEmpty();
  }

  /**
   * @return the list of reverse references as string
   */
  default String toReverseReferencesString(String separator) {
    return listReverseReferences().stream()
        .map(i -> i.toString())
        .collect(Collectors.joining(separator));
  }
}
