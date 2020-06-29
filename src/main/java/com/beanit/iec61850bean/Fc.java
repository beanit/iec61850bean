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
package com.beanit.iec61850bean;

public enum Fc {

  // The following FCs are not part of this enum because they are not really
  // FCs and only defined in part 8-1:
  // RP (report), LG (log), BR (buffered report), GO, GS, MS, US

  // FCs according to IEC 61850-7-2:
  /** Status information */
  ST,
  /** Measurands - analogue values */
  MX,
  /** Setpoint */
  SP,
  /** Substitution */
  SV,
  /** Configuration */
  CF,
  /** Description */
  DC,
  /** Setting group */
  SG,
  /** Setting group editable */
  SE,
  /** Service response / Service tracking */
  SR,
  /** Operate received */
  OR,
  /** Blocking */
  BL,
  /** Extended definition */
  EX,
  /** Control, deprecated but kept here for backward compatibility */
  CO,
  /** Unbuffered Reporting */
  RP,
  /** Buffered Reporting */
  BR;

  public static Fc fromString(String fc) {
    try {
      return Fc.valueOf(fc);
    } catch (Exception e) {
      return null;
    }
  }
}
