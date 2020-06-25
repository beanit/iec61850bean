/*
 * Copyright 2018 beanit
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
package com.beanit.iec61850bean.internal.cli;

import java.util.ArrayList;
import java.util.List;

public class StringListCliParameter extends ValueCliParameter {

  List<String> value;
  private List<String> defaultValue = null;

  StringListCliParameter(
      CliParameterBuilder builder, String parameterName, List<String> defaultValue) {
    super(builder, parameterName);
    this.defaultValue = defaultValue;
    value = defaultValue;
  }

  StringListCliParameter(CliParameterBuilder builder, String parameterName) {
    super(builder, parameterName);
  }

  public List<String> getValue() {
    return value;
  }

  @Override
  int parse(String[] args, int i) throws CliParseException {
    selected = true;

    value = new ArrayList<>();
    i++;
    while (i < args.length) {
      if (args[i].length() > 0 && args[i].startsWith("-")) {
        break;
      }
      value.add(args[i++]);
    }

    if (value.size() == 0) {
      throw new CliParseException("Parameter " + name + " has no value.");
    }

    return value.size() + 1;
  }

  @Override
  int appendSynopsis(StringBuilder sb) {
    int length = 0;
    if (optional) {
      sb.append("[");
      length++;
    }
    sb.append(name).append(" <").append(parameterName).append(">...");
    length += (name.length() + 6 + parameterName.length());
    if (optional) {
      sb.append("]");
      length++;
    }
    return length;
  }

  @Override
  public void appendDescription(StringBuilder sb) {
    sb.append("\t")
        .append(name)
        .append(" <")
        .append(parameterName)
        .append(">...\n\t    ")
        .append(description);
    if (defaultValue != null) {
      sb.append(" Default is \"");
      for (String stringValue : defaultValue) {
        sb.append(stringValue);
      }
      sb.append("\".");
    }
  }
}
