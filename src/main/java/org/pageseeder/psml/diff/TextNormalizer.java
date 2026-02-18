package org.pageseeder.psml.diff;

/**
 * Interface for text normalization operations.
 *
 * @since 1.7.2
 * @version 1.7.2
 */
@FunctionalInterface
public interface TextNormalizer {

  /**
   * Normalize the specified text.
   *
   * @param token The text to normalize.
   * @return The normalized text.
   */
  String normalize(String token);

}
