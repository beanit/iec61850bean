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

import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.DataSet;
import com.beanit.iec61850bean.ServiceError;
import java.io.IOException;
import javax.swing.tree.DefaultMutableTreeNode;

public class DataSetTreeNode extends DefaultMutableTreeNode implements DataTreeNode {

  private static final long serialVersionUID = 7919716359809465616L;

  private final DataSet node;

  public DataSetTreeNode(String name, DataSet node) {
    super(name);
    this.node = node;
  }

  public DataSet getNode() {
    return node;
  }

  @Override
  public void reset(ClientAssociation association) throws ServiceError, IOException {
    if (association != null) {
      association.getDataSetValues(node);
    }
    for (int i = 0; i < getChildCount(); i++) {
      if (getChildAt(i) instanceof DataObjectTreeNode) {
        DataTreeNode child = (DataTreeNode) getChildAt(i);
        child.reset(null);
      }
    }
  }

  @Override
  public void writeValues(ClientAssociation association) throws ServiceError, IOException {
    for (int i = 0; i < getChildCount(); i++) {
      if (getChildAt(i) instanceof DataObjectTreeNode) {
        DataTreeNode child = (DataTreeNode) getChildAt(i);
        child.writeValues(null);
      }
    }
    if (association != null) {
      association.setDataSetValues(node);
    }
  }

  @Override
  public BasicDataBind<?> getData() {
    return null;
  }

  @Override
  public boolean writable() {
    return true;
  }

  @Override
  public boolean readable() {
    return true;
  }
}
