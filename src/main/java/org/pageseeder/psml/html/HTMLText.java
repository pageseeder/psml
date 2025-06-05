/*
 * Copyright 2016 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.html;

import java.io.IOException;
import java.util.Objects;

import org.pageseeder.xmlwriter.XMLWriter;

/**
 * A simple text node.
 *
 * <p>Node: unlike DOM, there is no guarantee that consecutive text nodes will
 * be coalesced into a single node.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public class HTMLText implements HTMLNode {

  /**
   * Underlying text for the node.
   */
  private String text;

  /**
   * Create a new empty text node.
   */
  public HTMLText() {
    this.text = "";
  }

  /**
   * Create a new text node with the specified text.
   *
   * @param text The text to initialise this text node with.
   */
  public HTMLText(String text) {
    this.text = Objects.requireNonNull(text);
  }

  /**
   * Sets the text for this text node.
   *
   * @param text The text to initialise this text node with.
   */
  public void setText(String text) {
    this.text = Objects.requireNonNull(text);
  }

  /**
   * @return the text of this node.
   */
  public String getText() {
    return this.text;
  }

  /**
   * Does nothing if this text node does not include any text.
   */
  @Override
  public void toXML(XMLWriter xml) throws IOException {
    if (!this.text.isEmpty()) {
      xml.writeText(this.text);
    }
  }

}
