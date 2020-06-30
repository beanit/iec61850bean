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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class BasicDataAttribute extends FcModelNode {

  final List<Urcb> chgRcbs;
  final List<Urcb> dupdRcbs;
  BasicDataAttribute mirror;
  /** attribute value type */
  BdaType basicType = null;
  /** short address, can be used by SCSM and for local data mapping */
  String sAddr = null;

  boolean dchg;
  boolean qchg;
  boolean dupd;

  protected BasicDataAttribute(
      ObjectReference objectReference, Fc fc, String sAddr, boolean dchg, boolean dupd) {
    this.objectReference = objectReference;
    this.fc = fc;
    this.sAddr = sAddr;
    this.dchg = dchg;
    this.dupd = dupd;

    if (dchg) {
      chgRcbs = new ArrayList<>();
    } else {
      chgRcbs = null;
    }
    if (dupd) {
      dupdRcbs = new ArrayList<>();
    } else {
      dupdRcbs = null;
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
    return Collections.emptyIterator();
  }

  public abstract void setDefault();

  @Override
  public List<BasicDataAttribute> getBasicDataAttributes() {
    List<BasicDataAttribute> subBasicDataAttributes = new ArrayList<>();
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
