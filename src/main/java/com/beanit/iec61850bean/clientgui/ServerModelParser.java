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

import com.beanit.iec61850bean.DataSet;
import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.FcModelNode;
import com.beanit.iec61850bean.LogicalDevice;
import com.beanit.iec61850bean.LogicalNode;
import com.beanit.iec61850bean.ModelNode;
import com.beanit.iec61850bean.ServerModel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.tree.TreeNode;

public class ServerModelParser {

  private final ServerModel model;
  private DataObjectTreeNode modelTree;

  public ServerModelParser(ServerModel model) {
    this.model = model;
  }

  public TreeNode getModelTree() {
    if (modelTree == null) {
      createModelTree();
    }
    return modelTree;
  }

  private synchronized void createModelTree() {
    if (modelTree == null) {
      modelTree = new DataObjectTreeNode("server", null);
      for (ModelNode node : model.getChildren()) {
        if (node instanceof LogicalDevice == false) {
          System.out.println(
              "Node " + node.getName() + " is " + node.getClass() + " (should be LogicalDevice)");
          continue;
        }
        addLogicalDevice(modelTree, (LogicalDevice) node);
      }
      for (DataSet dataSet : model.getDataSets()) {
        addDataSet(modelTree, dataSet);
      }
    }
  }

  private void addLogicalDevice(DataObjectTreeNode root, LogicalDevice node) {
    DataObjectTreeNode treeLD = new DataObjectTreeNode(node.getName(), node);
    root.add(treeLD);
    for (ModelNode subNode : node.getChildren()) {
      if (subNode instanceof LogicalNode == false) {
        System.out.println(
            "Node " + subNode.getName() + " is " + subNode.getClass() + " (should be LogicalNode)");
        continue;
      }
      addLogicalNode(treeLD, (LogicalNode) subNode);
    }
  }

  private void addLogicalNode(DataObjectTreeNode parent, LogicalNode node) {
    DataObjectTreeNode treeLN = new DataObjectTreeNode(node.getName(), node);
    parent.add(treeLN);
    Collection<ModelNode> children = node.getChildren();
    Map<String, Set<Fc>> childMap = new HashMap<>();
    for (ModelNode child : children) {
      if (!childMap.containsKey(child.getName())) {
        childMap.put(child.getName(), new HashSet<Fc>());
      }
      childMap.get(child.getName()).add(((FcModelNode) child).getFc());
    }
    for (Map.Entry<String, Set<Fc>> childEntry : childMap.entrySet()) {
      addFunctionalConstraintObject(treeLN, node, childEntry.getKey(), childEntry.getValue());
    }
  }

  private void addDataSet(DataObjectTreeNode parent, DataSet node) {
    DataSetTreeNode treeDS = new DataSetTreeNode(node.getReferenceStr(), node);
    parent.add(treeDS);
    Collection<FcModelNode> children = node.getMembers();
    for (ModelNode child : children) {
      addFunctionalConstraintObject(treeDS, child);
    }
  }

  private void addFunctionalConstraintObject(
      DataObjectTreeNode parent, LogicalNode parentNode, String childName, Set<Fc> childFcs) {
    DataObjectTreeNode treeFCDO = new DataObjectTreeNode(childName, null);
    parent.add(treeFCDO);

    for (Fc constraint : childFcs) {
      ModelNode subNode = parentNode.getChild(childName, constraint);
      addDataObject(treeFCDO, "[" + constraint + "]", subNode);
    }
  }

  private void addFunctionalConstraintObject(DataSetTreeNode parent, ModelNode node) {
    DataObjectTreeNode treeFCDO = new DataObjectTreeNode(node.getReference().toString(), node);
    parent.add(treeFCDO);
    if (node.getChildren() != null) {
      for (ModelNode subNode : node.getChildren()) {
        addDataObject(treeFCDO, subNode.getName(), subNode);
      }
    }
  }

  private void addDataObject(DataObjectTreeNode parent, String name, ModelNode node) {
    DataObjectTreeNode treeDO = new DataObjectTreeNode(name, node);
    parent.add(treeDO);
    if (node.getChildren() != null) {
      for (ModelNode subNode : node.getChildren()) {
        addDataObject(treeDO, subNode.getName(), subNode);
      }
    }
  }
}
