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
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LogicalNode extends ModelNode {

  private final Map<Fc, Map<String, FcDataObject>> fcDataObjects = new EnumMap<>(Fc.class);

  private final Map<String, Urcb> urcbs = new HashMap<>();
  private final Map<String, Brcb> brcbs = new HashMap<>();

  public LogicalNode(ObjectReference objectReference, List<FcDataObject> fcDataObjects) {
    children = new LinkedHashMap<>();
    for (Fc fc : Fc.values()) {
      this.fcDataObjects.put(fc, new LinkedHashMap<String, FcDataObject>());
    }

    this.objectReference = objectReference;

    for (FcDataObject fcDataObject : fcDataObjects) {
      children.put(
          fcDataObject.getReference().getName() + fcDataObject.fc.toString(), fcDataObject);
      this.fcDataObjects
          .get(fcDataObject.getFc())
          .put(fcDataObject.getReference().getName(), fcDataObject);
      fcDataObject.setParent(this);
      if (fcDataObject.getFc() == Fc.RP) {
        addUrcb((Urcb) fcDataObject, false);
      } else if (fcDataObject.getFc() == Fc.BR) {
        addBrcb((Brcb) fcDataObject);
      }
    }
  }

  @Override
  public LogicalNode copy() {

    List<FcDataObject> dataObjectsCopy = new ArrayList<>();
    for (ModelNode obj : children.values()) {
      dataObjectsCopy.add((FcDataObject) obj.copy());
    }

    LogicalNode copy = new LogicalNode(objectReference, dataObjectsCopy);
    return copy;
  }

  public List<FcDataObject> getChildren(Fc fc) {
    Map<String, FcDataObject> requestedDataObjectsMap = fcDataObjects.get(fc);
    if (requestedDataObjectsMap == null) {
      return null;
    }

    Collection<FcDataObject> fcChildren = requestedDataObjectsMap.values();
    if (fcChildren.size() == 0) {
      return null;
    } else {
      return new ArrayList<>(fcChildren);
    }
  }

  void addUrcb(Urcb urcb, boolean addDataSet) {
    urcbs.put(urcb.getReference().getName(), urcb);
    if (addDataSet) {
      String dataSetRef = urcb.getDatSet().getStringValue();
      if (dataSetRef != null) {
        urcb.dataSet =
            ((ServerModel) getParent().getParent()).getDataSet(dataSetRef.replace('$', '.'));
      }
    }
  }

  public Collection<Urcb> getUrcbs() {
    return urcbs.values();
  }

  public Urcb getUrcb(String urcbName) {
    return urcbs.get(urcbName);
  }

  void addBrcb(Brcb brcb) {
    brcbs.put(brcb.getReference().getName(), brcb);
  }

  public Brcb getBrcb(String brcbName) {
    return brcbs.get(brcbName);
  }

  public Collection<Brcb> getBrcbs() {
    return brcbs.values();
  }

  @Override
  public ModelNode getChild(String childName, Fc fc) {
    if (fc != null) {
      return fcDataObjects.get(fc).get(childName);
    }
    for (Map<String, FcDataObject> map : fcDataObjects.values()) {
      FcDataObject fcDataObject = map.get(childName);
      if (fcDataObject != null) {
        return fcDataObject;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReference().toString());
    for (Map<String, FcDataObject> fcChildNodes : fcDataObjects.values()) {
      for (ModelNode childNode : fcChildNodes.values()) {
        sb.append("\n");
        sb.append(childNode.toString());
      }
    }
    return sb.toString();
  }
}
