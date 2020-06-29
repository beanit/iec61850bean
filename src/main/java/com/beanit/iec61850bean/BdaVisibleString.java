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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.Integer32;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;

public final class BdaVisibleString extends BasicDataAttribute {

  private final int maxLength;
  private byte[] value;

  public BdaVisibleString(
      ObjectReference objectReference,
      Fc fc,
      String sAddr,
      int maxLength,
      boolean dchg,
      boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    basicType = BdaType.VISIBLE_STRING;
    this.maxLength = maxLength;
    setDefault();
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    if (value == null || value.length > maxLength) {
      throw new IllegalArgumentException(
          "value was null or VISIBLE_STRING value size exceeds maxLength of " + maxLength);
    }
    this.value = value;
  }

  public void setValue(String value) {
    setValue(value.getBytes(UTF_8));
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    byte[] srcValue = ((BdaVisibleString) bda).getValue();
    if (value.length != srcValue.length) {
      value = new byte[srcValue.length];
    }
    System.arraycopy(srcValue, 0, value, 0, srcValue.length);
  }

  public int getMaxLength() {
    return maxLength;
  }

  public String getStringValue() {
    return new String(value, UTF_8);
  }

  @Override
  public void setDefault() {
    value = new byte[0];
  }

  @Override
  public BdaVisibleString copy() {
    BdaVisibleString copy = new BdaVisibleString(objectReference, fc, sAddr, maxLength, dchg, dupd);
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
    data.setVisibleString(new BerVisibleString(value));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getVisibleString() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: visible_string");
    }
    value = data.getVisibleString().value;
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setVisibleString(new Integer32(maxLength * -1));
    return typeDescription;
  }

  @Override
  public String toString() {
    if (value == null) {
      return getReference().toString() + ": null";
    }
    if (value.length == 0 || value[0] == (byte) 0) {
      return getReference().toString() + ": ''";
    }
    return getReference().toString() + ": " + new String(value, UTF_8);
  }

  @Override
  public String getValueString() {
    return new String(value, UTF_8);
  }
}
