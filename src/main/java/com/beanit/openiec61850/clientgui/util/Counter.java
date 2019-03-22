/*
 * Copyright 2011 The OpenIEC61850 Authors
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
package com.beanit.openiec61850.clientgui.util;

public class Counter {
  private int value;

  public Counter(int value) {
    this.value = value;
  }

  public Counter() {
    this(0);
  }

  public void increment() {
    value++;
  }

  public int getValue() {
    return value;
  }
}
