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

import com.beanit.iec61850bean.BdaInt8U;
import com.beanit.iec61850bean.BdaType;

public class Int8UDataBind extends TextFieldDataBind<BdaInt8U> {

  private static final UInt8Filter FILTER = new UInt8Filter();

  public Int8UDataBind(BdaInt8U data) {
    super(data, BdaType.INT8U, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(Short.toString(data.getValue()));
  }

  @Override
  protected void writeImpl() {
    data.setValue(Short.parseShort(inputField.getText()));
  }

  private static class UInt8Filter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        short value = Short.parseShort(text);
        return value >= 0 && value <= 0xFF;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
