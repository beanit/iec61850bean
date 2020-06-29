/*
 * Copyright 2019 beanit
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
package com.beanit.iec61850bean.internal.util;

public class SequenceNumber {

  private final int maxValue;
  private final int minValue;
  private int value;

  public SequenceNumber(int initValue, int minValue, int maxValue) {
    assert (initValue >= minValue) && (initValue <= maxValue);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.value = initValue;
  }

  public static int getIncrement(int value, int minValue, int maxValue) {
    assert (value >= minValue) && (value <= maxValue);
    if (value == maxValue) {
      return minValue;
    } else {
      return ++value;
    }
  }

  public int getAndIncrement() {
    int oldValue = value;
    if (value == maxValue) {
      value = minValue;
    } else {
      ++value;
    }
    return oldValue;
  }
}
