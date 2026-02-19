package org.pageseeder.psml.diff;

import org.jspecify.annotations.Nullable;
import org.pageseeder.diffx.token.TextToken;
import org.pageseeder.diffx.token.XMLToken;

/**
 * A text token with a normalized text version used for comparison.
 *
 * @author Christophe Lauret
 *
 * @since 1.7.2
 * @version 1.7.2
 */
public final class LexicalToken implements TextToken {

  /**
   * The original text.
   */
  private final String characters;

  /**
   * The normalized text.
   */
  private final String normalized;

  /**
   * Creates a new lexical token.
   *
   * @param text The original text.
   * @param normalized The normalized text.
   */
  public LexicalToken(String text, String normalized) {
    this.characters = text;
    this.normalized = normalized;
  }

  /**
   * Returns the original text.
   *
   * @return the original text
   */
  @Override
  public String getCharacters() {
    return this.characters;
  }

  /**
   * Returns the normalized text.
   *
   * @return the normalized text
   */
  @Override
  public String getValue() { return this.normalized; }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LexicalToken) return equals((TextToken)obj);
    if (obj instanceof XMLToken) return equals((XMLToken)obj);
    return false;
  }

  /**
   * Returns {@code true} if the specified token is equal to this token.
   *
   * @param token The {@code XMLToken} to compare with this token.
   *
   * @return {@code true} if the specified token is equal to this token; {@code false} otherwise.
   */
  @Override
  public boolean equals(@Nullable XMLToken token) {
    if (token == this) return true;
    if (token == null) return false;
    if (token instanceof LexicalToken) {
      return this.normalized.equals(((LexicalToken)token).normalized);
    }
    return super.equals(token);
  }

  @Override
  public int hashCode() {
    return this.normalized.hashCode();
  }

  @Override
  public String toString() {
    return this.characters;
  }

}
