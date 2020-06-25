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
package com.beanit.iec61850bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class LogicalDevice extends ModelNode {

  public LogicalDevice(ObjectReference objectReference, List<LogicalNode> logicalNodes) {
    children = new LinkedHashMap<>((int) ((logicalNodes.size() / 0.75) + 1));
    this.objectReference = objectReference;
    for (LogicalNode logicalNode : logicalNodes) {
      children.put(logicalNode.getReference().getName(), logicalNode);
      logicalNode.setParent(this);
    }
  }

  @Override
  public LogicalDevice copy() {
    List<LogicalNode> childCopies = new ArrayList<>(children.size());
    for (ModelNode childNode : children.values()) {
      childCopies.add((LogicalNode) childNode.copy());
    }
    return new LogicalDevice(objectReference, childCopies);
  }
}
