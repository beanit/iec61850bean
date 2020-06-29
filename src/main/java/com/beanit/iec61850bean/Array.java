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
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.TypeSpecification;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned32;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An Array can contain up to n instances of one and the same DataObject, ConstructedDataAttribute,
 * or BasicDataAttribute. The children of the array have the name that equals the index in the array
 * (e.g. "0","1" etc.)
 *
 * @author Stefan Feuerhahn
 */
public final class Array extends FcModelNode {

  private final List<ModelNode> items;

  /**
   * Creates an array object.
   *
   * @param objectReference the reference of the array
   * @param fc the functional constraint of the array
   * @param children the children of the array
   */
  public Array(ObjectReference objectReference, Fc fc, List<FcModelNode> children) {
    this.objectReference = objectReference;
    this.fc = fc;
    items = new ArrayList<>(children.size());
    for (ModelNode child : children) {
      items.add(child);
      child.setParent(this);
    }
  }

  @Override
  public Collection<ModelNode> getChildren() {
    return new ArrayList<>(items);
  }

  @Override
  public Iterator<ModelNode> iterator() {
    return items.iterator();
  }

  @Override
  public ModelNode getChild(String childName, Fc fc) {
    return items.get(Integer.parseInt(childName));
  }

  public ModelNode getChild(int index) {
    return items.get(index);
  }

  @Override
  public ModelNode copy() {
    List<FcModelNode> itemsCopy = new ArrayList<>(items.size());
    for (ModelNode item : items) {
      itemsCopy.add((FcModelNode) item.copy());
    }
    return new Array(objectReference, fc, itemsCopy);
  }

  @Override
  public List<BasicDataAttribute> getBasicDataAttributes() {
    List<BasicDataAttribute> subBasicDataAttributes = new ArrayList<>();
    for (ModelNode item : items) {
      subBasicDataAttributes.addAll(item.getBasicDataAttributes());
    }
    return subBasicDataAttributes;
  }

  public int size() {
    return items.size();
  }

  @Override
  Data getMmsDataObj() {

    Data.Array dataArray = new Data.Array();
    List<Data> arrayDataList = dataArray.getData();

    for (ModelNode modelNode : items) {
      Data mmsArrayItem = modelNode.getMmsDataObj();
      if (mmsArrayItem == null) {
        throw new IllegalArgumentException(
            "Unable to convert Child: " + modelNode.objectReference + " to MMS Data Object.");
      }
      arrayDataList.add(mmsArrayItem);
    }

    if (arrayDataList.size() == 0) {
      throw new IllegalArgumentException(
          "Converting ModelNode: "
              + objectReference
              + " to MMS Data Object resulted in Sequence of size zero.");
    }

    Data data = new Data();
    data.setArray(dataArray);
    return data;
  }

  @Override
  void setValueFromMmsDataObj(Data data) throws ServiceError {
    if (data.getArray() == null) {
      throw new ServiceError(ServiceError.TYPE_CONFLICT, "expected type: array");
    }
    if (data.getArray().getData().size() != items.size()) {
      throw new ServiceError(
          ServiceError.TYPE_CONFLICT, "expected type: array with " + children.size() + " elements");
    }

    Iterator<Data> iterator = data.getArray().getData().iterator();
    for (ModelNode child : items) {
      child.setValueFromMmsDataObj(iterator.next());
    }
  }

  @Override
  TypeDescription getMmsTypeSpec() {

    TypeSpecification elementType = new TypeSpecification();
    elementType.setTypeDescription(items.get(0).getMmsTypeSpec());

    TypeDescription.Array array = new TypeDescription.Array();
    array.setNumberOfElements(new Unsigned32(items.size()));
    array.setElementType(elementType);

    TypeDescription typeDescription = new TypeDescription();
    typeDescription.setArray(array);

    return typeDescription;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReference().toString()).append(" [").append(fc).append("]");
    for (ModelNode childNode : items) {
      sb.append("\n");
      sb.append(childNode.toString());
    }
    return sb.toString();
  }
}
