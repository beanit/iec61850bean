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

import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.FloatingPoint;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned8;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class BdaFloat64 extends BasicDataAttribute {

  private volatile byte[] value = new byte[] {11, 0, 0, 0, 0, 0, 0, 0, 0};

  public BdaFloat64(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    basicType = BdaType.FLOAT64;
    setDefault();
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    byte[] srcValue = ((BdaFloat64) bda).getValue();
    if (value.length != srcValue.length) {
      value = new byte[srcValue.length];
    }
    System.arraycopy(srcValue, 0, value, 0, srcValue.length);
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    if (value != null && value.length != 9) {
      throw new IllegalArgumentException("value does not have length 9");
    }
    this.value = value;
  }

  public Double getDouble() {
    if (value == null) {
      return null;
    }
    return Double.longBitsToDouble(
        ((0xffL & value[1]) << 56)
            | ((0xffL & value[2]) << 48)
            | ((0xffL & value[3]) << 40)
            | ((0xffL & value[4]) << 32)
            | ((0xffL & value[5]) << 24)
            | ((0xffL & value[6]) << 16)
            | ((0xffL & value[7]) << 8)
            | ((0xffL & value[8]) << 0));
  }

  public void setDouble(Double value) {
    this.value = ByteBuffer.allocate(1 + 8).put((byte) 11).putDouble(value).array();
  }

  @Override
  public void setDefault() {
    value = new byte[] {11, 0, 0, 0, 0, 0, 0, 0, 0};
  }

  @Override
  public BdaFloat64 copy() {
    BdaFloat64 copy = new BdaFloat64(objectReference, fc, sAddr, dchg, dupd);
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
    data.setFloatingPoint(new FloatingPoint(value));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getFloatingPoint() == null || data.getFloatingPoint().value.length != 9) {
      throw new ServiceError(
          ServiceError.TYPE_CONFLICT, "expected type: floating_point as an octet string of size 9");
    }
    value = data.getFloatingPoint().value;
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription.FloatingPoint floatingPointTypeDescription =
        new TypeDescription.FloatingPoint();
    floatingPointTypeDescription.setFormatWidth(new Unsigned8(64));
    floatingPointTypeDescription.setExponentWidth(new Unsigned8(11));

    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setFloatingPoint(floatingPointTypeDescription);
    return typeDescription;
  }

  @Override
  public String toString() {
    return getReference().toString() + ": " + getDouble();
  }

  @Override
  public String getValueString() {
    return "" + Arrays.toString(value);
  }
}
