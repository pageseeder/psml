package org.pageseeder.psml.diff;

import org.pageseeder.diffx.token.EndElementToken;
import org.pageseeder.diffx.token.StartElementToken;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.XMLEndElement;

import javax.xml.namespace.QName;

/**
 * A wrapper class that represents a "pseudo" end element token.
 *
 * <p>This class is used to wrap an existing {@link EndElementToken} or to generate a new
 * instance based on the provided {@link QName}.
 *
 * <p>This implementation is immutable and ensures the underlying {@link EndElementToken}
 * remains encapsulated.
 *
 * @author Christophe Lauret
 */
final class PseudoEndToken implements EndElementToken {

  private final EndElementToken token;

  public PseudoEndToken(QName qName) {
    this.token = new XMLEndElement(qName.getNamespaceURI(), qName.getLocalPart());
  }

  public PseudoEndToken(EndElementToken token) {
    this.token = token;
  }

  @Override
  public String getName() {
    return token.getName();
  }

  @Override
  public String getNamespaceURI() {
    return token.getNamespaceURI();
  }

  @Override
  public String getValue() {
    return token.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof XMLToken)) return false;
    return equals((XMLToken) o);
  }

  @Override
  public boolean equals(XMLToken token) {
    return token.equals(this.token);
  }

  @Override
  public int hashCode() {
    return this.token.hashCode();
  }

  @Override
  public StartElementToken getOpenElement() {
    return new PseudoStartToken(this.token.getStartElement());
  }

  @Override
  public boolean match(StartElementToken token) {
    return this.token.match(token);
  }

  @Override
  public String toString() {
    return "</" + this.token.getName() + "*>";
  }
}
