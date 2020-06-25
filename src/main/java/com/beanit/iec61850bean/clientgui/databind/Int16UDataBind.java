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
package com.beanit.iec61850bean.clientgui.databind;

import com.beanit.iec61850bean.BdaInt16U;
import com.beanit.iec61850bean.BdaType;

public class Int16UDataBind extends TextFieldDataBind<BdaInt16U> {

  private static final UInt16Filter FILTER = new UInt16Filter();

  public Int16UDataBind(BdaInt16U data) {
    super(data, BdaType.INT16U, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(Integer.toString(data.getValue()));
  }

  @Override
  protected void writeImpl() {
    data.setValue(Integer.parseInt(inputField.getText()));
  }

  private static class UInt16Filter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        int value = Integer.parseInt(text);
        return value >= 0 && value <= 0xFFFF;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
