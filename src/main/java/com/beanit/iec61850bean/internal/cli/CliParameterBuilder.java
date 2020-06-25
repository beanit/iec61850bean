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

import java.util.List;

public class CliParameterBuilder {

  final String name;
  String description;
  boolean optional = true;

  public CliParameterBuilder(String name) {
    this.name = name;
  }

  public CliParameterBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public CliParameterBuilder setMandatory() {
    optional = false;
    return this;
  }

  public LongCliParameter buildLongParameter(String parameterName, long defaultValue) {
    return new LongCliParameter(this, parameterName, defaultValue);
  }

  public LongCliParameter buildLongParameter(String parameterName) {
    return new LongCliParameter(this, parameterName);
  }

  public IntCliParameter buildIntParameter(String parameterName, int defaultValue) {
    return new IntCliParameter(this, parameterName, defaultValue);
  }

  public IntCliParameter buildIntParameter(String parameterName) {
    return new IntCliParameter(this, parameterName);
  }

  public StringCliParameter buildStringParameter(String parameterName, String defaultValue) {
    return new StringCliParameter(this, parameterName, defaultValue);
  }

  public StringCliParameter buildStringParameter(String parameterName) {
    return new StringCliParameter(this, parameterName);
  }

  public StringListCliParameter buildStringListParameter(String parameterName) {
    return new StringListCliParameter(this, parameterName);
  }

  public StringListCliParameter buildStringListParameter(
      String parameterName, List<String> defaultValue) {
    return new StringListCliParameter(this, parameterName, defaultValue);
  }

  public FlagCliParameter buildFlagParameter() {
    return new FlagCliParameter(this);
  }
}
