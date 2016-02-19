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
import java.util.Map;

/**
 * A PSML fragment template.
 *
 * @author Christophe Lauret
 */
public final class FragmentTemplate implements Template {

  /**
   * The charset used for this template.
   */
  private final Charset _charset;

  /**
   * The abstract fragment
   */
  private final TFragment _fragment;

  /**
   * Create a new template.
   */
  private FragmentTemplate(TFragment fragment, Charset charset) {
    this._fragment = fragment;
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
    String id = values.get("ps.fragmentid");
    String element = this._fragment.kind();
    psml.append('<').append(element);
    if (id != null) {
      psml.append(" id=\"").append(id).append('"');
    }
    psml.append(" type=\"").append(this._fragment.type()).append('"');
    if (this._fragment.mediatype() != null) {
      psml.append(" mediatype=\"").append(this._fragment.mediatype()).append('"');
    }
    psml.append('>');
    for (Token token : this._fragment.tokens()) {
      token.print(psml, values, this._charset);
    }
    psml.append("</").append(element).append('>');
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
    process(psml, values);
  }

  /**
   * Use this class to build a new template.
   */
  protected static final class Builder implements TemplateBuilder<FragmentTemplate> {

    /**
     * The charset used by the builder.
     */
    private final Charset _charset;

    /**
     * The fragment type craeted by the builder.
     */
    private final String _type;

    /**
     * The fragment.
     */
    private TFragment fragment = null;

    public Builder(Charset charset, String type) {
      this._charset = charset;
      this._type = type;
    }

    @Override
    public void addParameter(String name, String fallback, ParameterType type) {
    }

    @Override
    public void pushData(String data) {
    }

    @Override
    public void pushError(String error) {
    }

    @Override
    public void pushFragmentRef(String id, String type) {
    }

    @Override
    public void pushValue(String name, boolean attribute) {
    }

    @Override
    public void addFragment(TFragment fragment) {
      if (fragment.type().equals(this._type)) {
        this.fragment = fragment;
      }
    }

    /**
     * Adds a declared parameter
     *
     * @param name     The name of the parameter
     * @param fragment The default value for the parameter
     */
    public void setFragment(TFragment fragment) {
      this.fragment = fragment;
    }

    /**
     * Build the template.
     *
     * @return a new template instance.
     */
    @Override
    public FragmentTemplate build() {
      if (this.fragment != null)
        return new FragmentTemplate(this.fragment, this._charset);
      else return null;
    }
  }
}
