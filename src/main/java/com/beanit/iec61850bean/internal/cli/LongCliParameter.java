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

public class LongCliParameter extends ValueCliParameter {

  Long value;
  private Long defaultValue = null;

  LongCliParameter(CliParameterBuilder builder, String parameterName, long defaultValue) {
    super(builder, parameterName);
    this.defaultValue = defaultValue;
    value = defaultValue;
  }

  LongCliParameter(CliParameterBuilder builder, String parameterName) {
    super(builder, parameterName);
  }

  public long getValue() {
    return value;
  }

  @Override
  int parse(String[] args, int i) throws CliParseException {
    selected = true;

    if (args.length < (i + 2)) {
      throw new CliParseException("Parameter " + name + " has no value.");
    }

    try {
      value = Long.decode(args[i + 1]);
    } catch (Exception e) {
      throw new CliParseException(
          "Parameter value " + args[i + 1] + " cannot be converted to long.");
    }
    return 2;
  }

  @Override
  void appendDescription(StringBuilder sb) {
    super.appendDescription(sb);
    if (defaultValue != null) {
      sb.append(" Default is ").append(defaultValue).append(".");
    }
  }
}
