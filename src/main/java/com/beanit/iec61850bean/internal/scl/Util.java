/*
 * Copyright 2011 The IEC61850bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean.internal.scl;

import com.beanit.iec61850bean.SclParseException;

public final class Util {

  static boolean parseBooleanValue(String value) throws SclParseException {
    if (value.equals("true")) {
      return true;
    } else if (value.equals("false")) {
      return false;
    } else {
      throw new SclParseException("Not a boolean value");
    }
  }

  public static boolean isBasicType(String bType) throws SclParseException {
    if (bType.equals("BOOLEAN")) {
      return true;
    }
    if (bType.equals("INT8")) {
      return true;
    }
    if (bType.equals("INT16")) {
      return true;
    }
    if (bType.equals("INT32")) {
      return true;
    }
    if (bType.equals("INT64")) {
      return true;
    }
    if (bType.equals("INT8U")) {
      return true;
    }
    if (bType.equals("INT16U")) {
      return true;
    }
    if (bType.equals("INT32U")) {
      return true;
    }
    if (bType.equals("FLOAT32")) {
      return true;
    }
    if (bType.equals("FLOAT64")) {
      return true;
    }
    if (bType.equals("Timestamp")) {
      return true;
    }
    if (bType.equals("VisString32")) {
      return true;
    }
    if (bType.equals("VisString64")) {
      return true;
    }
    if (bType.equals("VisString65")) {
      return true;
    }
    if (bType.equals("VisString129")) {
      return true;
    }
    if (bType.equals("VisString255")) {
      return true;
    }
    if (bType.equals("Unicode255")) {
      return true;
    }
    if (bType.equals("Octet64")) {
      return true;
    }
    if (bType.equals("Struct")) {
      return false;
    }
    if (bType.equals("Enum")) {
      return true;
    }
    if (bType.equals("Quality")) {
      return true;
    }
    if (bType.equals("Check")) {
      return true;
    }
    if (bType.equals("Dbpos")) {
      return true;
    }
    if (bType.equals("Tcmd")) {
      return true;
    }

    throw new SclParseException("Invalid bType: " + bType);
  }
}
