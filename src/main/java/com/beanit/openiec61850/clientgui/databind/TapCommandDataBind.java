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

import com.beanit.openiec61850.BdaTapCommand;
import com.beanit.openiec61850.BdaType;
import com.beanit.openiec61850.clientgui.BasicDataBind;
import javax.swing.JComboBox;
import javax.swing.JComponent;

public class TapCommandDataBind extends BasicDataBind<BdaTapCommand> {

  @SuppressWarnings("unchecked")
  private final JComboBox tapCommand = new JComboBox(BdaTapCommand.TapCommand.values());

  public TapCommandDataBind(BdaTapCommand data) {
    super(data, BdaType.TAP_COMMAND);
  }

  @Override
  protected JComponent init() {
    return tapCommand;
  }

  @Override
  protected void resetImpl() {
    tapCommand.setSelectedItem(data.getTapCommand());
  }

  @Override
  protected void writeImpl() {
    // TODO uncomment once data.setTapCommand is implemented
    // data.setTapCommand(tapCommand.getSelectedItem());
  }
}
