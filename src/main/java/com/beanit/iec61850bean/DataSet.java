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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataSet implements Iterable<FcModelNode> {

  private final String dataSetReference;
  private final List<FcModelNode> members;
  private final Map<Fc, Map<String, FcModelNode>> membersMap = new EnumMap<>(Fc.class);
  private final boolean deletable;
  private ObjectName mmsObjectName = null;

  public DataSet(String dataSetReference, List<FcModelNode> members) {
    this(dataSetReference, members, true);
  }

  public DataSet(String dataSetReference, List<FcModelNode> members, boolean deletable) {
    if (!dataSetReference.startsWith("@") && dataSetReference.indexOf('/') == -1) {
      throw new IllegalArgumentException(
          "DataSet reference "
              + dataSetReference
              + " is invalid. Must either start with @ or contain a slash.");
    }
    this.members = new ArrayList<>();
    this.dataSetReference = dataSetReference;
    this.deletable = deletable;

    for (Fc myfc : Fc.values()) {
      membersMap.put(myfc, new LinkedHashMap<String, FcModelNode>());
    }

    for (FcModelNode member : members) {
      this.members.add(member);
      membersMap.get(member.getFc()).put(member.getReference().toString(), member);
    }
  }

  public String getReferenceStr() {
    return dataSetReference;
  }

  public DataSet copy() {
    List<FcModelNode> membersCopy = new ArrayList<>(members.size());
    for (FcModelNode node : members) {
      membersCopy.add((FcModelNode) node.copy());
    }
    return new DataSet(dataSetReference, membersCopy, deletable);
  }

  public FcModelNode getMember(ObjectReference memberReference, Fc fc) {
    if (fc != null) {
      return membersMap.get(fc).get(memberReference.toString());
    }
    for (FcModelNode member : members) {
      if (member.getReference().toString().equals(memberReference.toString())) {
        return member;
      }
    }
    return null;
  }

  public FcModelNode getMember(int index) {
    return members.get(index);
  }

  public List<FcModelNode> getMembers() {
    return members;
  }

  /**
   * Those DataSets defined in the SCL file are not deletable. All other DataSets are deletable.
   * Note that no Reports/Logs may be using the DataSet otherwise it cannot be deleted (but this
   * function will still return true).
   *
   * @return true if deletable
   */
  public boolean isDeletable() {
    return deletable;
  }

  @Override
  public Iterator<FcModelNode> iterator() {
    return members.iterator();
  }

  public List<BasicDataAttribute> getBasicDataAttributes() {
    List<BasicDataAttribute> subBasicDataAttributes = new ArrayList<>();
    for (ModelNode member : members) {
      subBasicDataAttributes.addAll(member.getBasicDataAttributes());
    }
    return subBasicDataAttributes;
  }

  ObjectName getMmsObjectName() {

    if (mmsObjectName != null) {
      return mmsObjectName;
    }

    if (dataSetReference.charAt(0) == '@') {
      mmsObjectName = new ObjectName();
      mmsObjectName.setAaSpecific(new Identifier(dataSetReference.getBytes(UTF_8)));
      return mmsObjectName;
    }

    int slash = dataSetReference.indexOf('/');
    String domainID = dataSetReference.substring(0, slash);
    String itemID = dataSetReference.substring(slash + 1).replace('.', '$');

    ObjectName.DomainSpecific domainSpecificObjectName = new ObjectName.DomainSpecific();
    domainSpecificObjectName.setDomainID(new Identifier(domainID.getBytes(UTF_8)));
    domainSpecificObjectName.setItemID(new Identifier(itemID.getBytes(UTF_8)));

    mmsObjectName = new ObjectName();
    mmsObjectName.setDomainSpecific(domainSpecificObjectName);

    return mmsObjectName;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getReferenceStr());
    for (FcModelNode member : members) {
      sb.append("\n");
      sb.append(member.toString());
    }
    return sb.toString();
  }
}
