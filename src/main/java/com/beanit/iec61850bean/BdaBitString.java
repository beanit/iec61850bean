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

import com.beanit.asn1bean.ber.types.BerBitString;
import com.beanit.iec61850bean.internal.HexString;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.Integer32;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;

public abstract class BdaBitString extends BasicDataAttribute {

  final int maxNumBits;
  volatile byte[] value;

  protected BdaBitString(
      ObjectReference objectReference,
      Fc fc,
      String sAddr,
      int maxNumBits,
      boolean dchg,
      boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    this.maxNumBits = maxNumBits;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    if (value.length != ((maxNumBits - 1) / 8 + 1)) {
      throw new IllegalArgumentException("value does not have correct length.");
    }
    this.value = value;
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    byte[] srcValue = ((BdaBitString) bda).getValue();
    if (value.length != srcValue.length) {
      value = new byte[srcValue.length];
    }
    System.arraycopy(srcValue, 0, value, 0, srcValue.length);
  }

  public int getMaxNumBits() {
    return maxNumBits;
  }

  /** Initializes BIT_STRING with all zeros */
  @Override
  public void setDefault() {
    value = new byte[(maxNumBits - 1) / 8 + 1];
  }

  @Override
  Data getMmsDataObj() {
    Data data = new Data();
    data.setBitString(new BerBitString(value, maxNumBits));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getBitString() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: bit_string");
    }
    if (data.getBitString().numBits > maxNumBits) {
      throw new ServiceError(
          ServiceError.TYPE_CONFLICT,
          objectReference
              + ": bit_string is bigger than type's size: "
              + data.getBitString().numBits
              + ">"
              + maxNumBits);
    }
    value = data.getBitString().value;
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setBitString(new Integer32(maxNumBits * -1));
    return typeDescription;
  }

  @Override
  public String toString() {
    return getReference().toString() + ": " + HexString.fromBytes(value);
  }

  @Override
  public String getValueString() {
    return HexString.fromBytes(value);
  }
}
