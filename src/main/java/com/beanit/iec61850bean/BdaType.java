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

/**
 * This Enumeration includes all possible Types for IEC 61850 leave nodes ( {@link
 * BasicDataAttribute}). This includes BasicTypes and CommonACSITypes as defined in part 7-2.
 */
public enum BdaType {
  BOOLEAN,
  INT8,
  INT16,
  INT32,
  INT64,
  INT128,
  INT8U,
  INT16U,
  INT32U,
  FLOAT32,
  FLOAT64,
  OCTET_STRING,
  VISIBLE_STRING,
  UNICODE_STRING,
  TIMESTAMP,
  ENTRY_TIME,
  CHECK,
  QUALITY,
  DOUBLE_BIT_POS,
  TAP_COMMAND,
  TRIGGER_CONDITIONS,
  OPTFLDS,
  REASON_FOR_INCLUSION
}
