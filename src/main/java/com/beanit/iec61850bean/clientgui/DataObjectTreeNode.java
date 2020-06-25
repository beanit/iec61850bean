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
import com.beanit.iec61850bean.BdaBoolean;
import com.beanit.iec61850bean.BdaCheck;
import com.beanit.iec61850bean.BdaDoubleBitPos;
import com.beanit.iec61850bean.BdaEntryTime;
import com.beanit.iec61850bean.BdaFloat32;
import com.beanit.iec61850bean.BdaFloat64;
import com.beanit.iec61850bean.BdaInt16;
import com.beanit.iec61850bean.BdaInt16U;
import com.beanit.iec61850bean.BdaInt32;
import com.beanit.iec61850bean.BdaInt32U;
import com.beanit.iec61850bean.BdaInt64;
import com.beanit.iec61850bean.BdaInt8;
import com.beanit.iec61850bean.BdaInt8U;
import com.beanit.iec61850bean.BdaOctetString;
import com.beanit.iec61850bean.BdaOptFlds;
import com.beanit.iec61850bean.BdaQuality;
import com.beanit.iec61850bean.BdaReasonForInclusion;
import com.beanit.iec61850bean.BdaTapCommand;
import com.beanit.iec61850bean.BdaTimestamp;
import com.beanit.iec61850bean.BdaTriggerConditions;
import com.beanit.iec61850bean.BdaUnicodeString;
import com.beanit.iec61850bean.BdaVisibleString;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.FcModelNode;
import com.beanit.iec61850bean.ModelNode;
import com.beanit.iec61850bean.ServiceError;
import com.beanit.iec61850bean.clientgui.databind.BooleanDataBind;
import com.beanit.iec61850bean.clientgui.databind.CheckDataBind;
import com.beanit.iec61850bean.clientgui.databind.DoubleBitPosDataBind;
import com.beanit.iec61850bean.clientgui.databind.EntryTimeDataBind;
import com.beanit.iec61850bean.clientgui.databind.Float32DataBind;
import com.beanit.iec61850bean.clientgui.databind.Float64DataBind;
import com.beanit.iec61850bean.clientgui.databind.Int16DataBind;
import com.beanit.iec61850bean.clientgui.databind.Int16UDataBind;
import com.beanit.iec61850bean.clientgui.databind.Int32DataBind;
import com.beanit.iec61850bean.clientgui.databind.Int32UDataBind;
import com.beanit.iec61850bean.clientgui.databind.Int64DataBind;
import com.beanit.iec61850bean.clientgui.databind.Int8DataBind;
import com.beanit.iec61850bean.clientgui.databind.Int8UDataBind;
import com.beanit.iec61850bean.clientgui.databind.OctetStringDataBind;
import com.beanit.iec61850bean.clientgui.databind.OptfldsDataBind;
import com.beanit.iec61850bean.clientgui.databind.QualityDataBind;
import com.beanit.iec61850bean.clientgui.databind.ReasonForInclusionDataBind;
import com.beanit.iec61850bean.clientgui.databind.TapCommandDataBind;
import com.beanit.iec61850bean.clientgui.databind.TimeStampDataBind;
import com.beanit.iec61850bean.clientgui.databind.TriggerConditionDataBind;
import com.beanit.iec61850bean.clientgui.databind.UnicodeStringDataBind;
import com.beanit.iec61850bean.clientgui.databind.VisibleStringDataBind;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;

public class DataObjectTreeNode extends DefaultMutableTreeNode implements DataTreeNode {

  private static final long serialVersionUID = -3596243932937737877L;

  private final ModelNode node;
  private final BasicDataBind<?> data;

  public DataObjectTreeNode(String name, ModelNode node) {
    super(name);
    this.node = node;
    if (node != null && node.getChildren() == null) {
      // for (ModelNode child : node.getChildren()) {
      // if (child instanceof BasicDataAttribute) {
      // data.add(createDataBind((BasicDataAttribute) child));
      // }
      // }
      data = createDataBind((BasicDataAttribute) node);
    } else {
      data = null;
    }
  }

  private static BasicDataBind<?> createDataBind(BasicDataAttribute bda) {
    switch (bda.getBasicType()) {
      case BOOLEAN:
        return new BooleanDataBind((BdaBoolean) bda);
      case ENTRY_TIME:
        return new EntryTimeDataBind((BdaEntryTime) bda);
      case FLOAT32:
        return new Float32DataBind((BdaFloat32) bda);
      case FLOAT64:
        return new Float64DataBind((BdaFloat64) bda);
      case INT16:
        return new Int16DataBind((BdaInt16) bda);
      case INT16U:
        return new Int16UDataBind((BdaInt16U) bda);
      case INT32:
        return new Int32DataBind((BdaInt32) bda);
      case INT32U:
        return new Int32UDataBind((BdaInt32U) bda);
      case INT64:
        return new Int64DataBind((BdaInt64) bda);
      case INT8:
        return new Int8DataBind((BdaInt8) bda);
      case INT8U:
        return new Int8UDataBind((BdaInt8U) bda);
      case OCTET_STRING:
        return new OctetStringDataBind((BdaOctetString) bda);
      case TIMESTAMP:
        return new TimeStampDataBind((BdaTimestamp) bda);
      case UNICODE_STRING:
        return new UnicodeStringDataBind((BdaUnicodeString) bda);
      case VISIBLE_STRING:
        return new VisibleStringDataBind((BdaVisibleString) bda);
      case CHECK:
        return new CheckDataBind((BdaCheck) bda);
      case DOUBLE_BIT_POS:
        return new DoubleBitPosDataBind((BdaDoubleBitPos) bda);
      case OPTFLDS:
        return new OptfldsDataBind((BdaOptFlds) bda);
      case QUALITY:
        return new QualityDataBind((BdaQuality) bda);
      case REASON_FOR_INCLUSION:
        return new ReasonForInclusionDataBind((BdaReasonForInclusion) bda);
      case TAP_COMMAND:
        return new TapCommandDataBind((BdaTapCommand) bda);
      case TRIGGER_CONDITIONS:
        return new TriggerConditionDataBind((BdaTriggerConditions) bda);
      default:
        throw new IllegalArgumentException("BasicType " + bda.getBasicType() + " unknown");
    }
  }

  public ModelNode getNode() {
    return node;
  }

  @Override
  public BasicDataBind<?> getData() {
    return data;
  }

  @Override
  public void reset(ClientAssociation association) throws ServiceError, IOException {
    if (association != null) {
      association.getDataValues((FcModelNode) node);
    }
    if (data != null) {
      data.reset();
    } else {
      for (int i = 0; i < getChildCount(); i++) {
        if (getChildAt(i) instanceof DataObjectTreeNode) {
          DataTreeNode child = (DataTreeNode) getChildAt(i);
          child.reset(null);
        }
      }
    }
  }

  @Override
  public void writeValues(ClientAssociation association) throws ServiceError, IOException {
    if (data != null) {
      data.write();
    } else {
      for (int i = 0; i < getChildCount(); i++) {
        if (getChildAt(i) instanceof DataObjectTreeNode) {
          DataTreeNode child = (DataTreeNode) getChildAt(i);
          child.writeValues(null);
        }
      }
    }
    if (association != null) {
      association.setDataValues((FcModelNode) node);
    }
  }

  @Override
  public boolean writable() {
    if (node instanceof FcModelNode) {
      FcModelNode modelNode = (FcModelNode) node;
      Fc constraint = modelNode.getFc();
      return constraint != Fc.ST && constraint != Fc.MX;
    }
    return false;
  }

  @Override
  public boolean readable() {
    return node instanceof FcModelNode;
  }
}
