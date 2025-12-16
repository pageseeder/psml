/*
 * Copyright 2025 Allette Systems (Australia)
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The possible types of parameters that can be used to generate a template.
 *
 * @author Christophe Lauret
 *
 * @version 1.6.9
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
      if (value.length() != 10 || value.charAt(4) != '-' || value.charAt(7) != '-') return false;
      try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        return true;
      } catch (DateTimeParseException ex) {
        return false;
      }
    }

  },

  /**
   * An ISO datetime
   */
  DATETIME {

    @Override
    public boolean matches(String value) {
      if (value.length() < 19 || value.charAt(10) != 'T') return false;
      try {
        if (hasOffsetOrZulu(value)) {
          OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
          LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return true;
      } catch (DateTimeParseException ex) {
        return false;
      }
    }

  },

  /**
   * An ISO time matching as "[hh]:[mm]:[ss][.ms?][timezone?]".
   */
  TIME {

    @Override
    public boolean matches(String value) {
      if (value.length() < 8) return false;
      if (value.charAt(2) != ':' || value.charAt(5) != ':') return false;
      try {
        if (hasOffsetOrZulu(value)) {
          OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME);
        } else {
          LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
        }
        return true;
      } catch (DateTimeParseException ex) {
        return false;
      }
    }

  },

  /**
   * An XML fragment.
   */
  XML {

    @Override
    public boolean matches(String value) {
      return org.pageseeder.psml.xml.XML.isWellFormedFragment(value);
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

  private static boolean hasOffsetOrZulu(String value) {
    // ISO offset time examples:
    // 10:15:30Z
    // 10:15:30+10:00
    // 10:15:30.123-05:00
    int z = value.indexOf('Z');
    if (z != -1) return true;

    // Look for + or - after the basic "HH:mm:ss" part.
    for (int i = 8; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '+' || c == '-') return true;
    }
    return false;
  }
}
