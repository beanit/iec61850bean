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
import com.beanit.iec61850bean.FcModelNode;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DataObjectTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = 1682378972258556129L;

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (value instanceof DataObjectTreeNode) {
      DataObjectTreeNode treeNode = (DataObjectTreeNode) value;
      if (!leaf && treeNode.getNode() instanceof FcModelNode) {
        setIcon(getLeafIcon());
      }

      if (treeNode.getNode() instanceof BasicDataAttribute) {
        BasicDataAttribute attribute = (BasicDataAttribute) treeNode.getNode();
        String tooltip = attribute.getSAddr();
        setToolTipText(tooltip);
      }
    }

    return this;
  }
}
