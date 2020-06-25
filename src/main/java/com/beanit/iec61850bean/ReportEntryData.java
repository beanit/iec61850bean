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

public class ReportEntryData {

  /** Not specified in IEC61850 but useful for data persistence */
  private long id;
  /** Reference to to {@link DataSet}-member */
  private String dataRef;
  /** Attribute value to be reported */
  private ModelNode value;
  /** Trigger that caused the data to be put into the report */
  // private TriggerConditions reasonCode;
  private ReasonCode reasonCode;
  /** Backreference to report */
  private Report report;

  public String getDataRef() {
    return dataRef;
  }

  public void setDataRef(String dataRef) {
    this.dataRef = dataRef;
  }

  public ModelNode getValue() {
    return value;
  }

  public void setValue(ModelNode value) {
    this.value = value;
  }

  public ReasonCode getReasonCode() {
    return reasonCode;
  }

  public void setReasonCode(ReasonCode reasonCode) {
    this.reasonCode = reasonCode;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Report getReport() {
    return report;
  }

  public void setReport(Report report) {
    this.report = report;
  }

  public enum ReasonCode {
    DCHG,
    QCHG,
    DUPD,
    INTEGRITY,
    GI,
    APPTRIGGER
  }
}
