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

/** Check packed list according to 61850-7-2 */
public final class BdaCheck extends BdaBitString {

  public BdaCheck(ObjectReference objectReference) {
    super(objectReference, Fc.CO, null, 2, false, false);
    basicType = BdaType.CHECK;
    setDefault();
  }

  public boolean getSynchrocheck() {
    return ((value[0] & 0x80) == 0x80);
  }

  public void setSynchrocheck(boolean synchrocheck) {
    if (synchrocheck) {
      value[0] = (byte) (value[0] | 0x80);
    } else {
      value[0] = (byte) (value[0] & 0x7f);
    }
  }

  public boolean getInterlockCheck() {
    return ((value[0] & 0x40) == 0x40);
  }

  public void setInterlockCheck(boolean interlockCheck) {
    if (interlockCheck) {
      value[0] = (byte) (value[0] | 0x40);
    } else {
      value[0] = (byte) (value[0] & 0xbf);
    }
  }

  @Override
  public BdaCheck copy() {
    BdaCheck copy = new BdaCheck(objectReference);

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
  public String toString() {
    return getReference().toString() + ": " + String.format("0x%x", value[0]);
  }
}
