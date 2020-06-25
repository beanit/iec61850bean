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

public final class BdaTriggerConditions extends BdaBitString {

  public BdaTriggerConditions(ObjectReference objectReference, Fc fc) {
    super(objectReference, fc, null, 6, false, false);
    basicType = BdaType.TRIGGER_CONDITIONS;
    setDefault();
  }

  @Override
  public void setDefault() {
    /* default of GI is true by default in IEC 61850-6 sec. 9.3.8 */
    value = new byte[] {0x04};
  }

  @Override
  public BdaTriggerConditions copy() {
    BdaTriggerConditions copy = new BdaTriggerConditions(objectReference, fc);
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

  public boolean isDataChange() {
    return (value[0] & 0x40) == 0x40;
  }

  public void setDataChange(boolean dataChange) {
    if (dataChange) {
      value[0] = (byte) (value[0] | 0x40);
    } else {
      value[0] = (byte) (value[0] & 0xbf);
    }
  }

  public boolean isQualityChange() {
    return (value[0] & 0x20) == 0x20;
  }

  public void setQualityChange(boolean qualityChange) {
    if (qualityChange) {
      value[0] = (byte) (value[0] | 0x20);
    } else {
      value[0] = (byte) (value[0] & 0xdf);
    }
  }

  public boolean isDataUpdate() {
    return (value[0] & 0x10) == 0x10;
  }

  public void setDataUpdate(boolean dataUpdate) {
    if (dataUpdate) {
      value[0] = (byte) (value[0] | 0x10);
    } else {
      value[0] = (byte) (value[0] & 0xef);
    }
  }

  public boolean isIntegrity() {
    return (value[0] & 0x08) == 0x08;
  }

  public void setIntegrity(boolean integrity) {
    if (integrity) {
      value[0] = (byte) (value[0] | 0x08);
    } else {
      value[0] = (byte) (value[0] & 0xf7);
    }
  }

  public boolean isGeneralInterrogation() {
    return (value[0] & 0x04) == 0x04;
  }

  public void setGeneralInterrogation(boolean generalInterrogation) {
    if (generalInterrogation) {
      value[0] = (byte) (value[0] | 0x04);
    } else {
      value[0] = (byte) (value[0] & 0xfb);
    }
  }

  @Override
  public String toString() {

    return super.toString()
        + ", data change: "
        + isDataChange()
        + ", data update: "
        + isDataUpdate()
        + ", quality change:"
        + isQualityChange()
        + ", integrity period: "
        + isIntegrity()
        + ", GI: "
        + isGeneralInterrogation();
  }
}
