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

public final class BdaTapCommand extends BdaBitString {

  public BdaTapCommand(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    super(objectReference, fc, sAddr, 2, dchg, dupd);
    basicType = BdaType.TAP_COMMAND;
    setDefault();
  }

  /** Sets the value to TapCommand.STOP */
  @Override
  public void setDefault() {
    value = new byte[] {0x00};
  }

  @Override
  public BdaTapCommand copy() {
    BdaTapCommand copy = new BdaTapCommand(objectReference, fc, sAddr, dchg, dupd);
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

  public TapCommand getTapCommand() {

    if ((value[0] & 0xC0) == 0xC0) {
      return TapCommand.RESERVED;
    }

    if ((value[0] & 0x80) == 0x80) {
      return TapCommand.HIGHER;
    }

    if ((value[0] & 0x40) == 0x40) {
      return TapCommand.LOWER;
    }

    return TapCommand.STOP;
  }

  public void setTapCommand(TapCommand tapCommand) {
    if (tapCommand == TapCommand.RESERVED) {
      value[0] = (byte) 0xC0;
    } else if (tapCommand == TapCommand.HIGHER) {
      value[0] = (byte) 0x80;
    } else if (tapCommand == TapCommand.LOWER) {
      value[0] = (byte) 0x40;
    } else {
      value[0] = (byte) 0x00;
    }
  }

  public enum TapCommand {
    STOP(0),
    LOWER(1),
    HIGHER(2),
    RESERVED(3);
    private final int value;

    TapCommand(int value) {
      this.value = value;
    }

    public int getIntValue() {
      return value;
    }
  }
}
