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

import com.beanit.asn1bean.ber.types.BerNull;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.UtcTime;
import java.time.Instant;
import java.util.Date;

public final class BdaTimestamp extends BasicDataAttribute {

  private volatile byte[] value;

  public BdaTimestamp(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    basicType = BdaType.TIMESTAMP;
    setDefault();
  }

  /**
   * The SecondSinceEpoch shall be the interval in seconds continuously counted from the epoch
   * 1970-01-01 00:00:00 UTC
   */

  /**
   * Returns the value as the number of seconds since epoch 1970-01-01 00:00:00 UTC
   *
   * @return the number of seconds since epoch 1970-01-01 00:00:00 UTC
   */
  private long getSecondsSinceEpoch() {
    return ((0xffL & value[0]) << 24
        | (0xffL & value[1]) << 16
        | (0xffL & value[2]) << 8
        | (0xffL & value[3]));
  }

  /**
   * The attribute FractionOfSecond shall be the fraction of the current second when the value of
   * the TimeStamp has been determined. The fraction of second shall be calculated as <code>
   * (SUM from I = 0 to 23 of bi*2**–(I+1) s).</code> NOTE 1 The resolution is the smallest unit by
   * which the time stamp is updated. The 24 bits of the integer provides 1 out of 16777216 counts
   * as the smallest unit; calculated by 1/2**24 which equals approximately 60 ns.
   *
   * <p>NOTE 2 The resolution of a time stamp may be 1/2**1 (= 0,5 s) if only the first bit is used;
   * or may be 1/2**2 (= 0,25 s) if the first two bits are used; or may be approximately 60 ns if
   * all 24 bits are used. The resolution provided by an IED is outside the scope of this standard.
   *
   * @return the fraction of seconds
   */
  private int getFractionOfSecond() {
    return ((0xff & value[4]) << 16 | (0xff & value[5]) << 8 | (0xff & value[6]));
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    byte[] srcValue = ((BdaTimestamp) bda).getValue();
    if (value.length != srcValue.length) {
      value = new byte[srcValue.length];
    }
    System.arraycopy(srcValue, 0, value, 0, srcValue.length);
  }

  public Instant getInstant() {
    if (value == null || value.length == 0) {
      return null;
    }
    long time =
        getSecondsSinceEpoch() * 1000L
            + (long) (((float) getFractionOfSecond()) / (1 << 24) * 1000 + 0.5);
    return Instant.ofEpochMilli(time);
  }

  public void setInstant(Instant instant) {
    setInstant(instant, true, false, false, 10);
  }

  public void setInstant(
      Instant instant,
      boolean leapSecondsKnown,
      boolean clockFailure,
      boolean clockNotSynchronized,
      int timeAccuracy) {
    if (value == null) {
      value = new byte[8];
    }

    int secondsSinceEpoch = (int) (instant.toEpochMilli() / 1000L);
    int fractionOfSecond = (int) ((instant.toEpochMilli() % 1000L) / 1000.0 * (1 << 24));

    int timeQuality = timeAccuracy & 0x1f;
    if (leapSecondsKnown) {
      timeQuality = timeQuality | 0x80;
    }
    if (clockFailure) {
      timeQuality = timeQuality | 0x40;
    }
    if (clockNotSynchronized) {
      timeQuality = timeQuality | 0x20;
    }

    value =
        new byte[] {
          (byte) ((secondsSinceEpoch >> 24) & 0xff),
          (byte) ((secondsSinceEpoch >> 16) & 0xff),
          (byte) ((secondsSinceEpoch >> 8) & 0xff),
          (byte) (secondsSinceEpoch & 0xff),
          (byte) ((fractionOfSecond >> 16) & 0xff),
          (byte) ((fractionOfSecond >> 8) & 0xff),
          (byte) (fractionOfSecond & 0xff),
          (byte) timeQuality
        };
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    if (value == null) {
      this.value = new byte[8];
    }
    this.value = value;
  }

  /**
   * The value TRUE of the attribute LeapSecondsKnown shall indicate that the value for
   * SecondSinceEpoch takes into account all leap seconds occurred. If it is FALSE then the value
   * does not take into account the leap seconds that occurred before the initialization of the time
   * source of the device.
   *
   * @return TRUE of the attribute LeapSecondsKnown shall indicate that the value for
   *     SecondSinceEpoch takes into account all leap seconds occurred
   */
  public boolean getLeapSecondsKnown() {
    return ((value[7] & 0x80) != 0);
  }

  /**
   * The attribute clockFailure shall indicate that the time source of the sending device is
   * unreliable. The value of the TimeStamp shall be ignored.
   *
   * @return true if the time source of the sending device is unreliable
   */
  public boolean getClockFailure() {
    return ((value[7] & 0x40) != 0);
  }

  /**
   * The attribute clockNotSynchronized shall indicate that the time source of the sending device is
   * not synchronized with the external UTC time.
   *
   * @return true if the time source of the sending device is not synchronized
   */
  public boolean getClockNotSynchronized() {
    return ((value[7] & 0x20) != 0);
  }

  /**
   * The attribute TimeAccuracy shall represent the time accuracy class of the time source of the
   * sending device relative to the external UTC time. The timeAccuracy classes shall represent the
   * number of significant bits in the FractionOfSecond
   *
   * <p>If the time is set via Java {@link Date} objects, the accuracy is 1 ms, that is a
   * timeAccuracy value of 10.
   *
   * @return the time accuracy
   */
  public int getTimeAccuracy() {
    return ((value[7] & 0x1f));
  }

  /** Sets Timestamp the empty byte array (indicating an invalid Timestamp) */
  @Override
  public void setDefault() {
    value = new byte[8];
  }

  /** Sets Timestamp to current time */
  public void setCurrentTime() {
    setInstant(Instant.now());
  }

  @Override
  public BdaTimestamp copy() {
    BdaTimestamp copy = new BdaTimestamp(objectReference, fc, sAddr, dchg, dupd);
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
    Data data = new Data();
    data.setUtcTime(new UtcTime(value));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getUtcTime() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: utc_time/timestamp");
    }
    value = data.getUtcTime().value;
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setUtcTime(new BerNull());
    return typeDescription;
  }

  @Override
  public String toString() {
    return getReference().toString() + ": " + getInstant();
  }

  @Override
  public String getValueString() {
    return getInstant().toString();
  }
}
