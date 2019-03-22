/*
 * Copyright 2011 The OpenIEC61850 Authors
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
package com.beanit.openiec61850.clientgui.databind;

import com.beanit.openiec61850.BdaInt8;
import com.beanit.openiec61850.BdaType;

public class Int8DataBind extends TextFieldDataBind<BdaInt8> {

  private static final Int8Filter FILTER = new Int8Filter();

  public Int8DataBind(BdaInt8 data) {
    super(data, BdaType.INT8, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(Byte.toString(data.getValue()));
  }

  @Override
  protected void writeImpl() {
    data.setValue(Byte.parseByte(inputField.getText()));
  }

  private static class Int8Filter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Byte.parseByte(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
