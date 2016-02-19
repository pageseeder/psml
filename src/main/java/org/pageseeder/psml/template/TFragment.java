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
package org.pageseeder.psml.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A PSML fragment definition.
 *
 * @author Christophe Lauret
 */
final class TFragment {

  /**
   * The kind of fragment (fragment, media-fragment, xref-fragment, properties-fragment, etc...)
   */
  private final String _kind;

  /**
   * The type of fragment.
   */
  private final String _type;

  /**
   * The mediatype of fragment (for 'media-fragment' only)
   */
  private final String _mediatype;

  /**
   * The list of tokens this fragment is made of (does NOT include the fragment element).
   */
  private final List<Token> _tokens;

  /**
   * Create a new template.
   *
   * @param tokens the tokens this template is made of.
   */
  private TFragment(Builder builder) {
    this._kind = builder.kind;
    this._type = builder._type;
    this._mediatype = builder.mediatype;
    this._tokens = new ArrayList<Token>(builder.tokens);
  }

  /**
   * @return The kind of fragment (fragment, media-fragment, xref-fragment, properties-fragment, etc...)
   */
  public String kind() {
    return this._kind;
  }

  /**
   * @return The type of fragment.
   */
  public String type() {
    return this._type;
  }

  /**
   * @return The type of fragment.
   */
  public String mediatype() {
    return this._mediatype;
  }

  /**
   * @return The list of tokens this fragment is made of.
   */
  public List<Token> tokens() {
    return this._tokens;
  }

  /**
   * Use this class to build a new fragment.
   */
  protected static final class Builder {

    /**
     * The fragment type.
     */
    private final String _type;

    /**
     * The kind of fragment.
     */
    private String kind;

    /**
     * The mediatype of the fragment.
     */
    private String mediatype;

    /**
     * The types for each parameter.
     */
    private final Map<String, ParameterType> types = new HashMap<String, ParameterType>();

    /**
     * The default values.
     */
    private final Map<String, String> defaults = new HashMap<String, String>();

    /**
     * The list of tokens this template is made of.
     */
    private List<Token> tokens = new ArrayList<Token>();

    public Builder(String type) {
      if (type == null) throw new NullPointerException("Fragment type is required");
      this._type = type;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(String kind) {
      this.kind = kind;
    }

    /**
     * @param mediatype the mediatype to set
     */
    public void setMediatype(String mediatype) {
      this.mediatype = mediatype;
    }

    /**
     * Adds a declared parameter
     *
     * @param name     The name of the parameter
     * @param fallback The default value for the parameter
     * @param type     The type of parameter.
     */
    public void addParameter(String name, String fallback, ParameterType type) {
      this.types.put(name, type);
      if (fallback != null) {
        this.defaults.put(name, fallback);
      }
    }

    /**
     * A template value.
     *
     * @param name The name of the parameter to use.
     */
    public void pushValue(String name, boolean attribute) {
      ParameterType type = this.types.get(name);
      if (type == null) {
        type = ParameterType.TEXT;
      }
      String fallback = this.defaults.get(name);
      if (fallback == null) {
        fallback = "";
      }
      this.tokens.add(new TValue(name, fallback, type, attribute));
    }

    /**
     * Data to write out verbatim
     *
     * @param data The data to write.
     */
    public void pushData(String data) {
      this.tokens.add(new TData(data));
    }

    /**
     * Build the template.
     *
     * @return a new template instance.
     */
    public TFragment build() {
      if (this.kind == null) {
        this.kind = "fragment";
        this.tokens.add(new TError("fragment definition does not include fragment"));
      }
      return new TFragment(this);
    }
  }
}
