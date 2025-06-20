package org.pageseeder.psml.toc;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.pageseeder.xmlwriter.XMLWritable;

/**
 * This interface represents a Tree structure. It provides methods to access
 * metadata such as the document's ID and title, and methods to retrieve
 * and manipulate references within the tree.
 *
 * <p>It extends XMLWritable for XML-based serialization and Serializable
 * for general object serialization.
 *
 * @author Christophe Lauret
 * @author Philip Rutherford
 *
 * @version 1.0
 * @since 1.0
 */
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
   */
  void print(Appendable out);

  /**
   * @return <code>true</code> if the tree has any reverse reference.
   */
  default boolean isReferenced() {
    return !listReverseReferences().isEmpty();
  }

  /**
   * @param separator The string used to separate each reference
   * @return the list of reverse references as string
   */
  default String toReverseReferencesString(String separator) {
    return listReverseReferences().stream()
        .map(Object::toString)
        .collect(Collectors.joining(separator));
  }
}
