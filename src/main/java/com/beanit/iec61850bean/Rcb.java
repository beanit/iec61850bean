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

import java.util.List;

public abstract class Rcb extends FcDataObject {

  DataSet dataSet;

  protected Rcb(ObjectReference objectReference, Fc fc, List<FcModelNode> children) {
    super(objectReference, fc, children);
  }

  /**
   * Gets the RptID (Report ID). The RptID will be included in every report sent by the server. If
   * it is equal to NULL, then the RptID sent will be equal to the reference of the RCB.
   *
   * @return the report ID
   */
  public BdaVisibleString getRptId() {
    return (BdaVisibleString) children.get("RptID");
  }

  /**
   * Gets the boolean value which states whether reporting is enabled.
   *
   * @return BdaBoolean that contains true as value if reporting is enabled.
   */
  public BdaBoolean getRptEna() {
    return (BdaBoolean) children.get("RptEna");
  }

  /**
   * Gets the object reference of the DataSet that is to be monitored for reporting events.
   *
   * @return the object reference of the DataSet
   */
  public BdaVisibleString getDatSet() {
    return (BdaVisibleString) children.get("DatSet");
  }

  /**
   * Configuration revision The attribute ConfRev shall represent a count of the number of times
   * that the configuration of the DATA-SET referenced by DatSet has been changed. Changes that
   * shall be counted are:
   *
   * <ul>
   *   <li>any deletion of a member of the DATA-SET;
   *   <li>the reordering of members of the DATA-SET; and
   *   <li>Successful SetBRCBValues of the DatSet attribute where the DatSet attribute value
   *       changes.
   * </ul>
   *
   * The counter shall be incremented when the configuration changes. At configuration time, the
   * configuration tool will be responsible for incrementing/maintaining the ConfRev value. When
   * configuration changes occur due to SetBRCBValues, the IED shall be responsible for incrementing
   * the value of ConfRev.
   *
   * @return the configuration revision
   */
  public BdaInt32U getConfRev() {
    return (BdaInt32U) children.get("ConfRev");
  }

  /**
   * Gets the optional fields parameter which specifies which optional fields should be included in
   * the reports sent by this RCB.
   *
   * @return the optional fields parameter
   */
  public BdaOptFlds getOptFlds() {
    return (BdaOptFlds) children.get("OptFlds");
  }

  /**
   * Gets the buffer time - The attribute BufTm (see Figure 27) shall specify the time interval in
   * milliseconds for the buffering of internal notifications caused by data-change (dchg),
   * quality-change (qchg), data update (dupd) by the BRCB for inclusion into a single report.
   *
   * @return the buffer time
   */
  public BdaInt32U getBufTm() {
    return (BdaInt32U) children.get("BufTm");
  }

  /**
   * Gets the sequence number - The attribute SqNum shall specify the sequence number for each BRCB
   * that has report enable set to TRUE. This number is to be incremented by the BRCB for each
   * report generated and sent. The increment shall occur once the BRCB has formatted the report and
   * requested for transmission.
   *
   * @return the sequence number
   */
  public BdaInt8U getSqNum() {
    return (BdaInt8U) children.get("SqNum");
  }

  public BdaTriggerConditions getTrgOps() {
    return (BdaTriggerConditions) children.get("TrgOps");
  }

  public BdaInt32U getIntgPd() {
    return (BdaInt32U) children.get("IntgPd");
  }
}
