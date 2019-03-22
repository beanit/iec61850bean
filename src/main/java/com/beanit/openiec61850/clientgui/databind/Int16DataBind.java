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

import com.beanit.openiec61850.BdaInt16;
import com.beanit.openiec61850.BdaType;

public class Int16DataBind extends TextFieldDataBind<BdaInt16> {

  private static final Int16Filter FILTER = new Int16Filter();

  public Int16DataBind(BdaInt16 data) {
    super(data, BdaType.INT16, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(Short.toString(data.getValue()));
  }

  @Override
  protected void writeImpl() {
    data.setValue(Short.parseShort(inputField.getText()));
  }

  private static class Int16Filter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Short.parseShort(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
