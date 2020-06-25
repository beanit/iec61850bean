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

import com.beanit.iec61850bean.BdaBoolean;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.clientgui.BasicDataBind;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class BooleanDataBind extends BasicDataBind<BdaBoolean> {

  private JCheckBox checkbox;

  public BooleanDataBind(BdaBoolean data) {
    super(data, BdaType.BOOLEAN);
  }

  @Override
  protected JComponent init() {
    checkbox = new JCheckBox();
    checkbox.setBorder(null);
    checkbox.setSelected(data.getValue());
    return checkbox;
  }

  @Override
  protected void resetImpl() {
    checkbox.setSelected(data.getValue());
  }

  @Override
  protected void writeImpl() {
    data.setValue(checkbox.isSelected());
  }
}
