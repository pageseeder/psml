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

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A PSML template.
 *
 * @author Christophe Lauret
 */
public final class DocumentTemplate implements Template {

  /**
   * The charset used for this template.
   */
  private final Charset _charset;

  /**
   * The list of tokens this template is made of.
   */
  private final List<Token> _tokens;

  /**
   * The list of fragments this template is made of.
   */
  private final Map<String, TFragment> _fragments;

  /**
   * Create a new template.
   *
   * @param tokens    the tokens this template is made of.
   * @param fragments the fragments in this template
   * @param charset   the charset
   */
  private DocumentTemplate(List<Token> tokens, Map<String, TFragment> fragments, Charset charset) {
    this._tokens = new ArrayList<Token>(tokens);
    this._fragments = new HashMap<String, TFragment>(fragments);
    this._charset = charset;
  }

  /**
   * The charset use by this template.
   *
   * @return the charset used by this template.
   */
  @Override
  public Charset charset() {
    return this._charset;
  }

  /**
   * Generate a new PSML document by filling out the values in the template using the charset used by the template.
   *
   * @param psml   The PSML template
   * @param values The values to use for the place holders
   */
  @Override
  public void process(PrintWriter psml, Map<String, String> values) {
    for (Token token : this._tokens) {
      token.print(psml, values, this._charset);
    }
    psml.flush();
  }

  /**
   * Generate a new PSML document by filling out the values in the template using the charset used by the template.
   *
   * @param psml   The PSML template
   * @param values The values to use for the place holders
   */
  @Override
  public void process(PrintWriter psml, Map<String, String> values, boolean failOnError) throws TemplateException {
    // If fail on error scan for any error reported during parsing
    if (failOnError) {
      for (Token token : this._tokens) {
        if (token instanceof TError) throw new TemplateException(((TError)token).message());
      }
    }
    process(psml, values);
  }

  /**
   * @return the fragment types from the template
   */
  public Collection<String> getFragments() {
    return new ArrayList<String>(this._fragments.keySet());
  }

  /**
   * Build a template for the fragment name provided.
   * 
   * @param type the fragment type
   * 
   * @return the fragment template
   */
  public FragmentTemplate getFragmentTemplate(String type) {
    TFragment frag = this._fragments.get(type);
    if (frag == null) return null;
    FragmentTemplate.Builder builder = new FragmentTemplate.Builder(this._charset, frag.type());
    builder.setFragment(frag);
    return builder.build();
  }
  
  /**
   * Use this class to build a new template.
   */
  protected static final class Builder implements TemplateBuilder<DocumentTemplate> {

    /**
     * The charset used by the builder.
     */
    private final Charset _charset;

    /**
     * The types for each parameter.
     */
    private final Map<String, ParameterType> _types = new HashMap<String, ParameterType>();

    /**
     * The types for each parameter.
     */
    private final Map<String, TFragment> _fragments = new HashMap<String, TFragment>();

    /**
     * The default values.
     */
    private final Map<String, String> _defaults = new HashMap<String, String>();

    /**
     * The list of tokens this template is made of.
     */
    private final List<Token> tokens = new ArrayList<Token>();

    public Builder(Charset charset) {
      this._charset = charset;
    }

    /**
     * Adds a declared parameter
     *
     * @param name     The name of the parameter
     * @param fragment The default value for the parameter
     */
    @Override
    public void addFragment(TFragment fragment) {
      this._fragments.put(fragment.type(), fragment);
    }

    /**
     * Adds a declared parameter
     *
     * @param name     The name of the parameter
     * @param fallback The default value for the parameter
     * @param type     The type of parameter.
     */
    @Override
    public void addParameter(String name, String fallback, ParameterType type) {
      this._types.put(name, type);
      if (fallback != null) {
        this._defaults.put(name, fallback);
      }
    }

    /**
     * A template value.
     *
     * @param name The name of the parameter to use.
     */
    @Override
    public void pushValue(String name, boolean attribute) {
      ParameterType type = this._types.get(name);
      if (type == null) {
        type = ParameterType.TEXT;
      }
      String fallback = this._defaults.get(name);
      if (fallback == null) {
        fallback = "";
      }
      this.tokens.add(new TValue(name, fallback, type, attribute));
    }

    /**
     * A error reported by the parser
     *
     * @param name The name of the parameter to use.
     */
    @Override
    public void pushError(String error) {
      this.tokens.add(new TError(error));
    }

    /**
     * A reference to a fragment.
     *
     * @param id   ID of the fragment
     * @param type type of the fragment
     */
    @Override
    public void pushFragmentRef(String id, String type) {
      if (id == null || type == null) {
        this.tokens.add(new TError("Fragment reference must specify both id and type"));
      } else {
        TFragment fragment = this._fragments.get(type);
        if (fragment != null) {
          this.tokens.add(new TFragmentRef(id, fragment));
        } else {
          this.tokens.add(new TError("Unknown fragment type '"+type+"'"));
        }
      }
    }

    /**
     * Data to write out verbatim
     *
     * @param data The data to write.
     */
    @Override
    public void pushData(String data) {
      this.tokens.add(new TData(data));
    }

    /**
     * Build the template.
     *
     * @return a new template instance.
     */
    @Override
    public DocumentTemplate build() {
      return new DocumentTemplate(this.tokens, this._fragments, this._charset);
    }
  }
}
