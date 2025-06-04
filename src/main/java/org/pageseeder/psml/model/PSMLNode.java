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
package org.pageseeder.psml.model;

import org.pageseeder.xmlwriter.XMLWritable;

/**
 * All PSML nodes must implement this interface.
 *
 * <p>Implementation node: the nodes use a lazy initialization model, where
 * most class attributes are set to <code>null</code> unless they are
 * requested via a getter method or set explicitly.
 *
 * <p>This is to avoid initialising a large number of attribute maps when
 * most elements do not require attributes.
 *
 * @author Christophe Lauret
 *
 * @version 1.0
 * @since 1.0
 */
public interface PSMLNode extends XMLWritable {

  /**
   * @return the text of this node.
   */
  String getText();

}
