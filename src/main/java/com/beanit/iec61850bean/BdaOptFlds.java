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

public final class BdaOptFlds extends BdaBitString {

  public BdaOptFlds(ObjectReference objectReference, Fc fc) {
    super(objectReference, fc, null, 10, false, false);
    basicType = BdaType.OPTFLDS;
    setDefault();
  }

  @Override
  public void setDefault() {
    /* default of buffer overflow is true by default in IEC 61850-6 sec. 9.3.8 */
    value = new byte[] {0x02, 0x00};
  }

  @Override
  public BdaOptFlds copy() {
    BdaOptFlds copy = new BdaOptFlds(objectReference, fc);
    byte[] valueCopy = new byte[value.length];
    System.arraycopy(value, 0, valueCopy, 0, value.length);
    copy.setValue(valueCopy);
    if (mirror == null) {
      copy.mirror = this;
    } else {
      copy.mirror = mirror;
    }
    return copy;
  }

  public boolean isSequenceNumber() {
    return (value[0] & 0x40) == 0x40;
  }

  public void setSequenceNumber(boolean sequenceNumber) {
    if (sequenceNumber) {
      value[0] = (byte) (value[0] | 0x40);
    } else {
      value[0] = (byte) (value[0] & 0xbf);
    }
  }

  public boolean isReportTimestamp() {
    return (value[0] & 0x20) == 0x20;
  }

  public void setReportTimestamp(boolean reportTimestamp) {
    if (reportTimestamp) {
      value[0] = (byte) (value[0] | 0x20);
    } else {
      value[0] = (byte) (value[0] & 0x2f);
    }
  }

  public boolean isReasonForInclusion() {
    return (value[0] & 0x10) == 0x10;
  }

  public void setReasonForInclusion(boolean reasonForInclusion) {
    if (reasonForInclusion) {
      value[0] = (byte) (value[0] | 0x10);
    } else {
      value[0] = (byte) (value[0] & 0xef);
    }
  }

  /**
   * Will the data set reference (not just the name) be included in the report.
   *
   * @return true if the data set reference (not just the name) will be included in the report
   */
  public boolean isDataSetName() {
    return (value[0] & 0x08) == 0x08;
  }

  public void setDataSetName(boolean dataSetName) {
    if (dataSetName) {
      value[0] = (byte) (value[0] | 0x08);
    } else {
      value[0] = (byte) (value[0] & 0xf7);
    }
  }

  public boolean isDataReference() {
    return (value[0] & 0x04) == 0x04;
  }

  public void setDataReference(boolean dataReference) {
    if (dataReference) {
      value[0] = (byte) (value[0] | 0x04);
    } else {
      value[0] = (byte) (value[0] & 0xfb);
    }
  }

  public boolean isBufferOverflow() {
    return (value[0] & 0x02) == 0x02;
  }

  public void setBufferOverflow(boolean bufferOverflow) {
    if (bufferOverflow) {
      value[0] = (byte) (value[0] | 0x02);
    } else {
      value[0] = (byte) (value[0] & 0xfd);
    }
  }

  public boolean isEntryId() {
    return (value[0] & 0x01) == 0x01;
  }

  public void setEntryId(boolean entryId) {
    if (entryId) {
      value[0] = (byte) (value[0] | 0x01);
    } else {
      value[0] = (byte) (value[0] & 0xfe);
    }
  }

  public boolean isConfigRevision() {
    return (value[1] & 0x80) == 0x80;
  }

  public void setConfigRevision(boolean configRevision) {
    if (configRevision) {
      value[1] = (byte) (value[1] | 0x80);
    } else {
      value[1] = (byte) (value[1] & 0x7f);
    }
  }

  public boolean isSegmentation() {
    return (value[1] & 0x40) == 0x40;
  }

  public void setSegmentation(boolean segmentation) {
    if (segmentation) {
      value[1] = (byte) (value[1] | 0x40);
    } else {
      value[1] = (byte) (value[1] & 0xbf);
    }
  }
}
