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

import com.beanit.iec61850bean.BdaReasonForInclusion;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.clientgui.BasicDataBind;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class ReasonForInclusionDataBind extends BasicDataBind<BdaReasonForInclusion> {

  private final JCheckBox applicationTrigger = new JCheckBox("ApplicationTrigger");
  private final JCheckBox dataChange = new JCheckBox("DataChange");
  private final JCheckBox dataUpdate = new JCheckBox("DataUpdate");
  private final JCheckBox generalInterrogation = new JCheckBox("GeneralInterrogation");
  private final JCheckBox integrity = new JCheckBox("Integrity");
  private final JCheckBox qualitychanged = new JCheckBox("QualityChanged");

  public ReasonForInclusionDataBind(BdaReasonForInclusion data) {
    super(data, BdaType.REASON_FOR_INCLUSION);
  }

  @Override
  protected JComponent init() {
    applicationTrigger.setAlignmentX(Component.LEFT_ALIGNMENT);
    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.PAGE_AXIS));
    valuePanel.add(applicationTrigger);
    valuePanel.add(dataChange);
    valuePanel.add(dataUpdate);
    valuePanel.add(generalInterrogation);
    valuePanel.add(integrity);
    valuePanel.add(qualitychanged);
    return valuePanel;
  }

  @Override
  protected void resetImpl() {
    applicationTrigger.setSelected(data.isApplicationTrigger());
    dataChange.setSelected(data.isDataChange());
    dataUpdate.setSelected(data.isDataUpdate());
    generalInterrogation.setSelected(data.isGeneralInterrogation());
    integrity.setSelected(data.isIntegrity());
    qualitychanged.setSelected(data.isQualityChange());
  }

  @Override
  protected void writeImpl() {
    data.setApplicationTrigger(applicationTrigger.isSelected());
    data.setDataChange(dataChange.isSelected());
    data.setDataUpdate(dataUpdate.isSelected());
    data.setGeneralInterrogation(generalInterrogation.isSelected());
    data.setIntegrity(integrity.isSelected());
    data.setQualityChange(qualitychanged.isSelected());
  }
}
