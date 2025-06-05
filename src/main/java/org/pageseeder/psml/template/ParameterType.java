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

/**
 * The possible types of parameters that can be used to generate a template.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.0
 * @since 1.0
 */
public enum ParameterType {

  /**
   * An integer that can be parsed as a Java integer.
   */
  INTEGER {

    @Override
    public boolean matches(String value) {
      try {
        Integer.parseInt(value);
        return true;
      } catch (NumberFormatException ex) {
        return false;
      }
    }

  },

  /**
   * A text value - no restriction on value.
   */
  TEXT {

    @Override
    public boolean matches(String value) {
      return true;
    }

  },

  /**
   * An ISO date matching "[YYYY]-[MM]-[DD]".
   */
  DATE {

    @Override
    public boolean matches(String value) {
      return value.matches("^([0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])$");
    }

  },

  /**
   * An ISO datetime
   */
  DATETIME {

    @Override
    public boolean matches(String value) {
      // TODO
      return true;
    }

  },

  /**
   * An ISO time matching as "[hh]:[mm]:[ss][.ms?][timezone?]".
   */
  TIME {

    @Override
    public boolean matches(String value) {
      return value.matches("^(2[0-3]|[0-1][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)??(Z|[+-](?:2[0-3]|[0-1][0-9]):[0-5][0-9])?$");
    }

  },

  /**
   * An XML fragment.
   */
  XML {

    @Override
    public boolean matches(String value) {
      // TODO
      return true;
    }

  };

  /**
   * Indicates whether the specified value matches the type.
   *
   * @param value the value to check.
   * @return <code>true</code> if it matches the requirements of the type;
   *         <code>false</code> otherwise.
   *
   * @throws NullPointerException May be thrown if the value is <code>null</code>.
   */
  public abstract boolean matches(String value);

  /**
   *
   * @param name the name of the type
   * @return The parameter type for the specified name.
   */
  public static ParameterType forName(String name) {
    for (ParameterType type : values())  {
      if (type.name().equalsIgnoreCase(name)) return type;
    }
    return TEXT;
  }
}
