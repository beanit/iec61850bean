/*
 * Copyright 2011 The OpenIEC61850 Authors
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
package com.beanit.openiec61850;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class BasicDataAttribute extends FcModelNode {

  BasicDataAttribute mirror;

  /** attribute value type */
  BdaType basicType = null;

  /** short address, can be used by SCSM and for local data mapping */
  String sAddr = null;

  boolean dchg;
  boolean qchg;
  boolean dupd;

  List<Urcb> chgRcbs = null;
  List<Urcb> dupdRcbs = null;

  BasicDataAttribute(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    this.objectReference = objectReference;
    this.fc = fc;
    this.sAddr = sAddr;
    this.dchg = dchg;
    this.dupd = dupd;

    if (dchg) {
      chgRcbs = new ArrayList<>();
    }
    if (dupd) {
      dupdRcbs = new ArrayList<>();
    }
  }

  public boolean getDchg() {
    return dchg;
  }

  public boolean getDupd() {
    return dupd;
  }

  public boolean getQchg() {
    return dupd;
  }

  public BdaType getBasicType() {
    return basicType;
  }

  public String getSAddr() {
    return sAddr;
  }

  @Override
  public ModelNode getChild(String childName, Fc fc) {
    return null;
  }

  @Override
  public ModelNode getChild(String childName) {
    return null;
  }

  @Override
  public Collection<ModelNode> getChildren() {
    return null;
  }

  @Override
  public Iterator<ModelNode> iterator() {
    return Collections.<ModelNode>emptyList().iterator();
  }

  public abstract void setDefault();

  @Override
  public List<BasicDataAttribute> getBasicDataAttributes() {
    List<BasicDataAttribute> subBasicDataAttributes = new LinkedList<>();
    subBasicDataAttributes.add(this);
    return subBasicDataAttributes;
  }

  public abstract void setValueFrom(BasicDataAttribute bda);

  void setMirror(BasicDataAttribute bda) {
    mirror = bda;
  }

  public String getValueString() {
    return null;
  }
}
