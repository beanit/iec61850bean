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
package com.beanit.iec61850bean.internal.scl;

import java.util.ArrayList;

public final class TypeDefinitions {

  private final ArrayList<LnType> lnodeTypes = new ArrayList<>();
  private final ArrayList<DoType> doTypes = new ArrayList<>();
  private final ArrayList<DaType> daTypes = new ArrayList<>();
  private final ArrayList<EnumType> enumTypes = new ArrayList<>();

  public TypeDefinitions() {}

  public void putLNodeType(LnType lnodeType) {
    lnodeTypes.add(lnodeType);
  }

  public void putDOType(DoType doType) {
    doTypes.add(doType);
  }

  public void putDAType(DaType daType) {
    daTypes.add(daType);
  }

  public void putEnumType(EnumType enumType) {
    enumTypes.add(enumType);
  }

  public DaType getDaType(String daType) {
    for (DaType datype : daTypes) {
      if (datype.id.equals(daType)) {
        return datype;
      }
    }

    return null;
  }

  public DoType getDOType(String doType) {
    for (DoType dotype : doTypes) {
      if (dotype.id.equals(doType)) {
        return dotype;
      }
    }

    return null;
  }

  public LnType getLNodeType(String lnType) {

    for (LnType ntype : lnodeTypes) {
      if (ntype.id.equals(lnType)) {
        return ntype;
      }
    }

    return null;
  }

  public EnumType getEnumType(String enumTypeRef) {
    for (EnumType enumType : enumTypes) {
      if (enumType.id.equals(enumTypeRef)) {
        return enumType;
      }
    }

    return null;
  }
}
