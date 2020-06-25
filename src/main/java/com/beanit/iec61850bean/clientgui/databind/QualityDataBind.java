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

import com.beanit.iec61850bean.BdaQuality;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.clientgui.BasicDataBind;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class QualityDataBind extends BasicDataBind<BdaQuality> {

  @SuppressWarnings("unchecked")
  private final JComboBox validity = new JComboBox(BdaQuality.Validity.values());

  private final JCheckBox badReference = new JCheckBox("BadReference");
  private final JCheckBox failure = new JCheckBox("Failure");
  private final JCheckBox inaccurate = new JCheckBox("Inaccurate");
  private final JCheckBox inconsistent = new JCheckBox("Inconsistent");
  private final JCheckBox oldData = new JCheckBox("OldData");
  private final JCheckBox operatorBlocked = new JCheckBox("OperatorBlocked");
  private final JCheckBox oscillatory = new JCheckBox("Oscillatory");
  private final JCheckBox outOfRange = new JCheckBox("OutOfRange");
  private final JCheckBox overflow = new JCheckBox("Overflow");
  private final JCheckBox substituded = new JCheckBox("Substituded");
  private final JCheckBox test = new JCheckBox("Test");

  public QualityDataBind(BdaQuality data) {
    super(data, BdaType.QUALITY);
  }

  @Override
  protected JComponent init() {
    validity.setAlignmentX(Component.LEFT_ALIGNMENT);
    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.PAGE_AXIS));
    valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    valuePanel.add(validity);
    valuePanel.add(badReference);
    valuePanel.add(failure);
    valuePanel.add(inaccurate);
    valuePanel.add(inconsistent);
    valuePanel.add(oldData);
    valuePanel.add(operatorBlocked);
    valuePanel.add(oscillatory);
    valuePanel.add(outOfRange);
    valuePanel.add(overflow);
    valuePanel.add(substituded);
    valuePanel.add(test);
    return valuePanel;
  }

  @Override
  protected void resetImpl() {
    validity.setSelectedItem(data.getValidity());
    badReference.setSelected(data.isBadReference());
    failure.setSelected(data.isFailure());
    inaccurate.setSelected(data.isInaccurate());
    inconsistent.setSelected(data.isInconsistent());
    oldData.setSelected(data.isOldData());
    operatorBlocked.setSelected(data.isOperatorBlocked());
    oscillatory.setSelected(data.isOscillatory());
    outOfRange.setSelected(data.isOutOfRange());
    overflow.setSelected(data.isOverflow());
    substituded.setSelected(data.isSubstituted());
    test.setSelected(data.isTest());
  }

  @Override
  protected void writeImpl() {
    // TODO uncomment once mutators are implemented
    // data.setValidity(validity.getSelectedItem());
    // data.setBadReference(badReference.isSelected());
    // data.setFailure(failure.isSelected());
    // data.setInaccurate(inaccurate.isSelected());
    // data.setInconsistent(inconsistent.isSelected());
    // data.setOldData(oldData.isSelected());
    // data.setOperatorBlocked(operatorBlocked.isSelected());
    // data.setOlscillatory(oscillatory.isSelected());
    // data.setOutOfRange(outOfRange.isSelected());
    // data.setOverflow(overflow.isSelected());
    // data.setSubstituded(substituded.isSelected());
    // data.setTest(test.isSelected());
  }
}
