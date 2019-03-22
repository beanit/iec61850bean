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

import com.beanit.openiec61850.BdaTimestamp;
import com.beanit.openiec61850.BdaType;
import com.beanit.openiec61850.clientgui.BasicDataBind;
import com.toedter.calendar.JDateChooser;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class TimeStampDataBind extends BasicDataBind<BdaTimestamp> {

  private static final Dimension DATECHOOSERDIMENSION = new Dimension(120, 20);

  private JDateChooser dateChooser;
  private JSpinner timeSpinner;

  public TimeStampDataBind(BdaTimestamp data) {
    super(data, BdaType.TIMESTAMP);
  }

  @Override
  protected JComponent init() {
    dateChooser = new JDateChooser();
    dateChooser.setDateFormatString("dd-MM-yyyy");
    dateChooser.setPreferredSize(DATECHOOSERDIMENSION);
    timeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
    timeSpinner.setEditor(timeEditor);

    Date d = data.getDate();
    if (d == null) {
      d = new Date(0);
    }
    dateChooser.setDate(d);
    timeSpinner.setValue(d);

    JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    dateTimePanel.add(dateChooser);
    dateTimePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    dateTimePanel.add(timeSpinner);
    return dateTimePanel;
  }

  @Override
  protected void resetImpl() {
    Date d = data.getDate();
    if (d == null) {
      d = new Date(0);
    }
    dateChooser.setDate(d);
    timeSpinner.setValue(d);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void writeImpl() {
    Date newDate = dateChooser.getDate();
    Date timeValues = (Date) timeSpinner.getValue();
    newDate.setHours(timeValues.getHours());
    newDate.setMinutes(timeValues.getMinutes());
    newDate.setSeconds(timeValues.getSeconds());
    data.setDate(newDate);
  }
}
