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

import com.beanit.iec61850bean.internal.HexString;
import java.util.List;

public class Report {

  private final String rptId;
  private final Integer sqNum;
  private final Integer subSqNum;
  private final boolean moreSegmentsFollow;
  private final String dataSetRef;
  private final Boolean bufOvfl;
  private final Long confRev;
  private final BdaEntryTime timeOfEntry;
  private final BdaOctetString entryId;
  private final boolean[] inclusionBitString;
  private final List<FcModelNode> values;
  private final List<BdaReasonForInclusion> reasonCodes;

  public Report(
      String rptId,
      Integer sqNum,
      Integer subSqNum,
      boolean moreSegmentsFollow,
      String dataSetRef,
      Boolean bufOvfl,
      Long confRev,
      BdaEntryTime timeOfEntry,
      BdaOctetString entryId,
      boolean[] inclusionBitString,
      List<FcModelNode> values,
      List<BdaReasonForInclusion> reasonCodes) {
    this.rptId = rptId;
    this.sqNum = sqNum;
    this.subSqNum = subSqNum;
    this.moreSegmentsFollow = moreSegmentsFollow;
    this.dataSetRef = dataSetRef;
    this.bufOvfl = bufOvfl;
    this.confRev = confRev;
    this.timeOfEntry = timeOfEntry;
    this.entryId = entryId;
    this.inclusionBitString = inclusionBitString;
    this.values = values;
    this.reasonCodes = reasonCodes;
  }

  public String getRptId() {
    return rptId;
  }

  /**
   * Sequence numberThe parameter MoreSegmentsFollow indicates that more report segments with the
   * same sequence number follow, counted up for every {@code Report} instance generated
   *
   * @return the sequence number
   */
  public Integer getSqNum() {
    return sqNum;
  }

  /**
   * For the case of long reports that do not fit into one message, a single report shall be divided
   * into subreports. Each segment – of one report – shall be numbered with the same sequence number
   * and a unique SubSqNum.
   *
   * @return the subsequence number
   */
  public Integer getSubSqNum() {
    return subSqNum;
  }

  /**
   * The parameter MoreSegmentsFollow indicates that more report segments with the same sequence
   * number follow
   *
   * @return true if more segments follow
   */
  public boolean isMoreSegmentsFollow() {
    return moreSegmentsFollow;
  }

  public String getDataSetRef() {
    return dataSetRef;
  }

  /**
   * The parameter BufOvfl shall indicate to the client that entries within the buffer may have been
   * lost. The detection of possible loss of information occurs when a client requests a
   * resynchronization to a non-existent entry or to the first entry in the queue.
   *
   * @return true if buffer overflow is true
   */
  public Boolean getBufOvfl() {
    return bufOvfl;
  }

  public Long getConfRev() {
    return confRev;
  }

  /**
   * The parameter TimeOfEntry shall specify the time when the EntryID was created
   *
   * @return the time of entry
   */
  public BdaEntryTime getTimeOfEntry() {
    return timeOfEntry;
  }

  public BdaOctetString getEntryId() {
    return entryId;
  }

  /**
   * Indicator of data set members included in the report
   *
   * @return the inclusion bit string as a byte array
   */
  public boolean[] getInclusionBitString() {
    return inclusionBitString;
  }

  /**
   * Gets the reasons for inclusion
   *
   * @return the reasons for inclusion
   */
  public List<BdaReasonForInclusion> getReasonCodes() {
    return reasonCodes;
  }

  public List<FcModelNode> getValues() {
    return values;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Report ID: ").append(rptId);
    sb.append("\nData set reference: ").append(dataSetRef);

    if (sqNum != null) {
      sb.append("\nSequence number: ").append(sqNum);
    }
    if (subSqNum != null) {
      sb.append("\nSubsequence number: ").append(subSqNum);
      if (moreSegmentsFollow) {
        sb.append(" (more segments follow)");
      }
    }
    if (timeOfEntry != null) {
      sb.append("\nTime of entry (unix timestamp): ").append(timeOfEntry.getTimestampValue());
    }
    if (bufOvfl != null) {
      sb.append("\nBuffer overflow: ").append(bufOvfl);
    }
    if (entryId != null) {
      sb.append("\nEntry ID: ").append(HexString.fromBytes(entryId.getValue()));
    }
    if (confRev != null) {
      sb.append("\nConfiguration revision: ").append(confRev.toString());
    }
    sb.append("\nReported data set members:");
    int index = 0;
    for (FcModelNode reportedDataSetMember : values) {
      sb.append("\n").append(reportedDataSetMember.toString());
      if (reasonCodes != null) {
        sb.append(", reason: ").append(reasonCodes.get(index));
      }
      index++;
    }

    return sb.toString();
  }
}
