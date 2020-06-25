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

public final class BdaDoubleBitPos extends BdaBitString {

  public BdaDoubleBitPos(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, 2, dchg, dupd);
    basicType = BdaType.DOUBLE_BIT_POS;
    setDefault();
  }

  /** Sets the value to DoubleBitPos.OFF */
  @Override
  public void setDefault() {
    value = new byte[] {0x40};
  }

  @Override
  public BdaDoubleBitPos copy() {
    BdaDoubleBitPos copy = new BdaDoubleBitPos(objectReference, fc, sAddr, dchg, dupd);
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

  public DoubleBitPos getDoubleBitPos() {

    if ((value[0] & 0xC0) == 0xC0) {
      return DoubleBitPos.BAD_STATE;
    }

    if ((value[0] & 0x80) == 0x80) {
      return DoubleBitPos.ON;
    }

    if ((value[0] & 0x40) == 0x40) {
      return DoubleBitPos.OFF;
    }

    return DoubleBitPos.INTERMEDIATE_STATE;
  }

  public void setDoubleBitPos(DoubleBitPos doubleBitPos) {
    if (doubleBitPos == DoubleBitPos.BAD_STATE) {
      value[0] = (byte) 0xC0;
    } else if (doubleBitPos == DoubleBitPos.ON) {
      value[0] = (byte) 0x80;
    } else if (doubleBitPos == DoubleBitPos.OFF) {
      value[0] = (byte) 0x40;
    } else {
      value[0] = (byte) 0;
    }
  }

  @Override
  public String getValueString() {
    return getDoubleBitPos().toString();
  }

  public enum DoubleBitPos {
    INTERMEDIATE_STATE(0),
    OFF(1),
    ON(2),
    BAD_STATE(3);
    private final int value;

    DoubleBitPos(int value) {
      this.value = value;
    }

    public int getIntValue() {
      return value;
    }
  }
}
