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

import com.beanit.iec61850bean.BdaOptFlds;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.clientgui.BasicDataBind;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class OptfldsDataBind extends BasicDataBind<BdaOptFlds> {

  private final JCheckBox bufferOverflow = new JCheckBox("BufferOverflow");
  private final JCheckBox configRevision = new JCheckBox("ConfigRevision");
  private final JCheckBox dataReference = new JCheckBox("DataReference");
  private final JCheckBox dataSetName = new JCheckBox("DataSetName");
  private final JCheckBox entryId = new JCheckBox("EntryId");
  private final JCheckBox reasonForInclusion = new JCheckBox("ReasonForInclusion");
  private final JCheckBox reportTimestamp = new JCheckBox("ReportTimestamp");
  private final JCheckBox segmentation = new JCheckBox("Segmentation");
  private final JCheckBox sequenceNumber = new JCheckBox("SequenceNumber");

  public OptfldsDataBind(BdaOptFlds data) {
    super(data, BdaType.OPTFLDS);
  }

  @Override
  protected JComponent init() {
    bufferOverflow.setAlignmentX(Component.LEFT_ALIGNMENT);
    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.PAGE_AXIS));
    valuePanel.add(bufferOverflow);
    valuePanel.add(configRevision);
    valuePanel.add(dataReference);
    valuePanel.add(dataSetName);
    valuePanel.add(entryId);
    valuePanel.add(reasonForInclusion);
    valuePanel.add(reportTimestamp);
    valuePanel.add(segmentation);
    valuePanel.add(sequenceNumber);
    return valuePanel;
  }

  @Override
  protected void resetImpl() {
    bufferOverflow.setSelected(data.isBufferOverflow());
    configRevision.setSelected(data.isConfigRevision());
    dataReference.setSelected(data.isDataReference());
    dataSetName.setSelected(data.isDataSetName());
    entryId.setSelected(data.isEntryId());
    reasonForInclusion.setSelected(data.isReasonForInclusion());
    reportTimestamp.setSelected(data.isReportTimestamp());
    segmentation.setSelected(data.isSegmentation());
    sequenceNumber.setSelected(data.isSequenceNumber());
  }

  @Override
  protected void writeImpl() {
    data.setBufferOverflow(bufferOverflow.isSelected());
    data.setConfigRevision(configRevision.isSelected());
    data.setDataReference(dataReference.isSelected());
    data.setDataSetName(dataSetName.isSelected());
    data.setEntryId(entryId.isSelected());
    data.setReasonForInclusion(reasonForInclusion.isSelected());
    data.setReportTimestamp(reportTimestamp.isSelected());
    data.setSegmentation(segmentation.isSelected());
    data.setSequenceNumber(sequenceNumber.isSelected());
  }
}
