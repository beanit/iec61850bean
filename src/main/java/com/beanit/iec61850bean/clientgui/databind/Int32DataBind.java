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

import com.beanit.iec61850bean.BdaInt32;
import com.beanit.iec61850bean.BdaType;

public class Int32DataBind extends TextFieldDataBind<BdaInt32> {

  private static final Int32Filter FILTER = new Int32Filter();

  public Int32DataBind(BdaInt32 data) {
    super(data, BdaType.INT32, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(Integer.toString(data.getValue()));
  }

  @Override
  protected void writeImpl() {
    data.setValue(Integer.parseInt(inputField.getText()));
  }

  private static class Int32Filter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Integer.parseInt(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
