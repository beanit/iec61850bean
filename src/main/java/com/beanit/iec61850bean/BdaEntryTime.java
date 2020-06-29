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

import com.beanit.iec61850bean.internal.BerBoolean;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.TimeOfDay;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;

/**
 * BdaEntryTime stores time in terms of days and ms since 1984.
 *
 * @author Stefan Feuerhahn
 */
public final class BdaEntryTime extends BasicDataAttribute {

  private volatile byte[] value;

  public BdaEntryTime(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    basicType = BdaType.ENTRY_TIME;
    setDefault();
  }

  /**
   * Set the value of this object to the given timestamp, where timestamp is the number of ms since
   * epoch 1970-01-01 00:00:00 UTC. Note that timestamps before 1984 are not valid as they cannot be
   * stored.
   *
   * @param timestamp the number of ms since epoch 1970-01-01
   */
  public void setTimestamp(long timestamp) {
    long msSince1984 = timestamp - 441763200000l;
    int days = (int) (msSince1984 / 86400000);
    int ms = (int) (msSince1984 % 86400000);
    value =
        new byte[] {
          (byte) (ms >> 24),
          (byte) (ms >> 16),
          (byte) (ms >> 8),
          (byte) ms,
          (byte) (days >> 8),
          (byte) days
        };
  }

  public long getTimestampValue() {
    if (value.length != 6) {
      return -1;
    }
    return (((value[0] & 0xffl) << 24)
            + ((value[1] & 0xffl) << 16)
            + ((value[2] & 0xffl) << 8)
            + (value[3] & 0xffl)
            + (((value[4] & 0xffl) << 8) + (value[5] & 0xffl)) * 86400000l)
        + 441763200000l;
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    byte[] srcValue = ((BdaEntryTime) bda).getValue();
    if (value.length != srcValue.length) {
      value = new byte[srcValue.length];
    }
    System.arraycopy(srcValue, 0, value, 0, srcValue.length);
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  /** Sets EntryTime to byte[6] with all zeros */
  @Override
  public void setDefault() {
    value = new byte[6];
  }

  @Override
  public BdaEntryTime copy() {
    BdaEntryTime copy = new BdaEntryTime(objectReference, fc, sAddr, dchg, dupd);
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

  @Override
  Data getMmsDataObj() {
    if (value == null) {
      return null;
    }
    Data data = new Data();
    data.setBinaryTime(new TimeOfDay(value));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getBinaryTime() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: binary_time/EntryTime");
    }
    value = data.getBinaryTime().value;
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setBinaryTime(new BerBoolean(true));
    return typeDescription;
  }

  @Override
  public String toString() {
    return getReference().toString() + ": " + getTimestampValue();
  }

  @Override
  public String getValueString() {
    return "" + getTimestampValue();
  }
}
