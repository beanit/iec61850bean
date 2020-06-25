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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class represents a functionally constraint DataObject. That means it has unique reference
 * and FunctionalConstraint. A DataObject as defined in part 7-3 is made up of 1..n FcDataObjects
 * where n is the number of different FunctionalConstraints that the children of the DataObject
 * have. An FcDataObject can have children of types FcDataObject, Array, ConstructedDataAttribute or
 * BasicDataAttribute.
 *
 * @author Stefan Feuerhahn
 */
public class FcDataObject extends FcModelNode {

  public FcDataObject(ObjectReference objectReference, Fc fc, List<FcModelNode> children) {

    this.children = new LinkedHashMap<>((int) ((children.size() / 0.75) + 1));
    this.objectReference = objectReference;
    for (ModelNode child : children) {
      this.children.put(child.getReference().getName(), child);
      child.setParent(this);
    }
    this.fc = fc;
  }

  @Override
  public FcDataObject copy() {
    List<FcModelNode> childCopies = new ArrayList<>(children.size());
    for (ModelNode childNode : children.values()) {
      childCopies.add((FcModelNode) childNode.copy());
    }
    return new FcDataObject(objectReference, fc, childCopies);
  }

  @Override
  Data getMmsDataObj() {
    Data.Structure dataStructure = new Data.Structure();
    List<Data> seq = dataStructure.getData();

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
    data.setStructure(dataStructure);

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
