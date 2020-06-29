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

abstract class ValueCliParameter extends CliParameter {

  final String parameterName;

  ValueCliParameter(CliParameterBuilder builder, String parameterName) {
    super(builder);
    this.parameterName = parameterName;
  }

  @Override
  int appendSynopsis(StringBuilder sb) {
    int length = 0;
    if (optional) {
      sb.append("[");
      length++;
    }
    sb.append(name).append(" <").append(parameterName).append(">");
    length += (name.length() + 3 + parameterName.length());
    if (optional) {
      sb.append("]");
      length++;
    }
    return length;
  }

  @Override
  void appendDescription(StringBuilder sb) {
    sb.append("\t")
        .append(name)
        .append(" <")
        .append(parameterName)
        .append(">\n\t    ")
        .append(description);
  }
}
