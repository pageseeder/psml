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

import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/**
 * A PSML fragment definition.
 *
 * @author Christophe Lauret
 */
final class TFragment {

  /**
   * The kind of fragment (fragment, media-fragment, xref-fragment, properties-fragment, etc...)
   */
  private final String kind;

  /**
   * The type of fragment.
   */
  private final String type;

  /**
   * The mediatype of fragment (for 'media-fragment' only)
   */
  private final @Nullable String mediatype;

  /**
   * The list of tokens this fragment is made of (does NOT include the fragment element).
   */
  private final List<Token> tokens;

  /**
   * Create a new template.
   *
   * @param builder the tokens this template is made of.
   */
  private TFragment(Builder builder) {
    assert builder.kind != null;
    this.kind = builder.kind;
    this.type = builder.type;
    this.mediatype = builder.mediatype;
    this.tokens = new ArrayList<>(builder.tokens);
  }

  /**
   * @return The kind of fragment (fragment, media-fragment, xref-fragment, properties-fragment, etc...)
   */
  public String kind() {
    return this.kind;
  }

  /**
   * @return The type of fragment.
   */
  public String type() {
    return this.type;
  }

  /**
   * @return The type of fragment.
   */
  public @Nullable String mediatype() {
    return this.mediatype;
  }

  /**
   * @return The list of tokens this fragment is made of.
   */
  public List<Token> tokens() {
    return this.tokens;
  }

  /**
   * Use this class to build a new fragment.
   */
  protected static final class Builder {

    /**
     * The fragment type.
     */
    private final String type;

    /**
     * The kind of fragment.
     */
    private @Nullable String kind;

    /**
     * The mediatype of the fragment.
     */
    private @Nullable String mediatype;

    /**
     * The types for each parameter.
     */
    private final Map<String, ParameterType> types = new HashMap<>();

    /**
     * The default values.
     */
    private final Map<String, String> defaults = new HashMap<>();

    /**
     * The list of tokens this template is made of.
     */
    private final List<Token> tokens = new ArrayList<>();

    public Builder(String type) {
       this.type = Objects.requireNonNull(type, "Fragment type is required");
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
    public void setMediatype(@Nullable String mediatype) {
      this.mediatype = mediatype;
    }

    /**
     * Adds a declared parameter
     *
     * @param name     The name of the parameter
     * @param fallback The default value for the parameter
     * @param type     The type of parameter.
     */
    public void addParameter(String name, @Nullable String fallback, ParameterType type) {
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
      ParameterType type = this.types.getOrDefault(name, ParameterType.TEXT);
      String fallback = this.defaults.getOrDefault(name, "");
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
