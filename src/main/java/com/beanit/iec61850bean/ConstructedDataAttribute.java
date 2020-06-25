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

import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.Data.Structure;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public final class ConstructedDataAttribute extends FcModelNode {

  public ConstructedDataAttribute(
      ObjectReference objectReference, Fc fc, List<FcModelNode> children) {
    this.objectReference = objectReference;
    this.fc = fc;
    this.children = new LinkedHashMap<>((int) ((children.size() / 0.75) + 1));
    for (ModelNode child : children) {
      this.children.put(child.getName(), child);
      child.setParent(this);
    }
  }

  @Override
  public ConstructedDataAttribute copy() {
    List<FcModelNode> subDataAttributesCopy = new ArrayList<>();
    for (ModelNode subDA : children.values()) {
      subDataAttributesCopy.add((FcModelNode) subDA.copy());
    }
    return new ConstructedDataAttribute(getReference(), fc, subDataAttributesCopy);
  }

  @Override
  Data getMmsDataObj() {
    Structure structure = new Structure();
    List<Data> seq = structure.getData();

    for (ModelNode modelNode : getChildren()) {
      Data child = modelNode.getMmsDataObj();
      if (child == null) {
        throw new IllegalArgumentException(
            "Unable to convert Child: " + modelNode.objectReference + " to MMS Data Object.");
      }
      seq.add(child);
    }
    if (seq.size() == 0) {
      throw new IllegalArgumentException(
          "Converting ModelNode: "
              + objectReference
              + " to MMS Data Object resulted in Sequence of size zero.");
    }

    Data data = new Data();
    data.setStructure(structure);

    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getStructure() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: structure");
    }
    if (data.getStructure().getData().size() != children.size()) {
      throw new ServiceError(
          ServiceError.TYPE_CONFLICT,
          "expected type: structure with " + children.size() + " elements");
    }

    Iterator<Data> iterator = data.getStructure().getData().iterator();
    for (ModelNode child : children.values()) {
      child.setValueFromMmsDataObj(iterator.next());
    }
  }
}
