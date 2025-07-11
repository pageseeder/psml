package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.XMLStartElement;

import javax.xml.namespace.QName;

/**
 * A wrapper class that represents a "pseudo" start element token.
 *
 * <p>This class is used to wrap an existing {@link StartElementToken} or to generate a new
 * instance based on the provided {@link QName}.
 *
 * <p>This implementation is immutable and ensures the underlying {@link StartElementToken}
 * remains encapsulated.
 *
 * @author Christophe Lauret
 */
final class PseudoStartToken implements StartElementToken {

  private final StartElementToken token;

  public PseudoStartToken(StartElementToken token) {
    this.token = token;
  }

  /**
   * Retrieves the name of the current token.
   *
   * @return The name of the token encapsulated by this {@code PseudoStartToken}.
   */
  @Override
  public String getName() {
    return this.token.getName();
  }

  /**
   * Retrieves the namespace URI associated with this token.
   *
   * @return The namespace URI of the token encapsulated by this {@code PseudoStartToken}.
   */
  @Override
  public String getNamespaceURI() {
    return this.token.getNamespaceURI();
  }

  /**
   * Retrieves the value associated with the encapsulated token.
   *
   * @return The value of the token encapsulated by this {@code PseudoStartToken}, or {@code null} if no value is set.
   */
  @Override
  public String getValue() {
    return this.token.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof XMLToken)) return false;
    return equals((XMLToken) o);
  }

  /**
   * Compares this token with the specified {@code XMLToken} for equality.
   *
   * @param token The {@code XMLToken} to be compared.
   * @return {@code true} if the specified token is equal to this token; {@code false} otherwise.
   */
  @Override
  public boolean equals(XMLToken token) {
    return this.token.equals(token);
  }

  @Override
  public int hashCode() {
    return this.token.hashCode();
  }

  /**
   * Returns a string representation of this {@code PseudoStartToken}.
   * The returned string includes the name of the encapsulated token,
   * surrounded by angle brackets and followed by an asterisk.
   *
   * @return A string in the format "<name*>" where "name" is the name of the encapsulated token.
   */
  @Override
  public String toString() {
    return "<" + this.token.getName() + "*>";
  }
}
