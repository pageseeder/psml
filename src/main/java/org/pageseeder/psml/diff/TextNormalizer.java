package org.pageseeder.psml.diff;

/**
 * Interface for text normalization operations.
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
