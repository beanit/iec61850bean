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
package com.beanit.iec61850bean.clientgui;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaType;
import javax.swing.JComponent;
import javax.swing.JLabel;

public abstract class BasicDataBind<E extends BasicDataAttribute> {
  protected final E data;

  private JComponent valueField;

  protected BasicDataBind(E data, BdaType type) {
    if (data.getBasicType() != type) {
      throw new IllegalArgumentException(data.getName() + " is no " + type);
    }
    this.data = data;
  }

  public JLabel getNameLabel() {
    return new JLabel(data.getName());
  }

  public JComponent getValueField() {
    if (valueField == null) {
      valueField = init();
    }

    return valueField;
  }

  public void reset() {
    if (valueField == null) {
      valueField = init();
    }

    resetImpl();
  }

  public void write() {
    if (valueField == null) {
      valueField = init();
    }

    writeImpl();
  }

  protected abstract JComponent init();

  protected abstract void resetImpl();

  protected abstract void writeImpl();
}
