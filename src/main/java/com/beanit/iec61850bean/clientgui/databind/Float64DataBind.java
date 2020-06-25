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

import com.beanit.iec61850bean.BdaFloat64;
import com.beanit.iec61850bean.BdaType;

public class Float64DataBind extends TextFieldDataBind<BdaFloat64> {

  private static final DoubleFilter FILTER = new DoubleFilter();

  public Float64DataBind(BdaFloat64 data) {
    super(data, BdaType.FLOAT64, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(data.getDouble().toString());
  }

  @Override
  protected void writeImpl() {
    double newDouble = Double.parseDouble(inputField.getText());
    data.setDouble(newDouble);
  }

  private static class DoubleFilter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Double.parseDouble(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
