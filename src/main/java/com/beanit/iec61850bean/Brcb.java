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
import java.util.List;

public class Brcb extends Rcb {

  public Brcb(ObjectReference objectReference, List<FcModelNode> children) {
    super(objectReference, Fc.BR, children);
  }

  public BdaBoolean getPurgeBuf() {
    return (BdaBoolean) children.get("PurgeBuf");
  }

  public BdaOctetString getEntryId() {
    return (BdaOctetString) children.get("EntryID");
  }

  public BdaEntryTime getTimeOfEntry() {
    return (BdaEntryTime) children.get("TimeOfEntry");
  }

  /**
   * Gets the ResvTms attribute. This attribute is optional. Will return NULL if the attribute is
   * not available.
   *
   * @return the ResvTms attribute, null if not available.
   */
  public BdaInt16 getResvTms() {
    return (BdaInt16) children.get("ResvTms");
  }

  @Override
  public FcDataObject copy() {
    List<FcModelNode> childCopies = new ArrayList<>(children.size());
    for (ModelNode childNode : children.values()) {
      childCopies.add((FcModelNode) childNode.copy());
    }
    Brcb brcb = new Brcb(objectReference, childCopies);
    brcb.dataSet = dataSet;
    return brcb;
  }
}
