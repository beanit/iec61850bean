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

import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned8;

public final class BdaInt32 extends BasicDataAttribute {

  private volatile int value;

  public BdaInt32(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, dchg, dupd);
    basicType = BdaType.INT32;
    setDefault();
  }

  @Override
  public void setValueFrom(BasicDataAttribute bda) {
    value = ((BdaInt32) bda).getValue();
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  @Override
  public void setDefault() {
    value = 0;
  }

  @Override
  public BdaInt32 copy() {
    BdaInt32 copy = new BdaInt32(objectReference, fc, sAddr, dchg, dupd);
    copy.setValue(value);
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
    data.setInteger(new BerInteger(value));
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getInteger() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: integer");
    }
    value = data.getInteger().value.intValue();
  }

  @Override
  TypeDescription getMmsTypeSpec() {
    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setInteger(new Unsigned8(32));
    return typeDescription;
  }

  @Override
  public String toString() {
    return getReference().toString() + ": " + value;
  }

  @Override
  public String getValueString() {
    return "" + value;
  }
}
