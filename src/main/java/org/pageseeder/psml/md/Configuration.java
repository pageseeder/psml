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
package org.pageseeder.psml.md;

public class Configuration {

  private int lineBreakThreshold = 66;


  /**
   * Indicates whether the parser should generate a document or a fragment.
   */
  private boolean isDocumentMode = false;


  public Configuration() {
  }

  public void setFragmentMode(boolean isFragmentMode) {
    this.isDocumentMode = !isFragmentMode;
  }

  public boolean isFragment() {
    return !this.isDocumentMode;
  }

  public boolean isDocumentMode() {
    return this.isDocumentMode;
  }

  public void setLineBreakThreshold(int lineBreakThreshold) {
    this.lineBreakThreshold = lineBreakThreshold;
  }

  public int getLineBreakThreshold() {
    return this.lineBreakThreshold;
  }

}
