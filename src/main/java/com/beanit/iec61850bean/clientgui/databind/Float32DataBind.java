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

import com.beanit.iec61850bean.BdaFloat32;
import com.beanit.iec61850bean.BdaType;

public class Float32DataBind extends TextFieldDataBind<BdaFloat32> {

  private static final FloatFilter FILTER = new FloatFilter();

  public Float32DataBind(BdaFloat32 data) {
    super(data, BdaType.FLOAT32, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(data.getFloat().toString());
  }

  @Override
  protected void writeImpl() {
    float newFloat = Float.parseFloat(inputField.getText());
    data.setFloat(newFloat);
  }

  private static class FloatFilter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Float.parseFloat(text);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
  }
}
